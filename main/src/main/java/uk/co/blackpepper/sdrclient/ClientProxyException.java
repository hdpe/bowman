package uk.co.blackpepper.sdrclient;

public class ClientProxyException extends RuntimeException {
	
	public ClientProxyException(String message) {
		super(message);
	}

	public ClientProxyException(String message, Throwable cause) {
		super(message, cause);
	}
}
