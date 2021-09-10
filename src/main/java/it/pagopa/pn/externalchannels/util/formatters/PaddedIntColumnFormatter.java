package it.pagopa.pn.externalchannels.util.formatters;

import it.pagopa.pn.externalchannels.entities.csvtemplate.Colonna;

public class PaddedIntColumnFormatter extends ColumnFormatter{

    @Override
    public String formatValue(Colonna c, String value) {
        Long maxLength = c.getLength();
        long l = Long.parseLong(value);
        value = String.format("%0" + maxLength + "d", l);
        return value;
    }

}
