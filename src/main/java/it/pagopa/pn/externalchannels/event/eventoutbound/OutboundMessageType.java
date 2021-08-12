/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package it.pagopa.pn.externalchannels.event.eventoutbound;

import java.util.Arrays;
import java.util.Collection;
import org.springframework.stereotype.Component;

/**
 *
 * @author GIANGR40
 */
@Component
public class OutboundMessageType {

    /**
     * The outbound message type header
     */
    
    public static final String KAFKA_HEADER_PUBLISHER = "publisher";
    public static final String KAFKA_HEADER_MESSAGEID = "messageId";    
    public static final String KAFKA_HEADER_PARTITIONKEY = "partitionKey";
    public static final String KAFKA_HEADER_MESSAGETYPE = "messageType";
    
    /**
     * The outbound event message types
     */
    public static final String PN_EXTCHN_PROGRESSSTATUS_MESSAGE_TYPE = "PN-EXT_CHN-PROGRESS_STATUS";

    private final Collection<String> outboundMessageTypeCollection;

    public OutboundMessageType() {
        this.outboundMessageTypeCollection
                = Arrays.asList(
                        PN_EXTCHN_PROGRESSSTATUS_MESSAGE_TYPE
                );
    }
    
    public Boolean checkIfKnown(String outboundmessagetype) {        
        if (outboundmessagetype!=null)
            return 
                    this.outboundMessageTypeCollection.contains(outboundmessagetype);
        else
            return Boolean.FALSE;
    }

}
