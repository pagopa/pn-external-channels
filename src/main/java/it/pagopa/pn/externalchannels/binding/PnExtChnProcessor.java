package it.pagopa.pn.externalchannels.binding;

import org.springframework.cloud.stream.annotation.Input;
import org.springframework.cloud.stream.annotation.Output;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.SubscribableChannel;

public interface PnExtChnProcessor {

	public static final String INPUT = "pnextchnnotifpecinbound";
	public static final String ERROR_OUTPUT = "error-messages";
	public static final String STATUS_OUTPUT = "status-messages";

	@Input(PnExtChnProcessor.INPUT)
	SubscribableChannel input();   
        /* not supported yet in SQS */
	@Output(PnExtChnProcessor.ERROR_OUTPUT)
	MessageChannel errorMessage();
	@Output(PnExtChnProcessor.STATUS_OUTPUT)
	MessageChannel statusMessage();
        
}
