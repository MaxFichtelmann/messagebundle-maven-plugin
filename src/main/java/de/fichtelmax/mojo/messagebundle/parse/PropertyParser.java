package de.fichtelmax.mojo.messagebundle.parse;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Properties;

import de.fichtelmax.mojo.messagebundle.model.MessagePropertyInfo;

public class PropertyParser {
	public Collection<MessagePropertyInfo> parse(InputStream data) throws IOException {
		Collection<MessagePropertyInfo> infos = new ArrayList<>();

		Properties properties = new Properties();
		properties.load(data);

		for (String property : properties.stringPropertyNames()) {
			MessagePropertyInfo propertyInfo = new MessagePropertyInfo();
			propertyInfo.setPropertyName(property);
			propertyInfo.setValue(properties.getProperty(property));

			infos.add(propertyInfo);
		}

		return infos;
	}
}
