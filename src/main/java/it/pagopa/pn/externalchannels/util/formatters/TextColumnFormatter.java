package it.pagopa.pn.externalchannels.util.formatters;

import it.pagopa.pn.externalchannels.entities.csvtemplate.Column;

public class TextColumnFormatter extends ColumnFormatter{

    @Override
    public String formatValue(Column c, String value) {
        return value;
    }

}
