package de.fichtelmax.mojo.messagebundle.generate;

import java.util.ResourceBundle;

import org.apache.commons.lang3.StringUtils;

import com.sun.codemodel.ClassType;
import com.sun.codemodel.JBlock;
import com.sun.codemodel.JClassAlreadyExistsException;
import com.sun.codemodel.JCodeModel;
import com.sun.codemodel.JDefinedClass;
import com.sun.codemodel.JExpr;
import com.sun.codemodel.JFieldVar;
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

			JFieldVar propertyNameField = _class.field(JMod.PRIVATE, String.class, "propertyName");
			generateConstructor(_class, propertyNameField);
			generatePropertyNameGetter(_class, propertyNameField);
			generateBundleField(_class, info);

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

	private void generateBundleField(JDefinedClass _class, MessageBundleInfo info) {
		JFieldVar bundleField = _class.field(JMod.PRIVATE | JMod.STATIC | JMod.FINAL, ResourceBundle.class, "_BUNDLE");

		bundleField.init(JExpr.direct(
				"ResourceBundle.getBundle(\"" + info.getBundleFileName().replaceAll("\\.properties$", "") + "\")"));
	}

	private void generatePropertyNameGetter(JDefinedClass _class, JFieldVar propertyNameField) {
		JMethod propertyNameGetter = _class.method(JMod.PUBLIC, String.class, "getPropertyName");
		propertyNameGetter.body()._return(propertyNameField);
	}

	private void generateConstructor(JDefinedClass _class, JFieldVar nameField) {
		JMethod constructor = _class.constructor(JMod.PRIVATE);
		JVar param = constructor.param(String.class, "propertyName");
		JBlock body = constructor.body();
		body.assign(JExpr._this().ref(nameField), param);
	}
}
