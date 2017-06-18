package org.eclipse.titan.regressiontests.designer.statictests.Basic_tests;

import java.util.ArrayList;

import org.eclipse.core.resources.IMarker;
import org.eclipse.titan.regressiontests.designer.Designer_plugin_tests;
import org.eclipse.titan.regressiontests.library.MarkerToCheck;

public class AST_Syntax_warning_tests {
	//ttcnpp_test_main_ttcn
	//expression_tests_ttcn
	//namingConvention_ttcn
	//t3doc_explicit_negative_test_ttcn
	//t3doc_explicit_test_ttcn

	@org.junit.Test
	public void ttcnpp_test_main_ttcnpp() throws Exception {
		Designer_plugin_tests.checkSyntaxMarkersOnFile(ttcnpp_test_main_ttcnpp_initializer(), "src/Basic_tests/preprocessor_test/ttcnpp_test_main.ttcnpp");
	}

	@org.junit.Test
	public void expression_tests_ttcn() throws Exception {
		Designer_plugin_tests.checkSyntaxMarkersOnFile(expression_tests_ttcn_initializer(), "src/Basic_tests/expression_tests.ttcn");
	}

	@org.junit.Test
	public void namingConvention_ttcn() throws Exception {
		Designer_plugin_tests.checkSyntaxMarkersOnFile(namingConvention_ttcn_initializer(), "src/Basic_tests/namingConvention.ttcn");
	}

	@org.junit.Test
	public void t3doc_explicit_negative_test_ttcn() throws Exception {
		Designer_plugin_tests.checkSyntaxMarkersOnFile(t3doc_explicit_negative_test_ttcn_initializer(), "src/Basic_tests/t3doc_explicit_negative_test.ttcn");
	}

	@org.junit.Test
	public void t3doc_explicit_test_ttcn() throws Exception {
		Designer_plugin_tests.checkSyntaxMarkersOnFile(t3doc_explicit_test_ttcn_initializer(), "src/Basic_tests/t3doc_explicit_test.ttcn");
	}

	private ArrayList<MarkerToCheck> ttcnpp_test_main_ttcnpp_initializer() {
		//ttcnpp_test_main.ttcnpp
		ArrayList<MarkerToCheck> markersToCheck = new ArrayList<MarkerToCheck>(2);
		int lineNum = 15;
		markersToCheck.add(new MarkerToCheck("Preprocessor directive #line is ignored",  lineNum, IMarker.SEVERITY_WARNING));
		lineNum += 5;
		markersToCheck.add(new MarkerToCheck("Preprocessor directive #line is ignored",  lineNum, IMarker.SEVERITY_WARNING));

		return markersToCheck;
	}

	private ArrayList<MarkerToCheck> expression_tests_ttcn_initializer() {
		//expression_tests.ttcn
		ArrayList<MarkerToCheck> markersToCheck = new ArrayList<MarkerToCheck>();
		int lineNum = 17;
		markersToCheck.add(new MarkerToCheck("External constants are deprecated and may be fully removed in a future edition of the TTCN-3 standard ",  lineNum, IMarker.SEVERITY_WARNING));

		return markersToCheck;
	}

	private ArrayList<MarkerToCheck> namingConvention_ttcn_initializer() {
		//namingConvention.ttcn
		ArrayList<MarkerToCheck> markersToCheck = new ArrayList<MarkerToCheck>();
		int lineNum = 19;
		int i = 0;
		for (i = 0; i < 2; i++) {
			markersToCheck.add(new MarkerToCheck("External constants are deprecated and may be fully removed in a future edition of the TTCN-3 standard ", lineNum++, IMarker.SEVERITY_WARNING));
		}

		return markersToCheck;
	}

	 private ArrayList<MarkerToCheck> t3doc_explicit_negative_test_ttcn_initializer() {
		//t3doc_explicit_negative_test.ttcn
		ArrayList<MarkerToCheck> markersToCheck = new ArrayList<MarkerToCheck>();
		int lineNum = 68;
		markersToCheck.add(new MarkerToCheck("Group style definition of module parameters is deprecated and may be fully removed in a future edition of the TTCN-3 standard ",  lineNum, IMarker.SEVERITY_WARNING));

		return markersToCheck;
	}

	 private ArrayList<MarkerToCheck> t3doc_explicit_test_ttcn_initializer() {
		//t3doc_explicit_test.ttcn
		ArrayList<MarkerToCheck> markersToCheck = new ArrayList<MarkerToCheck>();
		int lineNum = 51;
		markersToCheck.add(new MarkerToCheck("Group style definition of module parameters is deprecated and may be fully removed in a future edition of the TTCN-3 standard ",  lineNum, IMarker.SEVERITY_WARNING));

		return markersToCheck;
	}
}
