/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package it.pagopa.pn.externalchannels.binding;

import org.springframework.cloud.stream.annotation.Input;
import org.springframework.messaging.SubscribableChannel;

/**
 *
 * @author GIANGR40
 */
public interface PNExtChnInboundSink {
    
    public static final String INPUT = "pnextchnnotifpecinbound";

    @Input(PNExtChnInboundSink.INPUT)
    SubscribableChannel input();       
    
}
