package it.pagopa.pn.externalchannels.dto.postelmock;

import com.opencsv.bean.CsvBindByName;
import com.opencsv.bean.CsvBindByPosition;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class NormalizeRequestPostelInput {

    @CsvBindByName(column = "IdCodiceCliente")
    @CsvBindByPosition(position = 0)
    private String idCodiceCliente;

    @ToString.Exclude
    @CsvBindByName(column = "Provincia")
    @CsvBindByPosition(position = 1)
    private String provincia;

    @ToString.Exclude
    @CsvBindByName(column = "Cap")
    @CsvBindByPosition(position = 2)
    private String cap;

    @ToString.Exclude
    @CsvBindByName(column = "localita", required = true)
    @CsvBindByPosition(position = 3)
    private String localita;

    @ToString.Exclude
    @CsvBindByName(column = "localitaAggiuntiva")
    @CsvBindByPosition(position = 4)
    private String localitaAggiuntiva;

    @ToString.Exclude
    @CsvBindByName(column = "indirizzo", required = true)
    @CsvBindByPosition(position = 5)
    private String indirizzo;

    @ToString.Exclude
    @CsvBindByName(column = "indirizzoAggiuntivo")
    @CsvBindByPosition(position = 6)
    private String indirizzoAggiuntivo;

    @ToString.Exclude
    @CsvBindByName(column = "stato")
    @CsvBindByPosition(position = 7)
    private String stato;
}
