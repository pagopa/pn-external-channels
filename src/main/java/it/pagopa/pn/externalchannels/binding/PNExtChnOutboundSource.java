/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package it.pagopa.pn.externalchannels.binding;

import org.springframework.cloud.stream.annotation.Output;
import org.springframework.messaging.MessageChannel;

/**
 *
 * @author GIANGR40
 */
public interface PNExtChnOutboundSource {

    public static final String OUTPUT = "pnextchnnotifprogressstatusoutbound";

    @Output(PNExtChnOutboundSource.OUTPUT)
    MessageChannel output();
}
