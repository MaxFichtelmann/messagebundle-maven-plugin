package de.fichtelmax.mojo.messagebundle.generate;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.arrayWithSize;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Collections;
import java.util.Properties;
import java.util.ResourceBundle;

import org.codehaus.plexus.util.FileUtils;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.sun.codemodel.JCodeModel;

import de.fichtelmax.mojo.messagebundle.model.MessageBundleInfo;
import de.fichtelmax.mojo.messagebundle.model.MessagePropertyInfo;
import de.fichtelmax.mojo.messagebundle.util.Compiler;

public class EnumGeneratorTest {

	private static final String SOME_CLASS_NAME = "SomeClass";
	private static final String SOME_BUNDLE_FILENAME = "Something.properties";

	EnumGenerator cut = new EnumGenerator();

	File dir;

	@Before
	public void createDir() throws IOException {
		dir = File.createTempFile("dir", "");
		dir.delete();
		dir.mkdir();
	}

	@After
	public void removeDir() throws IOException {
		FileUtils.deleteDirectory(dir);
	}

	private PrintStream stdout;

	@Before
	public void disableStdout() {
		stdout = System.out;
		System.setOut(new PrintStream(new ByteArrayOutputStream()));
	}

	@After
	public void restoreStdout() {
		System.setOut(stdout);
	}

	@Test
	public void classInfoShouldBeGenerated() throws Exception {
		String name = "Foo";

		MessageBundleInfo info = new MessageBundleInfo();
		info.setBundleFileName(SOME_BUNDLE_FILENAME);
		info.setName(name);

		JCodeModel codeModel = new JCodeModel();
		cut.transformToEnumInfo(info, codeModel);

		codeModel.build(dir);
		Compiler.compile(name, dir);
		Compiler.createProperties(info.getBundleFileName(), new Properties(), dir);
		Class<?> fooClass = Compiler.loadClass(name, dir);

		assertThat(fooClass.isEnum(), is(true));
	}

	@Test
	public void qualifiedClassInfoShouldBeGenerated() throws Exception {
		String name = "Bar";
		String packageName = "foo";

		String fullName = "foo.Bar";

		MessageBundleInfo info = new MessageBundleInfo();
		info.setBundleFileName(SOME_BUNDLE_FILENAME);
		info.setName(name);
		info.setPackageName(packageName);

		JCodeModel codeModel = new JCodeModel();
		cut.transformToEnumInfo(info, codeModel);

		codeModel.build(dir);
		Compiler.compile(fullName, dir);
		Compiler.createProperties(info.getBundleFileName(), new Properties(), dir);
		Class<?> barClass = Compiler.loadClass(fullName, dir);

		assertThat(barClass.isEnum(), is(true));
	}

	@Test
	public void enumConstantShouldBeGenerated() throws Exception {
		String name = SOME_CLASS_NAME;
		String propertyName = "baz";

		MessageBundleInfo info = new MessageBundleInfo();
		info.setBundleFileName(SOME_BUNDLE_FILENAME);
		info.setName(name);
		MessagePropertyInfo propertyInfo = new MessagePropertyInfo();
		propertyInfo.setPropertyName(propertyName);
		info.setPropertyInfos(Collections.singleton(propertyInfo));

		JCodeModel codeModel = new JCodeModel();
		cut.transformToEnumInfo(info, codeModel);

		codeModel.build(dir);
		Compiler.compile(name, dir);
		Compiler.createProperties(info.getBundleFileName(), props(propertyName), dir);
		Class<?> barClass = Compiler.loadClass(name, dir);

		assertThat(barClass.getEnumConstants(), is(arrayWithSize(1)));
		assertThat(barClass.getEnumConstants()[0], is(instanceOf(Enum.class)));
	}

	@Test
	public void enumShouldRetainOriginalPropertyName() throws Exception {
		String name = SOME_CLASS_NAME;
		String propertyName = "foo.bar";

		MessageBundleInfo info = new MessageBundleInfo();
		info.setBundleFileName(SOME_BUNDLE_FILENAME);
		info.setName(name);
		MessagePropertyInfo propertyInfo = new MessagePropertyInfo();
		propertyInfo.setPropertyName(propertyName);
		info.setPropertyInfos(Collections.singleton(propertyInfo));

		JCodeModel codeModel = new JCodeModel();
		cut.transformToEnumInfo(info, codeModel);

		codeModel.build(dir);
		Compiler.compile(name, dir);
		Compiler.createProperties(info.getBundleFileName(), props(propertyName), dir);
		Class<?> barClass = Compiler.loadClass(name, dir);

		Object constant = barClass.getEnumConstants()[0];
		Method getter = barClass.getMethod("getPropertyName");

		String obtainedPropertyName = (String) getter.invoke(constant);
		assertThat(obtainedPropertyName, is(equalTo(propertyName)));
	}

	@Test
	public void enumShouldHaveResourceBundle() throws Exception {
		String name = SOME_CLASS_NAME;
		String propertyName = "foo.bar";

		MessageBundleInfo info = new MessageBundleInfo();
		info.setBundleFileName(SOME_BUNDLE_FILENAME);
		info.setName(name);
		MessagePropertyInfo propertyInfo = new MessagePropertyInfo();
		propertyInfo.setPropertyName(propertyName);
		info.setPropertyInfos(Collections.singleton(propertyInfo));

		JCodeModel codeModel = new JCodeModel();
		cut.transformToEnumInfo(info, codeModel);

		codeModel.build(dir);
		Compiler.compile(name, dir);
		Compiler.createProperties(info.getBundleFileName(), props(propertyName), dir);
		Class<?> barClass = Compiler.loadClass(name, dir);

		Field bundleField = barClass.getDeclaredField("_BUNDLE");

		bundleField.setAccessible(true);
		Object bundle = bundleField.get(null);

		assertThat(bundle, is(instanceOf(ResourceBundle.class)));
	}

	private Properties props(String key) {
		Properties properties = new Properties();
		properties.setProperty(key, "value for " + key);
		return properties;
	}
}
