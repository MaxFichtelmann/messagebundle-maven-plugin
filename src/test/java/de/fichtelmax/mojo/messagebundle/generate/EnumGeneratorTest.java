package de.fichtelmax.mojo.messagebundle.generate;

import static org.hamcrest.CoreMatchers.*;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.arrayWithSize;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.Collections;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;
import java.util.ResourceBundle;

import javax.annotation.Generated;

import org.codehaus.plexus.util.FileUtils;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.puppycrawl.tools.checkstyle.Checker;
import com.puppycrawl.tools.checkstyle.DefaultConfiguration;
import com.puppycrawl.tools.checkstyle.DefaultLogger;
import com.puppycrawl.tools.checkstyle.TreeWalker;
import com.puppycrawl.tools.checkstyle.api.AbstractCheck;
import com.puppycrawl.tools.checkstyle.api.AutomaticBean.OutputStreamOptions;
import com.puppycrawl.tools.checkstyle.api.CheckstyleException;
import com.puppycrawl.tools.checkstyle.checks.javadoc.JavadocMethodCheck;
import com.puppycrawl.tools.checkstyle.checks.javadoc.JavadocTypeCheck;
import com.puppycrawl.tools.checkstyle.checks.javadoc.JavadocVariableCheck;
import com.sun.codemodel.JCodeModel;

import de.fichtelmax.mojo.messagebundle.model.MessageBundleInfo;
import de.fichtelmax.mojo.messagebundle.model.MessagePropertyInfo;
import de.fichtelmax.mojo.messagebundle.util.Compiler;

public class EnumGeneratorTest {

	private static final String SOME_CLASS_NAME = "SomeClass";
	private static final String SOME_BUNDLE_FILENAME = "Something.properties";

	EnumGenerator cut;

	File dir;

	@Before
	public void createDir() throws IOException {
		cut = new EnumGenerator(false);

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
	public void classInfoShouldNotHaveGeneratedAnnotation() throws Exception {
		String name = "Foo";

		MessageBundleInfo info = new MessageBundleInfo();
		info.setBundleFileName(SOME_BUNDLE_FILENAME);
		info.setName(name);

		JCodeModel codeModel = new JCodeModel();
		cut.transformToEnumInfo(info, codeModel);

		codeModel.build(dir);
		
		byte[] bytes = Files.readAllBytes( new File(dir, name + ".java").toPath() );
		String fileContent = new String(bytes, StandardCharsets.UTF_8);
		
		assertThat(fileContent.contains( "@Generated" ), is(false));
	}
	
	@Test
	public void classInfoShouldHaveGeneratedAnnotationIfConfigured() throws Exception {
		String name = "Foo";
		cut = new EnumGenerator(true);

		MessageBundleInfo info = new MessageBundleInfo();
		info.setBundleFileName(SOME_BUNDLE_FILENAME);
		info.setName(name);

		JCodeModel codeModel = new JCodeModel();
		cut.transformToEnumInfo(info, codeModel);

		codeModel.build(dir);
		
		byte[] bytes = Files.readAllBytes( new File(dir, name + ".java").toPath() );
		String fileContent = new String(bytes, StandardCharsets.UTF_8);
		
		assertThat(fileContent.contains( "@Generated" ), is(true));
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

	@Test
	public void unparameterizedShouldBeRendered() throws Exception {
		String name = SOME_CLASS_NAME;
		String propertyName = "foo";
		String propertyValue = "a test value";

		Properties props = new Properties();
		props.setProperty(propertyName, propertyValue);

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
		Compiler.createProperties(info.getBundleFileName(), props, dir);
		Class<?> barClass = Compiler.loadClass(name, dir);
		Method renderMethod = barClass.getMethod("render", Object[].class);

		Object fooEnumConstant = barClass.getEnumConstants()[0];
		String renderedString = (String) renderMethod.invoke(fooEnumConstant, (Object) new Object[0]);

		assertThat(renderedString, is(equalTo(propertyValue)));
	}

	@Test
	public void parameterizedShouldBeRendered() throws Exception {
		String name = SOME_CLASS_NAME;
		String propertyName = "foo";
		String propertyValue = "{0}";
		String renderValue = "some value";

		Properties props = new Properties();
		props.setProperty(propertyName, propertyValue);

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
		Compiler.createProperties(info.getBundleFileName(), props, dir);
		Class<?> barClass = Compiler.loadClass(name, dir);
		Method renderMethod = barClass.getMethod("render", Object[].class);

		Object fooEnumConstant = barClass.getEnumConstants()[0];
		String renderedString = (String) renderMethod.invoke(fooEnumConstant, (Object) new Object[] { renderValue });

		assertThat(renderedString, is(equalTo(renderValue)));
	}

	@Test
	public void classInfoShouldHaveClassJavadoc() throws Exception {
		String name = "Foo";

		MessageBundleInfo info = new MessageBundleInfo();
		info.setBundleFileName(SOME_BUNDLE_FILENAME);
		info.setName(name);

		JCodeModel codeModel = new JCodeModel();
		cut.transformToEnumInfo(info, codeModel);

		codeModel.build(dir);

		Checker checker = createChecker(JavadocTypeCheck.class);

		int result = checker.process(Collections.singletonList(new File(dir, name + ".java")));

		assertThat(result, is(0));
	}

	@Test
	public void methodsShouldHaveJavadoc() throws Exception {
		String name = "Foo";

		MessageBundleInfo info = new MessageBundleInfo();
		info.setBundleFileName(SOME_BUNDLE_FILENAME);
		info.setName(name);

		JCodeModel codeModel = new JCodeModel();
		cut.transformToEnumInfo(info, codeModel);

		codeModel.build(dir);

		Checker checker = createChecker(JavadocMethodCheck.class);

		int result = checker.process(Collections.singletonList(new File(dir, name + ".java")));

		assertThat(result, is(0));
	}

	@Test
	public void enumConstantsShouldHaveJavadoc() throws Exception {
		String name = "Foo";
		String propertyName = "foo";
		String propertyName2 = "bar";

		MessageBundleInfo info = new MessageBundleInfo();
		info.setBundleFileName(SOME_BUNDLE_FILENAME);
		info.setName(name);
		MessagePropertyInfo propertyInfo = new MessagePropertyInfo();
		propertyInfo.setPropertyName(propertyName);
		propertyInfo.setValue("value of foo");
		info.getPropertyInfos().add(propertyInfo);
		propertyInfo = new MessagePropertyInfo();
		propertyInfo.setPropertyName(propertyName2);
		propertyInfo.setValue("value of bar: {0} {1}");
		info.getPropertyInfos().add(propertyInfo);

		JCodeModel codeModel = new JCodeModel();
		cut.transformToEnumInfo(info, codeModel);

		codeModel.build(dir);

		Checker checker = createChecker(JavadocVariableCheck.class, Collections.singletonMap("scope", "public"));

		int result = checker.process(Collections.singletonList(new File(dir, name + ".java")));

		assertThat(result, is(0));
	}

	private Checker createChecker(Class<? extends AbstractCheck> checkType) throws CheckstyleException {
		return createChecker(checkType, Collections.<String, String> emptyMap());
	}

	private Checker createChecker(Class<? extends AbstractCheck> checkType, Map<String, String> properties)
			throws CheckstyleException {
		DefaultConfiguration root = new DefaultConfiguration("configuration");
		root.addAttribute("charset", "UTF-8");

		DefaultConfiguration config = new DefaultConfiguration(checkType.getName());
		DefaultConfiguration twConfig = new DefaultConfiguration(TreeWalker.class.getName());

		twConfig.addChild(config);
		root.addChild(twConfig);

		for (Entry<String, String> property : properties.entrySet()) {
			config.addAttribute(property.getKey(), property.getValue());
		}

		Checker checker = new Checker();
		checker.setModuleClassLoader(Thread.currentThread().getContextClassLoader());

		checker.configure(root);
		checker.addListener(new DefaultLogger(stdout, OutputStreamOptions.NONE));

		return checker;
	}

	private Properties props(String key) {
		Properties properties = new Properties();
		properties.setProperty(key, "value for " + key);
		return properties;
	}
}
