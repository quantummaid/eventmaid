package de.quantummaid.eventmaid.sqs;

import com.amazonaws.services.lambda.runtime.events.SQSEvent;
import de.quantummaid.eventmaid.IncomingMessage;
import de.quantummaid.eventmaid.IncomingMessageBuilder;
import de.quantummaid.eventmaid.MessageBody;
import de.quantummaid.eventmaid.SourceInformation;

import java.util.HashMap;
import java.util.Map;

public class SqsMessageMapper {

    public static IncomingMessage map(SQSEvent.SQSMessage message) {
        final MessageBody messageBody = MessageBody.messageBody(message.getBody());
        return IncomingMessageBuilder.anIncomingMessage()
                .withBody(messageBody)
                .withSourceInformation(SourceInformation.sqs())
                .build();
    }

    @SuppressWarnings("unchecked")
    private static Map<String, String> extractHeaders(Map<String, Object> rawRecord) {
        final Map<String, String> headers = new HashMap<>();
        headers.put(SqsHeaders.EVENT_SOURCE_ARN, (String) rawRecord.get("eventSourceARN"));
        headers.put(SqsHeaders.AWS_REGION, (String) rawRecord.get("awsRegion"));
        headers.put(SqsHeaders.MD5_SUM_OF_BODY, (String) rawRecord.get("md5OfBody"));
        headers.put(SqsHeaders.RECEIPT_HANDLE, (String) rawRecord.get("receiptHandle"));
        final Map<String, Object> attributes = (Map<String, Object>) rawRecord.get("attributes");
        headers.put(SqsHeaders.APPROXIMATE_RECEIVE_COUNT, (String) attributes.get("ApproximateReceiveCount"));
        headers.put(SqsHeaders.SENT_TIMESTAMP, (String) attributes.get("SentTimestamp"));
        headers.put(SqsHeaders.SEQUENCE_NUMBER, (String) attributes.get("SequenceNumber"));
        headers.put(SqsHeaders.MESSAGE_GROUP_ID, (String) attributes.get("MessageGroupId"));
        headers.put(SqsHeaders.SENDER_ID, (String) attributes.get("SenderId"));
        headers.put(SqsHeaders.MESSAGE_DUPLICATION_ID, (String) attributes.get("MessageDeduplicationId"));
        headers.put(SqsHeaders.APPROXIMATE_FIRST_RECEIVE_TIMESTAMP, (String) attributes.get("ApproximateFirstReceiveTimestamp"));
        return headers;
    }

    public static IncomingMessage map(Map<String, Object> rawRecord) {
        final String bodyString = (String) rawRecord.get("body");
        final String messageIdString = (String) rawRecord.get("messageId");
        final MessageBody messageBody = MessageBody.messageBody(bodyString);
        final Map<String, String> messageHeaders = extractHeaders(rawRecord);
        return IncomingMessageBuilder.anIncomingMessage()
                .withBody(messageBody)
                .withMessageHeaders(messageHeaders)
                .withSourceInformation(SourceInformation.sqs())
                .withRawRequest(rawRecord)
                .withMessageId(messageIdString)
                .build();
    }


   /*
        message.getAttributes();
        message.getMessageId();
        message.getMessageAttributes();
        message.getBody();
        message.getReceiptHandle();
        message.getAwsRegion();
        message.getEventSource();
        message.getEventSourceArn();
        message.getMd5OfBody();
        message.getMd5OfMessageAttributes();
        message.getReceiptHandle();
     */
}
