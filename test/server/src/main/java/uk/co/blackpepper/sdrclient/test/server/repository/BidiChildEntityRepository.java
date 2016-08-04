package uk.co.blackpepper.sdrclient.test.server.repository;

import org.springframework.data.repository.CrudRepository;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;

import uk.co.blackpepper.sdrclient.test.server.model.BidiChildEntity;

@RepositoryRestResource(path = "/bidi-children")
public interface BidiChildEntityRepository extends CrudRepository<BidiChildEntity, Integer> {
}
