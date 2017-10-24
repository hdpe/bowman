package uk.co.blackpepper.bowman.test.server.repository;

import org.springframework.data.repository.CrudRepository;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;

import uk.co.blackpepper.bowman.test.server.model.NullLinkedCollectionEntity;

@RepositoryRestResource(path = "/null-linked-collections")
public interface NullLinkedCollectionEntityRepository extends CrudRepository<NullLinkedCollectionEntity, Integer> {
	// no additional methods
}
