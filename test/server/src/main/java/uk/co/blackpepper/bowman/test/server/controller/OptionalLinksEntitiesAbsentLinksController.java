package uk.co.blackpepper.bowman.test.server.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.Link;
import org.springframework.hateoas.Links;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import uk.co.blackpepper.bowman.test.server.model.OptionalLinksEntity;
import uk.co.blackpepper.bowman.test.server.repository.OptionalLinksEntityRepository;

@RestController
@RequestMapping("/optional-links-entities-query")
public class OptionalLinksEntitiesAbsentLinksController {
	
	@Autowired
	private OptionalLinksEntityRepository repository;
	
	@GetMapping("/{id}")
	public EntityModel<OptionalLinksEntity> get(@PathVariable("id") Integer id) {
		OptionalLinksEntity entity = repository.findById(id)
			.orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
		
		return EntityModel.of(entity, Links.of(Link.of(
			String.format("http://localhost:8080/optional-links-entities/%s", id))));
	}
}
