package it.pagopa.pn.externalchannels.service.mockpostel;

import com.opencsv.bean.CsvToBeanBuilder;
import com.opencsv.bean.StatefulBeanToCsv;
import com.opencsv.bean.StatefulBeanToCsvBuilder;
import com.opencsv.exceptions.CsvDataTypeMismatchException;
import com.opencsv.exceptions.CsvRequiredFieldEmptyException;
import it.pagopa.pn.commons.exceptions.PnInternalException;
import org.springframework.stereotype.Component;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;


@Component
@lombok.CustomLog
public class CsvService {

    public <T> String writeItemsOnCsvToString(List<T> items) {
        try (StringWriter writer = new StringWriter()) {
            StatefulBeanToCsv<T> beanToCsv = new StatefulBeanToCsvBuilder<T>(writer)
                    .withQuotechar('"')
                    .withSeparator(';')
                    .build();
            beanToCsv.write(items);
            return writer.toString();
        } catch (IOException | CsvDataTypeMismatchException | CsvRequiredFieldEmptyException e) {
            throw new PnInternalException("Error during convert csv in String", "ERROR_WRITE_ITEM_ON_CSV", e);
        }
    }

    public <T> List<T> readItemsFromCsv(Class<T> csvClass, byte[] file, int skipLines) {
        StringReader stringReader = new StringReader(new String(file, StandardCharsets.UTF_8));
        CsvToBeanBuilder<T> csvToBeanBuilder = new CsvToBeanBuilder<>(stringReader);
        csvToBeanBuilder.withSeparator(';');
        csvToBeanBuilder.withSkipLines(skipLines);
        csvToBeanBuilder.withType(csvClass);

        List<T> parsedItems = csvToBeanBuilder.build().parse();
        return new ArrayList<>(parsedItems);
    }

}