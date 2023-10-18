package uk.co.blackpepper.bowman.test.server.model;

import java.util.Set;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.ManyToOne;

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
