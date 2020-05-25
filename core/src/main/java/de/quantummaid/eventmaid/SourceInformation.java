package de.quantummaid.eventmaid;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

import static lombok.AccessLevel.PRIVATE;

@RequiredArgsConstructor(access = PRIVATE)
public class SourceInformation {
    @Getter
    private final SourceType type;

    public static SourceInformation sns() {
        return new SourceInformation(SourceType.SNS);
    }

    public static SourceInformation sqs() {
        return new SourceInformation(SourceType.SQS);
    }
}
