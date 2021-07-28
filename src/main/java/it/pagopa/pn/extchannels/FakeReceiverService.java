package it.pagopa.pn.extchannels;

import it.pagopa.pn.extchannels.dao.PecRequestMOM;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.Duration;

@Service
public class FakeReceiverService {

    private Logger logger = LoggerFactory.getLogger( this.getClass() );

    private final PecRequestMOM pecMom;

    public FakeReceiverService( PecRequestMOM pecMom ) {
        this.pecMom = pecMom;
    }

    @Scheduled(fixedDelay = 1000)
    public void scheduleFixedDelayTask() {
        logger.info("Method Scheduled" );

        pecMom.poll( Duration.ofSeconds(1)).thenApply( ( pecRequest ) -> {
            logger.info("Queue polling done" );
            pecRequest.forEach( n -> {
                logger.info("Received pec send request " + n.getIun() + " TO " + n.getAddress() );
            });
            return null;
        });

    }
}
