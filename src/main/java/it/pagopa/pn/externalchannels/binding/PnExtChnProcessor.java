package it.pagopa.pn.externalchannels.binding;

import org.springframework.cloud.stream.annotation.Input;
import org.springframework.messaging.SubscribableChannel;

public interface PnExtChnProcessor {

	public static final String NOTIF_PEC_INPUT = "pnextchnnotifpecinbound";
	public static final String ELAB_RESULT_INPUT = "pnextchnelabresult";

	public static final String ERROR_OUTPUT = "pnextchnerror";
	public static final String STATUS_OUTPUT = "pnextchnstatus";

	@Input(PnExtChnProcessor.NOTIF_PEC_INPUT)
	SubscribableChannel notifPecInput();
	@Input(PnExtChnProcessor.ELAB_RESULT_INPUT)
	SubscribableChannel elabResultInput();

	/* sqs binder does not yet support producers */
	/*
	@Output(PnExtChnProcessor.ERROR_OUTPUT)
	MessageChannel errorMessage();
	@Output(PnExtChnProcessor.STATUS_OUTPUT)
	MessageChannel statusMessage();
	*/
}
