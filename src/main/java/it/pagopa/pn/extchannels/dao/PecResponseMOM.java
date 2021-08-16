package it.pagopa.pn.extchannels.dao;

import it.pagopa.pn.api.dto.events.PnExtChnProgressStatusEvent;
import it.pagopa.pn.commons.mom.MomConsumer;
import it.pagopa.pn.commons.mom.MomProducer;

public interface PecResponseMOM extends MomProducer<PnExtChnProgressStatusEvent>, MomConsumer<PnExtChnProgressStatusEvent> {

}
