package de.fichtelmax.mojo.messagebundle.parse;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Collection;

import de.fichtelmax.mojo.messagebundle.model.MessageBundleInfo;
import de.fichtelmax.mojo.messagebundle.model.MessagePropertyInfo;

public class MessageResourceParser {
	public MessageBundleInfo parse(File baseDir, File file) throws IOException {
		String packagePath = baseDir.toURI().relativize(file.getParentFile().toURI()).getPath();
		if (packagePath.endsWith(File.separator)) {
			packagePath = packagePath.substring(0, packagePath.length() - 1);
		}

		String packageName = packagePath.replace(File.separatorChar, '.');

		MessageBundleInfo info = new MessageBundleInfo();
		info.setBundleFileName(packagePath + File.separatorChar + file.getName());
		info.setPackageName(packageName);
		info.setName(file.getName().replaceAll("\\..*$", ""));

		try (InputStream in = new FileInputStream(file)) {
			Collection<MessagePropertyInfo> propertyInfos = new PropertyParser().parse(in);

			info.getPropertyInfos().addAll(propertyInfos);

			return info;
		}
	}
}
