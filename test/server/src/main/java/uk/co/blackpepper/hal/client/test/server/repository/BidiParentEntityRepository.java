package uk.co.blackpepper.hal.client.test.server.repository;

import org.springframework.data.repository.CrudRepository;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;

import uk.co.blackpepper.hal.client.test.server.model.BidiParentEntity;

@RepositoryRestResource(path = "/bidi-parents")
public interface BidiParentEntityRepository extends CrudRepository<BidiParentEntity, Integer> {
}
