package de.quantummaid.eventmaid;

public interface MessageFilter {

    boolean letMessagePass(IncomingMessage incomingMessage);
}
