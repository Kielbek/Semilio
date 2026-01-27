package com.example.semilio.notification;

import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import java.util.Date;
import java.util.Map;


@Service
@Slf4j
@RequiredArgsConstructor
public class EmailServiceImpl implements EmailService {

    private final JavaMailSender mailSender;
    private final ApplicationMailRepository emailRepository;
    private final TemplateEngine templateEngine;

    @Transactional
    @Override
    public void sendEmail(String to, EmailTemplates template, Map<String, Object> variables) {
        try {
            String content = renderTemplate(template.getTemplate(), variables);

            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true);
            helper.setTo(to);
            helper.setFrom("matistore161@gmail.com");
            helper.setSubject(template.getSubject());
            helper.setText(content, true);
            mailSender.send(message);

            saveEmail(to, "matistore161@gmail.com", template, content, true);
        } catch (Exception e) {
            saveEmail(to, "matistore161@gmail.com", template, "Error sending email", false);
        }
    }

    private String renderTemplate(String templatePath, Map<String, Object> variables) {
        Context context = new Context();
        context.setVariables(variables);
        return templateEngine.process(templatePath, context);
    }

    private void saveEmail(String to, String from, EmailTemplates template, String content, boolean isSent) {
        ApplicationMail email = ApplicationMail.builder()
                .toMail(to)
                .fromMail(from)
                .subject(template.getSubject())
                .message(content)
                .template(template)
                .isSent(isSent)
                .sendAt(isSent ? new Date() : null)
                .build();
        emailRepository.save(email);
    }
}