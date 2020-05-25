package de.quantummaid.eventmaid.sqs;

import lombok.RequiredArgsConstructor;

import static lombok.AccessLevel.PRIVATE;

@RequiredArgsConstructor(access = PRIVATE)
public final class SqsHeaders {
    public static final String EVENT_SOURCE_ARN = "SQS:EVENT_SOURCE_ARN";
    public static final String AWS_REGION = "SQS:AWS_REGION";
    public static final String APPROXIMATE_RECEIVE_COUNT = "SQS:APPROXIMATE_RECEIVE_COUNT";
    public static final String RECEIPT_HANDLE = "SQS:RECEIPT_HANDLE";
    public static final String SENT_TIMESTAMP = "SQS:SENT_TIMESTAMP";
    public static final String SEQUENCE_NUMBER = "SQS:SEQUENCE_NUMBER";
    public static final String MESSAGE_GROUP_ID = "SQS:MESSAGE_GROUP_ID";
    public static final String SENDER_ID = "SQS:SENDER_ID";
    public static final String MESSAGE_DUPLICATION_ID = "SQS:MESSAGE_DUPLICATION_ID";
    public static final String APPROXIMATE_FIRST_RECEIVE_TIMESTAMP = "SQS:APPROXIMATE_FIRST_RECEIVE_TIMESTAMP";
    public static final String MD5_SUM_OF_BODY = "SQS:MD5_SUM_OF_BODY";
}
