package de.fichtelmax.mojo.messagebundle.generate;

import org.apache.commons.lang3.StringUtils;

import com.sun.codemodel.ClassType;
import com.sun.codemodel.JClassAlreadyExistsException;
import com.sun.codemodel.JCodeModel;
import com.sun.codemodel.JDefinedClass;
import com.sun.codemodel.JMod;
import com.sun.codemodel.JPackage;

import de.fichtelmax.mojo.messagebundle.model.MessageBundleInfo;

public class EnumGenerator {

	public void transformToEnumInfo(MessageBundleInfo info, JCodeModel codeModel) {
		JPackage _package;
		
		if (StringUtils.isNotBlank(info.getPackageName())) {
			_package = codeModel._package(info.getPackageName());
		} else {
			_package = codeModel.rootPackage();
		}
		
		try {
			JDefinedClass definedClass = _package._class(JMod.PUBLIC, info.getName(), ClassType.ENUM);
			System.out.println(definedClass.getClassType() == ClassType.ENUM);
		} catch (JClassAlreadyExistsException e) {
			throw new RuntimeException(e);
		}
	}
}
