package uk.co.blackpepper.bowman.test.server.repository;

import org.springframework.data.repository.CrudRepository;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;

import uk.co.blackpepper.bowman.test.server.model.HierarchyDerivedEntity2;

@RepositoryRestResource(path = "/hierarchy-derived-entity-twos")
public interface HierarchyDerivedEntity2Repository extends CrudRepository<HierarchyDerivedEntity2, Integer> {
	// no additional methods
}
