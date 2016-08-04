package uk.co.blackpepper.sdrclient.test.server.model;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;

import uk.co.blackpepper.sdrclient.annotation.RemoteResource;

@Entity
@RemoteResource("/entities")
public class Entity1 {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Integer id;

	private String name;
}
