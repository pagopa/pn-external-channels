package it.pagopa.pn.externalchannels.service;

import it.pagopa.pn.api.dto.events.PnExtChnPaperEvent;
import it.pagopa.pn.api.dto.events.PnExtChnPecEvent;
import it.pagopa.pn.api.dto.events.StandardEventHeader;
import it.pagopa.pn.externalchannels.controller.PnExtChnController;
import it.pagopa.pn.externalchannels.service.pnextchnservice.PnExtChnService;
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
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
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
    private static final String URL_GET_DOWNLOAD_LINKS = "/external-channel/attachments/getDownloadLinks";
    private static final String URL_GET_ATTACHMENT_KEYS = "/external-channel/attachments/getAttachmentKeys";



    @Autowired
    private MockMvc mockMvc;

    @MockBean
    PnExtChnService pnExtChnService;

    @MockBean
    PnExtChnFileTransferService fileTransferService;

    @Test
    void shouldGetAttachmentKeys() throws Exception {

        this.mockMvc
                .perform(
                        get(URL_GET_ATTACHMENT_KEYS)
                                .queryParam("eventId", "eventId")
                )
                .andDo(print())
                .andExpect(status().isOk());
    }

    @Test
    void shouldGetDownloadLinks() throws Exception {

        this.mockMvc
                .perform(
                        get(URL_GET_DOWNLOAD_LINKS)
                                .queryParam("attachmentKey", "key")
                )
                .andDo(print())
                .andExpect(status().isOk());
    }

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

        PnExtChnPaperEvent event = mockPaperMessage(null, "1");
        StandardEventHeader header = event.getHeader(); // should discard
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

        PnExtChnPecEvent event = mockPecMessage(null, "1");
        StandardEventHeader header = event.getHeader(); // should discard
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