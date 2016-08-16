package uk.co.blackpepper.hal.client;

import java.io.IOException;

import org.springframework.hateoas.MediaTypes;
import org.springframework.http.HttpRequest;
import org.springframework.http.client.ClientHttpRequestExecution;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.http.client.support.HttpRequestWrapper;

import static java.util.Arrays.asList;

public class JsonClientHttpRequestInterceptor implements ClientHttpRequestInterceptor {

	@Override
	public ClientHttpResponse intercept(HttpRequest request, byte[] body, ClientHttpRequestExecution execution)
			throws IOException {
		HttpRequestWrapper wrapped = new HttpRequestWrapper(request);
		wrapped.getHeaders().put("Content-Type", asList(MediaTypes.HAL_JSON_VALUE));
		wrapped.getHeaders().put("Accept", asList(MediaTypes.HAL_JSON_VALUE));
		return execution.execute(wrapped, body);
	}
}
