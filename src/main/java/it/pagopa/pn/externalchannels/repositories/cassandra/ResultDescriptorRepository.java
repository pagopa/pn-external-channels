package it.pagopa.pn.externalchannels.repositories.cassandra;

import it.pagopa.pn.externalchannels.entities.resultdescriptor.ResultDescriptor;
import org.springframework.data.repository.CrudRepository;

import java.util.LinkedList;
import java.util.List;

public interface ResultDescriptorRepository extends CrudRepository<ResultDescriptor, String> {

    default List<ResultDescriptor> listAll() {
        final List<ResultDescriptor> list = new LinkedList<>();
        Iterable<ResultDescriptor> iterable = findAll();
        iterable.forEach(list::add);
        return list;
    }

}
