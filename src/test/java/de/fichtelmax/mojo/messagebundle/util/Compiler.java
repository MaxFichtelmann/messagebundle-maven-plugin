package de.fichtelmax.mojo.messagebundle.util;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;

import javax.tools.JavaCompiler;
import javax.tools.ToolProvider;

public class Compiler {
	public static void compile(String name, File sourcePath) {
		File sourceFile = new File(sourcePath, name.replace(".", "/") + ".java");
		if (!sourceFile.exists()) {
			throw new RuntimeException("file not found: " + sourceFile);
		}
		
		JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();
		
		int returnCode = compiler.run(null, null, null, sourceFile.getPath()	);
		
		if (returnCode != 0) {
			throw new RuntimeException("failed to compile");
		}
	}
	
	public static Class<?> loadClass(String name, File classpath) {
		try {
			URLClassLoader classLoader = URLClassLoader.newInstance(new URL[]{classpath.toURI().toURL()});
			
			return Class.forName(name, true, classLoader);
		} catch (MalformedURLException | ClassNotFoundException e) {
			throw new RuntimeException(e);
		}
	}
}
