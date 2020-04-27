package de.quantummaid.eventmaid.serializable;

import de.quantummaid.eventmaid.identification.CorrelationId;
import de.quantummaid.eventmaid.identification.MessageId;
import de.quantummaid.eventmaid.internal.enforcing.InvalidInputException;
import de.quantummaid.eventmaid.subscribing.SubscriptionId;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class SerializingSpecs {

    @Test
    void messageIdCanBeSerializedAndDeserialized() {
        final MessageId original = MessageId.newUniqueMessageId();
        final String stringValue = original.stringValue();
        final MessageId deserialized = MessageId.fromString(stringValue);
        assertEquals(original, deserialized);
    }

    @Test
    void creatingAMessageIdFailsWhenSupplyingAInvalidUuidString() {
        assertThrows(InvalidInputException.class, () -> MessageId.fromString("invalid"));
    }

    @Test
    void correlationIdCanBeSerializedAndDeserialized() {
        final CorrelationId original = CorrelationId.newUniqueCorrelationId();
        final String stringValue = original.stringValue();
        final CorrelationId deserialized = CorrelationId.fromString(stringValue);
        assertEquals(original, deserialized);
    }

    @Test
    void creatingACorrelationIdFailsWhenSupplyingAInvalidUuidString() {
        assertThrows(InvalidInputException.class, () -> CorrelationId.fromString("invalid"));
    }

    @Test
    void subscriptionIdCanBeSerializedAndDeserialized() {
        final SubscriptionId original = SubscriptionId.newUniqueId();
        final String stringValue = original.stringValue();
        final SubscriptionId deserialized = SubscriptionId.fromString(stringValue);
        assertEquals(original, deserialized);
    }

    @Test
    void creatingASubscriptionIdFailsWhenSupplyingAInvalidUuidString() {
        assertThrows(InvalidInputException.class, () -> SubscriptionId.fromString("invalid"));
    }
}
