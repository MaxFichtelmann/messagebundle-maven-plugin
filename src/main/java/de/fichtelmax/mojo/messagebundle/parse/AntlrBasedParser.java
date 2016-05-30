package de.fichtelmax.mojo.messagebundle.parse;

import java.io.IOException;
import java.io.InputStream;
import java.util.Collection;

import org.antlr.v4.runtime.ANTLRInputStream;
import org.antlr.v4.runtime.CharStream;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.tree.ParseTree;
import org.antlr.v4.runtime.tree.ParseTreeWalker;

import de.fichtelmax.mojo.messagebundle.model.MessagePropertyInfo;
import de.fichtelmax.mojo.messagebundle.parse.antlr.JavaPropertiesLexer;
import de.fichtelmax.mojo.messagebundle.parse.antlr.JavaPropertiesParser;
import de.fichtelmax.mojo.messagebundle.parse.antlr.PropertyInfoCapturingWalker;

public class AntlrBasedParser {
	public Collection<MessagePropertyInfo> parse(InputStream data) throws IOException
	{
		CharStream stream = new ANTLRInputStream(data);
		JavaPropertiesLexer lexer = new JavaPropertiesLexer(stream);
		CommonTokenStream tokens = new CommonTokenStream(lexer);
		JavaPropertiesParser parser = new JavaPropertiesParser(tokens);
		ParseTree tree = parser.parse();

		ParseTreeWalker walker = new ParseTreeWalker();

		PropertyInfoCapturingWalker capturingWalker = new PropertyInfoCapturingWalker();
		walker.walk(capturingWalker, tree);
		
		return capturingWalker.getPropertyInfos();
	}
}
