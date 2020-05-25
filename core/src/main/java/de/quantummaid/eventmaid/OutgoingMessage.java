package de.quantummaid.eventmaid;

import de.quantummaid.eventmaid.endpoints.DestinationIdentifier;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.HashMap;
import java.util.Map;

import static de.quantummaid.eventmaid.endpoints.DestinationIdentifier.destinationIdentifier;
import static lombok.AccessLevel.PRIVATE;

@RequiredArgsConstructor(access = PRIVATE)
public final class OutgoingMessage {
    @Getter
    private final MessageBody messageBody;
    @Getter
    private final DestinationIdentifier destinationIdentifier;
    @Getter
    private final Map<String, String> messageHeaders;
    @Getter
    private final Map<String, String> messageProperties;
    @Getter
    private final MessageGroupId messageGroupId;
    @Getter
    private final DeduplicationId deduplicationId;
    //TODO: private final CorrelationId correlationId;


    public static OutgoingMessage outgoingMessage(final String messageBodyString, final String destinationIdentifierStrings) {
        final MessageBody messageBody = MessageBody.messageBody(messageBodyString);
        final DestinationIdentifier destinationIdentifier = destinationIdentifier(destinationIdentifierStrings);
        final HashMap<String, String> headers = new HashMap<>();
        final HashMap<String, String> properties = new HashMap<>();
        return new OutgoingMessage(messageBody, destinationIdentifier, headers, properties, null, null);
    }

    public static OutgoingMessage outgoingMessage(final MessageBody messageBody, final DestinationIdentifier destinationIdentifier) {
        final HashMap<String, String> headers = new HashMap<>();
        final HashMap<String, String> properties = new HashMap<>();
        return new OutgoingMessage(messageBody, destinationIdentifier, headers, properties, null, null);
    }

    //TODO: only a test factory method
    public static OutgoingMessage outgoingMessage(final String messageBodyString,
                                                  final String destinationIdentifierStrings,
                                                  final MessageGroupId messageGroupId,
                                                  final DeduplicationId deduplicationId) {
        final MessageBody messageBody = MessageBody.messageBody(messageBodyString);
        final DestinationIdentifier destinationIdentifier = destinationIdentifier(destinationIdentifierStrings);
        final HashMap<String, String> headers = new HashMap<>();
        final HashMap<String, String> properties = new HashMap<>();
        return new OutgoingMessage(messageBody, destinationIdentifier, headers, properties, messageGroupId, deduplicationId);
    }
}
