package de.quantummaid.eventmaid.routeselection;

import de.quantummaid.eventmaid.IncomingMessage;

public interface RouteSelectionExpression {

    RouteSelection determineRoute(IncomingMessage incomingMessage);
}
