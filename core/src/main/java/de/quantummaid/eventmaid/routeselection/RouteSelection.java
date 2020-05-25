package de.quantummaid.eventmaid.routeselection;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

import static de.quantummaid.eventmaid.routeselection.Route.route;
import static lombok.AccessLevel.PRIVATE;

@RequiredArgsConstructor(access = PRIVATE)
public class RouteSelection {
    private final boolean containsRouteSelection;
    @Getter
    private final Route route;

    public static RouteSelection selectRoute(final String route) {
        final Route aRoute = route(route);
        return new RouteSelection(true, aRoute);
    }

    public static RouteSelection selectionNotApplicable() {
        return new RouteSelection(false, null);
    }

    public boolean containsRouteSelection() {
        return containsRouteSelection;
    }
}
