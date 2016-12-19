package uk.co.blackpepper.hal.client.test.it;

import java.io.IOException;

import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpRequest;
import org.springframework.http.client.ClientHttpRequestExecution;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.web.client.RestTemplate;

import com.fasterxml.jackson.databind.ObjectMapper;

import uk.co.blackpepper.hal.client.ClientFactory;
import uk.co.blackpepper.hal.client.Configuration;
import uk.co.blackpepper.hal.client.DefaultRestTemplateFactory;

public class AbstractIT {

	private static class LoggingRestTemplateFactory extends DefaultRestTemplateFactory {

		@Override
		public RestTemplate create(ObjectMapper objectMapper) {
			RestTemplate restTemplate = super.create(objectMapper);
			restTemplate.getInterceptors().add(new LoggingClientHttpRequestInterceptor());
			return restTemplate;
		}
	}
	
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
	
	// CHECKSTYLE:OFF
	
	protected ClientFactory clientFactory;
	
	// CHECKSTYLE:ON
	
	protected AbstractIT() {
		clientFactory = new Configuration()
				.setBaseUri(System.getProperty("baseUrl"))
				.setRestTemplateFactory(new LoggingRestTemplateFactory())
				.buildClientFactory();
	}
}
