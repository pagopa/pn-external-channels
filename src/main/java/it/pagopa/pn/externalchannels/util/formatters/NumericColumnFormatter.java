package it.pagopa.pn.externalchannels.util.formatters;

import it.pagopa.pn.externalchannels.entities.csvtemplate.Column;

public class NumericColumnFormatter extends ColumnFormatter{

    @Override
    public String formatValue(Column c, String value) {
        long l = Long.parseLong(value);
        return String.valueOf(l);
    }

}
