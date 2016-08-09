package uk.co.blackpepper.sdrclient.test.server.model;

import java.util.Set;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.OneToMany;

import uk.co.blackpepper.sdrclient.annotation.LinkedResource;
import uk.co.blackpepper.sdrclient.annotation.RemoteResource;

@Entity
@RemoteResource("/bidi-parents")
public class BidiParentEntity {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Integer entityId;
	
	private String name;
	
	@OneToMany(mappedBy = "parent")
	@LinkedResource
	private Set<BidiChildEntity> children;
}
