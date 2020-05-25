package de.quantummaid.eventmaid.endpoints;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import de.quantummaid.eventmaid.EventMaid;
import de.quantummaid.eventmaid.IncomingMessage;
import de.quantummaid.eventmaid.sns.SnsMessageMapper;
import de.quantummaid.eventmaid.sqs.SqsMessageMapper;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public interface GenericLambdaSource extends Source, RequestHandler<Map<String, Object>, String> {

    @SuppressWarnings("unchecked")
    @Override
    default String handleRequest(final Map<String, Object> event, Context context) {
        System.out.println("STARTING");
        final EventMaid eventMaid = provideEventMaidInstance();
        System.out.println("GETTING EVENTMAID");
        final List<LinkedHashMap<String, Object>> records = (List<LinkedHashMap<String, Object>>) event.get("Records");
        for (LinkedHashMap<String, Object> record : records) {
            if (record.get("eventSource") != null && record.get("eventSource").equals("aws:sqs")) {
                final IncomingMessage incomingMessage = SqsMessageMapper.map(record);
                System.out.println("MAPPED");
                eventMaid.handle(incomingMessage);
                System.out.println("FINISHED");
            } else if (record.get("EventSource") != null && record.get("EventSource").equals("aws:sns")) {
                final IncomingMessage incomingMessage = SnsMessageMapper.map(record);
                eventMaid.handle(incomingMessage);
            } else {
                System.out.println("Unknown event:" + record);
            }
        }
        return null;
    }

    EventMaid provideEventMaidInstance();
}
