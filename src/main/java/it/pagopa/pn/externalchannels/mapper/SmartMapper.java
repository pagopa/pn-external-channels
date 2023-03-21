package it.pagopa.pn.externalchannels.mapper;

import org.modelmapper.ModelMapper;
import org.modelmapper.convention.MatchingStrategies;

public class SmartMapper {
    static ModelMapper modelMapper = new ModelMapper();

    private SmartMapper(){}
    
    public static <S,T> T mapToClass(S source, Class<T> destinationClass ){
        T result = null;
        if( source != null) {
            modelMapper.getConfiguration().setMatchingStrategy(MatchingStrategies.STRICT);
            result = modelMapper.map(source, destinationClass );
        }
        return result;
    }
}
