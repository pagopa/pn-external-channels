package it.pagopa.pn.externalchannels.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;

@Service
@Slf4j
@ConditionalOnProperty(name = "file-transfer-service.implementation", havingValue = "aws")
public class PnExtChnFileTransferServiceImpl implements PnExtChnFileTransferService {
    @Override
    public void transferCsv(byte[] csv) {

    }
}
