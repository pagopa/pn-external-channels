package it.pagopa.pn.externalchannels.dto;

import lombok.Data;
import lombok.Getter;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbAttribute;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbBean;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbPartitionKey;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbSortKey;

import java.time.Instant;
import java.util.List;

/**
 * Pojo che viene salvato nel database in memory. Tiene traccia dei messaggi (codici) da inviare.
 * Conserva in una queue i codici da mandare e dopo quanto tempo inviarli {@link #codeTimeToSendQueue}.
 * La coppia codice-tempo è confezionata nella classe @{@link CodeTimeToSend}.
 * <p>
 * Esempio: codeTimeToSendQueue = C000,5s C001,10s C005,5s C003,6s.
 * Il codice C000 sarà mandato dopo cinque secondi dall'ultimo invio, poi il codice C001 dopo 10 secondi e così via.
 * <p>
 * La prima volta che dovrà essere mandato un codice, verrà utilizzato il campo {@link #createMessageTimestamp}
 * essendo {@link #lastMessageSentTimestamp} non popolato ancora.
 */
@Data
@DynamoDbBean
public class NotificationProgress {

    public static final String COL_IUN = "iun";
    public static final String COL_DESTINATION_ADDRESS = "destinationAddress";

    private static final String COL_REGISTER_LETTER_CODE = "registeredLetterCode";
    private static final String COL_OUTPUT = "output";
    private static final String COL_OUTPUT_ENDPOINT = "outputEndpoint";
    private static final String COL_OUTPUT_SERVICE_ID = "outputServiceId";
    private static final String COL_OUTPUT_API_KEY = "outputApiKey";
    private static final String COL_REQUEST_ID = "requestId";
    private static final String COL_CODE_TIME_TO_SEND_QUEUE = "codeTimeToSendQueue";
    private static final String COL_LAST_MESSAGE_SENT_TIMESTAMP = "lastMessageSentTimestamp";
    private static final String COL_CREATE_MESSAGE_TIMESTAMP = "createMessageTimestamp";
    private static final String COL_CHANNEL = "channel";
    private static final String COL_DISCOVERED_ADDRESS = "discoveredAddress";
    private static final String COL_N_ATTEMPT = "nAttempt";
    private static final String COL_DOCUMNETS = "documents";


    @Getter(onMethod=@__({@DynamoDbPartitionKey, @DynamoDbAttribute(COL_IUN)})) private String iun;

    @Getter(onMethod=@__({@DynamoDbSortKey, @DynamoDbAttribute(COL_DESTINATION_ADDRESS)})) private String destinationAddress;

    @Getter(onMethod=@__({@DynamoDbAttribute(COL_REGISTER_LETTER_CODE)})) private String registeredLetterCode;    // usato negli eventi cartacei

    @Getter(onMethod=@__({@DynamoDbAttribute(COL_OUTPUT)})) private PROGRESS_OUTPUT_CHANNEL output;

    @Getter(onMethod=@__({@DynamoDbAttribute(COL_OUTPUT_ENDPOINT)})) private String outputEndpoint;

    @Getter(onMethod=@__({@DynamoDbAttribute(COL_OUTPUT_SERVICE_ID)})) private String outputServiceId;

    @Getter(onMethod=@__({@DynamoDbAttribute(COL_OUTPUT_API_KEY)})) private String outputApiKey;

    @Getter(onMethod=@__({@DynamoDbAttribute(COL_REQUEST_ID)})) private String requestId;

    @Getter(onMethod=@__({@DynamoDbAttribute(COL_CODE_TIME_TO_SEND_QUEUE)})) private List<CodeTimeToSend> codeTimeToSendQueue;

    @Getter(onMethod=@__({@DynamoDbAttribute(COL_LAST_MESSAGE_SENT_TIMESTAMP)})) private Instant lastMessageSentTimestamp;

    @Getter(onMethod=@__({@DynamoDbAttribute(COL_CREATE_MESSAGE_TIMESTAMP)})) private Instant createMessageTimestamp;

    @Getter(onMethod=@__({@DynamoDbAttribute(COL_CHANNEL)})) private String channel;

    @Getter(onMethod=@__({@DynamoDbAttribute(COL_DISCOVERED_ADDRESS)})) private DiscoveredAddressEntity discoveredAddress; // per notifica cartacea (esito negativo con indagine postino)

    @Getter(onMethod=@__({@DynamoDbAttribute(COL_N_ATTEMPT)})) private Integer nAttempt;

    @Getter(onMethod=@__({@DynamoDbAttribute(COL_DOCUMNETS)}))private List<List<String>> documents;


    public enum PROGRESS_OUTPUT_CHANNEL{
        QUEUE_DELIVERY_PUSH,
        QUEUE_USER_ATTRIBUTES,
        QUEUE_PAPER_CHANNEL,
        WEBHOOK_EXT_CHANNEL
    }

}
