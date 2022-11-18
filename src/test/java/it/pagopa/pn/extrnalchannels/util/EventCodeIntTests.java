package it.pagopa.pn.extrnalchannels.util;

import it.pagopa.pn.externalchannels.util.EventCodeInt;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertNotNull;

class EventCodeIntTests {


    @Test
    void enumTest() {
        assertNotNull(EventCodeInt.C000);
        assertNotNull(EventCodeInt.C001);
        assertNotNull(EventCodeInt.C002);
        assertNotNull(EventCodeInt.C003);
        assertNotNull(EventCodeInt.C004);
        assertNotNull(EventCodeInt.C005);
        assertNotNull(EventCodeInt.C006);
        assertNotNull(EventCodeInt.C007);
        assertNotNull(EventCodeInt.C008);
        assertNotNull(EventCodeInt.C009);
        assertNotNull(EventCodeInt.C010);
        assertNotNull(EventCodeInt.DP00);
        assertNotNull(EventCodeInt.DP10);
    }

}
