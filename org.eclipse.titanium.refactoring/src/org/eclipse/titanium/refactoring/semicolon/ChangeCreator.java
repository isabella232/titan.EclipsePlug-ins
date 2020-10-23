/******************************************************************************
 * Copyright (c) 2000-2020 Ericsson Telecom AB
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/org/documents/epl-2.0/EPL-2.0.html
 ******************************************************************************/
package org.eclipse.titanium.refactoring.semicolon;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashSet;
import java.util.Set;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.ltk.core.refactoring.Change;
import org.eclipse.ltk.core.refactoring.TextFileChange;
import org.eclipse.text.edits.InsertEdit;
import org.eclipse.text.edits.MultiTextEdit;
import org.eclipse.titan.common.logging.ErrorReporter;
import org.eclipse.titan.designer.AST.ASTVisitor;
import org.eclipse.titan.designer.AST.IVisitableNode;
import org.eclipse.titan.designer.AST.Location;
import org.eclipse.titan.designer.AST.Module;
import org.eclipse.titan.designer.AST.NULL_Location;
import org.eclipse.titan.designer.AST.TTCN3.definitions.Def_Port;
import org.eclipse.titan.designer.AST.TTCN3.definitions.Definition;
import org.eclipse.titan.designer.AST.TTCN3.definitions.Definitions;
import org.eclipse.titan.designer.AST.TTCN3.definitions.FormalParameterList;
import org.eclipse.titan.designer.AST.TTCN3.definitions.TTCN3Module;
import org.eclipse.titan.designer.AST.TTCN3.statements.AltGuard;
import org.eclipse.titan.designer.AST.TTCN3.statements.Alt_Statement;
import org.eclipse.titan.designer.AST.TTCN3.statements.DoWhile_Statement;
import org.eclipse.titan.designer.AST.TTCN3.statements.For_Statement;
import org.eclipse.titan.designer.AST.TTCN3.statements.If_Statement;
import org.eclipse.titan.designer.AST.TTCN3.statements.Statement;
import org.eclipse.titan.designer.AST.TTCN3.statements.TryCatch_Statement;
import org.eclipse.titan.designer.AST.TTCN3.statements.Unknown_Applied_Statement;
import org.eclipse.titan.designer.AST.TTCN3.statements.Unknown_Instance_Statement;
import org.eclipse.titan.designer.AST.TTCN3.statements.Unknown_Start_Statement;
import org.eclipse.titan.designer.AST.TTCN3.statements.Unknown_Stop_Statement;
import org.eclipse.titan.designer.AST.TTCN3.statements.While_Statement;
import org.eclipse.titan.designer.AST.TTCN3.types.ComponentTypeBody;
import org.eclipse.titan.designer.AST.TTCN3.types.PortTypeBody;
import org.eclipse.titan.designer.AST.TTCN3.types.Port_Type;
import org.eclipse.titan.designer.AST.TTCN3.types.TypeSet;
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
	 * Creates the {@link #change} object, which contains all missing semicolon
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
			final MultiTextEdit resultEdit = semicolonEdit(tModule, toVisit);
			if (!resultEdit.hasChildren()) {
				return null;
			}
			tfc.setEdit(resultEdit);
		} catch (BadLocationException e) {
			ErrorReporter.logExceptionStackTrace("Error while ...", e);
		} catch (CoreException e) {
			ErrorReporter.logError("AddSemicolonRefactoring/CreateChange.createFileChange(): "
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
	 * @param module 
	 * @param toVisit
	 *
	 * @return The edit, which contains the proper changes.
	 * @throws CoreException
	 */
	private MultiTextEdit semicolonEdit(final TTCN3Module module, final IFile toVisit) throws BadLocationException, CoreException {
		final MultiTextEdit edit = new MultiTextEdit();

		final Set<Location> list;
		final Set<Integer> locations = new HashSet<Integer>();
		final SemicolonVisitor visit = new SemicolonVisitor();
		module.accept(visit);
		list = visit.getLocations();

		for (final Location node : list) {
			final int offset = node.getOffset();
			final int endoffset = node.getEndOffset();
			if (locations.contains(endoffset)) {
				continue;
			}
			try {
				final InputStream is =  toVisit.getContents();
				final InputStreamReader isr = new InputStreamReader(is, toVisit.getCharset());
				final int lenght = endoffset-offset;
				final char[] content = new char[lenght];
				isr.skip(offset);
				isr.read(content);

				if (content[lenght-1] != ';'  && content[lenght-1] != ','  && content[lenght-1] != '{' && content[lenght-1] != '}') {
					char a = (char)isr.read();
					while (!Character.isAlphabetic(a) && !Character.isDigit(a) && a != ';' && a != ','&& a != '{' && a != '}' && a != ')') {
						if (a == '/') {
							char b = (char)isr.read();
							if (b == '/') { // line comment
								while (b != '\n') {
									b = (char)isr.read();
								}
							} else if (b == '*') {//block comment
								while (a != '*' || b!= '/') {
									a = b;
									b = (char)isr.read();
								}
							}
						}
						a = (char)isr.read();
					}
					if (a != ';' && a != ',' && a != '{'  && a != ')') {
						locations.add(endoffset);
						edit.addChild(new InsertEdit(endoffset, ";"));
					}
				}
			} catch (IOException e) {
				ErrorReporter.logError("AddSemicolonRefactoring.ChangeCreator: Error while reading source project.");
				ErrorReporter.logExceptionStackTrace(e);
			} catch (CoreException ce) {
				ErrorReporter.logError("ChangeCreator.loadFileContent(): Unable to get file contents (CoreException) for file: " + toVisit.getName());
				return null;
			}
		}

		return edit;
	}
}

/**
 * 
 * <p>
 * Call on modules.
 * */
class SemicolonVisitor extends ASTVisitor {

	private final Set<Location> locations;

	SemicolonVisitor() {
		locations = new HashSet<Location>();
	}

	public Set<Location> getLocations() {
		locations.remove(NULL_Location.INSTANCE);
		return locations;
	}


	@Override
	public int visit(final IVisitableNode node) {
		if(isGoodType(node)){
			locations.add(((Statement) node).getLocation());
			return V_CONTINUE;
		} 

		if (node instanceof AltGuard) {
			locations.add(( (AltGuard) node).getLocation());
			return V_CONTINUE;
		}

		if (node instanceof ComponentTypeBody) {
			ComponentTypeBody cmp = (ComponentTypeBody) node;
			for (Definition i : cmp.getDefinitions()) {
				locations.add(i.getLocation());
			}
			return V_CONTINUE;
		}

		if (node instanceof Definition) {
			locations.add(((Definition) node).getLocation());
			return V_CONTINUE;
		} 

		if (node instanceof PortTypeBody) {
			PortTypeBody port = ((PortTypeBody) node);
			FormalParameterList unparam = port.getUnmapParameters();
			if (unparam != null) {
				locations.add(unparam.getLocation());
			}
			
			FormalParameterList param = port.getMapParameters();
			if (param != null) {
				locations.add(param.getLocation());
			}
			
			Definitions def = port.getVariableDefinitions();
			if (def != null) {
				locations.add(def.getLocation());
			}
			//TypeSet mss = port.getInMessages();
			TypeSet mss =  port.getInMessages();
//			if (mss != null) {
//				for (msg : mss) {
//					
//				}
//			}
			return V_CONTINUE;
		} 
		
		if (node instanceof Def_Port) {
			Def_Port p = (Def_Port) node;
			locations.add(p.getLocation());
		}
		
		if (node instanceof Port_Type) {
			Port_Type p = (Port_Type) node;
			locations.add(p.getLocation());
		}
		
		return V_CONTINUE;
	}

	private boolean isGoodType(final IVisitableNode node) {
		return (node instanceof Statement
				&& !(node instanceof If_Statement)
				&& !(node instanceof TryCatch_Statement)
				&& !(node instanceof For_Statement)
				&& !(node instanceof While_Statement)
				&& !(node instanceof DoWhile_Statement)
				&& !(node instanceof Alt_Statement)
				&& !(node instanceof Unknown_Applied_Statement)
				&& !(node instanceof Unknown_Instance_Statement)
				&& !(node instanceof Unknown_Start_Statement)
				&& !(node instanceof Unknown_Stop_Statement));
	}
}