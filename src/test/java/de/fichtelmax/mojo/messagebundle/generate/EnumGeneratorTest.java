package de.fichtelmax.mojo.messagebundle.generate;

import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.Matchers.arrayWithSize;
import static org.junit.Assert.assertThat;

import java.io.File;
import java.io.IOException;
import java.util.Collections;

import org.codehaus.plexus.util.FileUtils;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.sun.codemodel.JCodeModel;

import de.fichtelmax.mojo.messagebundle.model.MessageBundleInfo;
import de.fichtelmax.mojo.messagebundle.model.MessagePropertyInfo;
import de.fichtelmax.mojo.messagebundle.util.Compiler;

public class EnumGeneratorTest {

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
	
	@Test
	public void classInfoShouldBeGenerated() throws Exception {
		String name = "Foo";

		MessageBundleInfo info = new MessageBundleInfo();
		info.setName(name);
		
		JCodeModel codeModel = new JCodeModel();
		cut.transformToEnumInfo(info, codeModel);
		
		codeModel.build(dir);
		Compiler.compile(name, dir);
		Class<?> fooClass = Compiler.loadClass(name, dir);
		
		assertThat(fooClass.isEnum(), is(true));
	}
	
	@Test
	public void qualifiedClassInfoShouldBeGenerated() throws Exception {
		String name = "Bar";
		String packageName = "foo";

		String fullName = "foo.Bar";
		
		MessageBundleInfo info = new MessageBundleInfo();
		info.setName(name);
		info.setPackageName(packageName);
		
		JCodeModel codeModel = new JCodeModel();
		cut.transformToEnumInfo(info, codeModel);
		
		codeModel.build(dir);
		Compiler.compile(fullName, dir);
		Class<?> barClass = Compiler.loadClass(fullName, dir);
		
		assertThat(barClass.isEnum(), is(true));
	}
	
	@Test
	public void enumConstantShouldBeGenerated() throws Exception {
		String name = "Bar";
		String propertyName = "baz";
		
		MessageBundleInfo info = new MessageBundleInfo();
		info.setName(name);
		MessagePropertyInfo propertyInfo = new MessagePropertyInfo();
		propertyInfo.setPropertyName(propertyName);
		info.setPropertyInfos(Collections.singleton(propertyInfo));
		
		JCodeModel codeModel = new JCodeModel();
		cut.transformToEnumInfo(info, codeModel);
		
		codeModel.build(dir);
		Compiler.compile(name, dir);
		Class<?> barClass = Compiler.loadClass(name, dir);
		
		assertThat(barClass.getEnumConstants(), is(arrayWithSize(1)));
		assertThat(barClass.getEnumConstants()[0], is(instanceOf(Enum.class)));
	}
}
