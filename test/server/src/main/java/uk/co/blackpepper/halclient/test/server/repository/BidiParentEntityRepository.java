package uk.co.blackpepper.halclient.test.server.repository;

import org.springframework.data.repository.CrudRepository;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;

import uk.co.blackpepper.halclient.test.server.model.BidiParentEntity;

@RepositoryRestResource(path = "/bidi-parents")
public interface BidiParentEntityRepository extends CrudRepository<BidiParentEntity, Integer> {
}
