package de.quantummaid.eventmaid.endpoints;

import de.quantummaid.eventmaid.DeduplicationId;
import de.quantummaid.eventmaid.MessageGroupId;
import de.quantummaid.eventmaid.OutgoingMessage;
import lombok.RequiredArgsConstructor;
import software.amazon.awssdk.services.sqs.SqsClient;
import software.amazon.awssdk.services.sqs.model.SendMessageRequest;

import static de.quantummaid.eventmaid.endpoints.MissingMessageGroupIdException.missingMessageGroupIdException;
import static lombok.AccessLevel.PRIVATE;

@RequiredArgsConstructor(access = PRIVATE)
public final class SqsFifoDestination implements Destination {
    private final String queueUrl;
    private final SqsClient amazonSQSClient;

    public static SqsFifoDestination sqsFifoDestination(final String queueUrl, final SqsClient amazonSQSClient) {
        return new SqsFifoDestination(queueUrl, amazonSQSClient);
    }

    public void send(final OutgoingMessage outgoingMessage) {
        final String content = outgoingMessage.getMessageBody().getContent();
        final SendMessageRequest.Builder messageRequestBuilder = SendMessageRequest.builder().queueUrl(queueUrl)
                .messageBody(content);
        setMandatoryGroupId(messageRequestBuilder, outgoingMessage);
        setOptionalDeduplicationId(messageRequestBuilder, outgoingMessage);
        //setOptionalMessageProperties(messageRequestBuilder, outgoingMessage);
        //setOptionalMessageHeaders(messageRequestBuilder, outgoingMessage);
        amazonSQSClient.sendMessage(messageRequestBuilder.build());
    }

    private void setMandatoryGroupId(final SendMessageRequest.Builder messageRequestBuilder , final OutgoingMessage outgoingMessage) {
        final MessageGroupId messageGroupId = outgoingMessage.getMessageGroupId();
        if (messageGroupId == null) {
            throw missingMessageGroupIdException("SQS Fifo Queues require a message group id to be set for all messages.");
        }
        final String value = messageGroupId.getValue();
        messageRequestBuilder.messageGroupId(value);
    }

    private void setOptionalDeduplicationId(final SendMessageRequest.Builder messageRequestBuilder , final OutgoingMessage outgoingMessage) {
        final DeduplicationId deduplicationId = outgoingMessage.getDeduplicationId();
        if (deduplicationId != null) {
            final String value = deduplicationId.getValue();
            messageRequestBuilder.messageDeduplicationId(value);
        }
    }
    //TODO:
    /*private void setOptionalMessageProperties(final SendMessageRequest.Builder messageRequestBuilder , final OutgoingMessage outgoingMessage) {
        final Map<String, String> messageProperties = outgoingMessage.getMessageProperties();
        messageProperties.forEach((key, value) -> {
            final MessageAttributeValue messageAttributeValue = MessageAttributeValue.builder()
                    .stringValue(value)
                    .dataType("String")
                    .build();
        });
        messageRequestBuilder.messageAttributes(key, messageAttributeValue)
    }

    private void setOptionalMessageHeaders(final SendMessageRequest messageRequest, final OutgoingMessage outgoingMessage) {
        final Map<String, String> messageProperties = outgoingMessage.getMessageHeaders();
        messageProperties.forEach((key, value) -> {
            final MessageSystemAttributeValue messageSystemAttributeValue = new MessageSystemAttributeValue()
                    .withStringValue(value);
            messageSystemAttributeValue.setDataType("String");
            messageRequest.addMessageSystemAttributesEntry(key, messageSystemAttributeValue);
        });
    }*/
}
