package uk.co.blackpepper.halclient;

public class ClientProxyException extends RuntimeException {
	
	private static final long serialVersionUID = 7398487411554253606L;

	public ClientProxyException(String message) {
		super(message);
	}

	public ClientProxyException(String message, Throwable cause) {
		super(message, cause);
	}
}
