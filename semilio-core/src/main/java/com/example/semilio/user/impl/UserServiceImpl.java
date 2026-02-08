package com.example.semilio.user.impl;

import com.example.semilio.comon.validation.FileValidator;
import com.example.semilio.exception.BusinessException;
import com.example.semilio.exception.ErrorCode;
import com.example.semilio.service.S3Service;
import com.example.semilio.service.SecurityService;
import com.example.semilio.user.User;
import com.example.semilio.user.UserMapper;
import com.example.semilio.user.UserRepository;
import com.example.semilio.user.UserService;
import com.example.semilio.user.request.ChangePasswordRequest;
import com.example.semilio.user.request.ProfileUpdateRequest;
import com.example.semilio.user.response.UserPublicResponse;
import com.example.semilio.user.response.UserResponse;
import io.micrometer.common.util.StringUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.Objects;

import static com.example.semilio.exception.ErrorCode.*;


@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final SecurityService securityService;
    private final UserMapper userMapper;
    private final S3Service s3Service;
    private final FileValidator fileValidator;


    @Override
    @Transactional(readOnly = true)
    public UserDetails loadUserByUsername(final String userEmail) throws UsernameNotFoundException {
        return this.userRepository.findByEmailIgnoreCase(userEmail)
                .orElseThrow(() -> new UsernameNotFoundException("User not found with username: " + userEmail));
    }

    @Override
    public UserResponse getUser(String userId) {
        final User user = loadUserById(userId);

        return this.userMapper.toDTO(user);
    }

    @Override
    @Transactional
    public void updateProfileInfo(final ProfileUpdateRequest request, final MultipartFile profileImage, final Authentication principal) {
        this.fileValidator.validateImage(profileImage);

        final User user = this.securityService.getCurrentUser(principal);

        updateNicknameIfChanged(user, request.getNickName());
        updateSimpleFields(user, request);
        updateProfileImageIfPresent(user, profileImage);

        this.userRepository.save(user);
    }

    private void updateNicknameIfChanged(User user, String newNickName) {
        if (StringUtils.isBlank(newNickName) || Objects.equals(user.getNickName(), newNickName)) {
            return;
        }

        if (userRepository.existsByNickName(newNickName)) {
            throw new BusinessException(NICKNAME_ALREADY_EXISTS);
        }

        user.setNickName(newNickName);
    }

    private void updateSimpleFields(User user, ProfileUpdateRequest request) {
        if (!Objects.equals(user.getBio(), request.getBio())) {
            user.setBio(request.getBio());
        }

        if (StringUtils.isNotBlank(request.getCountryCode())
                && !Objects.equals(user.getCountry(), request.getCountryCode())) {
            user.setCountry(request.getCountryCode());
        }
    }

    private void updateProfileImageIfPresent(User user, MultipartFile image) {
        if (image == null || image.isEmpty()) {
            return;
        }
        s3Service.deleteFile(user.getProfilePictureUrl());

        String directory = "user/%s".formatted(user.getId());
        String imageUrl = s3Service.uploadImage(image, directory);

        user.setProfilePictureUrl(imageUrl);
    }

    @Override
    public void changePassword(final ChangePasswordRequest req, final String userId) {

        if (!req.getNewPassword()
                .equals(req.getConfirmNewPassword())) {
            throw new BusinessException(CHANGE_PASSWORD_MISMATCH);
        }

        final User savedUser = loadUserById(userId);

        if (!this.passwordEncoder.matches(req.getCurrentPassword(),
                savedUser.getPassword())) {
            throw new BusinessException(INVALID_CURRENT_PASSWORD);
        }

        final String encoded = this.passwordEncoder.encode(req.getNewPassword());
        savedUser.setPassword(encoded);
        this.userRepository.save(savedUser);
    }

    @Override
    public void deactivateAccount(final String userId) {

        final User user = loadUserById(userId);

        if (!user.isEnabled()) {
            throw new BusinessException(ErrorCode.ACCOUNT_ALREADY_DEACTIVATED);
        }

        user.setEnabled(false);
        this.userRepository.save(user);
    }

    @Override
    public void reactivateAccount(final String userId) {

        final User user = loadUserById(userId);

        if (user.isEnabled()) {
            throw new BusinessException(ErrorCode.ACCOUNT_ALREADY_DEACTIVATED);
        }

        user.setEnabled(true);
        this.userRepository.save(user);
    }

    @Override
    public void deleteAccount(final String userId) {
        // this method need the rest of the entities
        // the logic is just to schedule a profile for deletion
        // and then a scheduled job will pick up the profiles and delete everything
    }

    @Override
    public UserPublicResponse getUserById(String userId) {
        final User user = loadUserById(userId);

        return this.userMapper.toPublicDTO(user);
    }

    public User loadUserById(String userId) {
        return this.userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException(USER_NOT_FOUND));
    }
}