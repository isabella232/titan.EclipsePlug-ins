/******************************************************************************
 * Copyright (c) 2000-2018 Ericsson Telecom AB
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/org/documents/epl-2.0/EPL-2.0.html
 ******************************************************************************/

public class Error_Message {

	String text;
	int line;
	String filename;
	boolean isempty = false;
	
	public Error_Message(String p_text, int p_line, String p_filename, boolean p_isempty)
	{
		text = p_text;
		line = p_line;
		filename = p_filename;
		isempty = p_isempty; 
	}
		

}
