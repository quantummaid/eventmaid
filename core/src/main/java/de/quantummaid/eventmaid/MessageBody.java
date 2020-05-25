package de.quantummaid.eventmaid;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

import static lombok.AccessLevel.PRIVATE;

@RequiredArgsConstructor(access = PRIVATE)
public final class MessageBody {
    @Getter
    private final String content;

    public static MessageBody messageBody(final String content) {
        return new MessageBody(content);
    }
}
