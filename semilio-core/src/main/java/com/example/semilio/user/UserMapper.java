package com.example.semilio.user;

import com.example.semilio.auth.request.RegistrationRequest;
import com.example.semilio.comon.dictionary.DictionaryService;
import com.example.semilio.user.request.ProfileUpdateRequest;
import com.example.semilio.user.response.UserPublicResponse;
import com.example.semilio.user.response.UserResponse;
import io.micrometer.common.util.StringUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

@Service
@RequiredArgsConstructor
public class UserMapper {
    private final PasswordEncoder passwordEncoder;
    private final DictionaryService dictionaryService;

    public User toUser(final RegistrationRequest request) {
        return User.builder()
                .firstName(request.getFirstName())
                .lastName(request.getLastName())
                .email(request.getEmail())
                .phoneNumber(request.getPhoneNumber())
                .password(this.passwordEncoder.encode(request.getPassword()))
                .country("PL")
                .preferredLanguage("pl")
                .enabled(true)
                .locked(false)
                .credentialsExpired(false)
                .emailVerified(false)
                .phoneVerified(false)
                .build();
    }

    public UserResponse toDTO(final User user) {
        String lang = user.getPreferredLanguage() != null ? user.getPreferredLanguage() : "pl";
        String translatedCountry = dictionaryService.getCountryNameByCode(user.getCountry(), lang);

        return UserResponse.builder()
                .id(user.getId())
                .firstName(user.getFirstName())
                .lastName(user.getLastName())
                .nickName(user.getNickName())
                .email(user.getEmail())
                .phoneNumber(user.getPhoneNumber())
                .bio(user.getBio())
                .dateOfBirth(user.getDateOfBirth())
                .countryName(translatedCountry)
                .enabled(user.isEnabled())
                .locked(!user.isAccountNonLocked())
                .credentialsExpired(!user.isCredentialsNonExpired())
                .emailVerified(user.isEmailVerified())
                .phoneVerified(user.isPhoneVerified())
                .profilePictureUrl(user.getProfilePictureUrl())
                .createdDate(user.getCreatedDate())
                .roles(
                        CollectionUtils.isEmpty(user.getRoles())
                                ? java.util.List.of()
                                : user.getRoles().stream()
                                .map(r -> r.getName().toString())
                                .toList()
                )
                .build();
    }

    public UserPublicResponse toPublicDTO(final User user) {
        if (user == null) return null;

        String currentLang = LocaleContextHolder.getLocale().getLanguage();
        String translatedCountry = dictionaryService.getCountryNameByCode(user.getCountry(), currentLang);

        return UserPublicResponse.builder()
                .id(user.getId())
                .nickName(user.getNickName())
                .bio(user.getBio())
                .countryName(translatedCountry)
                .profilePictureUrl(user.getProfilePictureUrl())
                .createdDate(user.getCreatedDate())
                .build();
    }
}