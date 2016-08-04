package uk.co.blackpepper.sdrclient.test.server.repository;

import org.springframework.data.repository.CrudRepository;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;

import uk.co.blackpepper.sdrclient.test.server.model.BidiParentEntity;

@RepositoryRestResource(path = "/bidi-parents")
public interface BidiParentEntityRepository extends CrudRepository<BidiParentEntity, Integer> {
}
