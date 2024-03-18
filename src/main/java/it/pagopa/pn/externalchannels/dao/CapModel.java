package it.pagopa.pn.externalchannels.dao;

import com.opencsv.bean.CsvBindByPosition;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CapModel {

    @CsvBindByPosition(position = 0)
    private String cap;

    @CsvBindByPosition(position = 1)
    private String city;

    @CsvBindByPosition(position = 2)
    private String province;


}
