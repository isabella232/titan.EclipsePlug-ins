/******************************************************************************
 * Copyright (c) 2000-2018 Ericsson Telecom AB
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 ******************************************************************************/
package org.eclipse.titan.designer.actions;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.StringWriter;
import java.util.Map;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.filesystem.URIUtil;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.QualifiedName;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.titan.common.logging.ErrorReporter;
import org.eclipse.titan.common.path.PathConverter;
import org.eclipse.titan.common.path.TITANPathUtilities;
import org.eclipse.titan.designer.consoles.TITANDebugConsole;
import org.eclipse.titan.designer.core.ProjectBasedBuilder;
import org.eclipse.titan.designer.core.TITANBuilder;
import org.eclipse.titan.designer.core.TITANBuilderResourceVisitor;
import org.eclipse.titan.designer.license.LicenseValidator;
import org.eclipse.titan.designer.preferences.PreferenceConstants;
import org.eclipse.titan.designer.productUtilities.ProductConstants;
import org.eclipse.titan.designer.properties.data.DOMErrorHandlerImpl;
import org.eclipse.titan.designer.properties.data.MakefileCreationData;
import org.eclipse.titan.designer.properties.data.ProjectBuildPropertyData;
import org.eclipse.titan.designer.properties.data.ProjectFileHandler;
import org.eclipse.ui.IObjectActionDelegate;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.handlers.HandlerUtil;
import org.w3c.dom.DOMConfiguration;
import org.w3c.dom.DOMImplementation;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.bootstrap.DOMImplementationRegistry;
import org.w3c.dom.ls.DOMImplementationLS;
import org.w3c.dom.ls.LSOutput;
import org.w3c.dom.ls.LSParser;
import org.w3c.dom.ls.LSSerializer;

/**
 * @author Kristof Szabados
 * */
public final class GenerateBuilderInformation extends AbstractHandler implements IObjectActionDelegate {
	private static final String DOM_IMPLEMENTATION_SOURCE = "com.sun.org.apache.xerces.internal.dom.DOMImplementationSourceImpl";
	private static final String LOAD_SAVE_VERSION = "LS 3.0";
	private static final String XML_SCHEMA = "http://www.w3.org/2001/XMLSchema";

	private ISelection selection;

	@Override
	/** {@inheritDoc} */
	public void setActivePart(final IAction action, final IWorkbenchPart targetPart) {
		//Do nothing
	}

	@Override
	/** {@inheritDoc} */
	public void run(final IAction action) {
		if (!LicenseValidator.check()) {
			return;
		}

		if (!(selection instanceof IStructuredSelection)) {
			return;
		}

		final IStructuredSelection structSelection = (IStructuredSelection) selection;

		for (Object selected : structSelection.toList()) {
			if (selected instanceof IProject && TITANBuilder.isBuilderEnabled((IProject) selected)) {
				final IProject tempProject = (IProject) selected;
				try {
					generateInfoForProject(tempProject);
				} catch (CoreException e) {
					ErrorReporter.logExceptionStackTrace(e);
				}
			}
		}
	}

	private void generateInfoForProject(final IProject project) throws CoreException {
		final boolean win32 = Platform.OS_WIN32.equals(Platform.getOS());
		final boolean reportDebugInformation = Platform.getPreferencesService().getBoolean(ProductConstants.PRODUCT_ID_DESIGNER,
				PreferenceConstants.DISPLAYDEBUGINFORMATION, false, null);

		final DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
		factory.setNamespaceAware(true);
		DocumentBuilder builder;
		try {
			builder = factory.newDocumentBuilder();
		} catch (ParserConfigurationException e) {
			ErrorReporter.logExceptionStackTrace(e);
			return;
		}

		final DOMImplementation impl = builder.getDOMImplementation();
		final Document document = impl.createDocument(null, "TITAN_External_Builder_Information", null);

		final Element root = document.getDocumentElement();
		root.setAttribute("version", "1.0");

		String temp;
		Node node;
		final Element makefileSettings = document.createElement("Makefile_settings");
		root.appendChild(makefileSettings);
		for (int i = 0; i < MakefileCreationData.MAKEFILE_PROPERTIES.length; i++) {
			try {
				temp = project.getPersistentProperty(new QualifiedName(ProjectBuildPropertyData.QUALIFIER,
						MakefileCreationData.MAKEFILE_PROPERTIES[i]));
				node = document.createElement(MakefileCreationData.MAKEFILE_TAGS[i]);
				node.appendChild(document.createTextNode(temp));
				makefileSettings.appendChild(node);
			} catch (CoreException ce) {
				ErrorReporter.logExceptionStackTrace(ce);
			}
		}
		
		final String projectLocationStr = project.getLocation().toOSString();
		node = document.createElement("projectName");
		node.appendChild(document.createTextNode(project.getName()));

		makefileSettings.appendChild(node);
		node = document.createElement("projectRoot");
		node.appendChild(document.createTextNode(project.getLocationURI().toString()));
		makefileSettings.appendChild(node);

		temp = project.getPersistentProperty(new QualifiedName(ProjectBuildPropertyData.QUALIFIER, "workingDir"));
		node = document.createElement("workingDirectory");
		node.appendChild(document.createTextNode(TITANPathUtilities.resolvePathURI(temp, projectLocationStr).toString()));
		makefileSettings.appendChild(node);

		temp = project.getPersistentProperty(new QualifiedName(ProjectBuildPropertyData.QUALIFIER, "targetExecutable"));
		node = document.createElement("targetExecutable");
		node.appendChild(document.createTextNode(TITANPathUtilities.resolvePathURI(temp, projectLocationStr).toString()));
		makefileSettings.appendChild(node);

		temp = project.getPersistentProperty(new QualifiedName(ProjectBuildPropertyData.QUALIFIER, "makefileUpdateScript"));
		node = document.createElement("MakefileScript");
		node.appendChild(document.createTextNode(TITANPathUtilities.resolvePathURI(temp, projectLocationStr).toString()));
		makefileSettings.appendChild(node);

		temp = project.getPersistentProperty(new QualifiedName(ProjectBuildPropertyData.QUALIFIER, "makefileFlags"));
		node = document.createElement("MakefileFlags");
		node.appendChild(document.createTextNode(temp));
		makefileSettings.appendChild(node);

		final Element projectsElement = document.createElement("ReferencedProjects");
		root.appendChild(projectsElement);
		final IProject[] referencedProjects = ProjectBasedBuilder.getProjectBasedBuilder(project).getReferencedProjects();
		for (IProject tempProject : referencedProjects) {
			final Element element = document.createElement("ReferencedProject");
			element.setAttribute("name", tempProject.getName());
			element.setAttribute("location", tempProject.getLocationURI().toString());
			if (win32 && tempProject.getLocation() != null) {
				final String converted = PathConverter.convert(tempProject.getLocation().toOSString(), reportDebugInformation,
						TITANDebugConsole.getConsole());
				final Path path = new Path(converted);
				element.setAttribute("cygwinPath", URIUtil.toURI(path).toString());
			}
			projectsElement.appendChild(element);
		}

		final Element filesElement = document.createElement("Files");
		root.appendChild(filesElement);
		final TITANBuilderResourceVisitor visitor = ProjectBasedBuilder.getProjectBasedBuilder(project).getResourceVisitor();
		final Map<String, IFile> files = visitor.getFiles();
		for (IFile file : files.values()) {
			final Element element = document.createElement("File");
			element.setAttribute("path", file.getLocationURI().toString());
			if (win32 && file.getLocation() != null) {
				final String fileLocation = file.getLocation().toOSString();
				final String converted = PathConverter.convert(fileLocation, reportDebugInformation,
						TITANDebugConsole.getConsole());
				if (!converted.equals(fileLocation)) {
					final Path path = new Path(converted);
					element.setAttribute("cygwinPath", URIUtil.toURI(path).toString());
				}
			}

			element.setAttribute("relativePath", org.eclipse.core.runtime.URIUtil.makeRelative(file.getLocationURI(), project.getLocationURI())
					.toString());
			filesElement.appendChild(element);
		}

		final Map<String, IFile> contralStorageFiles = visitor.getCentralStorageFiles();
		for (IFile file : contralStorageFiles.values()) {
			final Element element = document.createElement("File");
			final String fileLocation = file.getLocation().toString();
			element.setAttribute("path", fileLocation);
			if (win32 && file.getLocation() != null) {
				final String converted = PathConverter.convert(fileLocation, reportDebugInformation,
						TITANDebugConsole.getConsole());
				if (!converted.equals(fileLocation)) {
					final Path path = new Path(converted);
					element.setAttribute("cygwinPath", URIUtil.toURI(path).toString());
				}
			}
			element.setAttribute("relativePath", org.eclipse.core.runtime.URIUtil.makeRelative(file.getLocationURI(), project.getLocationURI())
					.toString());
			element.setAttribute("centralStorage", "true");
			filesElement.appendChild(element);
		}

		final Map<String, IFile> filesOfReferencedProjects = ProjectBasedBuilder.getProjectBasedBuilder(project).getFilesofReferencedProjects();
		for (IFile file : filesOfReferencedProjects.values()) {
			final Element element = document.createElement("File");
			element.setAttribute("path", file.getLocationURI().toString());
			if (win32 && file.getLocation() != null) {
				final String fileLocation = file.getLocation().toOSString();
				final String converted = PathConverter.convert(fileLocation, reportDebugInformation,
						TITANDebugConsole.getConsole());
				if (!converted.equals(fileLocation)) {
					final Path path = new Path(converted);
					element.setAttribute("cygwinPath", URIUtil.toURI(path).toString());
				}
			}
			element.setAttribute("relativePath", org.eclipse.core.runtime.URIUtil.makeRelative(file.getLocationURI(), project.getLocationURI())
					.toString());
			element.setAttribute("fromProject", file.getProject().getName());
			filesElement.appendChild(element);
		}

		ProjectFileHandler.indentNode(document, document.getDocumentElement(), 1);

		System.setProperty(DOMImplementationRegistry.PROPERTY, DOM_IMPLEMENTATION_SOURCE);
		DOMImplementationRegistry registry = null;
		try {
			registry = DOMImplementationRegistry.newInstance();
		} catch (ClassNotFoundException ce) {
			ErrorReporter.logExceptionStackTrace(ce);
			return;
		} catch (InstantiationException ie) {
			ErrorReporter.logExceptionStackTrace(ie);
			return;
		} catch (IllegalAccessException iae) {
			ErrorReporter.logExceptionStackTrace(iae);
			return;
		}
		// Specifying "LS 3.0" in the features list ensures that the
		// DOMImplementation
		// object implements the load and save features of the DOM 3.0
		// specification.
		final DOMImplementation domImpl = registry.getDOMImplementation(LOAD_SAVE_VERSION);
		final DOMImplementationLS domImplLS = (DOMImplementationLS) domImpl;
		// If the mode is MODE_SYNCHRONOUS, the parse and parseURI
		// methods of
		// the LSParser
		// object return the org.w3c.dom.Document object. If the mode is
		// MODE_ASYNCHRONOUS,
		// the parse and parseURI methods return null.
		final LSParser parser = domImplLS.createLSParser(DOMImplementationLS.MODE_SYNCHRONOUS, XML_SCHEMA);
		final DOMConfiguration config = parser.getDomConfig();
		final DOMErrorHandlerImpl errorHandler = new DOMErrorHandlerImpl();
		config.setParameter("error-handler", errorHandler);
		config.setParameter("validate", Boolean.TRUE);
		config.setParameter("schema-type", XML_SCHEMA);
		config.setParameter("validate-if-schema", Boolean.TRUE);
		final LSSerializer dom3Writer = domImplLS.createLSSerializer();
		final LSOutput output = domImplLS.createLSOutput();

		final IFile propertiesFile = project.getFile('/' + "external_build_information.xml");
		final File file = propertiesFile.getLocation().toFile();
		StringWriter sw = null;
		try {
			propertiesFile.refreshLocal(IResource.DEPTH_ZERO, null);
			sw = new StringWriter();
			output.setCharacterStream(sw);
			output.setEncoding("UTF-8");
			dom3Writer.write(document, output);
			final String temporaloutput = sw.getBuffer().toString();

			// temporalStorage will hold the contents of the
			// existing .TITAN_properties file
			String temporalStorage = null;

			if (propertiesFile.isAccessible() && file.exists() && file.canRead()) {
				final InputStream is = propertiesFile.getContents(true);
				final BufferedReader br = new BufferedReader(new InputStreamReader(is));
				final StringBuilder sb = new StringBuilder();
				boolean firstLine = true;
				String line = br.readLine();
				while (line != null) {
					if (firstLine) {
						firstLine = false;
					} else {
						sb.append('\n');
					}
					sb.append(line);
					line = br.readLine();
				}
				temporalStorage = sb.toString();
				br.close();
			}

			// If there is a difference between the old
			// .TITAN_properties file and the one to be written the
			// old
			// one will be overwritten by the new one.
			if (temporalStorage == null || !temporalStorage.equals(temporaloutput)) {
				if (file.exists()) {
					propertiesFile.setContents(new ByteArrayInputStream(temporaloutput.getBytes()), IResource.FORCE
							| IResource.KEEP_HISTORY, null);
				} else {
					propertiesFile.create(new ByteArrayInputStream(temporaloutput.getBytes()), IResource.FORCE, null);
				}
				try {
					propertiesFile.refreshLocal(IResource.DEPTH_ZERO, null);
				} catch (CoreException e) {
					ErrorReporter.logExceptionStackTrace(e);
				}
			}

		} catch (IOException e) {
			ErrorReporter.logExceptionStackTrace(e);
		} catch (CoreException e) {
			ErrorReporter.logExceptionStackTrace(e);
		} finally {
			if (sw != null) {
				try {
					sw.close();
				} catch (IOException e) {
					ErrorReporter.logExceptionStackTrace(e);
				}
			}
		}
	}

	@Override
	/** {@inheritDoc} */
	public void selectionChanged(final IAction action, final ISelection selection) {
		this.selection = selection;
	}

	@Override
	/** {@inheritDoc} */
	public Object execute(final ExecutionEvent event) throws ExecutionException {
		if (!LicenseValidator.check()) {
			return null;
		}

		selection = HandlerUtil.getActiveWorkbenchWindow(event).getActivePage().getSelection();
		if (!(selection instanceof IStructuredSelection)) {
			return null;
		}

		final IStructuredSelection structSelection = (IStructuredSelection) selection;

		for (Object selected : structSelection.toList()) {
			if (selected instanceof IProject && TITANBuilder.isBuilderEnabled((IProject) selected)) {
				final IProject tempProject = (IProject) selected;
				try {
					generateInfoForProject(tempProject);
				} catch (CoreException e) {
					ErrorReporter.logExceptionStackTrace(e);
				}
			}
		}
		return null;
	}

}
