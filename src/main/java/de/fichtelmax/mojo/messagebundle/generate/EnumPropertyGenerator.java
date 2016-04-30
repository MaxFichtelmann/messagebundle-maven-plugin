package de.fichtelmax.mojo.messagebundle.generate;

import java.text.MessageFormat;

import com.sun.codemodel.JDefinedClass;
import com.sun.codemodel.JDocComment;
import com.sun.codemodel.JEnumConstant;
import com.sun.codemodel.JExpr;

import de.fichtelmax.mojo.messagebundle.model.MessagePropertyInfo;

public class EnumPropertyGenerator {
	public JEnumConstant generateEnumConstant(MessagePropertyInfo info, JDefinedClass enumClass) {
		return generateEnumConstant(enumClass, info.getPropertyName(), info.getValue());
	}

	public JEnumConstant generateDummyEnumConstant(JDefinedClass enumClass) {
		return generateEnumConstant(enumClass, "__", null);
	}

	private JEnumConstant generateEnumConstant(JDefinedClass _class, String propertyName, String value) {
		String enumConstantName = propertyName.replaceAll("[^a-zA-Z0-9_]", "_").toUpperCase();
		JEnumConstant enumConstant = _class.enumConstant(enumConstantName);
		enumConstant.arg(JExpr.lit(propertyName));

		JDocComment javadoc = enumConstant.javadoc();
		javadoc.add("Property '" + propertyName + "'.");

		if (null != value) {
			MessageFormat format = new MessageFormat(value);
			int numberOfParamters = format.getFormatsByArgumentIndex().length;

			if (numberOfParamters == 0) {
				javadoc.add(" Does not require format parameters.");
			} else {
				javadoc.add(" Uses " + numberOfParamters + " format parameters.");
			}
		}

		return enumConstant;
	}
}
