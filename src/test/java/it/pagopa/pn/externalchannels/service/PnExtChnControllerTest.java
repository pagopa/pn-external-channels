package it.pagopa.pn.externalchannels.service;

import it.pagopa.pn.api.dto.events.PnExtChnPaperEvent;
import it.pagopa.pn.api.dto.events.PnExtChnPecEvent;
import it.pagopa.pn.api.dto.events.StandardEventHeader;
import it.pagopa.pn.externalchannels.controller.PnExtChnController;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.cassandra.CassandraAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.web.servlet.MockMvc;

import static it.pagopa.pn.api.dto.events.StandardEventHeader.*;
import static it.pagopa.pn.externalchannels.service.TestUtils.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@EnableAutoConfiguration(exclude = {CassandraAutoConfiguration.class})
@ContextConfiguration(classes = {
        PnExtChnController.class
})
@AutoConfigureMockMvc
class PnExtChnControllerTest {

    private static final String URL_SAVE_PAPER_NOTIF = "/external-channel/paper/saveNotification";
    private static final String URL_SAVE_DIGITAL_NOTIF = "/external-channel/digital/saveNotification";

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    PnExtChnService pnExtChnService;

    @Test
    void paperShouldReturnStatus200() throws Exception {


        PnExtChnPaperEvent event = mockPaperMessage();
        StandardEventHeader header = event.getHeader();
        String content = toJson(event.getPayload());

        this.mockMvc
                .perform(
                        post(URL_SAVE_PAPER_NOTIF)
                                .contentType(MediaType.APPLICATION_JSON)
                                .header(PN_EVENT_HEADER_EVENT_ID, header.getEventId())
                                .header(PN_EVENT_HEADER_EVENT_TYPE, header.getEventType())
                                .header(PN_EVENT_HEADER_CREATED_AT, header.getCreatedAt().toString())
                                .header(PN_EVENT_HEADER_IUN, header.getIun())
                                .header(PN_EVENT_HEADER_PUBLISHER, header.getPublisher())
                                .content(content)
                )
                .andDo(print())
                .andExpect(status().isOk());
    }

    @Test
    void paperShouldReturnStatus400() throws Exception {

        PnExtChnPaperEvent event = mockPaperMessage();
        StandardEventHeader header = event.getHeader();
        header.setIun(""); // should discard
        event.getPayload().setIun(null); // should discard
        String content = toJson(event.getPayload());

        this.mockMvc
                .perform(
                        post(URL_SAVE_PAPER_NOTIF)
                                .contentType(MediaType.APPLICATION_JSON)
                                .header(PN_EVENT_HEADER_EVENT_ID, header.getEventId())
                                .header(PN_EVENT_HEADER_EVENT_TYPE, header.getEventType())
                                .header(PN_EVENT_HEADER_CREATED_AT, header.getCreatedAt().toString())
                                .header(PN_EVENT_HEADER_IUN, header.getIun())
                                .header(PN_EVENT_HEADER_PUBLISHER, header.getPublisher())
                                .content(content)
                )
                .andDo(print())
                .andExpect(status().isBadRequest());
    }

    @Test
    void digitalShouldReturnStatus200() throws Exception {

        PnExtChnPecEvent event = mockPecMessage();
        StandardEventHeader header = event.getHeader();
        String content = toJson(event.getPayload());

        this.mockMvc
                .perform(
                        post(URL_SAVE_DIGITAL_NOTIF)
                                .contentType(MediaType.APPLICATION_JSON)
                                .header(PN_EVENT_HEADER_EVENT_ID, header.getEventId())
                                .header(PN_EVENT_HEADER_EVENT_TYPE, header.getEventType())
                                .header(PN_EVENT_HEADER_CREATED_AT, header.getCreatedAt().toString())
                                .header(PN_EVENT_HEADER_IUN, header.getIun())
                                .header(PN_EVENT_HEADER_PUBLISHER, header.getPublisher())
                                .content(content)
                )
                .andDo(print())
                .andExpect(status().isOk());
    }

    @Test
    void digitalShouldReturnStatus400() throws Exception {

        PnExtChnPecEvent event = mockPecMessage();
        StandardEventHeader header = event.getHeader();
        header.setIun(""); // should discard
        event.getPayload().setIun(null); // should discard
        String content = toJson(event.getPayload());

        this.mockMvc
                .perform(
                        post(URL_SAVE_DIGITAL_NOTIF)
                                .contentType(MediaType.APPLICATION_JSON)
                                .header(PN_EVENT_HEADER_EVENT_ID, header.getEventId())
                                .header(PN_EVENT_HEADER_EVENT_TYPE, header.getEventType())
                                .header(PN_EVENT_HEADER_CREATED_AT, header.getCreatedAt().toString())
                                .header(PN_EVENT_HEADER_IUN, header.getIun())
                                .header(PN_EVENT_HEADER_PUBLISHER, header.getPublisher())
                                .content(content)
                )
                .andDo(print())
                .andExpect(status().isBadRequest());
    }

}