package de.quantummaid.eventmaid;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.UUID;

import static lombok.AccessLevel.PRIVATE;

@EqualsAndHashCode
@RequiredArgsConstructor(access = PRIVATE)
public final class DeduplicationId {
    @Getter
    private final String value;

    public static DeduplicationId deduplicationId(final String value) {
        return new DeduplicationId(value);
    }

    public static DeduplicationId createRandomDeduplicationId() {
        final UUID uuid = UUID.randomUUID();
        return new DeduplicationId(uuid.toString());
    }
}
