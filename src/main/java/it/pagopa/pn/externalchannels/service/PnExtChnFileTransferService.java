package it.pagopa.pn.externalchannels.service;

import java.util.Map;

public interface PnExtChnFileTransferService {

    void transferCsv(byte[] csv);

    Map<String, String> retrieveElaborationResult(String key);
}
