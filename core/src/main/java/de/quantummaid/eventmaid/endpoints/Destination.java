package de.quantummaid.eventmaid.endpoints;

import de.quantummaid.eventmaid.OutgoingMessage;

public interface Destination {
    void send(OutgoingMessage message);
}
