package uk.co.blackpepper.bowman;

import java.io.IOException;

import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpRequest;
import org.springframework.http.MediaType;
import org.springframework.http.client.ClientHttpRequestExecution;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.is;
import static org.mockito.AdditionalMatchers.aryEq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class JsonClientHttpRequestInterceptorTest {
	
	private JsonClientHttpRequestInterceptor interceptor;
	
	@Before
	public void setUp() {
		interceptor = new JsonClientHttpRequestInterceptor();
	}
	
	@Test
	public void interceptSetsContentTypeAndAcceptHeaders() throws IOException {
		HttpRequest request = mock(HttpRequest.class);
		when(request.getHeaders()).thenReturn(new HttpHeaders());
		
		ClientHttpRequestExecution execution = mock(ClientHttpRequestExecution.class);
		
		interceptor.intercept(request, new byte[] {1}, execution);
		
		ArgumentCaptor<HttpRequest> finalRequest = ArgumentCaptor.forClass(HttpRequest.class);
		verify(execution).execute(finalRequest.capture(), aryEq(new byte[] {1}));
		
		HttpHeaders finalHeaders = finalRequest.getValue().getHeaders();
		assertThat(finalHeaders.getAccept(), contains(MediaType.valueOf("application/hal+json")));
		assertThat(finalHeaders.getContentType(), is(MediaType.valueOf("application/hal+json")));
	}
}
