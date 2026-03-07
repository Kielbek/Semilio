package com.example.semilio.message.model.payload;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

@JsonTypeInfo(
        use = JsonTypeInfo.Id.NAME,
        include = JsonTypeInfo.As.PROPERTY,
        property = "type"
)
@JsonSubTypes({
        @JsonSubTypes.Type(value = TextPayload.class, name = "TEXT"),
        @JsonSubTypes.Type(value = ImagePayload.class, name = "IMAGE"),
        @JsonSubTypes.Type(value = ProposalPayload.class, name = "PROPOSAL")
})
public sealed interface MessagePayload permits TextPayload, ImagePayload, ProposalPayload {
}