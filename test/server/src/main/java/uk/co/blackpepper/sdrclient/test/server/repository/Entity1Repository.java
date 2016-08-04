package uk.co.blackpepper.sdrclient.test.server.repository;

import org.springframework.data.repository.CrudRepository;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;

import uk.co.blackpepper.sdrclient.test.server.model.Entity1;

@RepositoryRestResource(path = "/entities")
public interface Entity1Repository extends CrudRepository<Entity1, Integer> {
}
