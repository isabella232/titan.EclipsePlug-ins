/******************************************************************************
 * Copyright (c) 2000-2018 Ericsson Telecom AB
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/org/documents/epl-2.0/EPL-2.0.html
 ******************************************************************************/
package org.eclipse.titanium.refactoring.runsonscopereduction;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.ltk.core.refactoring.Change;
import org.eclipse.ltk.core.refactoring.TextFileChange;
import org.eclipse.text.edits.DeleteEdit;
import org.eclipse.text.edits.MultiTextEdit;
import org.eclipse.text.edits.ReplaceEdit;
import org.eclipse.titan.common.logging.ErrorReporter;
import org.eclipse.titan.designer.AST.ASTVisitor;
import org.eclipse.titan.designer.AST.Assignment;
import org.eclipse.titan.designer.AST.IVisitableNode;
import org.eclipse.titan.designer.AST.Identifier;
import org.eclipse.titan.designer.AST.Module;
import org.eclipse.titan.designer.AST.Reference;
import org.eclipse.titan.designer.AST.TTCN3.definitions.Def_Altstep;
import org.eclipse.titan.designer.AST.TTCN3.definitions.Def_Function;
import org.eclipse.titan.designer.AST.TTCN3.definitions.Def_Testcase;
import org.eclipse.titan.designer.AST.TTCN3.definitions.Definition;
import org.eclipse.titan.designer.AST.TTCN3.definitions.TTCN3Module;
import org.eclipse.titan.designer.AST.TTCN3.types.ComponentTypeBody;
import org.eclipse.titan.designer.AST.TTCN3.types.Component_Type;
import org.eclipse.titan.designer.parsers.CompilationTimeStamp;
import org.eclipse.titan.designer.parsers.GlobalParser;
import org.eclipse.titan.designer.parsers.ProjectSourceParser;

/** 
 * 
 * @author Farkas Izabella Ingrid 
 */

public class ChangeCreator {

	//in
	private final IFile selectedFile;

	//out
	private Change change;

	ChangeCreator(final IFile selectedFile) {
		this.selectedFile = selectedFile;
	}

	public Change getChange() {
		return change;
	}

	/**
	 * Creates the {@link #change} object, which contains all the remove and change runs on component
	 * in the selected resources.
	 * */
	public void perform() {
		if (selectedFile == null) {
			return;
		}
		change = createFileChange(selectedFile);
	}

	private Change createFileChange(final IFile toVisit){
		if (toVisit == null) {
			return null;
		}

		final ProjectSourceParser sourceParser = GlobalParser.getProjectSourceParser(toVisit.getProject());
		final Module module = sourceParser.containedModule(toVisit);
		if(module == null || !(module instanceof TTCN3Module)) {
			return null;
		}

		//create a change
		final TextFileChange tfc = new TextFileChange(toVisit.getName(), toVisit);
		final TTCN3Module tModule = (TTCN3Module) module;

		try {
			final MultiTextEdit resultEdit = runsOnScopeEdit(tModule, toVisit);
			if (!resultEdit.hasChildren()) {
				return null;
			}
			tfc.setEdit(resultEdit);
		} catch (BadLocationException e) {
			ErrorReporter.logExceptionStackTrace("Error while runs on scope", e);
		} catch (CoreException e) {
			ErrorReporter.logError("RunsOnScopeReductionRefactoring/CreateChange.createFileChange(): "
					+ "CoreException while calculating edit locations. ");
			ErrorReporter.logExceptionStackTrace(e);
		}

		return tfc;
	}



	/**
	 *
	 * These changes are not applied in the function, just collected in a
	 * <link>MultiTextEdit</link>, which is then returned.
	 * 
	 * @param module where the runs on component modifier is not yet minimal.
	 * @param toVisit 
	 * 
	 * @return The edit, which contains the proper changes.
	 * @throws CoreException 
	 */
	private static MultiTextEdit runsOnScopeEdit(final TTCN3Module module, IFile toVisit) throws BadLocationException, CoreException {
		final MultiTextEdit edit = new MultiTextEdit();

		final Set<Definition> list;
		final RunsOnComponentVisitor visit = new RunsOnComponentVisitor();
		module.accept(visit);
		list = visit.getLocations();

		for (Definition node : list) {

			final Set<Identifier> definitions = new HashSet<Identifier>();
			final Reference runsOnReference;
			final CompilationTimeStamp timestamp = CompilationTimeStamp.getBaseTimestamp();
			boolean isTestCase = false;
			final Component_Type componentType;

			if (node instanceof Def_Function) {
				final Def_Function variable = (Def_Function) node;
				runsOnReference = variable.getRunsOnReference(timestamp);
				componentType = variable.getRunsOnType(timestamp);
				if (componentType == null) {
					continue;
				}
			} else if (node instanceof Def_Altstep) {
				final Def_Altstep variable = (Def_Altstep) node;
				runsOnReference =  variable.getRunsOnReference(timestamp);
				componentType = variable.getRunsOnType(timestamp);
				if (componentType == null) {
					continue;
				}
			} else {
				final Def_Testcase variable = (Def_Testcase) node;
				runsOnReference =  variable.getRunsOnReference(timestamp);
				componentType = variable.getRunsOnType(timestamp);
				if (componentType == null) {
					continue;
				}
				isTestCase = true;
			}

			final ReferenceCheck chek = new ReferenceCheck();
			node.accept(chek);
			definitions.addAll(chek.getIdentifiers());

			// check runs on

			final int offset = runsOnReference.getLocation().getOffset();
			final int endoffset = runsOnReference.getLocation().getEndOffset();

			if (definitions.isEmpty()) {
				if (!isTestCase) {
					final InputStream is =  toVisit.getContents();
					final InputStreamReader isr = new InputStreamReader(is);
					int lenght = offset - node.getIdentifier().getLocation().getEndOffset();
					final char[] content = new char[lenght];
					int currOffset = 0;
					try {
						isr.skip(offset - lenght);
						isr.read(content);
						lenght--;
						while(content[lenght] != ')') {
							lenght--;
							currOffset++;
						}
						
					} catch (IOException e) {
						ErrorReporter.logError("RunsOnScopeReductionRefactoring.ChangeCreator: Error while reading source project.");
						ErrorReporter.logExceptionStackTrace(e);
					}
					
					edit.addChild(new DeleteEdit(offset-currOffset, endoffset - offset+currOffset));
				}
			} else if (!definitions.contains(runsOnReference.getId())) {
				if (definitions.size() == 1) {
					final Identifier id = definitions.iterator().next();
					edit.addChild(new ReplaceEdit(offset, endoffset - offset, id.getDisplayName()));
				} else {
					final ComponentTypeBody variable = searchComponent(componentType.getComponentBody(), definitions, new HashSet<Identifier>());
					if (variable != null && variable.getIdentifier() != runsOnReference.getId()) {
						edit.addChild(new ReplaceEdit(offset, endoffset - offset, variable.getIdentifier().getDisplayName()));
					}		
				}
			}

		}

		return edit;
	}

	private static ComponentTypeBody searchComponent(final ComponentTypeBody component, final Set<Identifier> definitions, Set<Identifier> identifiersOfTree) {
		final List<ComponentTypeBody> parentComponentBodies = component.getExtensions().getComponentBodies();
		if (parentComponentBodies.isEmpty()) {
			identifiersOfTree.add(component.getIdentifier());
			return null;
		}
		final Set<Identifier> setNodes = new HashSet<Identifier>();
		setNodes.add(component.getIdentifier());
		for (ComponentTypeBody variable : parentComponentBodies) {
			final Set<Identifier> identifiersOfNode = new HashSet<Identifier>();
			final ComponentTypeBody cb = searchComponent(variable, definitions, identifiersOfNode);
			if (cb != null) {
				return cb;
			}
			setNodes.addAll(identifiersOfNode);
		}

		if (setNodes.containsAll(definitions)) {
			identifiersOfTree.addAll(setNodes);
			return component;
		}

		identifiersOfTree.addAll(setNodes);
		return null;
	}
}


class ReferenceCheck extends ASTVisitor {

	private Set<Identifier> setOfIdentifier = new HashSet<Identifier>();

	public ReferenceCheck() {
		setOfIdentifier.clear();
	}

	public Set<Identifier> getIdentifiers() {
		return setOfIdentifier;
	}

	@Override
	public int visit(final IVisitableNode node) {
		if (node instanceof Reference) {
			if (((Reference) node).getIsErroneous(CompilationTimeStamp.getBaseTimestamp())) {
				return V_CONTINUE;
			}
			final Reference reference = (Reference) node;
			final CompilationTimeStamp timestamp = CompilationTimeStamp.getBaseTimestamp();
			if (reference != null) {
				final Assignment assignment = reference.getRefdAssignment(timestamp, false);
				if (assignment != null){
					if (assignment instanceof Def_Function) {
						final Component_Type componentType = ((Def_Function) assignment).getRunsOnType(timestamp); 
						if (componentType == null) {
							return V_CONTINUE;
						}
						final Identifier sc = componentType.getComponentBody().getIdentifier();
						setOfIdentifier.add(sc);
					}
					if (assignment.getMyScope() instanceof ComponentTypeBody ) {
						final Identifier sc =((ComponentTypeBody)assignment.getMyScope()).getIdentifier();
						setOfIdentifier.add(sc);
					}
				}
			}
		}
		return V_CONTINUE;
	}
}


/**
 * Collects the locations of all the runs on component used in a module where the component modifier
 *  is not yet minimal.
 * <p>
 * Call on modules.
 * */
class RunsOnComponentVisitor extends ASTVisitor {

	private final Set<Definition> locations;

	RunsOnComponentVisitor() {
		locations = new HashSet<Definition>();
	}

	public Set<Definition> getLocations() {
		return locations;
	}

	@Override
	public int visit(final IVisitableNode node) {
		if(isGoodType(node)){
			locations.add((Definition) node);
			return V_SKIP;
		}
		return V_CONTINUE;
	}

	private boolean isGoodType(final IVisitableNode node) {
		return (node instanceof Def_Altstep ||
				node instanceof Def_Testcase ||
				node instanceof Def_Function );
	}
}