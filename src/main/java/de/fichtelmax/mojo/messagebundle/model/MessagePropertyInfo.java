package de.fichtelmax.mojo.messagebundle.model;

import java.util.ArrayList;
import java.util.List;

public class MessagePropertyInfo {
	private String propertyName;
	private String value;
	private String description;
	private List<String> parameterDescriptions = new ArrayList<>();

	public String getPropertyName() {
		return propertyName;
	}

	public void setPropertyName(String propertyName) {
		this.propertyName = propertyName;
	}

	public String getValue() {
		return value;
	}

	public void setValue(String value) {
		this.value = value;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public List<String> getParameterDescriptions() {
		return parameterDescriptions;
	}

	public void setParameterDescriptions(List<String> parameterDescriptions) {
		this.parameterDescriptions = parameterDescriptions;
	}
}
