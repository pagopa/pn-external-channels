package it.pagopa.pn.externalchannels.arubapec.jmailutils;

import lombok.Getter;

import javax.mail.Header;
import javax.mail.Message;
import javax.mail.MessagingException;
import java.io.IOException;
import java.time.Instant;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;

@Getter
public class PecEntry {

    public static final String RECIPE_TYPE_HEADER__DELIVERED_VALUE = "avvenuta-consegna";
    public static final String RECIPE_TYPE_HEADER__ACCEPTED_VALUE = "accettazione";

    public static final String RECIPE_TYPE_HEADER_LOWERCASE_NAME = "x-ricevuta";
    public static final String MESSAGE_ID_HEADER_LOWERCASE_NAME = "message-id";
    public static final String REFERENCED_MESSAGE_ID_HEADER_LOWERCASE_NAME = "x-riferimento-message-id";
    public static final String TO_HEADER_LOWERCASE_NAME = "to";
    public static final String FROM_HEADER_LOWERCASE_NAME = "from";

    public enum Type {
        MESSAGE,
        ACCEPTED_RECIPE,
        DELIVERED_RECIPE
    }

    private final Type type;
    private final String entryId;
    private final String referredId;

    private final String subject;
    private final String to;
    private final String from;
    private final Instant whenReceived;

    public PecEntry( Message jmailMsg ) throws MessagingException, IOException {
        Map<String, String> headers = headersToMap( jmailMsg );

        String recipeType = headers.get( RECIPE_TYPE_HEADER_LOWERCASE_NAME );
        this.type = recipeTypeToPecEntryType( recipeType );
        this.entryId = headers.get( MESSAGE_ID_HEADER_LOWERCASE_NAME );
        this.referredId = headers.get( REFERENCED_MESSAGE_ID_HEADER_LOWERCASE_NAME );
        this.to = headers.get( TO_HEADER_LOWERCASE_NAME);
        this.from = headers.get( FROM_HEADER_LOWERCASE_NAME );;
        this.whenReceived = jmailMsg.getReceivedDate().toInstant();
        this.subject = jmailMsg.getSubject();
    }

    private Type recipeTypeToPecEntryType(String recipeType) {
        Type pecEntryType;
        if( recipeType == null ) {
            pecEntryType = Type.MESSAGE;
        }
        else if( RECIPE_TYPE_HEADER__ACCEPTED_VALUE.equals( recipeType )) {
            pecEntryType = Type.ACCEPTED_RECIPE;
        }
        else if( RECIPE_TYPE_HEADER__DELIVERED_VALUE.equals( recipeType )) {
            pecEntryType = Type.DELIVERED_RECIPE;
        }
        else {
            throw new IllegalArgumentException(String.format("Recipe type [%s] not supported", recipeType));
        }

        return pecEntryType;
    }

    private Map<String,String> headersToMap( Message jmailMsg ) throws MessagingException {
        Map<String,String> headersMap = new HashMap<>();

        Enumeration headers = jmailMsg.getAllHeaders();
        while( headers.hasMoreElements() ) {
            Header header = (Header) headers.nextElement();
            String headerName = header.getName();
            String headerValue = header.getValue();

            headersMap.put( headerName.toLowerCase(), headerValue );
        }

        return headersMap;
    }

}
