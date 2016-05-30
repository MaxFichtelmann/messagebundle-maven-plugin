grammar JavaProperties;


parse
  :  (property | emptyLine | comment)* EOF
  ;

property
  :  comment* Space* keyValue
  ;

comment
  :  CommentChar commentContent eol
  ;

commentContent
  :  (EscapedBackslash | Backslash | Colon | Equals | CommentChar | Space | Unicode | AnyChar)*
  ;

emptyLine
  :  Space* LineBreak
  ;

keyValue
  :  key separator Space* value eol
  ;

key
  :  keyChar+
  ;

value
  :  valueChar+
  ;

keyChar
  :  EscapedBackslash
  |  Backslash (Colon | Equals)
  |  EscapedWhitespace
  |  escapedLineBreak
  |  Unicode
  |  Space
  |  AnyChar
  ;

valueChar
  :  EscapedBackslash
  |  EscapedWhitespace
  |  separator 
  |  escapedLineBreak
  |  Unicode
  |  AnyChar
  ;
  
separator
  :  (Space | Colon | Equals)
  ;

escapedLineBreak
  :  Backslash LineBreak Space*
  ;

eol
  :  LineBreak
  |  EOF
  ;

any
  :  Unicode
  |  CommentChar
  |  AnyChar
  ;



fragment
HexChar
  :  ('0'..'9' | 'a'..'f' | 'A'..'F')
  ;

Unicode
  :  '\\' 'u' HexChar HexChar HexChar HexChar
  {
    String hex = getText().replaceAll("^\\\\u", "");
  	setText(String.valueOf((char)Integer.parseInt(hex, 16)));
  }
  ;

EscapedBackslash
  :  '\\\\'
  {
    setText("\\");
  }
  ;

EscapedWhitespace
  :  '\\' ('r' | 'n' | 't' | 'f')
  {
    String escaped = getText();
    String resolved;
    switch(escaped.charAt(1)) {
    case 'r':
    	resolved = "\r";
    	break;
	case 'n':
    	resolved = "\n";
    	break;
    case 't':
    	resolved = "\t";
    	break;
    case 'f':
    	resolved = "\f";
    	break;
    default:
    	throw new IllegalStateException("unexpected: " + escaped);
    }
    setText(resolved);
  }
  ;

Backslash : '\\';
Colon     : ':';
Equals    : '=';

CommentChar
  :  '!' 
  |  '#'
  ;

LineBreak
  :  '\r'? '\n'
  |  '\r'
  ;

Space
  :  ' ' 
  |  '\t' 
  |  '\f'
  ;

AnyChar   : .;