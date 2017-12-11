/*
 * Copyright 2016 Black Pepper Software
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package uk.co.blackpepper.bowman;

import java.net.URI;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import org.springframework.hateoas.Link;
import org.springframework.hateoas.PagedResources;
import org.springframework.hateoas.Resource;
import org.springframework.hateoas.Resources;
import org.springframework.hateoas.PagedResources.PageMetadata;
import org.springframework.http.HttpStatus;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

import uk.co.blackpepper.bowman.annotation.Children;
import uk.co.blackpepper.bowman.annotation.RemoteResource;

class RestOperations {

	private final RestTemplate restTemplate;
	
	private final ObjectMapper objectMapper;
	
	RestOperations(RestTemplate restTemplate, ObjectMapper objectMapper) {
		this.restTemplate = restTemplate;
		this.objectMapper = objectMapper;
	}
	
	public <T> Resource<T> getResource(URI uri, Class<T> entityType) {
		ObjectNode node;
		
		try {
			node = restTemplate.getForObject(uri, ObjectNode.class);
		}
		catch (HttpClientErrorException exception) {
			if (exception.getStatusCode() == HttpStatus.NOT_FOUND) {
				return null;
			}
			
			throw exception;
		}
		
        JavaType targetType = objectMapper.getTypeFactory().constructParametricType(Resource.class, entityType);

        Resource<T> rawResource = objectMapper.convertValue(node, targetType);
        String selfLink = rawResource.getLink("self").getHref();
        
        Class<?> actualEntityType = getActualEntityClass(selfLink, entityType);
        
        targetType = objectMapper.getTypeFactory().constructParametricType(Resource.class, actualEntityType);

        return objectMapper.convertValue(node, targetType);
	}

	public <T> Resources<Resource<T>> getResources(URI uri, Class<T> entityType) {
		ObjectNode node;
		
		try {
			node = restTemplate.getForObject(uri, ObjectNode.class);
		}
		catch (HttpClientErrorException exception) {
			if (exception.getStatusCode() == HttpStatus.NOT_FOUND) {
				return Resources.wrap(Collections.<T>emptyList());
			}
			
			throw exception;
		}
		
        if (entityType.getAnnotation(Children.class) != null) {
            return convertChildResources(node, entityType);
        } else {
            JavaType targetType = constructResourcesType(entityType);
            return objectMapper.convertValue(node, targetType);
        }
	}
	
    /**
     * Besides of obvious, modifies input "node" object
     */
    protected <T> Resources<Resource<T>> convertChildResources(ObjectNode resourceListNode, Class<T> parentEntityType) {
        Collection<Resource<T>> resources = new ArrayList<>();
        Collection<Link> links = new ArrayList<>();
        PageMetadata pageMetadata = null;

        Map<Class<?>, JsonNode> map = cutChildrenNodes(resourceListNode, parentEntityType);

        for (Entry<Class<?>, JsonNode> childEntry : map.entrySet()) {
            String childNodeName = getJsonNodeName(childEntry.getKey());

            ((ObjectNode) resourceListNode.get("_embedded")).set(childNodeName, childEntry.getValue());

            // Here only instances of child type is expected to be present in
            // the list of resources inside "node"
            JavaType childTargetType = constructResourcesType(childEntry.getKey());

            PagedResources<Resource<T>> childArray = objectMapper.convertValue(resourceListNode, childTargetType);

            resources.addAll(childArray.getContent());
            links = childArray.getLinks();
            pageMetadata = childArray.getMetadata();

            ((ObjectNode) resourceListNode.get("_embedded")).remove(childNodeName);
        }
        return new PagedResources<>(resources, pageMetadata, links);
    }

    protected <T> Map<Class<?>, JsonNode> cutChildrenNodes(ObjectNode resourceListNode, Class<T> parentEntityType) {
        HashMap<Class<?>, JsonNode> chilredNodeMap = new HashMap<>();
        for (Class<?> child : parentEntityType.getAnnotation(Children.class).value()) {
            String childNodeName = getJsonNodeName(child);
            JsonNode childNodeValue = ((ObjectNode) resourceListNode.get("_embedded")).remove(childNodeName);
            if (childNodeValue != null) {
                chilredNodeMap.put(child, childNodeValue);
            }
        }
        return chilredNodeMap;
    }

    protected String getJsonNodeName(Class<?> resourceClass) {
        // Expected correct resource annotation e.g.
        // @RemoteResource("/internalUser")
        return resourceClass.getAnnotation(RemoteResource.class).value().substring(1);
    }
    
    protected JavaType constructResourcesType(Class<?> targetClass) {
        JavaType childType = objectMapper.getTypeFactory().constructParametricType(Resource.class, targetClass);
        return objectMapper.getTypeFactory().constructParametricType(PagedResources.class, childType);
    }
	
    protected Class<?> getActualEntityClass(String selfLink, Class<?> parentEntityType){
        if (parentEntityType.getAnnotation(Children.class) != null) {
            for (Class<?> child : parentEntityType.getAnnotation(Children.class).value()) {
                String childRootURI = child.getAnnotation(RemoteResource.class).value();
                if (selfLink.substring(0, selfLink.lastIndexOf("/")).endsWith(childRootURI)) {
                    return child;
                }
            }
        }
        return parentEntityType;
    }
    
	public URI postObject(URI uri, Object object) {
		return restTemplate.postForLocation(uri, object);
	}
	
	public void putObject(URI uri, Object object) {
		restTemplate.put(uri, object);
	}
	
	public void deleteResource(URI uri) {
		restTemplate.delete(uri);
	}
	
	RestTemplate getRestTemplate() {
		return restTemplate;
	}
	
	ObjectMapper getObjectMapper() {
		return objectMapper;
	}
}
