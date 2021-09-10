package it.pagopa.pn.externalchannels.util.formatters;

public enum ColumnFormatters {

    TEXT_FORMATTER("text", new TextColumnFormatter()),
    NUMERIC_FORMATTER("numeric", new NumericColumnFormatter()),
    PADDED_INT_FORMATTER("paddedInt", new PaddedIntColumnFormatter()),
    CURRENCY_FORMATTER("currency", new CurrencyColumnFormatter());

    private final String type;
    private final ColumnFormatter formatter;

    ColumnFormatters(String type, ColumnFormatter formatter) {
        this.type = type;
        this.formatter = formatter;
    }

    public static ColumnFormatter get(String type){
        for (ColumnFormatters e : values())
            if (e.type.equals(type))
                return e.formatter;
        return TEXT_FORMATTER.formatter;
    }

}
