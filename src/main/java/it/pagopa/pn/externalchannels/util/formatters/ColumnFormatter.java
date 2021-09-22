package it.pagopa.pn.externalchannels.util.formatters;

import it.pagopa.pn.externalchannels.entities.csvtemplate.Column;

public abstract class ColumnFormatter {

    public String format(Column c, String value) {
        if (value == null || "".equals(value))
            return "";
        return formatValue(c, value);
    }

    abstract String formatValue(Column c, String value);

}
