package it.pagopa.pn.externalchannels.util.formatters;

import it.pagopa.pn.externalchannels.entities.csvtemplate.Column;

public class PaddedIntColumnFormatter extends ColumnFormatter{

    @Override
    public String formatValue(Column c, String value) {
        Long maxLength = c.getLength();
        long l = Long.parseLong(value);
        String format = "%0" + maxLength + "d";
        value = String.format(format, l);
        return value;
    }

}
