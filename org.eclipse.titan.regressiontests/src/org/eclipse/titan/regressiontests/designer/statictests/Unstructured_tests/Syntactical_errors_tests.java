/******************************************************************************
 * Copyright (c) 2000-2020 Ericsson Telecom AB
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/org/documents/epl-2.0/EPL-2.0.html
 ******************************************************************************/
package org.eclipse.titan.regressiontests.designer.statictests.Unstructured_tests;

import java.util.ArrayList;

import org.eclipse.core.resources.IMarker;
import org.eclipse.titan.regressiontests.designer.Designer_plugin_tests;
import org.eclipse.titan.regressiontests.library.MarkerToCheck;
import org.junit.Test;

public class Syntactical_errors_tests {

	//SemanticErrors2_asn
	//SemanticErrors3_asn
	//ReturnValueTest.ttcn
	//SyntaxErrors.ttcn

	@Test
	public void SemanticErrors2_asn() throws Exception {
		Designer_plugin_tests.checkSyntaxMarkersOnFile(SemanticErrors2_asn_initializer(), "src/Unstructured_tests/SemanticErrors2.asn");
	}

	@Test
	public void SemanticErrors3_asn() throws Exception {
		Designer_plugin_tests.checkSyntaxMarkersOnFile(SemanticErrors3_asn_initializer(), "src/Unstructured_tests/SemanticErrors3.asn");
	}

	@Test
	public void ReturnValueTest_ttcn() throws Exception {
		Designer_plugin_tests.checkSyntaxMarkersOnFile(ReturnValueTest_ttcn_initializer(), "src/Unstructured_tests/ReturnValueTest.ttcn");
	}

	@Test
	public void SyntaxErrors_ttcn() throws Exception {
		Designer_plugin_tests.checkSyntaxMarkersOnFile(SyntaxErrors_ttcn_initializer(), "src/Unstructured_tests/SyntaxErrors.ttcn");
	}

	//===== Initializers =====

	private ArrayList<MarkerToCheck> SemanticErrors2_asn_initializer() {
		//SemanticErrors2.asn
		ArrayList<MarkerToCheck> markersToCheck = new ArrayList<MarkerToCheck>(1);
		int lineNum = 5;
		markersToCheck.add(new MarkerToCheck("`Semantic_errors3' is not a valid ASN.1 identifier. Did you mean `Semantic-errors3' ?",  lineNum, IMarker.SEVERITY_ERROR));

		return markersToCheck;
	}

	private ArrayList<MarkerToCheck> SemanticErrors3_asn_initializer() {
		//SemanticErrors3.asn
		ArrayList<MarkerToCheck> markersToCheck = new ArrayList<MarkerToCheck>(1);
		int lineNum = 20;
		markersToCheck.add(new MarkerToCheck("Duplicate named bit `first'",  lineNum, IMarker.SEVERITY_ERROR));

		return markersToCheck;
	}

	private ArrayList<MarkerToCheck> ReturnValueTest_ttcn_initializer() {
		//ReturnValueTest.ttcn
		ArrayList<MarkerToCheck> markersToCheck = new ArrayList<MarkerToCheck>(5);
		int lineNum = 37;
		markersToCheck.add(new MarkerToCheck("mismatched input '..' expecting {'action', 'activate', 'all', 'alt', 'and', 'and4b', 'any', 'break', 'connect', 'const', 'continue', 'deactivate', 'disconnect', 'do', 'execute', 'for', 'goto', 'if', 'ifpresent', 'interleave', 'kill', 'label', 'length', 'log', 'map', 'mod', 'mtc', 'or', 'or4b', 'port', 'rem', 'repeat', 'return', 'select', 'self', 'setverdict', 'stop', 'template', 'testcase', 'timer', 'unmap', 'var', 'while', 'xor', 'xor4b', 'int2enum', 'string2ttcn', IDENTIFIER, '@try', '@update', ';', '{', '}', '<', '>', '!=', '>=', '<=', '==', '+', '-', '*', '/', '<<', '>>', '<@', '@>', '&'}",  lineNum, IMarker.SEVERITY_ERROR));
		lineNum += 39;
		markersToCheck.add(new MarkerToCheck("mismatched input '..' expecting {'action', 'activate', 'all', 'alt', 'and', 'and4b', 'any', 'break', 'connect', 'const', 'continue', 'deactivate', 'disconnect', 'do', 'execute', 'for', 'goto', 'if', 'ifpresent', 'interleave', 'kill', 'label', 'length', 'log', 'map', 'mod', 'mtc', 'or', 'or4b', 'port', 'rem', 'repeat', 'return', 'select', 'self', 'setverdict', 'stop', 'template', 'testcase', 'timer', 'unmap', 'var', 'while', 'xor', 'xor4b', 'int2enum', 'string2ttcn', IDENTIFIER, '@try', '@update', ';', '{', '}', '<', '>', '!=', '>=', '<=', '==', '+', '-', '*', '/', '<<', '>>', '<@', '@>', '&'}",  lineNum, IMarker.SEVERITY_ERROR));
		lineNum += 9;
		markersToCheck.add(new MarkerToCheck("mismatched input '..' expecting {'and', 'and4b', 'ifpresent', 'length', 'mod', 'or', 'or4b', 'rem', 'xor', 'xor4b', ',', ')', '<', '>', '!=', '>=', '<=', '==', '+', '-', '*', '/', '<<', '>>', '<@', '@>', '&'}",  lineNum, IMarker.SEVERITY_ERROR));
		lineNum += 145;
		markersToCheck.add(new MarkerToCheck("no viable alternative at input '?'",  lineNum, IMarker.SEVERITY_ERROR));
		lineNum += 61;
		markersToCheck.add(new MarkerToCheck("no viable alternative at input '?'",  lineNum, IMarker.SEVERITY_ERROR));

		return markersToCheck;
	}

	private ArrayList<MarkerToCheck> SyntaxErrors_ttcn_initializer() {
		//SyntaxErrors.ttcn
		ArrayList<MarkerToCheck> markersToCheck = new ArrayList<MarkerToCheck>(11);
		int lineNum = 4;
		markersToCheck.add(new MarkerToCheck("Bitstring value contains invalid character",  lineNum, IMarker.SEVERITY_ERROR));
		markersToCheck.add(new MarkerToCheck("no viable alternative at input ''20'B'",  lineNum, IMarker.SEVERITY_ERROR));
		markersToCheck.add(new MarkerToCheck("Bitstring value contains invalid character",  ++lineNum, IMarker.SEVERITY_ERROR));
		markersToCheck.add(new MarkerToCheck("no viable alternative at input ''aA'B'",  lineNum, IMarker.SEVERITY_ERROR));
		markersToCheck.add(new MarkerToCheck("Invalid character `G' in binary string",  ++lineNum, IMarker.SEVERITY_ERROR));
		markersToCheck.add(new MarkerToCheck("Invalid character `x' in binary string",  ++lineNum, IMarker.SEVERITY_ERROR));
		markersToCheck.add(new MarkerToCheck("no viable alternative at input ''0'O'",  ++lineNum, IMarker.SEVERITY_ERROR));
		markersToCheck.add(new MarkerToCheck("Octetstring value contains odd number of hexadecimal digits",  lineNum, IMarker.SEVERITY_ERROR));
		markersToCheck.add(new MarkerToCheck("Invalid character `G' in binary string",  ++lineNum, IMarker.SEVERITY_ERROR));
		markersToCheck.add(new MarkerToCheck("no viable alternative at input ''0G'O'",  lineNum, IMarker.SEVERITY_ERROR));
		markersToCheck.add(new MarkerToCheck("Octetstring value contains odd number of hexadecimal digits",  lineNum, IMarker.SEVERITY_ERROR));

		return markersToCheck;
	}
}
