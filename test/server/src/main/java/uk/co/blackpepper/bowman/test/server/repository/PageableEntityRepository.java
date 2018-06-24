package uk.co.blackpepper.bowman.test.server.repository;

import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;

import uk.co.blackpepper.bowman.test.server.model.PageableEntity;

@RepositoryRestResource(path = "/pageable-entities")
public interface PageableEntityRepository extends PagingAndSortingRepository<PageableEntity, Integer> {
	// no additional methods
}
