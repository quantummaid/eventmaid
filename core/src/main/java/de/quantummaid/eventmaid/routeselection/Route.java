package de.quantummaid.eventmaid.routeselection;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

import static lombok.AccessLevel.PRIVATE;

@RequiredArgsConstructor(access = PRIVATE)
public class Route {
    @Getter
    private final String value;

    public static Route route(final String value) {
        return new Route(value);
    }
}
