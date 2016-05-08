package de.fichtelmax.mojo.messagebundle.generate;

import java.text.MessageFormat;

import org.apache.commons.lang3.StringUtils;

import com.sun.codemodel.JDefinedClass;
import com.sun.codemodel.JDocComment;
import com.sun.codemodel.JEnumConstant;
import com.sun.codemodel.JExpr;

import de.fichtelmax.mojo.messagebundle.model.MessagePropertyInfo;

public class EnumPropertyGenerator {
	public JEnumConstant generateEnumConstant(MessagePropertyInfo info, JDefinedClass enumClass) {
		return generateEnumConstant(enumClass, info.getPropertyName(), info.getValue(), info.getDescription());
	}

	public JEnumConstant generateDummyEnumConstant(JDefinedClass enumClass) {
		return generateEnumConstant(enumClass, "__", null, null);
	}

	private JEnumConstant generateEnumConstant(JDefinedClass _class, String propertyName, String value, String description) {
		String enumConstantName = propertyName.replaceAll("[^a-zA-Z0-9_]", "_").toUpperCase();
		JEnumConstant enumConstant = _class.enumConstant(enumConstantName);
		enumConstant.arg(JExpr.lit(propertyName));

		JDocComment javadoc = enumConstant.javadoc();
		
		if (StringUtils.isNotBlank(description)) {
			javadoc.add(description);
			javadoc.add("\n\n");
		}
		
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
