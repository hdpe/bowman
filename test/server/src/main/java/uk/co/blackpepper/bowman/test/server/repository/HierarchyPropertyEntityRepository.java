package uk.co.blackpepper.bowman.test.server.repository;

import org.springframework.data.repository.CrudRepository;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;

import uk.co.blackpepper.bowman.test.server.model.HierarchyPropertyEntity;

@RepositoryRestResource(path = "/hierarchy-property-entities")
public interface HierarchyPropertyEntityRepository extends CrudRepository<HierarchyPropertyEntity, Integer> {
	// no additional methods
}
