/******************************************************************************
 * Copyright (c) 2000-2018 Ericsson Telecom AB
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/org/documents/epl-2.0/EPL-2.0.html
 ******************************************************************************/
package org.eclipse.titanium.markers.spotters.implementation;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.eclipse.titan.designer.AST.ASTVisitor;
import org.eclipse.titan.designer.AST.ISubReference;
import org.eclipse.titan.designer.AST.IType;
import org.eclipse.titan.designer.AST.IValue;
import org.eclipse.titan.designer.AST.IVisitableNode;
import org.eclipse.titan.designer.AST.Identifier;
import org.eclipse.titan.designer.AST.Reference;
import org.eclipse.titan.designer.AST.Value;
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
import org.eclipse.titanium.markers.spotters.BaseModuleCodeSmellSpotter;
import org.eclipse.titanium.markers.types.CodeSmellType;

/**
 * @author Mate Kovacs
 */
public class SelectUnion extends BaseModuleCodeSmellSpotter {

	private static final String ERR_MSG = "Suspected a select, which can be transformed into a select union statement.";
	
	private final CompilationTimeStamp timestamp;
	
	public SelectUnion() {
		super(CodeSmellType.SELECT_UNION);
		timestamp = CompilationTimeStamp.getBaseTimestamp();
	}

	@Override
	protected void process(IVisitableNode node, Problems problems) {
		if (!(node instanceof SelectCase_Statement)) {
			return;
		}
		final SelectCase_Statement s = (SelectCase_Statement)node;
		
		final Value v = s.getExpression();
		if (v == null || v.getIsErroneous(timestamp)) {
			return;
		}
		
		final SelectCases scs = s.getSelectCases();
		if (scs == null || scs.getSelectCaseArray() == null) {
			return;
		}
		
		// Check the expression - must be true.
		if(!(v instanceof Boolean_Value) || !((Boolean_Value) v).getValue()){
			return;
		}
		
		// Check the cases.
		final CaseVisitor caseVisitor = new CaseVisitor();
		scs.accept(caseVisitor);
		if (caseVisitor.isErronous()) {
			return;
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
			return;
		}
		if(caseVisitor.getUnionType() == null){
			return;
		}
		caseVisitor.getUnionType().accept(unionVisitor);
		
		// Check if the found types are the same as the union types.
		List<Identifier> unionItems = unionVisitor.getItemsFound();
		if(unionItems.isEmpty()){
			return;
		}
		for(Identifier item : unionItems){
			foundIds.remove(item);
		}
		if(foundIds.isEmpty()){
			problems.report(s.getLocation(), ERR_MSG);
		}
	}

	@Override
	public List<Class<? extends IVisitableNode>> getStartNode() {
		final List<Class<? extends IVisitableNode>> ret = new ArrayList<Class<? extends IVisitableNode>>(1);
		ret.add(SelectCase_Statement.class);
		return ret;
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
	
	private static final class IsChoosenItemVisitor extends ASTVisitor {

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
