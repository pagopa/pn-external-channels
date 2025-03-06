package it.pagopa.pn.externalchannels.service.mockpostel;

import com.opencsv.bean.CsvToBeanBuilder;
import com.opencsv.bean.StatefulBeanToCsv;
import com.opencsv.bean.StatefulBeanToCsvBuilder;
import com.opencsv.exceptions.CsvDataTypeMismatchException;
import com.opencsv.exceptions.CsvRequiredFieldEmptyException;
import it.pagopa.pn.commons.exceptions.PnInternalException;
import it.pagopa.pn.externalchannels.config.PnExternalChannelsProperties;
import it.pagopa.pn.externalchannels.dao.CapModel;
import it.pagopa.pn.externalchannels.dao.CountryModel;
import lombok.CustomLog;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;
import org.springframework.util.ResourceUtils;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.stream.Collectors;



@Component
@CustomLog
@RequiredArgsConstructor
public class CsvService {

    private final PnExternalChannelsProperties pnExternalChannelsProperties;

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

    public Map<String, String> countryMap() {
        try(FileReader fileReader = new FileReader(ResourceUtils.getFile("classpath:" + pnExternalChannelsProperties.getCsv().getPathCountry()))) {
            CsvToBeanBuilder<CountryModel> csvToBeanBuilder = new CsvToBeanBuilder<>(fileReader);
            csvToBeanBuilder.withSkipLines(1);
            csvToBeanBuilder.withType(CountryModel.class);
            return csvToBeanBuilder.build().parse()
                    .stream()
                    .filter(countryModel -> !StringUtils.isBlank(countryModel.getName()))
                    .collect(Collectors.toMap(
                            model -> StringUtils.normalizeSpace(model.getName()).toUpperCase(),
                            CountryModel::getIsocode, (o, o2) -> o)
                    );
        } catch (IOException e) {
            throw new PnInternalException("Error reading file: " + pnExternalChannelsProperties.getCsv().getPathCountry(), "CSV_ERROR");
        }
    }

    public List<CapModel> capList() {
        try(FileReader fileReader = new FileReader(ResourceUtils.getFile("classpath:" + pnExternalChannelsProperties.getCsv().getPathCap()))) {
            CsvToBeanBuilder<CapModel> csvToBeanBuilder = new CsvToBeanBuilder<>(fileReader);
            csvToBeanBuilder.withSkipLines(1);
            csvToBeanBuilder.withSeparator(';');
            csvToBeanBuilder.withType(CapModel.class);
            return csvToBeanBuilder.build().parse()
                    .stream()
                    .filter(capModel -> !StringUtils.isBlank(capModel.getCap()))
                    .toList();
        } catch (IOException e) {
            throw new PnInternalException("Error reading file: " + pnExternalChannelsProperties.getCsv().getPathCap(),"CSV_ERROR");
        }
    }

}