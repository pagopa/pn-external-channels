package it.pagopa.pn.externalchannels.util.formatters;

import it.pagopa.pn.externalchannels.entities.csvtemplate.Colonna;

import java.text.DecimalFormat;

public class CurrencyColumnFormatter extends ColumnFormatter{

    private final DecimalFormat df;

    public CurrencyColumnFormatter() {
        df = new DecimalFormat("0.00");
        df.getDecimalFormatSymbols().setDecimalSeparator(',');
        df.setDecimalSeparatorAlwaysShown(true);
    }

    @Override
    public String formatValue(Colonna c, String value) {
        double d = Double.parseDouble(value.replace(',','.'));
        return df.format(d);
    }

}
