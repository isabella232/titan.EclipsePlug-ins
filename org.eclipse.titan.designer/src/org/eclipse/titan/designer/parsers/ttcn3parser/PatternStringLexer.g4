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
WS
:
	[ \t\n\x0B\f\r]+ -> channel(HIDDEN)
;

NEWLINE
:
	[\r\n]
;

IDENTIFIER
:
	[A-Za-z] [A-Za-z0-9_]*
;

UID
:
	[uU] [+]? [0-9a-fA-F]+
;
DIGIT
:
	[0-9]
;
INT
:
	[1-9] DIGIT*
	| '0'
;

NUMBER
:
	INT
;
/* ***************** rules ************************* */
REFERENCE_RULE : '{' WS? IDENTIFIER (( WS? '.' WS? IDENTIFIER ) | ( WS? '[' WS? ( IDENTIFIER | NUMBER ) WS? ']'))* WS? '}' {
	if (in_set) {
		// in a set a reference (without the \N at start) is just simple text,
  		// the matched token does not contain characters that are special in a set
  		
  		ps.addString(tokenStr);
	} else {
		List<String> identifiers = new ArrayList<String>();
		List<Integer> beginnings = new ArrayList<Integer>();
		/*for(;;) {
			
			}*/ 
	}
	
}
;

INVALID_REFERENCE_RULE : '{' [ ^} ]* '}' {
  Location location = new Location(actualFile, actualLine, startToken.getStartIndex(), startToken.getStopIndex());
  location.reportSyntacticError(String.format("Invalid reference expression: %s", tokenStr));	
};

REFERENCE_WITH_N : '\\N' (WS)? '{' (WS)? IDENTIFIER (WS)? '}' {
	int id_begin = 3;
	while(!Character.isAlphabetic(tokenStr.charAt(id_begin))) {
		id_begin++;
		}
	int id_end = id_begin;
	while(Character.isLetterOrDigit(tokenStr.charAt(id_end)) || tokenStr.charAt(id_end) == '_') {
		id_end++;
		}
	String id_str = tokenStr.substring(id_begin, id_end);
	Location location = new Location(actualFile, actualLine, startToken.getStartIndex() + id_begin, id_end);
	Reference ref = new Reference(new Identifier(Identifier_type.ID_TTCN, id_str, location));
	ps.addRef(ref, true);
	
	ref.setLocation(location);
	if (in_set) {
			location.reportSyntacticWarning(String.format("Character set reference \\N{%s} is not supported, dropped out from the set", id_str));
		}
};

UNIVERSAL_CHARSTRING_REFERENCE : '\\N' WS? '{' WS? 'universal' WS? 'charstring' WS? '}' {
	/* The third {WS?} is optional but if it's empty then the previous rule catches it*/
	final String id_str = "universal charstring";
 
	Location location = new Location(actualFile, actualLine, startToken.getStartIndex(), startToken.getStopIndex());
	Reference ref = new Reference(new Identifier(Identifier_type.ID_TTCN, id_str, location));


	ps.addRef(ref, true); 
	ref.setLocation(location);
	if (in_set) {
		location.reportSyntacticWarning(String.format("Character set reference \\N{%s} is not supported, dropped out from the set", id_str));
	} 
};

INVALID_SET_REFERENCE_RULE : '\\N' WS? '{' [^}]* '}' {
  Location location = new Location(actualFile, actualLine, startToken.getStartIndex(), startToken.getStopIndex());
  location.reportSyntacticError(String.format("Invalid character set reference: %s", tokenStr));
};

QUADRUPLE_RULE : '\\q'  WS? '{' WS? NUMBER WS? ',' WS? NUMBER WS? ',' WS? NUMBER  WS? ',' WS? NUMBER WS? '}' {
/* quadruple - group */
			int group_begin = 3;
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
				Location group_location = new Location(actualFile, actualLine, startToken.getStartIndex() + group_begin, startToken.getStartIndex() + group_end);  
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
				Location plane_location = new Location(actualFile, actualLine, startToken.getStartIndex() + plane_begin, startToken.getStartIndex() + plane_end);  
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
				Location row_location = new Location(actualFile, actualLine, startToken.getStartIndex() + row_begin, startToken.getStartIndex() + row_end);  
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
				Location cell_location = new Location(actualFile, actualLine, startToken.getStartIndex() + cell_begin, startToken.getStartIndex() + cell_end);  
				cell_location.reportSemanticError(String.format("The fourth number of quadruple (cell) must be within the range 0 .. 255 instead of %s", cell_str));
				cell = cell < 0 ? 0 : 255; 
			}
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
				ps.addString(String.format("\\q{%s, %s, %s, %s}", group, plane, row, cell));
			}
};

UID_RULE : '\\q' ( WS? '{' WS? ( UID WS? ',' WS? )* UID WS? '}') {
//Split UID-s. For example: \q{ U23423 , U+001 } -> [U23423, U+001] 
	int begin = 3; 
	List<String> uids = new ArrayList<String>();
	while(tokenStr.charAt(begin) != '}') {
		//Find first digit
		while(tokenStr.charAt(begin) != 'U' && tokenStr.charAt(begin) != 'u') {
			begin++; 		
		}
		int end = begin + 2; 
		//Find last digit
		while(Character.isDigit(tokenStr.charAt(end))) {
			end++;
		}
		uids.add(tokenStr.substring(begin, end)); 
		//Skip whitespaces until the next UID or the end
		while(!Character.isDigit(tokenStr.charAt(end)) && tokenStr.charAt(end) != 'U' && tokenStr.charAt(end) != 'u' && tokenStr.charAt(end) != '}') {
			 end++;
		}
		begin = end; 	
	}
	ps.addStringUSI(uids);
	//Free
	uids = null;
};

INVALID_QUADRUPLE_UID_RULE : '\\q' ( WS? '{' [ ^}]* '}')? {
  Location location = new Location(actualFile, actualLine, startToken.getStartIndex(), startToken.getStopIndex());
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
};
 
COMPLEMENT: '[^]' {
  if(in_set) {
    ps.addString("\\[\\^]");
    in_set = false;
  } else {
    ps.addString("[^\\]");
    in_set = true;
  }
};

OPENING_SQUARE_BRACKET : '[' {
  if(in_set) {
    ps.addString("\\[");
  } else {
    ps.addChar('[');
    in_set = true;
  }
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
};
/* 
SQUARE_BRACES : '{'|'}' {
  Location location = new Location(actualFile, actualLine, startToken.getStartIndex(), startToken.getStopIndex() + 1);
  location.reportSyntacticWarning(String.format("Unmatched %c was treated literally", tokenStr.charAt(0)));
  ps.addString("\\");
  ps.addChar(tokenStr.charAt(0));
};
*/
QUOTE_MARKS : '\\\"' |'\"\"' {
  ps.addChar('"');
};
 
/* \metachars and escaped metachars  
METACHARS : [dwtnrsb?*\\\[\]\-\^|()#+] {
 ps.addString(tokenStr);
}; 
 */
/*UNRECOGNIZED_ESCAPE_SEQUENCE : '\\'(.| NEWLINE) {
 Location location = new Location(actualFile, actualLine, startToken.getStartIndex(), startToken.getStartIndex() + 1);
 if (!Character.isISOControl(tokenStr.charAt(1)) && !Character.isWhitespace((tokenStr.charAt(1)))) {
 	location.reportSyntacticWarning(String.format("Use of unrecognized escape sequence `\\%c' is deprecated", tokenStr.charAt(1)));
 } else {
 	location.reportSyntacticWarning("Use of unrecognized escape sequence is deprecated");
 }
 ps.addString(tokenStr.substring(1,tokenStr.length() - 1));
}; */
REPETITION : '#' WS? [0-9] {
	if (in_set) {
		Location location = new Location(actualFile, actualLine, startToken.getStartIndex(), startToken.getStopIndex());
		location.reportSemanticError("Number of repetitions `#n' cannot be given inside a set expression");
	} else if (tokenStr.charAt(tokenStr.length() - 1) != '1')  {
		ps.addChar('#'); 
		ps.addChar(tokenStr.charAt(tokenStr.length() - 1));
 	}
}
;
 
N_REPETITION: '#' WS? '(' WS? NUMBER WS? ')' {
	if (in_set) {
		Location location = new Location(actualFile, actualLine, startToken.getStartIndex(), startToken.getStopIndex());
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
 			Location number_location = new Location(actualFile, actualLine, startToken.getStartIndex() + number_begin, startToken.getStartIndex() + number_end);
 			number_location.reportSemanticError(String.format("A non-negative integer value was expected as the number of repetitions instead of %s", number_str));
 		} else if (number != 1) {
			ps.addString("#(" + number_str + ")"); 
 		}
 	}
}; 

N_M_REPETITION : '#' WS? '(' WS? NUMBER WS? ',' WS? NUMBER WS? ')' {
	if (in_set) {
		Location location = new Location(actualFile, actualLine, startToken.getStartIndex(), startToken.getStopIndex());
		location.reportSemanticError("Number of repetitions `#(n,m)' cannot be given inside a set expression");		 
	}  
 	int lower_begin = 2;
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
	Location upper_location = new Location(actualFile, actualLine, startToken.getStartIndex() + upper_begin, startToken.getStartIndex() + upper_end);
	int upper = Integer.parseInt(upper_str);
	if (upper < 0) {
		upper_location.reportSemanticError(String.format("A non-negative integer value was expected as the maximum number of repetitions instead of %s", upper_str));
	} else if (lower > upper) {
		Location location = new Location(actualFile, actualLine, startToken.getStartIndex(), startToken.getStopIndex());
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
}; 

N_M_REPETITION_WITHOUT_M : '#' WS? '(' WS? NUMBER WS? ',' WS? ')' {
  if (in_set) {
	Location location = new Location(actualFile, actualLine, startToken.getStartIndex(), startToken.getStopIndex());
    location.reportSemanticError("Number of repetitions `#(n,)' cannot be given inside a set expression");
  } else {
    int lower_begin = 2;
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
  }
};

N_M_REPETITION_WITHOUT_N : '#' WS? '(' WS? ',' WS? NUMBER WS? ')' {
  if (in_set) {
  	Location location = new Location(actualFile, actualLine, startToken.getStartIndex(), startToken.getStopIndex());
    location.reportSemanticError("Number of repetitions `#(,m)' cannot be given inside a set expression");
  } else {
    int upper_begin = 3;
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
  }
}; 

EMPTY_REPETITION : '#' (WS)? '(' WS? ',' WS? ')' {
 if (in_set) {
 	Location location = new Location(actualFile, actualLine, startToken.getStartIndex(), startToken.getStopIndex());
 	location.reportSyntacticError("Number of repetitions `#(,)' cannot be given inside a set expression");
  } else { 
  	ps.addString("#(,)");
  }
}; 

 
 
INVALID_NUMBER_REPETITION : '#' WS? '(' [^)]* ')' {
 Location location = new Location(actualFile, actualLine, startToken.getStartIndex(), startToken.getStopIndex());
 location.reportSyntacticError(String.format("Invalid notation for the number of repetitions: %s", tokenStr));
}; 
 
HASHMARK : '#' {
	Location location = new Location(actualFile, actualLine, startToken.getStartIndex(), startToken.getStopIndex());
	if (in_set) {
		location.reportSyntacticWarning("Unescaped `#' inside character set was treated literally");
		ps.addChar('\\');
		ps.addChar('#');
		} else {
			location.reportSyntacticError("Syntax error in the number of repetitions `#...'");
			}
};

PLUS : '+' {
  if (in_set) {
	Location location = new Location(actualFile, actualLine, startToken.getStartIndex(), startToken.getStopIndex());
    location.reportSyntacticWarning("Unescaped `+' inside character set was treated literally");
    ps.addChar('\\');
  }
  ps.addChar('+');
};

DOT_OR_NEWLINE : .| NEWLINE {
  ps.addString(tokenStr);
};
 

