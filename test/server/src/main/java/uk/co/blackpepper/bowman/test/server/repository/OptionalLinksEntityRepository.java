package uk.co.blackpepper.bowman.test.server.repository;

import org.springframework.data.repository.CrudRepository;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;

import uk.co.blackpepper.bowman.test.server.model.OptionalLinksEntity;

@RepositoryRestResource(path = "/optional-links-entities")
public interface OptionalLinksEntityRepository extends CrudRepository<OptionalLinksEntity, Integer> {
	// no additional methods
}
