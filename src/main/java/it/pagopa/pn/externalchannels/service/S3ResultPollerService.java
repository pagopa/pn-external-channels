package it.pagopa.pn.externalchannels.service;


import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.ListObjectsV2Result;
import com.amazonaws.services.s3.model.S3ObjectSummary;
import it.pagopa.pn.externalchannels.config.properties.S3Properties;
import it.pagopa.pn.externalchannels.event.elaborationresult.PnExtChnElaborationResultEvent;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Slf4j
@ConditionalOnProperty(name = "file-transfer-service.implementation", havingValue = "aws")
public class S3ResultPollerService {

	@Autowired
	AmazonS3 s3client;

	@Autowired
	S3Properties s3Properties;

	@Autowired
	PnExtChnElaborationResultInboundService service;

	private List<String> knownFiles = new ArrayList<>();

//	@PostConstruct
//	public void init() {
//		List<String> keys = getKeys();
//		knownFiles.addAll(keys);
//	}

	@Scheduled(fixedDelay = 2000)
	public void pollResultsBucket() throws IOException {
		List<String> keys = getKeys();
		String key = keys.stream()
				.filter(k -> !knownFiles.contains(k))
				.findFirst()
				.orElse(null);
		if(key != null) {
			PnExtChnElaborationResultEvent evt = new PnExtChnElaborationResultEvent();
			evt.setKey(key);
			knownFiles.add(key);
			service.handleElaborationResult(evt);
		}
	}

	private List<String> getKeys() {
		ListObjectsV2Result res = s3client.listObjectsV2(s3Properties.getInBucket());
		return res.getObjectSummaries().stream()
				.map(S3ObjectSummary::getKey)
				.filter(f -> f.endsWith(PnExtChnS3FileTransferService.CSV_EXTENSION))
				.collect(Collectors.toList());
	}

}
