package de.fichtelmax.mojo.messagebundle.generate;

import java.text.MessageFormat;
import java.util.ResourceBundle;

import org.apache.commons.lang3.StringUtils;

import com.sun.codemodel.ClassType;
import com.sun.codemodel.JBlock;
import com.sun.codemodel.JClassAlreadyExistsException;
import com.sun.codemodel.JCodeModel;
import com.sun.codemodel.JDefinedClass;
import com.sun.codemodel.JDocComment;
import com.sun.codemodel.JExpr;
import com.sun.codemodel.JFieldVar;
import com.sun.codemodel.JInvocation;
import com.sun.codemodel.JMethod;
import com.sun.codemodel.JMod;
import com.sun.codemodel.JPackage;
import com.sun.codemodel.JVar;

import de.fichtelmax.mojo.messagebundle.model.MessageBundleInfo;
import de.fichtelmax.mojo.messagebundle.model.MessagePropertyInfo;

public class EnumGenerator {

	private EnumPropertyGenerator propertyGenerator = new EnumPropertyGenerator();

	public void transformToEnumInfo(MessageBundleInfo info, JCodeModel codeModel) {
		JPackage _package;

		if (StringUtils.isNotBlank(info.getPackageName())) {
			_package = codeModel._package(info.getPackageName());
		} else {
			_package = codeModel.rootPackage();
		}

		try {
			JDefinedClass _class = _package._class(JMod.PUBLIC, info.getName(), ClassType.ENUM);
			JDocComment typeJavaDoc = _class.javadoc();
			typeJavaDoc.add("A generated enum that wraps the {@link ResourceBundle} for " + info.getName() + '.');

			JFieldVar propertyNameField = _class.field(JMod.PRIVATE, String.class, "propertyName");
			generateConstructor(_class, propertyNameField);
			generatePropertyNameGetter(_class, propertyNameField);
			JFieldVar bundleField = generateBundleField(_class, info);
			generateRenderMethod(_class, bundleField, propertyNameField, codeModel);

			if (null == info.getPropertyInfos() || info.getPropertyInfos().isEmpty()) {
				propertyGenerator.generateDummyEnumConstant(_class);
			}

			for (MessagePropertyInfo propertyInfo : info.getPropertyInfos()) {
				propertyGenerator.generateEnumConstant(propertyInfo, _class);
			}
		} catch (JClassAlreadyExistsException e) {
			throw new RuntimeException(e);
		}
	}

	private void generateRenderMethod(JDefinedClass _class, JFieldVar bundle, JFieldVar nameField,
			JCodeModel codeModel) {
		JMethod renderMethod = _class.method(JMod.PUBLIC, String.class, "render");
		JVar renderParameters = renderMethod.varParam(Object.class, "parameters");
		JBlock methodBody = renderMethod.body();

		JInvocation template = JExpr.invoke(bundle, "getString").arg(nameField);
		JInvocation body = codeModel.ref(MessageFormat.class).staticInvoke("format").arg(template)
				.arg(renderParameters);

		methodBody._return(body);

		JDocComment javadoc = renderMethod.javadoc();
		javadoc.add("Render the message by turning the value in the {@link ResourceBundle} "
				+ "into a {@link MessageFormat} and formatting it using the provided parameters.");
		javadoc.addParam(renderParameters).add("the parameters that should be used during formatting");
		javadoc.addReturn().add("the formatted String.");
	}

	private JFieldVar generateBundleField(JDefinedClass _class, MessageBundleInfo info) {
		JFieldVar bundleField = _class.field(JMod.PRIVATE | JMod.STATIC | JMod.FINAL, ResourceBundle.class, "_BUNDLE");

		bundleField.init(JExpr.direct(
				"ResourceBundle.getBundle(\"" + info.getBundleFileName().replaceAll("\\.properties$", "") + "\")"));

		return bundleField;
	}

	private void generatePropertyNameGetter(JDefinedClass _class, JFieldVar propertyNameField) {
		JMethod propertyNameGetter = _class.method(JMod.PUBLIC, String.class, "getPropertyName");
		propertyNameGetter.body()._return(propertyNameField);

		JDocComment javadoc = propertyNameGetter.javadoc();
		javadoc.add("Returns the property name in the {@link ResourceBundle} this refers to.");
		javadoc.addReturn().add("the property name in the {@link ResourceBundle} this refers to.");
	}

	private void generateConstructor(JDefinedClass _class, JFieldVar nameField) {
		JMethod constructor = _class.constructor(JMod.PRIVATE);
		JVar param = constructor.param(String.class, "propertyName");
		JBlock body = constructor.body();
		body.assign(JExpr._this().ref(nameField), param);

		JDocComment javadoc = constructor.javadoc();
		javadoc.add("Create the enum constant with the given property key.");
		javadoc.addParam(param).add("the name of the property the created enum constant refers to");
	}
}
