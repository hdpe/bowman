package uk.co.blackpepper.sdrclient.test.server.model;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.ManyToOne;

import com.fasterxml.jackson.annotation.JsonBackReference;

@Entity
public class EmbeddedBidiChildEntity {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Integer id;
	
	@ManyToOne
	@JsonBackReference
	private EmbeddedBidiParentEntity parent;
	
	private String name;

	@ManyToOne
	private SimpleEntity related;
}