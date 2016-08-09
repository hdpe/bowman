package uk.co.blackpepper.sdrclient.test.server.model;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.ManyToOne;

import uk.co.blackpepper.sdrclient.annotation.RemoteResource;

@Entity
@RemoteResource("/bidi-children")
public class BidiChildEntity {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Integer id;
	
	@ManyToOne
	private BidiParentEntity parent;
	
	private String name;
}
