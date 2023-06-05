package it.pagopa.pn.externalchannels.dto;

import it.pagopa.pn.externalchannels.model.DiscoveredAddress;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbBean;

@DynamoDbBean
public class DiscoveredAddressEntity extends DiscoveredAddress {

    @Override
    public DiscoveredAddressEntity city(String city) {
        this.setCity(city);
        return this;
    }

    @Override
    public DiscoveredAddressEntity address(String address) {
        this.setAddress(address);
        return this;
    }

    @Override
    public DiscoveredAddressEntity name(String name) {
        this.setName(name);
        return this;
    }

    @Override
    public DiscoveredAddressEntity country(String country) {
        this.setCountry(country);
        return this;
    }

    @Override
    public DiscoveredAddressEntity cap(String cap) {
        this.setCap(cap);
        return this;
    }

    @Override
    public DiscoveredAddressEntity pr(String pr) {
        this.setPr(pr);
        return this;
    }
}
