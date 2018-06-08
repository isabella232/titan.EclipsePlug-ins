/******************************************************************************
 * Copyright (c) 2000-2018 Ericsson Telecom AB
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 ******************************************************************************/
package org.eclipse.titanium.refactoring.select_union;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.ltk.core.refactoring.Change;
import org.eclipse.ltk.core.refactoring.Refactoring;
import org.eclipse.ltk.core.refactoring.TextFileChange;
import org.eclipse.text.edits.MalformedTreeException;
import org.eclipse.text.edits.MultiTextEdit;
import org.eclipse.text.edits.ReplaceEdit;
import org.eclipse.titan.designer.AST.ASTVisitor;
import org.eclipse.titan.designer.AST.ISubReference;
import org.eclipse.titan.designer.AST.IType;
import org.eclipse.titan.designer.AST.IValue;
import org.eclipse.titan.designer.AST.Identifier;
import org.eclipse.titan.designer.AST.Reference;
import org.eclipse.titan.designer.AST.Value;
import org.eclipse.titan.designer.AST.IVisitableNode;
import org.eclipse.titan.designer.AST.Module;
import org.eclipse.titan.designer.AST.TTCN3.attributes.WithAttributesPath;
import org.eclipse.titan.designer.AST.TTCN3.statements.SelectCase;
import org.eclipse.titan.designer.AST.TTCN3.statements.SelectCase_Statement;
import org.eclipse.titan.designer.AST.TTCN3.statements.SelectCases;
import org.eclipse.titan.designer.AST.TTCN3.templates.TemplateInstance;
import org.eclipse.titan.designer.AST.TTCN3.templates.TemplateInstances;
import org.eclipse.titan.designer.AST.TTCN3.types.CompField;
import org.eclipse.titan.designer.AST.TTCN3.types.CompFieldMap;
import org.eclipse.titan.designer.AST.TTCN3.types.Referenced_Type;
import org.eclipse.titan.designer.AST.TTCN3.types.TTCN3_Choice_Type;
import org.eclipse.titan.designer.AST.TTCN3.values.Boolean_Value;
import org.eclipse.titan.designer.AST.TTCN3.values.expressions.IsChoosenExpression;
import org.eclipse.titan.designer.parsers.CompilationTimeStamp;
import org.eclipse.titan.designer.parsers.GlobalParser;
import org.eclipse.titan.designer.parsers.ProjectSourceParser;

/**
 * This class is only instantiated by the {@link ChangeToSelectUnionRefactoring} once per each refactoring operation.
 * <p>
 * By passing the selection through the constructor and calling {@link ChangeCreator#perform()}, this class
 *  creates a {@link Change} object, which can be returned by the standard
 *  {@link Refactoring#createChange(IProgressMonitor)} method in the refactoring class.
 *
 * @author Mate Kovacs
 */
class ChangeCreator {
	// in
	private final IFile selectedFile;

	// out
	private Change change;

	private final CompilationTimeStamp timestamp;
	
	ChangeCreator(final IFile selectedFile) {
		this.selectedFile = selectedFile;
		timestamp = CompilationTimeStamp.getBaseTimestamp();
	}

	public Change getChange() {
		return change;
	}

	/**
	 * Creates the {@link #change} object, which contains all the inserted and edited visibility modifiers
	 * in the selected resources.
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
		final List<SelectCase_Statement> nodes = vis.getLocations();

		if (nodes.isEmpty()) {
			return null;
		}

		// create a change for each edit location
		final TextFileChange tfc = new TextFileChange(toVisit.getName(), toVisit);
		final MultiTextEdit rootEdit = new MultiTextEdit();
		tfc.setEdit(rootEdit);

		if (nodes.isEmpty()) {
			return tfc;
		}

		for(Integer i = 0; i < nodes.size(); i++) {
			makeChange(nodes.get(i), rootEdit, vis.getReferences().get(i));
		}

		if (!rootEdit.hasChildren()) {
			return null;
		}

		return tfc;
	}
	
	private void makeChange(final SelectCase_Statement statement, final MultiTextEdit rootEdit, final List<Reference> elements){
		// Insert the "union" word after the "select"
		final ReplaceEdit insertUnion = new ReplaceEdit(statement.getLocation().getOffset()+6, 0, " union");
		
		// Statement part changes
		int statementBegin = statement.getExpression().getLocation().getOffset(); 
		int statementEnd = statement.getExpression().getLocation().getEndOffset();
		int elementNameLength = elements.get(0).getDisplayName().lastIndexOf(".");
		String elementName = elements.get(0).getDisplayName().substring(0, elementNameLength);
		final ReplaceEdit changeExpression = new ReplaceEdit(statementBegin, statementEnd-statementBegin, elementName);
		
		// Calculate the case branch changes
		ReplaceEdit[] changeCases = new ReplaceEdit[elements.size()];
		List<SelectCase> scs = statement.getSelectCases().getSelectCaseArray();
		if(scs.size() < elements.size()){
			return;
		}
		for(Integer i = 0; i < elements.size(); i++){
			int startOfIschosen = scs.get(i).getLocation().getOffset();
			int startOfStatement = scs.get(i).getStatementBlock().getLocation().getOffset();
			String fieldName = elements.get(i).getDisplayName().substring(elementNameLength+1);
			changeCases[i] = new ReplaceEdit(startOfIschosen, startOfStatement - startOfIschosen, "case("+fieldName+")");
		}
		
		try {
			rootEdit.addChild(insertUnion);
			rootEdit.addChild(changeExpression);
			rootEdit.addChildren(changeCases);
		} catch (MalformedTreeException e) {
		}
	}

	/**
	 * Collects the locations of all the definitions in a module where the visibility modifier
	 *  is not yet minimal.
	 * <p>
	 * Call on modules.
	 * */

	private class DefinitionVisitor extends ASTVisitor {

		private final List<SelectCase_Statement> locations;
		private final List<List<Reference>> references;

		DefinitionVisitor() {
			locations = new ArrayList<SelectCase_Statement>();
			references = new ArrayList<List<Reference>>();
		}

		private List<SelectCase_Statement> getLocations() {
			return locations;
		}
		
		private List<List<Reference>> getReferences(){
			return references;
		}

		@Override
		public int visit(final IVisitableNode node) {
			if (!(node instanceof SelectCase_Statement)) {
				return V_CONTINUE;
			}
			final SelectCase_Statement s = (SelectCase_Statement)node;
			
			final Value v = s.getExpression();
			if (v == null || v.getIsErroneous(timestamp)) {
				return V_CONTINUE;
			}
			
			final SelectCases scs = s.getSelectCases();
			if (scs == null || scs.getSelectCaseArray() == null) {
				return V_CONTINUE;
			}
			// No report if an else branch exists.
			boolean hasElseBranch = false;
			for (final SelectCase sc: scs.getSelectCaseArray()) {
				if (sc.hasElse()) {
					hasElseBranch = true;
				}
			}
			
			// Check the expression - must be true.
			if(!(v instanceof Boolean_Value) || !((Boolean_Value) v).getValue()){
				return V_CONTINUE;
			}
			
			// Check the cases.
			final CaseVisitor caseVisitor = new CaseVisitor();
			scs.accept(caseVisitor);
			if (caseVisitor.isErronous()) {
				return V_CONTINUE;
			}
			
			// Check the union, get the types.
			final UnionItemVisitor unionVisitor = new UnionItemVisitor();
			List<Identifier> foundIds = new ArrayList<Identifier>();
			for(Reference ref : caseVisitor.getReferenceList()){
				List<ISubReference> reflist = ref.getSubreferences();
				if(reflist.isEmpty()){
					continue;
				}
				foundIds.add(reflist.get(reflist.size()-1).getId());
			}
			if(foundIds.isEmpty()){
				return V_CONTINUE;
			}
			if(caseVisitor.getUnionType() == null){
				return V_CONTINUE;
			}
			caseVisitor.getUnionType().accept(unionVisitor);
			
			// Check if the found types are the same as the union types.
			List<Identifier> unionItems = unionVisitor.getItemsFound();
			if(unionItems.isEmpty() || (!hasElseBranch && unionItems.size() != foundIds.size())){
				return V_CONTINUE;
			}
			for(Identifier item : foundIds){
				unionItems.remove(item);
			}
			if(!hasElseBranch && unionItems.isEmpty() || hasElseBranch){
				locations.add(s);
				references.add(caseVisitor.getReferenceList());
			}
			return V_CONTINUE;
		}

		private final class CaseVisitor extends ASTVisitor {
			private boolean errorDuringVisiting = false;
			private List<Reference> references = new ArrayList<Reference>();
			IType unionType = null;
			
			public boolean isErronous() {
				return this.errorDuringVisiting;
			}
			
			public List<Reference> getReferenceList(){
				return this.references;
			}
			
			public IType getUnionType(){
				return this.unionType;
			}
			
			@Override
			public int visit(final IVisitableNode node) {
				if (node instanceof SelectCases) {
					return V_CONTINUE;
				} else if (node instanceof SelectCase) {
					return V_CONTINUE;
				} else if (node instanceof TemplateInstances) {
					return V_CONTINUE;
				} else if (node instanceof TemplateInstance) {
					final TemplateInstance ti = (TemplateInstance)node;
					IValue val = ti.getTemplateBody().getValue();
					if (val == null || val.getIsErroneous(timestamp) || !(val instanceof IsChoosenExpression)) {
						errorDuringVisiting = true;
						return V_ABORT;
					}
					
					IsChoosenExpression expr = (IsChoosenExpression)val;
					final IsChoosenItemVisitor itemVisitor = new IsChoosenItemVisitor();
					expr.accept(itemVisitor);
					if(itemVisitor.getReference() == null){
						errorDuringVisiting = true;
						return V_ABORT;
					}
					// Throw out the cases: same types, different variables.
					if(!references.isEmpty() && !(itemVisitor.getReference().getRefdAssignment(timestamp, false).equals(references.get(0).getRefdAssignment(timestamp, false)))){
						errorDuringVisiting = true;
						return V_ABORT;
					}
					if(unionType == null){
						IType itype = itemVisitor.getReference().checkVariableReference(timestamp);
						if (itype instanceof Referenced_Type) {
							itype = itype.getTypeRefdLast(timestamp);
						}
						if (itype != null) {
							unionType = itype.getParentType();
						}
					}
					references.add(itemVisitor.getReference());
					return V_SKIP;
				}
				return V_SKIP;
			}
		}
		
		private final class IsChoosenItemVisitor extends ASTVisitor {

			private Reference reference;
			
			public Reference getReference(){
				return this.reference;
			}

			@Override
			public int visit(final IVisitableNode node) {
				if (node instanceof IsChoosenExpression) {
					return V_CONTINUE;
				} else if (node instanceof Reference) {
					reference = (Reference) node;
					return V_SKIP;
				}
				return V_SKIP;
			}

		}
		
		private final class UnionItemVisitor extends ASTVisitor {

			private final List<Identifier> itemsFound = new ArrayList<Identifier>();
			
			public List<Identifier> getItemsFound() {
				return this.itemsFound;
			}

			@Override
			public int visit(final IVisitableNode node) {
				if (node instanceof TTCN3_Choice_Type) {
					return V_CONTINUE;
				} else if (node instanceof WithAttributesPath) {
					return V_CONTINUE;
				} else if (node instanceof CompFieldMap){
					CompFieldMap cm = (CompFieldMap) node;
					Map<String, CompField> map = cm.getComponentFieldMap(timestamp);
					for(Map.Entry<String, CompField> entry : map.entrySet()) {
						itemsFound.add(entry.getValue().getIdentifier());
					}
					return V_CONTINUE;
				}
				return V_SKIP;
			}

		}
	}

}
