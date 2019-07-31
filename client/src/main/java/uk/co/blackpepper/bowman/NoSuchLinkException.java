package uk.co.blackpepper.bowman;

/**
 * An exception thrown when no link could be found for a linked association.
 *
 * @author Ryan Pickett
 *
 */
public class NoSuchLinkException extends ClientProxyException {
	
	public static final long serialVersionUID = 4161584113275074573L;
	
	private final String linkName;
	
	NoSuchLinkException(String linkName) {
		super(String.format("Link '%s' could not be found!", linkName));
		this.linkName = linkName;
	}
	
	public String getLinkName() {
		return linkName;
	}
}
