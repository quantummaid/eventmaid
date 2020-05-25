package de.quantummaid.eventmaid;

import de.quantummaid.eventmaid.identification.MessageId;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.util.Map;

import static de.quantummaid.eventmaid.Validators.validateNotNull;

@ToString
public final class IncomingMessage {
    @Getter
    private final MessageId messageId;
    @Getter
    private final MessageBody messageBody;
    @Getter
    private final Map<String, String> messageHeaders;
    @Getter
    private final Map<String, String> messageProperties;
    @Getter
    private final SourceInformation sourceInformation;
    @Getter
    private final Object rawRequest;
    @Getter
    @Setter
    private String route;

    private IncomingMessage(final MessageId messageId,
                            final MessageBody messageBody,
                            final Map<String, String> messageHeaders,
                            final Map<String, String> messageProperties,
                            final SourceInformation sourceInformation,
                            final Object rawRequest,
                            final String route) {
        this.messageId = messageId;
        this.messageBody = messageBody;
        this.messageHeaders = messageHeaders;
        this.messageProperties = messageProperties;
        this.sourceInformation = sourceInformation;
        this.rawRequest = rawRequest;
        this.route = route;
    }

    public static IncomingMessage incomingMessage(final MessageId messageId,
                                                  final MessageBody messageBody,
                                                  final Map<String, String> messageHeaders,
                                                  final Map<String, String> messageProperties,
                                                  final SourceInformation sourceInformation,
                                                  final Object rawRequest) {
        return incomingMessage(messageId, null, messageBody, messageHeaders,
                messageProperties, sourceInformation, rawRequest);
    }

    public static IncomingMessage incomingMessage(final MessageId messageId,
                                                  final String route,
                                                  final MessageBody messageBody,
                                                  final Map<String, String> messageHeaders,
                                                  final Map<String, String> messageProperties,
                                                  final SourceInformation sourceInformation,
                                                  final Object rawRequest) {
        validateNotNull(messageId, "messageId");
        validateNotNull(messageBody, "messageBody");
        validateNotNull(messageHeaders, "messageHeaders");
        validateNotNull(messageProperties, "messageProperties");
        validateNotNull(sourceInformation, "sourceInformation");
        validateNotNull(rawRequest, "rawRequest");
        return new IncomingMessage(messageId, messageBody, messageHeaders,
                messageProperties, sourceInformation, rawRequest, route);
    }

}
