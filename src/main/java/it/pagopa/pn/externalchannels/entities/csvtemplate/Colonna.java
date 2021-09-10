package it.pagopa.pn.externalchannels.entities.csvtemplate;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.cassandra.core.mapping.UserDefinedType;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Colonna {

    private String nomeColonna;
    private Boolean required;
    private String type;
    private String attrMessaggio;
    private Long length;
    private String note;

}
