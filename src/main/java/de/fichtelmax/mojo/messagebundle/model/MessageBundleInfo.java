package de.fichtelmax.mojo.messagebundle.model;

import java.util.ArrayList;
import java.util.Collection;

public class MessageBundleInfo {
	private String packageName;
	private String name;
	private String bundleFileName;
	private Collection<MessagePropertyInfo> propertyInfos = new ArrayList<>();

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public Collection<MessagePropertyInfo> getPropertyInfos() {
		return propertyInfos;
	}

	public void setPropertyInfos(Collection<MessagePropertyInfo> propertyInfos) {
		this.propertyInfos = propertyInfos;
	}

	public String getPackageName() {
		return packageName;
	}

	public void setPackageName(String packageName) {
		this.packageName = packageName;
	}

	public String getBundleFileName() {
		return bundleFileName;
	}

	public void setBundleFileName(String bundleFileName) {
		this.bundleFileName = bundleFileName;
	}
}
