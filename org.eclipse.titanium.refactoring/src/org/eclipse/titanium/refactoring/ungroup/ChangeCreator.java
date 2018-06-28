/******************************************************************************
 * Copyright (c) 2000-2018 Ericsson Telecom AB
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/org/documents/epl-2.0/EPL-2.0.html
 ******************************************************************************/
package org.eclipse.titanium.refactoring.ungroup;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.NavigableSet;
import java.util.TreeSet;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.WorkspaceJob;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.ltk.core.refactoring.Change;
import org.eclipse.ltk.core.refactoring.Refactoring;
import org.eclipse.ltk.core.refactoring.TextFileChange;
import org.eclipse.text.edits.DeleteEdit;
import org.eclipse.text.edits.InsertEdit;
import org.eclipse.text.edits.MultiTextEdit;
import org.eclipse.titan.common.logging.ErrorReporter;
import org.eclipse.titan.designer.AST.ASTVisitor;
import org.eclipse.titan.designer.AST.IVisitableNode;
import org.eclipse.titan.designer.AST.Location;
import org.eclipse.titan.designer.AST.Module;
import org.eclipse.titan.designer.AST.TTCN3.definitions.Def_ModulePar;
import org.eclipse.titan.designer.AST.TTCN3.definitions.Def_ModulePar_Template;
import org.eclipse.titan.designer.AST.TTCN3.definitions.Definition;
import org.eclipse.titan.designer.parsers.CompilationTimeStamp;
import org.eclipse.titan.designer.parsers.GlobalParser;
import org.eclipse.titan.designer.parsers.ProjectSourceParser;

/**
 * This class is only instantiated by the {@link UngroupModuleparRefactoring}.
 * <p>
 * By passing the selection through the constructor and calling {@link ChangeCreator#perform()}, this class
 *  creates a {@link Change} object, which can be returned by the standard
 *  {@link Refactoring#createChange(IProgressMonitor)} method in the refactoring class.
 *
 * @author Nagy Mátyás
 */
public class ChangeCreator {

	// in
	private final IFile selectedFile;

	// out
	private Change change;

	ChangeCreator(final IFile selectedFile) {
		this.selectedFile = selectedFile;
	}

	public Change getChange() {
		return change;
	}

	/**
	 * Creates the {@link #change} object, which contains all the inserted ungrouped module parameters and
	 * deleted grouped module parameters in the selected resources.
	 * */
	public void perform() {
		if (selectedFile == null) {
			return;
		}

		change = createFileChange(selectedFile);
	}

	private Change createFileChange(final IFile toVisit) {
		if (toVisit == null) {
			return null;
		}

		final ProjectSourceParser sourceParser = GlobalParser.getProjectSourceParser(toVisit.getProject());
		final Module module = sourceParser.containedModule(toVisit);
		if (module == null) {
			return null;
		}

		// find all locations in the module that should be edited
		final DefinitionVisitor vis = new DefinitionVisitor();
		module.accept(vis);
		final NavigableSet<Definition> nodes = vis.getLocations();
		if (nodes.isEmpty()) {
			return null;
		}

		// calculate edit locations
		final List<Definition> locations = new ArrayList<Definition>();
		try {
			final WorkspaceJob job1 = calculateEditLocations(nodes, toVisit, locations);
			job1.join();
		} catch (InterruptedException ie) {
			ErrorReporter.logExceptionStackTrace(ie);
		} catch (CoreException ce) {
			ErrorReporter.logError("UngroupModuleparRefactoring/CreateChange.createFileChange(): "
					+ "CoreException while calculating edit locations. ");
			ErrorReporter.logExceptionStackTrace(ce);
		}
		if (locations.isEmpty()) {
			return null;
		}

		// create a change for each edit location
		final TextFileChange tfc = new TextFileChange(toVisit.getName(), toVisit);
		final MultiTextEdit rootEdit = new MultiTextEdit();
		tfc.setEdit(rootEdit);

		int precedeOffset = -1;
		final String fileContents = loadFileContent(toVisit);

		for (Definition node : locations) {

			final Location l = node.getCumulativeDefinitionLocation();
			final Location typeLocation = node.getType(CompilationTimeStamp.getBaseTimestamp()).getLocation();
			final Location identifierLocation = node.getIdentifier().getLocation();

			if (precedeOffset != l.getOffset()) {
				precedeOffset = l.getOffset();
				final int len = l.getEndOffset() - l.getOffset();
				rootEdit.addChild(new DeleteEdit(l.getOffset(), len + 1));

			}

			String typeText = fileContents.substring(typeLocation.getOffset(), typeLocation.getEndOffset()).trim();
			String name = fileContents.substring(identifierLocation.getOffset(), identifierLocation.getEndOffset()).trim();
			String newModulePar = "";
			if (node instanceof Def_ModulePar) {
				Def_ModulePar modulePar = (Def_ModulePar) node;
				if (modulePar.getDefaultValue() != null) {
					final Location valueLocation = modulePar.getDefaultValue().getLocation();
					String valueText = fileContents.substring(valueLocation.getOffset(), valueLocation.getEndOffset()).trim();
					newModulePar = "modulepar " + typeText + " " + name + " := " + valueText + ";\n";
				} else {
					newModulePar = "modulepar " + typeText + " "  + name + ";\n";
				}
				
			} else if (node instanceof Def_ModulePar_Template) {
				Def_ModulePar_Template modulePar = (Def_ModulePar_Template) node;
				if (modulePar.getDefaultTemplate() != null) {
					final Location valueLocation = modulePar.getDefaultTemplate().getLocation();
					String temlateText = fileContents.substring(valueLocation.getOffset(), valueLocation.getEndOffset()).trim();
					newModulePar = "modulepar template " + typeText + " "  + name + " := " + temlateText + ";\n";
				} else {
					newModulePar = "modulepar template " + typeText + " "  + name + ";\n";
				}
			}

			rootEdit.addChild(new InsertEdit(l.getOffset(), newModulePar));

		}

		return tfc;
	}

	private WorkspaceJob calculateEditLocations(final NavigableSet<Definition> nodes, final IFile file, final List<Definition> locations_out)
			throws CoreException {
		final WorkspaceJob job = new WorkspaceJob("UngroupModuleparRefactoring: calculate edit locations") {

			@Override
			public IStatus runInWorkspace(final IProgressMonitor monitor) throws CoreException {
				int precedeOffset = -1;
				Definition precedeNode = null;

				for (Definition node : nodes) {

					if (node.getCumulativeDefinitionLocation().getOffset() == precedeOffset) {
						locations_out.add(precedeNode);
					} else {
						if (locations_out.size() != 0) {
							final Definition lastInserted = locations_out.get(locations_out.size() - 1);

							if (precedeNode.getCumulativeDefinitionLocation().getOffset() == lastInserted
									.getCumulativeDefinitionLocation().getOffset()) {
								locations_out.add(precedeNode);
							}
						}
					}

					precedeOffset = node.getCumulativeDefinitionLocation().getOffset();
					precedeNode = node;

				}
				if (locations_out.size() != 0) {
					final Definition lastnode = nodes.last();
					final Definition lastInserted = locations_out.get(locations_out.size() - 1);

					if (lastnode.getCumulativeDefinitionLocation().getOffset() == lastInserted.getCumulativeDefinitionLocation()
							.getOffset()) {
						locations_out.add(nodes.last());
					}
				}

				return Status.OK_STATUS;
			}
		};
		job.setUser(true);
		job.schedule();
		return job;
	}

	/**
	 * Collects the locations of all the modulepar definitions in a module.
	 * <p>
	 * Call on modules.
	 * */
	private static class DefinitionVisitor extends ASTVisitor {

		private final NavigableSet<Definition> locations;

		DefinitionVisitor() {
			locations = new TreeSet<Definition>(new LocationComparator());
		}

		private NavigableSet<Definition> getLocations() {
			return locations;
		}

		@Override
		public int visit(final IVisitableNode node) {

			if (node instanceof Def_ModulePar) {
				final Def_ModulePar d = (Def_ModulePar) node;
				if (d.getCumulativeDefinitionLocation() != null) {
					if (hasValidLocation(d)) {
						locations.add(d);
					}
				}
			} else if (node instanceof Def_ModulePar_Template) {
				final Def_ModulePar_Template d = (Def_ModulePar_Template) node;
				if (d.getCumulativeDefinitionLocation() != null) {
					if (hasValidLocation(d)) {
						locations.add(d);
					}
				}
			}

			return V_CONTINUE;
		}

		private boolean hasValidLocation(final Definition def) {
			final Location location = def.getLocation();
			return location != null && location.getOffset() >= 0 && location.getEndOffset() >= 0;
		}

	}

	/**
	 * Compares {@link Def_ModulePar}s by comparing the file paths as strings.
	 * If the paths are equal, the two offset integers are compared.
	 * */
	private static class LocationComparator implements Comparator<Definition> {

		@Override
		public int compare(final Definition arg0, final Definition arg1) {
			final IResource f0 = arg0.getLocation().getFile();
			final IResource f1 = arg1.getLocation().getFile();
			if (!f0.equals(f1)) {
				return f0.getFullPath().toString().compareTo(f1.getFullPath().toString());
			}
			final int o0 = arg0.getLocation().getOffset();
			final int o1 = arg1.getLocation().getOffset();

			// When getLocation will implement correctly this part of code not needed (the full if-else statements)
			// and the commented code (under this statement) will be suitable.
			if (o0 == o1) {

				if (arg0 instanceof Def_ModulePar && arg1 instanceof Def_ModulePar) {
					final String s0 = ((Def_ModulePar) arg0).getIdentifier().getDisplayName();
					final String s1 = ((Def_ModulePar) arg1).getIdentifier().getDisplayName();

					return s0.compareTo(s1);
				} else if (arg0 instanceof Def_ModulePar_Template && arg1 instanceof Def_ModulePar_Template) {
					final String s0 = ((Def_ModulePar_Template) arg0).getIdentifier().getDisplayName();
					final String s1 = ((Def_ModulePar_Template) arg1).getIdentifier().getDisplayName();

					return s0.compareTo(s1);
				}

				return 0;
			} else {
				return (o0 < o1) ? -1 : 1;
			}
		}

	}

	private static String loadFileContent(final IFile toLoad) {
		StringBuilder fileContents;
		try {
			final InputStream is = toLoad.getContents();
			final BufferedReader br = new BufferedReader(new InputStreamReader(is));
			fileContents = new StringBuilder();
			final char[] buff = new char[1024];
			while (br.ready()) {
				final int len = br.read(buff);
				fileContents.append(buff, 0, len);
			}
			br.close();
		} catch (IOException e) {
			ErrorReporter.logError("ChangeCreator.loadFileContent(): Unable to get file contents (IOException) for file: " + toLoad.getName());
			return null;
		} catch (CoreException ce) {
			ErrorReporter.logError("ChangeCreator.loadFileContent(): Unable to get file contents (CoreException) for file: " + toLoad.getName());
			return null;
		}
		return fileContents.toString();
	}

}
