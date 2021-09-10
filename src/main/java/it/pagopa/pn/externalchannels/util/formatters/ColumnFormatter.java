package it.pagopa.pn.externalchannels.util.formatters;

import it.pagopa.pn.externalchannels.entities.csvtemplate.Colonna;

public abstract class ColumnFormatter {

    public String format(Colonna c, String value) {
        if (value == null || "".equals(value))
            return "";
        return formatValue(c, value);
    }

    abstract String formatValue(Colonna c, String value);

}
