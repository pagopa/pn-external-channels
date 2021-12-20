package it.pagopa.pn.externalchannels.service;

import it.pagopa.pn.externalchannels.entities.csvtemplate.Column;
import it.pagopa.pn.externalchannels.util.Util;
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

    @Test
    void shouldFormatNumeric(){
        Util.toInt("1", 1);
        Util.toInt(null, 1);
        ColumnFormatter cf = ColumnFormatters.get("numeric");
        Column column = new Column();
        column.setType("numeric");
        String formattedValue = cf.format(column, "1");
        assertEquals("1", formattedValue);
    }

}