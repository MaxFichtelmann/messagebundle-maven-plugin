package de.fichtelmax.mojo.messagebundle.generate;

import org.apache.commons.lang3.StringUtils;

import com.sun.codemodel.ClassType;
import com.sun.codemodel.JClassAlreadyExistsException;
import com.sun.codemodel.JCodeModel;
import com.sun.codemodel.JDefinedClass;
import com.sun.codemodel.JMod;
import com.sun.codemodel.JPackage;

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
			
			for (MessagePropertyInfo propertyInfo: info.getPropertyInfos()) {
				String enumConstantName = propertyInfo.getPropertyName().replaceAll("[^a-zA-Z0-9_]", "_").toUpperCase();
				_class.enumConstant(enumConstantName);
			}
		} catch (JClassAlreadyExistsException e) {
			throw new RuntimeException(e);
		}
	}
}
