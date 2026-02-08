package com.example.semilio.message.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class MessageRequest {

    private String chatId;

    private String productId;

    @NotBlank(message = "Message content cannot be empty")
    @Size(max = 250, message = "Message cannot exceed 250 characters")
    private String content;
}