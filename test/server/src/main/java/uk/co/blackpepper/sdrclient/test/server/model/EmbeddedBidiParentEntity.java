package uk.co.blackpepper.sdrclient.test.server.model;

import java.util.Set;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.OneToMany;

import com.fasterxml.jackson.annotation.JsonManagedReference;

import uk.co.blackpepper.sdrclient.annotation.EmbeddedResource;
import uk.co.blackpepper.sdrclient.annotation.RemoteResource;

@Entity
@RemoteResource("/embedded-bidi-parents")
public class EmbeddedBidiParentEntity {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Integer id;
	
	private String name;

	@OneToMany(mappedBy = "parent", cascade = CascadeType.ALL)
	@EmbeddedResource
	@JsonManagedReference
	private Set<EmbeddedBidiChildEntity> children;
}
