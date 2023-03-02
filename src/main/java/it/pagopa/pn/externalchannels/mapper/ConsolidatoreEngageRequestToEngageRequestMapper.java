package it.pagopa.pn.externalchannels.mapper;


import it.pagopa.pn.externalchannels.model.PaperEngageRequest;

public class ConsolidatoreEngageRequestToEngageRequestMapper {
    private ConsolidatoreEngageRequestToEngageRequestMapper(){}

    public static PaperEngageRequest map(it.pagopa.pn.externalchannels.model.consolidatore.PaperEngageRequest input) {
      return SmartMapper.mapToClass(input, PaperEngageRequest.class );
    }
}
