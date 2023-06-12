package it.pagopa.pn.extrnalchannels.util;

import it.pagopa.pn.externalchannels.util.EventCodeIntForDigital;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertNotNull;

class EventCodeIntForDigitalTests {


    @Test
    void enumTest() {
        assertNotNull(EventCodeIntForDigital.C000);
        assertNotNull(EventCodeIntForDigital.C001);
        assertNotNull(EventCodeIntForDigital.C002);
        assertNotNull(EventCodeIntForDigital.C003);
        assertNotNull(EventCodeIntForDigital.C004);
        assertNotNull(EventCodeIntForDigital.C005);
        assertNotNull(EventCodeIntForDigital.C006);
        assertNotNull(EventCodeIntForDigital.C007);
        assertNotNull(EventCodeIntForDigital.C008);
        assertNotNull(EventCodeIntForDigital.C009);
        assertNotNull(EventCodeIntForDigital.C010);
        assertNotNull(EventCodeIntForDigital.DP00);
        assertNotNull(EventCodeIntForDigital.DP10);
    }

}
