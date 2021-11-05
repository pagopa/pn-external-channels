package it.pagopa.pn.externalchannels.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.MappingIterator;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.csv.CsvMapper;
import com.fasterxml.jackson.dataformat.csv.CsvSchema;
import it.pagopa.pn.externalchannels.config.properties.JobProperties;
import it.pagopa.pn.externalchannels.entities.csvtemplate.Column;
import it.pagopa.pn.externalchannels.entities.csvtemplate.CsvTemplate;
import it.pagopa.pn.externalchannels.entities.queuedmessage.QueuedMessage;
import it.pagopa.pn.externalchannels.event.QueuedMessageChannel;
import it.pagopa.pn.externalchannels.pojos.CsvTransformationResult;
import it.pagopa.pn.externalchannels.pojos.ElaborationResult;
import it.pagopa.pn.externalchannels.repositories.cassandra.CsvTemplateRepository;
import it.pagopa.pn.externalchannels.util.Constants;
import it.pagopa.pn.externalchannels.util.Util;
import it.pagopa.pn.externalchannels.util.formatters.ColumnFormatter;
import it.pagopa.pn.externalchannels.util.formatters.ColumnFormatters;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.beanutils.PropertyUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.StringWriter;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.time.LocalTime;
import java.time.ZoneId;
import java.util.*;
import java.util.stream.Collectors;

@Service
@Slf4j
public class CsvServiceImpl implements CsvService {

    private static final String QUEUED_MESSAGES_FILENAME = "%04d_%04d_%d_%tY%4$tm%4$td_PZ%d.csv";

    private static final String AUTOINC_PLACEHOLDER = "*AUTOINC*";

    @Autowired
    JobProperties jobProperties;

    @Autowired
    CsvTemplateRepository csvTemplateRepository;

    public String formatFileName(Integer rows, QueuedMessageChannel channel){
        Date now = new Date();
        Integer prog = LocalTime.now(ZoneId.of("Europe/Rome")).toSecondOfDay();
        Integer customer = jobProperties.getCustomer();
        Integer macroservice;
        if (QueuedMessageChannel.DIGITAL == channel)
            macroservice = jobProperties.getDigitalMacroservice();
        else
            macroservice = jobProperties.getPhysicalMacroservice();
        return String.format(QUEUED_MESSAGES_FILENAME, customer, macroservice, prog, now, rows);
    }

    @Override
    public CsvTransformationResult queuedMessagesToCsv(List<QueuedMessage> messages) {
        log.info("CsvServiceImpl - queuedMessagesToCsv - START");

        CsvTransformationResult result = new CsvTransformationResult();

        CsvTemplate template = csvTemplateRepository.findFirstByIdCsv(jobProperties.getMessagesCsvTemplateId());
        List<Column> templateColumns = getTemplateColumns(template);

        StringWriter writer = new StringWriter();

        List<Map<String, String>> csvRows = messages.stream()
                .map(m -> {
                    Map<String, String> map = this.queuedMessageToMap(m, templateColumns);
                    if (map.isEmpty()) {
                        result.getDiscardedMessages().add(m);
                        map = null;
                    }
                    return map;
                })
                .filter(Objects::nonNull)
                .collect(Collectors.toList());

        for (int i = 0; i < csvRows.size(); i++) {
            int autoInc = i + 1;
            csvRows.get(i).replaceAll((k, v) -> AUTOINC_PLACEHOLDER.equals(v) ? String.valueOf(autoInc) : v);
        }

        csvWriter(csvRows, writer);

        String csvString = writer.toString();

        if(csvString != null && !"".equals(csvString))
            result.setCsvContent(csvString.getBytes(StandardCharsets.UTF_8));

        QueuedMessageChannel channel = findChannel(messages);

        result.setFileName(formatFileName(csvRows.size(), channel));

        log.info("CsvServiceImpl - queuedMessagesToCsv - END");
        return result;
    }

    private Map<String, String> queuedMessageToMap(QueuedMessage message, List<Column> columns) {
        Map<String, String> map = new LinkedHashMap<>();
        try {
            columns.forEach(c -> {
                String prop = getProperty(message, c);
                ColumnFormatter formatter = ColumnFormatters.get(c.getType());
                prop = formatter.format(c, prop);
                map.put(c.getName(), prop);
            });
        } catch (Exception e) {
            map.clear();
        }
        return map;
    }

    private String getProperty(Object source, Column column) {
        String prop = column.getDefaultAttribute();
        String accessString = column.getMessageAttribute();
        try {
            if (!Constants.SKIP_PROPERTY_FLAG.equals(accessString)) {
                String value = (String) PropertyUtils.getProperty(source, accessString);
                prop = StringUtils.isEmpty(value) ? prop : value;
            }
        } catch (Exception e) {
            log.warn("CsvServiceImpl - getProperty - property not found", e);
        }
        return prop == null ? "" : prop;
    }

    private void csvWriter(List<Map<String, String>> listOfMap, Writer writer) {
        try {
            CsvSchema schema = null;
            CsvSchema.Builder schemaBuilder = CsvSchema.builder();
            if (listOfMap != null && !listOfMap.isEmpty()) {
                listOfMap.get(0).keySet().stream()
                        .forEachOrdered(schemaBuilder::addColumn);
                schema = schemaBuilder.build()
                        .withHeader()
                        .withLineSeparator("\r\n")
                        .withColumnSeparator(';')
                        .withoutEscapeChar()
                        .withoutQuoteChar();
            }
            CsvMapper mapper = new CsvMapper();
            mapper.writer(schema).writeValues(writer).writeAll(listOfMap);
            writer.flush();
        } catch (Exception e) {
            log.warn("CsvServiceImpl - csvWriter - write incomplete", e);
        }
    }

    @Override
    public List<ElaborationResult> csvToElaborationResults(byte[] csvBytes) {
        log.info("CsvServiceImpl - csvToElaborationResults - END");

        String csv = new String(csvBytes, StandardCharsets.UTF_8);

        CsvTemplate template = csvTemplateRepository.findFirstByIdCsv(jobProperties.getResultCsvTemplateId());
        List<Column> templateColumns = getTemplateColumns(template);

        List<Map<String, String>> csvRows = csvReader(csv);

        List<ElaborationResult> elaborationResults = csvRows.stream()
                .map(row -> mapToElaborationResult(row, templateColumns))
                .collect(Collectors.toList());

        log.info("CsvServiceImpl - csvToElaborationResults - END");
        return elaborationResults;
    }

    private ElaborationResult mapToElaborationResult(Map<String, String> map, List<Column> columns){
        ElaborationResult res = new ElaborationResult();
        columns.forEach(c -> {
            String value = map.getOrDefault(c.getName(), "");
            setProperty(res, c.getMessageAttribute(), value);
        });
        return res;
    }

    private void setProperty(Object target, String accessString, Object value) {
        try {
            if (!Constants.SKIP_PROPERTY_FLAG.equals(accessString))
                PropertyUtils.setProperty(target, accessString, value);
        } catch (Exception e) {
            log.warn("CsvServiceImpl - setProperty - property not set", e);
        }
    }

    private List<Map<String, String>> csvReader(String csv) {
        try {
            CsvSchema schema = CsvSchema.builder().build()
                        .withHeader()
                        .withLineSeparator("\r\n")
                        .withColumnSeparator(';')
                        .withoutEscapeChar()
                        .withQuoteChar('"');
            CsvMapper mapper = new CsvMapper();
            MappingIterator<Map<String, String>> mappingIterator = mapper.readerForMapOf(String.class)
                    .with(schema)
                    .readValues(csv);
            List<Map<String, String>> rows = mappingIterator.readAll();

            rows = rows.stream().map(m -> {
                Map<String, String> map = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);
                map.putAll(m);
                return map;
            }).collect(Collectors.toList());

            return rows;
        } catch (Exception e) {
            log.warn("CsvServiceImpl - csvReader - read incomplete", e);
            return Collections.emptyList();
        }
    }

    private List<Column> getTemplateColumns(CsvTemplate csvTemplate){
        try {
            return new ObjectMapper().readValue(csvTemplate.getColumns(), new TypeReference<List<Column>>() {});
        } catch (JsonProcessingException e) {
            log.error("CsvServiceImpl - getTemplateColumns - could not read template columns", e);
            return Collections.emptyList();
        }
    }

    private QueuedMessageChannel findChannel(List<QueuedMessage> messages){
        return messages.stream()
                .map(Util::getChannel)
                .findAny()
                .orElse(null);
    }

}
