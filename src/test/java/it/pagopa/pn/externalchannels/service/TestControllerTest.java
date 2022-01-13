package it.pagopa.pn.externalchannels.service;

import it.pagopa.pn.externalchannels.controller.TestController;
import it.pagopa.pn.externalchannels.entities.csvtemplate.CsvTemplate;
import it.pagopa.pn.externalchannels.entities.queuedmessage.QueuedMessage;
import it.pagopa.pn.externalchannels.entities.resultdescriptor.ResultDescriptor;
import it.pagopa.pn.externalchannels.entities.senderpa.SenderConfigByDenomination;
import it.pagopa.pn.externalchannels.entities.senderpa.SenderPecByDenomination;
import it.pagopa.pn.externalchannels.repositories.cassandra.*;
import org.junit.jupiter.api.BeforeEach;
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

import java.util.ArrayList;
import java.util.Arrays;

import static it.pagopa.pn.externalchannels.service.TestUtils.toJson;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@EnableAutoConfiguration(exclude = {CassandraAutoConfiguration.class})
@ContextConfiguration(classes = {
        TestController.class
})
@AutoConfigureMockMvc
class TestControllerTest {

    private static final String URL_getCassandraQueuedMessage = "/external-channel/test/cassandra/getQueuedMessage";
    private static final String URL_getCassandra = "/external-channel/test/cassandra";
    private static final String URL_postCassandraQueuedMessage = "/external-channel/test/cassandra/postQueuedMessage";
    private static final String URL_postCassandraCsvTemplate = "/external-channel/test/cassandra/postCsvTemplate";
    private static final String URL_postResultDescriptors = "/external-channel/test/cassandra/postResultDescriptors";
    private static final String URL_postPaPecs = "/external-channel/test/cassandra/postPaPecs";
    private static final String URL_postPaConfigs = "/external-channel/test/cassandra/postPaConfigs";
    private static final String URL_clear = "/external-channel/test/any/clear";
    private static final String URL_triggerJob = "/external-channel/test/job/trigger";

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    QueuedMessageRepository queuedMessageRepository;

    @MockBean
    DiscardedMessageRepository discardedMessageRepository;

    @MockBean
    CsvTemplateRepository csvTemplateRepository;

    @MockBean
    ResultDescriptorRepository resultDescriptorRepository;

    @MockBean
    ScheduledSenderService scheduledSenderService;

    @MockBean
    SenderConfigByDenominationRepository senderConfigByDenominationRepository;

    @MockBean
    SenderPecByDenominationRepository senderPecByDenominationRepository;

    @BeforeEach
    void init(){
        when(queuedMessageRepository.findAll()).thenReturn(new ArrayList<>());
        when(discardedMessageRepository.findAll()).thenReturn(new ArrayList<>());
        when(senderConfigByDenominationRepository.saveAll(any())).thenAnswer(i -> i.getArguments()[0]);
        when(senderPecByDenominationRepository.saveAll(any())).thenAnswer(i -> i.getArguments()[0]);
        when(resultDescriptorRepository.saveAll(any())).thenAnswer(i -> i.getArguments()[0]);
        when(queuedMessageRepository.findAll()).thenReturn(new ArrayList<>());
    }

    @Test
    void testGetCassandraQueuedMessage() throws Exception {

        this.mockMvc
                .perform(
                        get(URL_getCassandraQueuedMessage + "/1")
                )
                .andDo(print())
                .andExpect(status().isOk());
    }

    @Test
    void shouldGetCassandra() throws Exception {

        this.mockMvc
                .perform(
                        get(URL_getCassandra)
                )
                .andDo(print())
                .andExpect(status().isOk());
    }

    @Test
    void testPostCassandraQueuedMessage() throws Exception {

        String content = toJson(new QueuedMessage());

        this.mockMvc
                .perform(
                        post(URL_postCassandraQueuedMessage)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(content)
                )
                .andDo(print())
                .andExpect(status().isOk());
    }

    @Test
    void testPostCassandraCsvTemplate() throws Exception {

        String content = toJson(new CsvTemplate());

        this.mockMvc
                .perform(
                        post(URL_postCassandraCsvTemplate)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(content)
                )
                .andDo(print())
                .andExpect(status().isOk());
    }

    @Test
    void testPostResultDescriptors() throws Exception {

        String content = toJson(Arrays.asList(new ResultDescriptor()));

        this.mockMvc
                .perform(
                        post(URL_postResultDescriptors)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(content)
                )
                .andDo(print())
                .andExpect(status().isOk());
    }

    @Test
    void testPostPaPecs() throws Exception {

        String content = toJson(Arrays.asList(new SenderPecByDenomination()));

        this.mockMvc
                .perform(
                        post(URL_postPaPecs)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(content)
                )
                .andDo(print())
                .andExpect(status().isOk());
    }

    @Test
    void testPostPaConfigs() throws Exception {

        String content = toJson(Arrays.asList(new SenderConfigByDenomination()));

        this.mockMvc
                .perform(
                        post(URL_postPaConfigs)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(content)
                )
                .andDo(print())
                .andExpect(status().isOk());
    }

    @Test
    void testClear() throws Exception {

        this.mockMvc
                .perform(
                        get(URL_clear)
                )
                .andDo(print())
                .andExpect(status().isOk());
    }

    @Test
    void testTriggerJob() throws Exception {

        this.mockMvc
                .perform(
                        get(URL_triggerJob)
                )
                .andDo(print())
                .andExpect(status().isOk());
    }

}