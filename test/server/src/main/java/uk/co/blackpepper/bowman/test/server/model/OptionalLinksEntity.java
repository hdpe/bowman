package uk.co.blackpepper.bowman.test.server.model;

import java.util.Set;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.ManyToOne;

@SuppressWarnings("unused")
@Entity
public class OptionalLinksEntity {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Integer id;

	private String name;
	
	@ManyToOne
	private SimpleEntity optionalLinkItem;
	
	@ManyToMany
	private Set<SimpleEntity> optionalLinkCollection;
}
