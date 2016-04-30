package de.fichtelmax.mojo.messagebundle;

import java.io.File;
import java.io.IOException;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.apache.maven.model.FileSet;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.codehaus.plexus.util.FileUtils;

import com.sun.codemodel.JCodeModel;

import de.fichtelmax.mojo.messagebundle.generate.EnumGenerator;
import de.fichtelmax.mojo.messagebundle.generate.MessageResourceParser;
import de.fichtelmax.mojo.messagebundle.model.MessageBundleInfo;

@Mojo(name = "generate", defaultPhase = LifecyclePhase.GENERATE_SOURCES)
public class MessageBundleGenerator extends AbstractMojo {

	private static final String DEFAULT_RESOURCE_DIR = "src/main/resources";

	@Parameter
	private FileSet fileset = new FileSet();

	@Parameter(defaultValue = "${project.build.directory}/generated-sources/message-bundles")
	private File outputDirectory;

	@Parameter(defaultValue = "messages")
	private String packageName;

	private EnumGenerator generator = new EnumGenerator();
	private MessageResourceParser parser = new MessageResourceParser();

	@Override
	public void execute() throws MojoExecutionException, MojoFailureException {
		outputDirectory.mkdirs();

		File sourceDir = locateSourceDir();

		JCodeModel codeModel = new JCodeModel();
		for (File file : collectFiles()) {
			if (file.getName().matches(".*_[a-zA-Z]{2}(?:-[a-zA-Z]{2})?\\.properties$")) {
				continue;
			}
			try {
				MessageBundleInfo bundleInfo = parser.parse(sourceDir, file);
				bundleInfo.setPackageName(packageName);
				generator.transformToEnumInfo(bundleInfo, codeModel);

			} catch (IOException e) {
				throw new MojoExecutionException(e.getMessage(), e);
			}
		}

		try {
			codeModel.build(outputDirectory);
		} catch (IOException e) {
			throw new MojoFailureException("failed to write compiled files: " + e.getMessage(), e);
		}
	}

	private List<File> collectFiles() throws MojoExecutionException {
		if (fileset.getIncludes().isEmpty()) {
			fileset.addInclude("messages/**/*.properties");
		}
		String includes = StringUtils.join(fileset.getIncludes(), ',');
		String excludes = StringUtils.join(fileset.getExcludes(), ',');
		if (StringUtils.isBlank(fileset.getDirectory())) {
			fileset.setDirectory(DEFAULT_RESOURCE_DIR);
		}
		String directory = fileset.getDirectory();
		try {
			return FileUtils.getFiles(new File(directory), includes, excludes);
		} catch (IOException e) {
			throw new MojoExecutionException(e.getMessage(), e);
		}
	}

	private File locateSourceDir() {
		String directory = fileset.getDirectory();
		if (StringUtils.isNotBlank(directory)) {
			return new File(directory);
		} else {
			return new File(DEFAULT_RESOURCE_DIR);
		}
	}
}
