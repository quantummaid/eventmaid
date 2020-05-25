package de.quantummaid.eventmaid.endpoints;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.lambda.runtime.events.SQSEvent;
import de.quantummaid.eventmaid.EventMaid;
import de.quantummaid.eventmaid.IncomingMessage;

import static de.quantummaid.eventmaid.sqs.SqsMessageMapper.map;

public interface SqsLambdaSource extends Source, RequestHandler<SQSEvent, String> {

    @Override
    default String handleRequest(SQSEvent event, Context context) {
        final EventMaid eventMaid = provideEventMaidInstance();
        for (SQSEvent.SQSMessage record : event.getRecords()) {
            final IncomingMessage incomingMessage = map(record);
            eventMaid.handle(incomingMessage);
        }
        return null;
    }

    EventMaid provideEventMaidInstance();
}
