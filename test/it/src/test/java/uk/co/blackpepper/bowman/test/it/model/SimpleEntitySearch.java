package uk.co.blackpepper.bowman.test.it.model;

import java.util.List;

import uk.co.blackpepper.bowman.annotation.LinkedResource;
import uk.co.blackpepper.bowman.annotation.RemoteResource;

@RemoteResource("/simple-entities/search")
public interface SimpleEntitySearch {
	
	@LinkedResource
	SimpleEntity findByName(String name);
	
	@LinkedResource
	List<SimpleEntity> findByNameContaining(String query);
}
