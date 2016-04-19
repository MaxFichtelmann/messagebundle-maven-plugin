package de.fichtelmax.mojo.messagebundle.generate;

import com.sun.codemodel.JDefinedClass;
import com.sun.codemodel.JEnumConstant;
import com.sun.codemodel.JExpr;

import de.fichtelmax.mojo.messagebundle.model.MessagePropertyInfo;

public class EnumPropertyGenerator {
	public JEnumConstant generateEnumConstant(MessagePropertyInfo info, JDefinedClass enumClass) {
		return generateEnumConstant(enumClass, info.getPropertyName());
	}

	public JEnumConstant generateDummyEnumConstant(JDefinedClass enumClass) {
		return generateEnumConstant(enumClass, "_");
	}

	private JEnumConstant generateEnumConstant(JDefinedClass _class, String propertyName) {
		String enumConstantName = propertyName.replaceAll("[^a-zA-Z0-9_]", "_").toUpperCase();
		JEnumConstant enumConstant = _class.enumConstant(enumConstantName);
		enumConstant.arg(JExpr.lit(propertyName));

		return enumConstant;
	}
}
