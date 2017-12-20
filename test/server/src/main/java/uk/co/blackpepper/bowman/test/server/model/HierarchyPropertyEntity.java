package uk.co.blackpepper.bowman.test.server.model;

import java.util.Set;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.ManyToMany;
import javax.persistence.ManyToOne;

@Entity
public class HierarchyPropertyEntity {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Integer id;

	@ManyToOne
	private HierarchyBaseEntity linkedEntity;
	
	@ManyToMany
	private Set<HierarchyBaseEntity> linkedEntityCollection;
}
