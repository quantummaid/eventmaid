package de.quantummaid.eventmaid.routeselection;

import de.quantummaid.eventmaid.IncomingMessage;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import static de.quantummaid.eventmaid.routeselection.CompetingRouteSelectionExpressionsException.competingRouteSelectionExpressionsException;

public class RouteSelectionExpressionEvaluator {
    private final List<RouteSelectionExpression> routeSelectionExpressions = new ArrayList<>();

    public void addRouteSelectionExpression(final RouteSelectionExpression routeSelectionExpression) {
        routeSelectionExpressions.add(routeSelectionExpression);
    }

    public Route evaluate(final IncomingMessage incomingMessage) {
        final List<RouteSelection> applicableRouteSelections = routeSelectionExpressions.stream()
                .map(e -> e.determineRoute(incomingMessage))
                .filter(RouteSelection::containsRouteSelection)
                .collect(Collectors.toList());
        if (applicableRouteSelections.size() == 1) {
            return applicableRouteSelections.get(0).getRoute();
        } else if (applicableRouteSelections.size() == 0) {
            return null;
        } else {
            throw competingRouteSelectionExpressionsException(applicableRouteSelections);
        }
    }
}
