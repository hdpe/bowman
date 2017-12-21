package uk.co.blackpepper.bowman.test.server.repository;

import org.springframework.data.repository.CrudRepository;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;

import uk.co.blackpepper.bowman.test.server.model.HierarchyBaseEntity;

@RepositoryRestResource(path = "/hierarchy-base-entities")
public interface HierarchyBaseEntityRepository extends CrudRepository<HierarchyBaseEntity, Integer> {
	// no additional methods
}
