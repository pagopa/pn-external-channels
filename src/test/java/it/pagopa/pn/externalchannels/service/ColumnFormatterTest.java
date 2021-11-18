package it.pagopa.pn.externalchannels.service;

import it.pagopa.pn.externalchannels.entities.csvtemplate.Column;
import it.pagopa.pn.externalchannels.util.formatters.ColumnFormatter;
import it.pagopa.pn.externalchannels.util.formatters.ColumnFormatters;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class ColumnFormatterTest {

    @Test
    void shouldFormatCurrency(){
        ColumnFormatter cf = ColumnFormatters.get("currency");
        Column column = new Column();
        column.setType("currency");
        String formattedValue = cf.format(column, "88,88");
        assertEquals("88,88", formattedValue);
        formattedValue = cf.format(column, "88.88");
        assertEquals("88,88", formattedValue);
        formattedValue = cf.format(column, "88");
        assertEquals("88,00", formattedValue);
        formattedValue = cf.format(column, "0");
        assertEquals("0,00", formattedValue);
    }

}