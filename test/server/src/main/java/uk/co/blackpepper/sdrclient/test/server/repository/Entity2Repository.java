package uk.co.blackpepper.sdrclient.test.server.repository;

import org.springframework.data.repository.CrudRepository;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;

import uk.co.blackpepper.sdrclient.test.server.model.Entity2;

@RepositoryRestResource(path = "/entities2")
public interface Entity2Repository extends CrudRepository<Entity2, Integer> {
}
