package de.quantummaid.eventmaid;

import de.quantummaid.eventmaid.endpoints.Destination;
import de.quantummaid.eventmaid.endpoints.DestinationIdentifier;
import de.quantummaid.eventmaid.routeselection.Route;
import de.quantummaid.eventmaid.routeselection.RouteSelectionExpression;
import de.quantummaid.eventmaid.routeselection.RouteSelectionExpressionEvaluator;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

import static de.quantummaid.eventmaid.DuplicateDestinationIdentifierException.duplicateDestinationIdentifierException;
import static de.quantummaid.eventmaid.UnknownDestinationException.unknownDestinationException;

public class EventMaid {
    private final List<Consumer<IncomingMessage>> handlers = new ArrayList<>();
    private final RouteSelectionExpressionEvaluator routeSelectionExpressionEvaluator = new RouteSelectionExpressionEvaluator();
    private final Map<DestinationIdentifier, Destination> identifierDestinationMap = new HashMap<>();

    public EventMaid() {
    }

    /*
    Step 1: message filter / selector
           - Acknowledge but no forward
           - Ignore
           ? Vlt endpunkt spezifisch, e.g. sns ignore, sqs acknowledge

    Step 2: Processing and Enriching
    Step 3: An UseCaseMaid
     */
    public void handle(final IncomingMessage message) {
        final Route route = routeSelectionExpressionEvaluator.evaluate(message);
        if (route != null) {
            message.setRoute(route.getValue());
        }
        handlers.forEach(h -> h.accept(message));
    }

    public void defineRoute(final RouteSelectionExpression routeSelectionExpression) {
        this.routeSelectionExpressionEvaluator.addRouteSelectionExpression(routeSelectionExpression);
    }

    public void addHandler(final Consumer<IncomingMessage> handler) {
        this.handlers.add(handler);
    }

    public void send(final OutgoingMessage message) {
        final DestinationIdentifier destinationIdentifier = message.getDestinationIdentifier();
        final Destination destination = getDestination(destinationIdentifier);
        destination.send(message);
    }

    private Destination getDestination(final DestinationIdentifier destinationIdentifier) {
        final Destination destination = identifierDestinationMap.get(destinationIdentifier);
        if (destination != null) {
            return destination;
        } else {
            throw unknownDestinationException(destinationIdentifier);
        }
    }

    public void addDestination(final DestinationIdentifier destinationIdentifier, final Destination destination) {
        if (!identifierDestinationMap.containsKey(destinationIdentifier)) {
            identifierDestinationMap.put(destinationIdentifier, destination);
        } else {
            throw duplicateDestinationIdentifierException(destinationIdentifier);
        }
    }
}
