package de.quantummaid.eventmaid.endpoints;

public class MissingMessageGroupIdException extends RuntimeException {
    public MissingMessageGroupIdException(final String message) {
        super(message);
    }

    public static MissingMessageGroupIdException missingMessageGroupIdException(final String message) {
        return new MissingMessageGroupIdException(message);
    }
}
