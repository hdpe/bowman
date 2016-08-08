package uk.co.blackpepper.sdrclient;

import java.io.IOException;

import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpRequest;
import org.springframework.http.client.ClientHttpRequestExecution;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.http.client.ClientHttpResponse;

public class LoggingClientHttpRequestInterceptor implements ClientHttpRequestInterceptor {

	private static final Logger LOG = LoggerFactory.getLogger(LoggingClientHttpRequestInterceptor.class);

	@Override
	public ClientHttpResponse intercept(HttpRequest request, byte[] body, ClientHttpRequestExecution execution)
			throws IOException {

		if (LOG.isTraceEnabled()) {
			LOG.trace(request.getMethod().name() + " " + request.getURI() + " : " + new String(body, "UTF-8"));
		}

		ClientHttpResponse response = execution.execute(request, body);

		if (LOG.isTraceEnabled()) {
			LOG.trace("response " + response.getStatusCode().value() + " : " + IOUtils.toString(response.getBody()));
		}

		return response;
	}
}
