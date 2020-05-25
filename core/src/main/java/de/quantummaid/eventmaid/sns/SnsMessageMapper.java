package de.quantummaid.eventmaid.sns;

import com.amazonaws.services.lambda.runtime.events.SNSEvent;
import de.quantummaid.eventmaid.IncomingMessage;
import de.quantummaid.eventmaid.IncomingMessageBuilder;
import de.quantummaid.eventmaid.MessageBody;
import de.quantummaid.eventmaid.SourceInformation;

import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

/*
        message.getEventSource();
        message.getEventSubscriptionArn();
        message.getEventVersion();
        final SNSEvent.SNS sns = message.getSNS();
        sns.getMessage();
        sns.getMessageAttributes();
        sns.getMessageId();
        sns.getSignature();
        sns.getSigningCertUrl();
        sns.getSubject();
        sns.getTimestamp();
        sns.getTopicArn();
        sns.getType();
        sns.getUnsubscribeUrl();
     */
public class SnsMessageMapper {

    public static IncomingMessage map(SNSEvent.SNSRecord message) {
        final SNSEvent.SNS sns = message.getSNS();
        final MessageBody messageBody = MessageBody.messageBody(sns.getMessage());
        final Map<String, String> properties = mapSnsAttributesToProperties(sns.getMessageAttributes());
        final Map<String, String> headers = extractHeaders(message);
        return IncomingMessageBuilder.anIncomingMessage()
                .withSourceInformation(SourceInformation.sns())
                .withMessageHeaders(headers)
                .withMessageProperties(properties)
                .withBody(messageBody)
                .withRawRequest(message)
                .build();
    }

    public static IncomingMessage map(Map<String, Object> rawRecord) {
        final String messageContent = (String) rawRecord.get("Message");
        final MessageBody messageBody = MessageBody.messageBody(messageContent);
        return IncomingMessageBuilder.anIncomingMessage()
                .withSourceInformation(SourceInformation.sns())
                .withBody(messageBody)
                .build();
    }

    private static Map<String, String> extractHeaders(SNSEvent.SNSRecord message) {
        final Map<String, String> headers = new HashMap<>();
        headers.put(SnsHeaders.EVENT_VERSION, message.getEventVersion());
        headers.put(SnsHeaders.SUBSCRIPTION_ARN, message.getEventSubscriptionArn());
        headers.put(SnsHeaders.TYPE, message.getEventSubscriptionArn());
        final SNSEvent.SNS sns = message.getSNS();
        headers.put(SnsHeaders.TOPIC_ARN, sns.getTopicArn());
        headers.put(SnsHeaders.SUBJECT, sns.getSubject());
        headers.put(SnsHeaders.TIMESTAMP, sns.getTimestamp().toString());
        headers.put(SnsHeaders.SIGNATURE_VERSION, sns.getSignatureVersion());
        headers.put(SnsHeaders.SIGNATURE, sns.getSignature());
        headers.put(SnsHeaders.SIGNING_CERT_URL, sns.getSigningCertUrl());
        headers.put(SnsHeaders.UNSUBSCRIBE_URL, sns.getUnsubscribeUrl());
        return headers;
    }

    private static Map<String, String> mapSnsAttributesToProperties(Map<String, SNSEvent.MessageAttribute> messageAttributes) {
        return messageAttributes.entrySet().stream()
                .collect(Collectors.toMap(
                        Map.Entry::getKey,
                        entry -> entry.getValue().getValue()
                ));
    }
}
