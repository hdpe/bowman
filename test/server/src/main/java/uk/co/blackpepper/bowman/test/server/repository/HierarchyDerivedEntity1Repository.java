package uk.co.blackpepper.bowman.test.server.repository;

import org.springframework.data.repository.CrudRepository;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;

import uk.co.blackpepper.bowman.test.server.model.HierarchyDerivedEntity1;

@RepositoryRestResource(path = "/hierarchy-derived-entity-ones")
public interface HierarchyDerivedEntity1Repository extends CrudRepository<HierarchyDerivedEntity1, Integer> {
	// no additional methods
}
