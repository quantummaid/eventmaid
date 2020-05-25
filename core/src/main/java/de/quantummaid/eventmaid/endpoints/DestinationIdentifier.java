package de.quantummaid.eventmaid.endpoints;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import static lombok.AccessLevel.PRIVATE;

@EqualsAndHashCode
@RequiredArgsConstructor(access = PRIVATE)
public final class DestinationIdentifier {
    @Getter
    private final String value;

    public static DestinationIdentifier destinationIdentifier(final String value) {
        return new DestinationIdentifier(value);
    }
}
