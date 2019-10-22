/******************************************************************************
 * Copyright (c) 2000-2019 Ericsson Telecom AB
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/org/documents/epl-2.0/EPL-2.0.html
 ******************************************************************************/
package org.eclipse.titanium.sonar.metrics;

import org.eclipse.titan.designer.AST.Module;
import org.eclipse.titan.designer.AST.TTCN3.definitions.Def_Altstep;
import org.eclipse.titan.designer.AST.TTCN3.definitions.Def_Function;
import org.eclipse.titan.designer.AST.TTCN3.definitions.Def_Testcase;
import org.eclipse.titanium.metrics.AltstepMetric;
import org.eclipse.titanium.metrics.FunctionMetric;
import org.eclipse.titanium.metrics.MetricData;
import org.eclipse.titanium.metrics.ModuleMetric;
import org.eclipse.titanium.metrics.TestcaseMetric;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.TransformerFactoryConfigurationError;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

public class SonarMetricsExporter {

	public void export(final MetricData metricData, final File file) throws ParserConfigurationException,
																			TransformerFactoryConfigurationError,
																			FileNotFoundException,
																			TransformerException {

		final DocumentBuilderFactory documentFactory = DocumentBuilderFactory.newInstance();
		final DocumentBuilder documentBuilder = documentFactory.newDocumentBuilder();
		final Document document = documentBuilder.newDocument();

        final Element projectElement = document.createElement("project");
		final Element nameElement = document.createElement("name");
		final String projectName = metricData.getProject().getName();
		nameElement.appendChild(document.createTextNode(projectName));
		projectElement.appendChild(nameElement);
		final Element modulesElement = document.createElement("modules");
		projectElement.appendChild(modulesElement);
		for (final Module module : metricData.getModules()) {
			final Element moduleElement = document.createElement("module");

			final Element projectRelativePathElement = document.createElement("projectRelativePath");
			final String projectRelativePath = module.getLocation().getFile().getProjectRelativePath().toPortableString();
			projectRelativePathElement.appendChild(document.createTextNode(projectRelativePath));
			moduleElement.appendChild(projectRelativePathElement);

			final Element linesOfCodeElement = document.createElement("linesOfCode");
			linesOfCodeElement.appendChild(document.createTextNode(metricData.get(ModuleMetric.LINES_OF_CODE, module).toString()));
			moduleElement.appendChild(linesOfCodeElement);

			//TODO: use "lines" node if needed

			final Element statementsElement = document.createElement("statements");
			statementsElement.appendChild(document.createTextNode(metricData.get(ModuleMetric.NOF_STATEMENTS, module).toString()));
			moduleElement.appendChild(statementsElement);

			final Element functionsElement = document.createElement("functions");
			functionsElement.appendChild(document.createTextNode(metricData.get(ModuleMetric.NOF_FUNCTIONS, module).toString()));
			moduleElement.appendChild(functionsElement);

			final Element altstepsElement = document.createElement("altsteps");
			altstepsElement.appendChild(document.createTextNode(metricData.get(ModuleMetric.NOF_ALTSTEPS, module).toString()));
			moduleElement.appendChild(altstepsElement);

			final Element testCasesElement = document.createElement("testCases");
			testCasesElement.appendChild(document.createTextNode(metricData.get(ModuleMetric.NOF_TESTCASES, module).toString()));
			moduleElement.appendChild(testCasesElement);

			final Element complexityElement = document.createElement("complexity");
			complexityElement.appendChild(document.createTextNode("" + calculateComplexity(metricData, module)));
			moduleElement.appendChild(complexityElement);

			modulesElement.appendChild(moduleElement);
		}
		document.appendChild(projectElement);

		final Transformer tr = TransformerFactory.newInstance().newTransformer();
        tr.setOutputProperty(OutputKeys.INDENT, "yes");
        tr.setOutputProperty(OutputKeys.METHOD, "xml");
        tr.setOutputProperty(OutputKeys.ENCODING, "UTF-8");
        tr.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "4");

        // send DOM to file
        tr.transform(new DOMSource(document), new StreamResult(new FileOutputStream(file)));
	}

	private int calculateComplexity(final MetricData metricData, final Module module) {
		int complexity = 0;
		for (final Def_Function function : metricData.getFunctions().get(module)) {
			complexity += metricData.get(FunctionMetric.CYCLOMATIC_COMPLEXITY, function).intValue();
		}

		for (final Def_Altstep altstep : metricData.getAltsteps().get(module)) {
			complexity += metricData.get(AltstepMetric.CYCLOMATIC_COMPLEXITY, altstep).intValue();
		}

		for (final Def_Testcase testcase : metricData.getTestcases().get(module)) {
			complexity += metricData.get(TestcaseMetric.CYCLOMATIC_COMPLEXITY, testcase).intValue();
		}
		return complexity;
	}
}
