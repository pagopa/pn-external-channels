package it.pagopa.pn.externalchannels.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.csv.CsvMapper;
import com.fasterxml.jackson.dataformat.csv.CsvSchema;
import it.pagopa.pn.externalchannels.entities.csvtemplate.Column;
import it.pagopa.pn.externalchannels.entities.csvtemplate.CsvTemplate;
import it.pagopa.pn.externalchannels.entities.queuedmessage.QueuedMessage;
import it.pagopa.pn.externalchannels.pojos.CsvTransformationResult;
import it.pagopa.pn.externalchannels.repositories.cassandra.CsvTemplateRepository;
import it.pagopa.pn.externalchannels.util.Constants;
import it.pagopa.pn.externalchannels.util.formatters.ColumnFormatter;
import it.pagopa.pn.externalchannels.util.formatters.ColumnFormatters;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.beanutils.PropertyUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.StringWriter;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

@Service
@Slf4j
public class CsvServiceImpl implements CsvService {

    @Value("${job.csv-template-id}")
    private String csvTemplateId;

    @Autowired
    CsvTemplateRepository csvTemplateRepository;

    @Override
    public CsvTransformationResult queuedMessagesToCsv(List<QueuedMessage> messages) {
        log.info("CsvServiceImpl - queuedMessagesToCsv - START");

        CsvTransformationResult result = new CsvTransformationResult();

        CsvTemplate template = csvTemplateRepository.findFirstByIdCsv(csvTemplateId);

        StringWriter writer = new StringWriter();

        List<Map<String, String>> csvRows = messages.stream()
                .map(m -> {
                    Map<String, String> map = this.queuedMessageToMap(m, template);
                    if (map.isEmpty()) {
                        result.getDiscardedMessages().add(m);
                        map = null;
                    }
                    return map;
                })
                .filter(Objects::nonNull)
                .collect(Collectors.toList());

        csvWriter(csvRows, writer);

        String csvString = writer.toString();

        if(csvString != null && !"".equals(csvString))
            result.setCsvContent(csvString.getBytes(StandardCharsets.UTF_8));

        log.info("CsvServiceImpl - queuedMessagesToCsv - END");
        return result;
    }

    private Map<String, String> queuedMessageToMap(QueuedMessage message, CsvTemplate template) {
        Map<String, String> map = new LinkedHashMap<>();
        ObjectMapper mapper = new ObjectMapper();
        try {
            mapper.readValue(template.getColumns(), new TypeReference<List<Column>>() {}).forEach(c -> {
                String prop = getProperty(message, c.getMessageAttribute());
                ColumnFormatter formatter = ColumnFormatters.get(c.getType());
                prop = formatter.format(c, prop);
                map.put(c.getName(), prop);
            });
        } catch (Exception e) {
            map.clear();
        }
        return map;
    }

    private String getProperty(QueuedMessage m, String accessString) {
        String prop = null;
        try {
            if (!Constants.SKIP_PROPERTY_FLAG.equals(accessString))
                prop = (String) PropertyUtils.getProperty(m, accessString);
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

}
