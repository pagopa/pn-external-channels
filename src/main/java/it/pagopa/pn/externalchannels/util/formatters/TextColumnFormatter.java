package it.pagopa.pn.externalchannels.util.formatters;

import it.pagopa.pn.externalchannels.entities.csvtemplate.Colonna;

public class TextColumnFormatter extends ColumnFormatter{

    @Override
    public String formatValue(Colonna c, String value) {
        return value;
    }

}
