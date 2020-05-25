package de.quantummaid.eventmaid;

import de.quantummaid.eventmaid.endpoints.DestinationIdentifier;

public class DuplicateDestinationIdentifierException extends RuntimeException {

    public DuplicateDestinationIdentifierException(final String message) {
        super(message);
    }

    public static DuplicateDestinationIdentifierException duplicateDestinationIdentifierException(final DestinationIdentifier destinationIdentifier) {
        final String message = "Could not add Destination for DestinationIdentifier '" + destinationIdentifier.getValue() + "', " +
                "because another destination was already registered for this identifier.";
        return new DuplicateDestinationIdentifierException(message);
    }
}
