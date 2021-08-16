package it.pagopa.pn.extchannels.dao;

import it.pagopa.pn.api.dto.events.PnExtChnPecEvent;
import it.pagopa.pn.commons.mom.MomConsumer;
import it.pagopa.pn.commons.mom.MomProducer;
import it.pagopa.pn.extchannels.events.PecRequest;

public interface PecRequestMOM extends MomProducer<PnExtChnPecEvent>, MomConsumer<PnExtChnPecEvent> {

}
