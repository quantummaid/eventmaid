package de.quantummaid.eventmaid.endpoints;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.lambda.runtime.events.SNSEvent;
import de.quantummaid.eventmaid.EventMaid;
import de.quantummaid.eventmaid.IncomingMessage;

import static de.quantummaid.eventmaid.sns.SnsMessageMapper.map;

public interface SnsLambdaSource extends Source, RequestHandler<SNSEvent, String> {

    @Override
    default String handleRequest(SNSEvent event, Context context) {
        final EventMaid eventMaid = provideEventMaidInstance();
        for (SNSEvent.SNSRecord record : event.getRecords()) {
            final IncomingMessage incomingMessage = map(record);
            eventMaid.handle(incomingMessage);
        }
        return null;
    }

    EventMaid provideEventMaidInstance();
}
