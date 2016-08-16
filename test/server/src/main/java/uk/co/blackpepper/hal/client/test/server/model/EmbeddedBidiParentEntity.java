package uk.co.blackpepper.hal.client.test.server.model;

import java.util.Set;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;

import com.fasterxml.jackson.annotation.JsonManagedReference;

@Entity
public class EmbeddedBidiParentEntity {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Integer id;
	
	private String name;

	@OneToMany(mappedBy = "parent", cascade = CascadeType.ALL)
	@JsonManagedReference
	private Set<EmbeddedBidiChildEntity> children;
	
	@OneToOne(cascade = CascadeType.ALL)
	private EmbeddedBidiChildEntity child;
}
