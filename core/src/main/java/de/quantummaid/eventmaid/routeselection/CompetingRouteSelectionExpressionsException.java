package de.quantummaid.eventmaid.routeselection;

import java.util.List;
import java.util.stream.Collectors;

public class CompetingRouteSelectionExpressionsException extends RuntimeException {

    public CompetingRouteSelectionExpressionsException(String message) {
        super(message);
    }

    public static CompetingRouteSelectionExpressionsException competingRouteSelectionExpressionsException(
            List<RouteSelection> competingRouteSelections) {
        final String competingRoutes = competingRouteSelections.stream()
                .map(routeSelection -> routeSelection.getRoute().getValue())
                .collect(Collectors.joining(", "));
        final String message = "Found competing route selection expressions for routes: " + competingRoutes;
        return new CompetingRouteSelectionExpressionsException(message);
    }
}
