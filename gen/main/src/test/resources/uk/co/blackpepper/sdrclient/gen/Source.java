package uk.co.blackpepper.sdrclient.gen;

import uk.co.blackpepper.sdrclient.annotation.RemoteResource;

import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;

public class NonAnnotatedClass {
}

@RemoteResource("/path/to/resource")
public class AnnotatedClass {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Integer id;
  
  private String name;
}
