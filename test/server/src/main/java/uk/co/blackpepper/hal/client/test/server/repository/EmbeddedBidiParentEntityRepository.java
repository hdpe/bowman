package uk.co.blackpepper.hal.client.test.server.repository;

import org.springframework.data.repository.CrudRepository;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;

import uk.co.blackpepper.hal.client.test.server.model.EmbeddedBidiParentEntity;

@RepositoryRestResource(path = "/embedded-bidi-parents")
public interface EmbeddedBidiParentEntityRepository extends CrudRepository<EmbeddedBidiParentEntity, Integer> {
}
