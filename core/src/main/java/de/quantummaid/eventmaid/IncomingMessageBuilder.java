package de.quantummaid.eventmaid;

import de.quantummaid.eventmaid.identification.MessageId;
import lombok.RequiredArgsConstructor;

import java.util.HashMap;
import java.util.Map;

import static de.quantummaid.eventmaid.IncomingMessage.incomingMessage;
import static lombok.AccessLevel.PRIVATE;

@RequiredArgsConstructor(access = PRIVATE)
public final class IncomingMessageBuilder {
    private MessageId messageId;
    private Map<String, String> messageHeaders = new HashMap<>();
    private Map<String, String> messageProperties = new HashMap<>();
    private MessageBody messageBody = null;
    private SourceInformation sourceInformation = null;
    private Object rawRequest;

    public static IncomingMessageBuilder anIncomingMessage() {
        return new IncomingMessageBuilder();
    }

    public IncomingMessageBuilder withBody(final MessageBody messageBody) {
        this.messageBody = messageBody;
        return this;
    }

    public IncomingMessageBuilder withSourceInformation(final SourceInformation sourceInformation) {
        this.sourceInformation = sourceInformation;
        return this;
    }

    public IncomingMessageBuilder withMessageProperties(final Map<String, String> messageProperties) {
        this.messageProperties = messageProperties;
        return this;
    }

    public IncomingMessageBuilder withMessageHeaders(final Map<String, String> messageHeaders) {
        this.messageHeaders = messageHeaders;
        return this;
    }

    public IncomingMessageBuilder withRawRequest(final Object rawRequest) {
        this.rawRequest = rawRequest;
        return this;
    }

    public IncomingMessageBuilder withMessageId(final String messageIdString) {
        final MessageId messageId = MessageId.fromString(messageIdString);
        return withMessageId(messageId);
    }

    public IncomingMessageBuilder withMessageId(final MessageId messageId) {
        this.messageId = messageId;
        return this;
    }

    public IncomingMessage build() {
        return incomingMessage(messageId, messageBody, messageHeaders, messageProperties, sourceInformation, rawRequest);
    }
}
