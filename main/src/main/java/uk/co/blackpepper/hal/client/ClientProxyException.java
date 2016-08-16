package uk.co.blackpepper.hal.client;

public class ClientProxyException extends RuntimeException {
	
	public ClientProxyException(String message) {
		super(message);
	}

	public ClientProxyException(String message, Throwable cause) {
		super(message, cause);
	}
}
