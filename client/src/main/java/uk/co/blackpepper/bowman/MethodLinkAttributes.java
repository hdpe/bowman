package uk.co.blackpepper.bowman;

class MethodLinkAttributes {
	
	private String linkName;
	
	private boolean optional;
	
	MethodLinkAttributes(String linkName, boolean optional) {
		this.linkName = linkName;
		this.optional = optional;
	}
	
	public String getLinkName() {
		return linkName;
	}
	
	public boolean isOptional() {
		return optional;
	}
}
