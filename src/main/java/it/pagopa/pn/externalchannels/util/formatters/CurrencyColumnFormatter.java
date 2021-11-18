package it.pagopa.pn.externalchannels.util.formatters;

import it.pagopa.pn.externalchannels.entities.csvtemplate.Column;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;

public class CurrencyColumnFormatter extends ColumnFormatter{

    private final DecimalFormat df;

    public CurrencyColumnFormatter() {
        df = new DecimalFormat("0.00");
        DecimalFormatSymbols decimalFormatSymbols = df.getDecimalFormatSymbols();
        decimalFormatSymbols.setDecimalSeparator(',');
        df.setDecimalFormatSymbols(decimalFormatSymbols);
        df.setDecimalSeparatorAlwaysShown(true);
    }

    @Override
    public String formatValue(Column c, String value) {
        double d = Double.parseDouble(value.replace(',','.'));
        return df.format(d);
    }

}
