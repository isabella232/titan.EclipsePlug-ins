lexer grammar PatternStringLexer;

/*
 ******************************************************************************
 * Copyright (c) 2000-2019 Ericsson Telecom AB
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/org/documents/epl-2.0/EPL-2.0.html
 ******************************************************************************
*/

/* TODO: better comment
 * Lexer for a TTCN-3 pattern string to convert it to Java Pattern.
 * 
 * @author Gergo Ujhelyi
 */
@header {
import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.resources.IFile;	

import org.eclipse.titan.designer.AST.Identifier;
import org.eclipse.titan.designer.AST.Identifier.Identifier_type;
import org.eclipse.titan.designer.AST.Location;
import org.eclipse.titan.designer.AST.Reference;
import org.eclipse.titan.designer.AST.TTCN3.templates.PatternString;

}

@members {	
private boolean in_set = false; /* inside a [] */
private PatternString ps = new PatternString();
private IFile actualFile = null;
private int actualLine = 1;
private int offset = 0;
private String tokenStr = null;
private Location actualLocation = null;
private Token startToken = null;
private Token endToken = null;
private int actualColumn = 0;

public void setActualFile(IFile file) {
	actualFile = file;
	}

public void setActualLine(int line) {
	actualLine = line;
	}
	
public int getOffset() {
	return offset;
}

public void setOffset(int offset) {
	this.offset = offset;
}	
	
public PatternString getPatternString() {
	return ps;
	}
	
public void setTokenString(final String p_str) {
	if (p_str != null) {
		this.tokenStr = p_str;
		}
	}

public void setActualLocation(final Location p_loc) {
	this.actualLocation = p_loc;
	}
	
public void setStartToken(final Token p_starToken) {
	this.startToken = p_starToken;
	}
	
public void setEndToken(final Token p_endToken) {
	this.endToken = p_endToken;
	}	  	
}

/* ***************** definitions ***************** */
fragment
WS
:
	[ \t\n\x0B\f\r]+
;
fragment
NEWLINE
:
	[\r\n]
;
fragment
DIGIT
:
	[0-9]
;

fragment
INT
:
	[1-9] DIGIT* | '0'
;
fragment 
IDENTIFIER
:
	[A-Za-z] [A-Za-z0-9_]*
;

/* ***************** rules ************************* */
REFERENCE_RULE : '{' (WS)? IDENTIFIER (( (WS)? '.' (WS)? IDENTIFIER ) | ( (WS)? '[' (WS)? ( IDENTIFIER | NUMBER ) (WS)? ']'))* (WS)? '}' {
	if (in_set) {
		// in a set a reference (without the \N at start) is just simple text,
  		// the matched token does not contain characters that are special in a set
  		int begin = actualColumn; 
  		int end = actualColumn; 
  		while(tokenStr.charAt(end) != '}') {
  			end++;
  		}
  		ps.addString(tokenStr.substring(begin,end));
  		actualColumn = end;
	} else {
		/**
		 * Find references in the actual token but in another way than C++.
		 * 1. Skip '{', whitespace and '['.
		 * 2. Get the ID(s) string.
		 * 3. Break at '}'.
		 */
		//end of the reference
		int end = actualColumn;
		//begin of the reference (reference location)
		int begin = actualColumn;
		List<String> identifiers = new ArrayList<String>();
		while(tokenStr.charAt(end) != '}') {
			//current ID begin/end
			int current_begin = 0;
			int current_end = 0;
			// skip whitespace and [	
			while(Character.isWhitespace(tokenStr.charAt(end)) || tokenStr.charAt(end) == '[' || tokenStr.charAt(end) == '{') {
				end++;
			}
			current_begin = end;
			current_end = current_begin;
			begin = current_begin;
			while(Character.isLetterOrDigit(tokenStr.charAt(current_end)) || tokenStr.charAt(current_end) == '_' || tokenStr.charAt(current_end) == '.') {
				current_end++;
			}
			String identifier = tokenStr.substring(current_begin, current_end);
			identifiers.add(identifier);
			end = current_end;
			while(Character.isWhitespace(tokenStr.charAt(end)) || tokenStr.charAt(end) == ']') {
				end++;
			}	
		}
		actualColumn = end + 1;
		TTCN3ReferenceAnalyzer analyzer = new TTCN3ReferenceAnalyzer();
		Location ref_location = new Location(actualFile, actualLine, startToken.getStartIndex() + begin + 1, startToken.getStopIndex() + end);
		Reference ref = null;
		if (identifiers.size() == 1) {
			ref = analyzer.parse(actualFile, identifiers.get(0), false, ref_location.getLine(), ref_location.getOffset());
		} else if (identifiers.size() > 1) {
			String id_str = "";
			for (int i = 0; i < identifiers.size(); i++) {
				if (i == 0) {
					id_str = identifiers.get(i);
				} else {
					id_str += "[" + identifiers.get(i) +"]";
				}						
			}
			ref = analyzer.parse(actualFile, id_str, false, ref_location.getLine(), ref_location.getOffset());
		}
		if (ref != null) {
			ps.addRef(ref, false);
		} else {
			ref_location.reportSemanticError("Invalid reference expression");
		}	
	}
}
;
INVALID_REFERENCE_RULE : '{' [ ^} ]* '}' {
  Location location = new Location(actualFile, actualLine, startToken.getStartIndex() + 1, startToken.getStopIndex());
  location.reportSyntacticError(String.format("Invalid reference expression: %s", tokenStr));	
};
UNIVERSAL_CHARSTRING_REFERENCE : '\\N' (WS)? '{' (WS)? 'universal' (WS)? 'charstring' (WS)? '}' {
	/* The third {(WS)?} is optional but if it's empty then the previous rule catches it*/
	final String id_str = "universal charstring";
 
	Location location = new Location(actualFile, actualLine, startToken.getStartIndex(), startToken.getStopIndex());
	TTCN3ReferenceAnalyzer analyzer = new TTCN3ReferenceAnalyzer();
	Reference ref = new Reference(new Identifier(Identifier_type.ID_TTCN, id_str, location));

	ps.addRef(ref, true); 
	ref.setLocation(location);
	if (in_set) {
		location.reportSyntacticWarning(String.format("Character set reference \\N{%s} is not supported, dropped out from the set", id_str));
	}
	int end = actualColumn;
	while(tokenStr.charAt(end) != '}') {
		end++;
	}
	actualColumn = end + 1; 
};
CHARSTRING_REFERENCE : '\\N' (WS)? '{' (WS)? 'charstring' (WS)? '}' {
	/* The third {(WS)?} is optional but if it's empty then the previous rule catches it*/
	final String id_str = "charstring";
 
	Location location = new Location(actualFile, actualLine, startToken.getStartIndex(), startToken.getStopIndex());
	Reference ref = new Reference(new Identifier(Identifier_type.ID_TTCN, id_str, location));

	ps.addRef(ref, true); 
	ref.setLocation(location);
	if (in_set) {
		location.reportSyntacticWarning(String.format("Character set reference \\N{%s} is not supported, dropped out from the set", id_str));
	}
	int end = actualColumn;
	while(tokenStr.charAt(end) != '}') {
		end++;
	}
	actualColumn = end + 1;
};
REFERENCE_WITH_N : '\\N' (WS)? '{' (WS)? IDENTIFIER (WS)? '}' {
	int id_begin = 3 + actualColumn;
	while(!Character.isAlphabetic(tokenStr.charAt(id_begin))) {
		id_begin++;
		}
	int id_end = id_begin;
	while(Character.isLetterOrDigit(tokenStr.charAt(id_end)) || tokenStr.charAt(id_end) == '_') {
		id_end++;
		}
	String id_str = tokenStr.substring(id_begin, id_end);
	actualColumn = id_end + 1;
	Location location = new Location(actualFile, actualLine, startToken.getStartIndex() + id_begin + 1, startToken.getStartIndex() + id_end + 1);
	TTCN3ReferenceAnalyzer analyzer = new TTCN3ReferenceAnalyzer();
	Reference ref = analyzer.parse(actualFile, id_str, false, location.getLine(), location.getOffset());
	ps.addRef(ref, true);
	if (in_set) {
			location.reportSyntacticWarning(String.format("Character set reference \\N{%s} is not supported, dropped out from the set", id_str));
		}
};
INVALID_SET_REFERENCE_RULE : '\\N' (WS)? '{' [^}]* '}' {
  Location location = new Location(actualFile, actualLine, startToken.getStartIndex() + 1, startToken.getStopIndex());
  location.reportSyntacticError(String.format("Invalid character set reference: %s", tokenStr));
};

QUADRUPLE_RULE : '\\q'  (WS)? '{' (WS)? NUMBER (WS)? ',' (WS)? NUMBER (WS)? ',' (WS)? NUMBER  (WS)? ',' (WS)? NUMBER (WS)? '}' {
/* quadruple - group */
			int group_begin = 3 + actualColumn;
			while (!Character.isDigit(tokenStr.charAt(group_begin))) {
				group_begin++;
			} 
			int group_end = group_begin;
			while (Character.isDigit(tokenStr.charAt(group_end))) { 
				group_end++;
			}
			final String group_str = tokenStr.substring(group_begin, group_end); 
			int group = Integer.parseInt(group_str); 
			if (group < 0 || group > 127) {
				Location group_location = new Location(actualFile, actualLine, startToken.getStartIndex() + group_begin + 1, startToken.getStartIndex() + group_end + 1);  
				group_location.reportSemanticError(String.format("The first number of quadruple (group) must be within the range 0 .. 127 instead of %s", group_str));
				group = group < 0 ? 0 : 127; 
			}

			/* quadruple - plane */ 
			int plane_begin = group_end + 1;
			while (!Character.isDigit(tokenStr.charAt(plane_begin))) {
				plane_begin++;
			} 
			int plane_end = plane_begin;
			while (Character.isDigit(tokenStr.charAt(plane_end))) { 
				plane_end++;
			}
			final String plane_str = tokenStr.substring(plane_begin, plane_end); 
			int plane = Integer.parseInt(plane_str); 
			if (plane < 0 || plane > 255) {
				Location plane_location = new Location(actualFile, actualLine, startToken.getStartIndex() + plane_begin + 1, startToken.getStartIndex() + plane_end + 1);  
				plane_location.reportSemanticError(String.format("The second number of quadruple (plane) must be within the range 0 .. 255 instead of %s", plane_str));
				plane = plane < 0 ? 0 : 255; 
			} 

			/* quadruple - row */ 
			int row_begin = plane_end + 1;
			while (!Character.isDigit(tokenStr.charAt(row_begin))) {
				row_begin++;
			} 
			int row_end = row_begin;
			while (Character.isDigit(tokenStr.charAt(row_end))) { 
				row_end++;
			}
			final String row_str = tokenStr.substring(row_begin, row_end); 
			int row = Integer.parseInt(row_str); 
			if (row < 0 || row > 255) {
				Location row_location = new Location(actualFile, actualLine, startToken.getStartIndex() + row_begin + 1, startToken.getStartIndex() + row_end + 1);  
				row_location.reportSemanticError(String.format("The third number of quadruple (row) must be within the range 0 .. 255 instead of %s", row_str));
				row = row < 0 ? 0 : 255; 
			}

			/* quadruple - cell */ 
			int cell_begin = row_end + 1;
			while (!Character.isDigit(tokenStr.charAt(cell_begin))) {
				cell_begin++;
			} 
			int cell_end = cell_begin;
			while (Character.isDigit(tokenStr.charAt(cell_end))) { 
				cell_end++;
			}
			final String cell_str = tokenStr.substring(cell_begin, cell_end); 
			int cell = Integer.parseInt(cell_str); 
			if (cell < 0 || cell > 255) {
				Location cell_location = new Location(actualFile, actualLine, startToken.getStartIndex() + cell_begin + 1, startToken.getStartIndex() + cell_end + 1);  
				cell_location.reportSemanticError(String.format("The fourth number of quadruple (cell) must be within the range 0 .. 255 instead of %s", cell_str));
				cell = cell < 0 ? 0 : 255; 
			}
			int end = cell_end;
			while(tokenStr.charAt(end) != '}') {
				end++;
			}
			actualColumn = end + 1;
			boolean add_quadruple = true;
			if (group == 0 && plane == 0 && row == 0) {
				if(!Character.isISOControl((char) cell) && !Character.isWhitespace((char) cell)) { 
					switch ((char) cell) {
					case '-':
					case '^':
						if (!in_set) {
							break;
						}
					case '?':
					case '*':
					case '\\':
					case '[':
					case ']':
					case '{':
					case '}':
					case '"':
					case '|':
					case '(':
					case ')':
					case '#':
					case '+': 
						ps.addChar('\\');
					default:
						break;
					}
					ps.addChar((char) cell);
					add_quadruple = false;	
				} else {
					switch((char) cell) {
					case '\t':
						ps.addString("\\t");
						add_quadruple = false;
						break;
					case '\r':
						ps.addString("\\r");
						add_quadruple = false;
					}	
				}
			}
			if (add_quadruple) {
				ps.addString(String.format("\\\\q{%s, %s, %s, %s}", group, plane, row, cell));
			}
};

UID_RULE : '\\q' ( (WS)? '{' (WS)? ( UID (WS)? ',' (WS)? )* UID (WS)? '}') {
//Split UID-s. For example: \q{ U23423 , U+001 } -> [U23423, U+001] 
	int begin = 3 + actualColumn;
	int end = 0;
	List<String> uids = new ArrayList<String>();
	while(tokenStr.charAt(begin) != '}') {
		//Find first digit
		while(tokenStr.charAt(begin) != 'U' && tokenStr.charAt(begin) != 'u') {
			begin++; 		
		}
		end = begin + 2; 
		//Find last digit
		while(Character.isDigit(tokenStr.charAt(end))) {
			end++;
		}
		uids.add(tokenStr.substring(begin, end)); 
		//Skip whitespaces until the next UID or the end
		while(!Character.isDigit(tokenStr.charAt(end)) && tokenStr.charAt(end) != 'U' && tokenStr.charAt(end) != 'u' && tokenStr.charAt(end) != '}') {
			if (tokenStr.charAt(end) == '-') {
				ps.addChar('-'); 
			}	
			 end++;
		}
		begin = end; 	
	}
	actualColumn = end + 1;
	ps.addStringUSI(uids);
	//Free
	uids = null;
};

INVALID_QUADRUPLE_UID_RULE : '\\q' ( (WS)? '{' (IDENTIFIER)? [ ^}]* '}')? {
  Location location = new Location(actualFile, actualLine, startToken.getStartIndex() + 1, startToken.getStopIndex());
  location.reportSyntacticError(String.format("Invalid quadruple or UID-like notation: %s", tokenStr));
};

SQUARE_BRACKETS: '[]' {
  if(in_set) {
    ps.addString("\\[]");
    in_set = false;
  } else {
    ps.addString("[\\]");
    in_set = true;
  }
  actualColumn += 2;
};
 
COMPLEMENT: '[^]' {
  if(in_set) {
    ps.addString("\\[\\^]");
    in_set = false;
  } else {
    ps.addString("[^\\]");
    in_set = true;
  }
  actualColumn += 3;
};

OPENING_SQUARE_BRACKET : '[' {
  if(in_set) {
    ps.addString("\\[");
  } else {
    ps.addChar('[');
    in_set = true;
  }
  actualColumn++;
};

CLOSING_SQUARE_BRACKET : ']' {
  if (in_set) {
    ps.addString("]");
    in_set = false;
  } else {
    Location location = new Location(actualFile, actualLine, startToken.getStartIndex(), startToken.getStopIndex() + 1);
    location.reportSyntacticError("Unmatched `]'. Did you mean `\\]'?");
    ps.addString("\\]");
  }
  actualColumn++;
};
SQUARE_BRACES : '{'|'}' {
  Location location = new Location(actualFile, actualLine, startToken.getStartIndex(), startToken.getStopIndex() + 1);
  location.reportSyntacticWarning(String.format("Unmatched `%c' was treated literally", tokenStr.charAt(0)));
  ps.addString("\\");
  ps.addChar(tokenStr.charAt(0));
  actualColumn++;
};
QUOTE_MARKS : '\\\"' |'\"\"' {
  ps.addChar('"');
  actualColumn += 2;
};
//metachars and escaped metachars  
METACHARS : '\\'[dwtnrsb?*\\\[\]\-\^|()#+-] { 
 ps.addChar('\\');
 ps.addString(getText());
 actualColumn += 2; 
};
UNRECOGNIZED_ESCAPE_SEQUENCE : '\\'(.| NEWLINE) {
 Location location = new Location(actualFile, actualLine, startToken.getStartIndex(), startToken.getStartIndex() + 1);
 if (!Character.isISOControl(tokenStr.charAt(1)) && !Character.isWhitespace((tokenStr.charAt(1)))) {
 	location.reportSyntacticWarning(String.format("Use of unrecognized escape sequence `\\%c' is deprecated", tokenStr.charAt(1)));
 } else {
 	location.reportSyntacticWarning("Use of unrecognized escape sequence is deprecated");
 }
 ps.addChar('\\');
 ps.addString(getText());
 actualColumn += 2;
};
REPETITION : '#' (WS)? [0-9] {
	if (in_set) {
		Location location = new Location(actualFile, actualLine, startToken.getStartIndex() + 1, startToken.getStopIndex());
		location.reportSemanticError("Number of repetitions `#n' cannot be given inside a set expression");
	} else if (tokenStr.charAt(actualColumn - 1) != '1')  {
		ps.addChar('#'); 
		ps.addChar(tokenStr.charAt(actualColumn - 1));
 	}
 	
};
 
N_REPETITION: '#' (WS)? '(' (WS)? NUMBER (WS)? ')' {
	if (in_set) {
		Location location = new Location(actualFile, actualLine, startToken.getStartIndex() + 1, startToken.getStopIndex());
		location.reportSemanticError("Number of repetitions `#(n)' cannot be given inside a set expression");
 	} else {
 		int number_begin = 2;
 		while(!Character.isDigit(tokenStr.charAt(number_begin))) {
 			number_begin++;
 		} 
 		int number_end = number_begin;
 		while(Character.isDigit(tokenStr.charAt(number_end))) {
 			number_end++;
 		}
 		String number_str = tokenStr.substring(number_begin, number_end);
 		int number = Integer.parseInt(number_str);
 		if (number < 0) {
 			Location number_location = new Location(actualFile, actualLine, startToken.getStartIndex() + number_begin + 1, startToken.getStartIndex() + number_end);
 			number_location.reportSemanticError(String.format("A non-negative integer value was expected as the number of repetitions instead of %s", number_str));
 		} else if (number != 1) {
			ps.addString("#(" + number_str + ")"); 
 		}
 	}
}; 

N_M_REPETITION : '#' (WS)? '(' (WS)? NUMBER (WS)? ',' (WS)? NUMBER (WS)? ')' {
	if (in_set) {
		Location location = new Location(actualFile, actualLine, startToken.getStartIndex() + 1, startToken.getStopIndex());
		location.reportSemanticError("Number of repetitions `#(n,m)' cannot be given inside a set expression");		 
	}  
 	int lower_begin = 2 + actualColumn;
 	while(!Character.isDigit(tokenStr.charAt(lower_begin))) {
 		lower_begin++;
 	}
 	int lower_end = lower_begin;
 	while(Character.isDigit(tokenStr.charAt(lower_end))) {
 		lower_end++;
 	}
 	String lower_str = tokenStr.substring(lower_begin, lower_end); 
	Location lower_location = new Location(actualFile, actualLine, startToken.getStartIndex() + lower_begin + 1, startToken.getStartIndex() + lower_end);
	int lower = Integer.parseInt(lower_str);
	if (lower < 0) {
		lower_location.reportSemanticError(String.format("A non-negative integer value was expected as the minimum number of repetitions instead of %s", lower_str));
		lower = 0;
	}
	int upper_begin = lower_end + 1;
 	while(!Character.isDigit(tokenStr.charAt(upper_begin))) {
 		upper_begin++;
 	}
 	int upper_end = upper_begin;
 	while(Character.isDigit(tokenStr.charAt(upper_end))) {
 		upper_end++;
 	}
 	String upper_str = tokenStr.substring(upper_begin, upper_end); 
	Location upper_location = new Location(actualFile, actualLine, startToken.getStartIndex() + upper_begin + 1, startToken.getStartIndex() + upper_end);
	int upper = Integer.parseInt(upper_str);
	if (upper < 0) {
		upper_location.reportSemanticError(String.format("A non-negative integer value was expected as the maximum number of repetitions instead of %s", upper_str));
	} else if (lower > upper) {
		Location location = new Location(actualFile, actualLine, startToken.getStartIndex() + 1, startToken.getStopIndex());
		location.reportSemanticError(String.format("The lower bound is higher than the upper bound in the number of repetitions: `#(%s,%s)'", lower_str, upper_str)); 	
	} else if (lower == upper) {
		if (lower != 1) {
			ps.addString("#(" + lower_str + ")"); 
			}		 
	} else {
		if (lower == 0) {
			ps.addString("#(," + upper_str + ")");
		} else {
			ps.addString("#(" + lower_str + "," + upper_str + ")");	
			}
		}
	actualColumn = upper_end + 1;
}; 

N_M_REPETITION_WITHOUT_M : '#' (WS)? '(' (WS)? NUMBER (WS)? ',' (WS)? ')' {
  if (in_set) {
	Location location = new Location(actualFile, actualLine, startToken.getStartIndex() + 1, startToken.getStopIndex());
    location.reportSemanticError("Number of repetitions `#(n,)' cannot be given inside a set expression");
  } else {
    int lower_begin = 2 + actualColumn;
    while(!Character.isDigit(tokenStr.charAt(lower_begin))) {
 		lower_begin++;
 	}
    int lower_end = lower_begin;
 	while(Character.isDigit(tokenStr.charAt(lower_end))) {
 		lower_end++;
 	}
    String lower_str = tokenStr.substring(lower_begin, lower_end);
    Location lower_location = new Location(actualFile, actualLine, startToken.getStartIndex() + lower_begin, startToken.getStartIndex() + lower_end);
	int lower = Integer.parseInt(lower_str);
    if (lower < 0) {
      lower_location.reportSemanticError(String.format("A non-negative integer value was expected as the minimum number of repetitions instead of %s",lower_str));
    } else if (lower == 1) {
    	ps.addChar('+');
     } else {
    	ps.addString("#(" + lower_str + ",)");
   	 }
   	 int end = lower_end; 
   	 while(tokenStr.charAt(end) != ')') {
   	 	end++;
   	 }
   	 actualColumn = end + 1;
  }
};

N_M_REPETITION_WITHOUT_N : '#' (WS)? '(' (WS)? ',' (WS)? NUMBER (WS)? ')' {
  if (in_set) {
  	Location location = new Location(actualFile, actualLine, startToken.getStartIndex() + 1, startToken.getStopIndex());
    location.reportSemanticError("Number of repetitions `#(,m)' cannot be given inside a set expression");
  } else {
    int upper_begin = 3 + actualColumn;
    while(!Character.isDigit(tokenStr.charAt(upper_begin))) {
 		upper_begin++;
 	}
 	int upper_end = upper_begin;
 	while(Character.isDigit(tokenStr.charAt(upper_end))) {
 		upper_end++;
 	}
 	String upper_str = tokenStr.substring(upper_begin, upper_end); 
	Location upper_location = new Location(actualFile, actualLine, startToken.getStartIndex() + upper_begin, startToken.getStartIndex() + upper_end);
	int upper = Integer.parseInt(upper_str);
    if (upper < 0) {
      upper_location.reportSemanticError(String.format("A non-negative integer value was expected as the maximum number of repetitions instead of %s",upper_str));
    } else {
    	 ps.addString("#(," + upper_str + ")");
    }
    int end = upper_end;
    while(tokenStr.charAt(end) != ')') {
    	end++;
    }
    actualColumn = end + 1;
  }
}; 

EMPTY_REPETITION : '#' (WS)? '(' (WS)? ',' (WS)? ')' {
 if (in_set) {
 	Location location = new Location(actualFile, actualLine, startToken.getStartIndex() + 1, startToken.getStopIndex());
 	location.reportSyntacticError("Number of repetitions `#(,)' cannot be given inside a set expression");
  } else { 
  	ps.addString("#(,)");
  }
}; 

INVALID_NUMBER_REPETITION : '#' (WS)? '(' [^)]* ')' {
 Location location = new Location(actualFile, actualLine, startToken.getStartIndex() + 1, startToken.getStopIndex());
 location.reportSyntacticError(String.format("Invalid notation for the number of repetitions: %s", tokenStr));
}; 
 
HASHMARK : '#' {
	Location location = new Location(actualFile, actualLine, startToken.getStartIndex() + 1, startToken.getStopIndex());
	if (in_set) {
		location.reportSyntacticWarning("Unescaped `#' inside character set was treated literally");
		ps.addChar('\\');
		ps.addChar('#');
		} else {
			location.reportSyntacticError("Syntax error in the number of repetitions `#...'");
		}
	actualColumn++;
};

PLUS : '+' {
  if (in_set) {
	Location location = new Location(actualFile, actualLine, startToken.getStartIndex() + 1, startToken.getStopIndex());
    location.reportSyntacticWarning("Unescaped `+' inside character set was treated literally");
    ps.addChar('\\');
  }
  ps.addChar('+');
  actualColumn++;
};  

NUMBER
:
	INT
{
	ps.addString(getText());
	actualColumn+=getText().length();	
};
 
UID
:
	[uU] [+]? [0-9a-fA-F]+
;

ANYCHAR:
.+? {
ps.addString(getText());
actualColumn+=getText().length();
};