package de.fichtelmax.mojo.messagebundle.util;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.Properties;

import javax.tools.JavaCompiler;
import javax.tools.ToolProvider;

public class Compiler {
	public static void compile(String name, File sourcePath) {
		File sourceFile = new File(sourcePath, name.replace(".", "/") + ".java");
		if (!sourceFile.exists()) {
			throw new RuntimeException("file not found: " + sourceFile);
		}

		JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();

		int returnCode = compiler.run(null, null, null, sourceFile.getPath());

		if (returnCode != 0) {
			throw new RuntimeException("failed to compile");
		}
	}

	public static Class<?> loadClass(String name, File classpath) {
		try {
			URLClassLoader classLoader = URLClassLoader.newInstance(new URL[] { classpath.toURI().toURL() });

			return Class.forName(name, true, classLoader);
		} catch (MalformedURLException | ClassNotFoundException e) {
			throw new RuntimeException(e);
		}
	}

	public static void createProperties(String bundleFilename, Properties properties, File location)
			throws IOException {
		File file = new File(location, bundleFilename);
		if (!location.equals(file.getParentFile())) {
			file.getParentFile().mkdirs();
		}

		properties.store(new FileOutputStream(file), "generated for unit test");
	}
}
