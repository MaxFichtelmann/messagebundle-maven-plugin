package de.fichtelmax.mojo.messagebundle.generate;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import de.fichtelmax.mojo.messagebundle.model.MessageBundleInfo;
import de.fichtelmax.mojo.messagebundle.model.MessagePropertyInfo;

public class MessageResourceParser {

	public MessageBundleInfo parse(File baseDir, File file) throws IOException {
		String packagePath = baseDir.toURI().relativize(file.getParentFile().toURI()).getPath();
		if (packagePath.endsWith(File.separator)) {
			packagePath = packagePath.substring(0, packagePath.length() - 1);
		}

		String packageName = packagePath.replace(File.separatorChar, '.');

		try (InputStream in = new FileInputStream(file)) {
			Properties properties = new Properties();
			properties.load(in);

			MessageBundleInfo info = new MessageBundleInfo();
			info.setBundleFileName(packagePath + File.separatorChar + file.getName());
			info.setPackageName(packageName);
			info.setName(file.getName().replaceAll("\\..*$", ""));

			for (String property : properties.stringPropertyNames()) {
				MessagePropertyInfo propertyInfo = new MessagePropertyInfo();
				propertyInfo.setPropertyName(property);

				info.getPropertyInfos().add(propertyInfo);
			}

			return info;
		}
	}
}
