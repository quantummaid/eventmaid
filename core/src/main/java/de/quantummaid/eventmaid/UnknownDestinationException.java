package de.quantummaid.eventmaid;

import de.quantummaid.eventmaid.endpoints.DestinationIdentifier;

public class UnknownDestinationException extends RuntimeException {

    public UnknownDestinationException(final String message) {
        super(message);
    }

    public static UnknownDestinationException unknownDestinationException(final DestinationIdentifier destinationIdentifier) {
        final String message = "Not Destination configured for DestinationIdentifier '" + destinationIdentifier.getValue() + "'.";
        return new UnknownDestinationException(message);
    }
}
