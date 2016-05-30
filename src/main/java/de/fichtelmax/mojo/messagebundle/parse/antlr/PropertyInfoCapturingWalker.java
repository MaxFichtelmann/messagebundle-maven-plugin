package de.fichtelmax.mojo.messagebundle.parse.antlr;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import de.fichtelmax.mojo.messagebundle.model.MessagePropertyInfo;
import de.fichtelmax.mojo.messagebundle.parse.antlr.JavaPropertiesParser.CommentContext;
import de.fichtelmax.mojo.messagebundle.parse.antlr.JavaPropertiesParser.KeyContext;
import de.fichtelmax.mojo.messagebundle.parse.antlr.JavaPropertiesParser.KeyValueContext;
import de.fichtelmax.mojo.messagebundle.parse.antlr.JavaPropertiesParser.ParseContext;
import de.fichtelmax.mojo.messagebundle.parse.antlr.JavaPropertiesParser.PropertyContext;
import de.fichtelmax.mojo.messagebundle.parse.antlr.JavaPropertiesParser.ValueContext;

public class PropertyInfoCapturingWalker extends JavaPropertiesBaseListener {

	Collection<MessagePropertyInfo> propertyInfos = new ArrayList<>();

	@Override
	public void enterParse(ParseContext ctx) {
		for (PropertyContext property : ctx.property()) {
			MessagePropertyInfo propertyInfo = new MessagePropertyInfo();

			List<CommentContext> commentLines = property.comment();
			StringBuilder description = new StringBuilder();
			for (CommentContext commentLine : commentLines) {
				if (description.length() > 0) {
					description.append('\n');
				}
				description.append(commentLine.commentContent().getText().trim());
			}
			propertyInfo.setDescription(description.toString());

			KeyValueContext keyValue = property.keyValue();
			KeyContext key = keyValue.key();
			propertyInfo.setPropertyName(removeEscapedLinebreaks(key.getText()).trim());

			ValueContext value = keyValue.value();
			if (value != null) {
				propertyInfo.setValue(removeEscapedLinebreaks(value.getText()).trim());
			}
			
			propertyInfos.add(propertyInfo);
		}
	}
	
	public Collection<MessagePropertyInfo> getPropertyInfos() {
		return propertyInfos;
	}

	private String removeEscapedLinebreaks(String s) {
		return s.replaceAll("\\\\(\r?\n|\r)[ \t\f]*", "");
	}
}
