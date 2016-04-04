package de.fichtelmax.mojo.messagebundle.generate;

import org.apache.commons.lang3.StringUtils;

import com.sun.codemodel.ClassType;
import com.sun.codemodel.JBlock;
import com.sun.codemodel.JClassAlreadyExistsException;
import com.sun.codemodel.JCodeModel;
import com.sun.codemodel.JDefinedClass;
import com.sun.codemodel.JEnumConstant;
import com.sun.codemodel.JExpr;
import com.sun.codemodel.JFieldVar;
import com.sun.codemodel.JMethod;
import com.sun.codemodel.JMod;
import com.sun.codemodel.JPackage;
import com.sun.codemodel.JVar;

import de.fichtelmax.mojo.messagebundle.model.MessageBundleInfo;
import de.fichtelmax.mojo.messagebundle.model.MessagePropertyInfo;

public class EnumGenerator {

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

			if (null == info.getPropertyInfos() || info.getPropertyInfos().isEmpty()) {
				generateEnumConstant(_class, "DUMMY");
			}

			for (MessagePropertyInfo propertyInfo : info.getPropertyInfos()) {
				generateEnumConstant(_class, propertyInfo.getPropertyName());
			}
		} catch (JClassAlreadyExistsException e) {
			throw new RuntimeException(e);
		}
	}

	private void generateEnumConstant(JDefinedClass _class, String propertyName) {
		String enumConstantName = propertyName.replaceAll("[^a-zA-Z0-9_]", "_").toUpperCase();
		JEnumConstant enumConstant = _class.enumConstant(enumConstantName);
		enumConstant.arg(JExpr.lit(propertyName));
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
