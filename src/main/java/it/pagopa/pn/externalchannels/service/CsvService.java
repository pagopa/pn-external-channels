package it.pagopa.pn.externalchannels.service;

import it.pagopa.pn.externalchannels.entities.queuedmessage.QueuedMessage;
import it.pagopa.pn.externalchannels.pojos.CsvTransformationResult;

import java.util.List;

public interface CsvService {

    CsvTransformationResult queuedMessagesToCsv(List<QueuedMessage> messages);

}
