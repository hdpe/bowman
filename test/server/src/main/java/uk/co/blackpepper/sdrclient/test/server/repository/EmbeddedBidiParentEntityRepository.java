package uk.co.blackpepper.sdrclient.test.server.repository;

import org.springframework.data.repository.CrudRepository;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;

import uk.co.blackpepper.sdrclient.test.server.model.EmbeddedBidiParentEntity;

@RepositoryRestResource(path = "/embedded-bidi-parents")
public interface EmbeddedBidiParentEntityRepository extends CrudRepository<EmbeddedBidiParentEntity, Integer> {
}
