package de.quantummaid.eventmaid;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.UUID;

import static de.quantummaid.eventmaid.Validators.validateNotNullNorEmpty;
import static lombok.AccessLevel.PRIVATE;

@EqualsAndHashCode
@RequiredArgsConstructor(access = PRIVATE)
public final class MessageGroupId {
    @Getter
    private final String value;

    public static MessageGroupId messageGroupId(final String value) {
        validateNotNullNorEmpty(value, "MessageGroupId");
        return new MessageGroupId(value);
    }

    public static MessageGroupId createRandomMessageGroupId() {
        final UUID uuid = UUID.randomUUID();
        return new MessageGroupId(uuid.toString());
    }
}
