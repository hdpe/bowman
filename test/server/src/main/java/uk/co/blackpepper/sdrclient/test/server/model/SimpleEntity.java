package uk.co.blackpepper.sdrclient.test.server.model;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.ManyToOne;

import uk.co.blackpepper.sdrclient.annotation.RemoteResource;

@Entity
@RemoteResource("/simple-entities")
public class SimpleEntity {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Integer id;

	private String name;
	
	@ManyToOne
	private SimpleEntity related;
}
