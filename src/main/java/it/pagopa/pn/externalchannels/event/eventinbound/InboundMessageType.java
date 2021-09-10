/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package it.pagopa.pn.externalchannels.event.eventinbound;

import java.util.Arrays;
import java.util.Collection;
import org.springframework.stereotype.Component;

/**
 *
 * @author GIANGR40
 */
@Component
public class InboundMessageType {

    /**
     * The inbound message type header
     */
    
    public static final String KAFKA_HEADER_PUBLISHER = "publisher";
    public static final String KAFKA_HEADER_MESSAGEID = "messageId";    
    public static final String KAFKA_HEADER_PARTITIONKEY = "partitionKey";
    public static final String KAFKA_HEADER_MESSAGETYPE = "messageType";
    
    /**
     * The inbound event message types
     */
    public static final String PN_EXTCHN_PEC_MESSAGE_TYPE = "PN-EXT_CHN-PEC";

    private final Collection<String> inboundMessageTypeCollection;

    public InboundMessageType() {
        this.inboundMessageTypeCollection
                = Arrays.asList(
                        PN_EXTCHN_PEC_MESSAGE_TYPE
                );
    }
    
    public boolean checkIfKnown(String inboundmessagetype) {        
        if (inboundmessagetype!=null)
            return 
                    this.inboundMessageTypeCollection.contains(inboundmessagetype);
        else
            return Boolean.FALSE;
    }

}
