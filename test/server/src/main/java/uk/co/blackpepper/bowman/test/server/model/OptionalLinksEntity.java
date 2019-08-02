package uk.co.blackpepper.bowman.test.server.model;

import java.util.Set;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.ManyToMany;
import javax.persistence.ManyToOne;

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
