package uk.co.blackpepper.sdrclient.test.server.model;

import java.util.Set;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.OneToMany;

import uk.co.blackpepper.sdrclient.gen.annotation.RestRepository;

@Entity
@RestRepository("/bidi-parents")
public class BidiParentEntity {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Integer entityId;
	
	private String name;
	
	@OneToMany(mappedBy = "parent")
	private Set<BidiChildEntity> children;
}
