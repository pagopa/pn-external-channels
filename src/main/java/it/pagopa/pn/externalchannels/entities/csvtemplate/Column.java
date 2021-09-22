package it.pagopa.pn.externalchannels.entities.csvtemplate;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Column {

    private String name;
    private Boolean required;
    private String type;
    private String messageAttribute;
    private Long length;
    private String note;

}
