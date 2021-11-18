package it.pagopa.pn.externalchannels.service;

import java.io.IOException;

public interface PnExtChnFileTransferService {

    void transferCsv(byte[] csv, String name);

    void transferAttachment(byte[] attachment, String name);

    byte[] retrieveCsv(String key) throws IOException;

    String getDownloadLink(String attachmentKey);

}
