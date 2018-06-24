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
package uk.co.blackpepper.bowman.test.it;

import java.io.IOException;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.io.IOUtils;
import org.junit.After;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpRequest;
import org.springframework.http.client.BufferingClientHttpRequestFactory;
import org.springframework.http.client.ClientHttpRequestExecution;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import com.google.common.collect.Lists;

import uk.co.blackpepper.bowman.ClientFactory;
import uk.co.blackpepper.bowman.Configuration;
import uk.co.blackpepper.bowman.RestTemplateConfigurer;

import static java.util.Arrays.asList;

public class AbstractIT {
	
	private static class LoggingClientHttpRequestInterceptor implements ClientHttpRequestInterceptor {

		private static final Logger LOG = LoggerFactory.getLogger(LoggingClientHttpRequestInterceptor.class);

		@Override
		public ClientHttpResponse intercept(HttpRequest request, byte[] body, ClientHttpRequestExecution execution)
				throws IOException {

			if (LOG.isTraceEnabled()) {
				LOG.trace(request.getMethod().name() + " " + request.getURI() + " : "
						+ new String(body, "UTF-8"));
			}

			ClientHttpResponse response = execution.execute(request, body);

			if (LOG.isTraceEnabled()) {
				LOG.trace("response " + response.getStatusCode().value() + " : "
						+ IOUtils.toString(response.getBody()));
			}

			return response;
		}
	}
	
	private static class CreatedEntityRecordingClientHttpRequestInterceptor implements ClientHttpRequestInterceptor {
		
		private List<URI> createdEntities = new ArrayList<>();
		
		@Override
		public ClientHttpResponse intercept(HttpRequest request, byte[] body,
			ClientHttpRequestExecution execution) throws IOException {
			
			ClientHttpResponse response = execution.execute(request, body);
			
			if (request.getMethod() == HttpMethod.POST) {
				createdEntities.add(response.getHeaders().getLocation());
			}
			
			return response;
		}
		
		List<URI> getCreatedEntities() {
			return createdEntities;
		}
	}
	
	// CHECKSTYLE:OFF
	
	protected URI baseUri;
	
	protected ClientFactory clientFactory;
	
	// CHECKSTYLE:ON
	
	private CreatedEntityRecordingClientHttpRequestInterceptor createdEntityRecordingInterceptor =
		new CreatedEntityRecordingClientHttpRequestInterceptor();
	
	protected AbstractIT() {
		baseUri = URI.create(System.getProperty("baseUrl", "http://localhost:8080"));
		
		clientFactory = Configuration.builder()
				.setBaseUri(baseUri)
				.setClientHttpRequestFactory(new BufferingClientHttpRequestFactory(
						new HttpComponentsClientHttpRequestFactory()))
				.setRestTemplateConfigurer(new RestTemplateConfigurer() {
					
					@Override
					public void configure(RestTemplate restTemplate) {
						restTemplate.getInterceptors().addAll(asList(
							new LoggingClientHttpRequestInterceptor(),
							createdEntityRecordingInterceptor
						));
					}
				})
				.build()
				.buildClientFactory();
	}
	
	@After
	public void tearDown() {
		RestTemplate cleanUpRestTemplate = new RestTemplate();
		
		for (URI createdEntity : Lists.reverse(createdEntityRecordingInterceptor.getCreatedEntities())) {
			try {
				cleanUpRestTemplate.delete(createdEntity);
			}
			catch (RestClientException exception) {
				// perhaps already deleted; continue
			}
		}
	}
}
