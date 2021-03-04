/******************************************************************************
 * Copyright (c) 2000-2021 Ericsson Telecom AB
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/org/documents/epl-2.0/EPL-2.0.html
 ******************************************************************************/
package org.eclipse.titan.designer.AST.TTCN3.types;

import java.text.MessageFormat;
import java.util.List;

import org.eclipse.titan.designer.AST.ArraySubReference;
import org.eclipse.titan.designer.AST.Assignment;
import org.eclipse.titan.designer.AST.FieldSubReference;
import org.eclipse.titan.designer.AST.GovernedSimple.CodeSectionType;
import org.eclipse.titan.designer.AST.IReferenceChain;
import org.eclipse.titan.designer.AST.ISubReference;
import org.eclipse.titan.designer.AST.ISubReference.Subreference_type;
import org.eclipse.titan.designer.AST.ReferenceFinder.Hit;
import org.eclipse.titan.designer.AST.IType;
import org.eclipse.titan.designer.AST.Location;
import org.eclipse.titan.designer.AST.NamedBridgeScope;
import org.eclipse.titan.designer.AST.ParameterisedSubReference;
import org.eclipse.titan.designer.AST.Reference;
import org.eclipse.titan.designer.AST.ReferenceFinder;
import org.eclipse.titan.designer.AST.Scope;
import org.eclipse.titan.designer.AST.Type;
import org.eclipse.titan.designer.AST.TypeCompatibilityInfo;
import org.eclipse.titan.designer.AST.TypeCompatibilityInfo.Chain;
import org.eclipse.titan.designer.AST.TTCN3.Expected_Value_type;
import org.eclipse.titan.designer.AST.TTCN3.definitions.ClassModifier;
import org.eclipse.titan.designer.AST.TTCN3.statements.StatementBlock;
import org.eclipse.titan.designer.AST.TTCN3.templates.ITTCN3Template;
import org.eclipse.titan.designer.compiler.JavaGenData;
import org.eclipse.titan.designer.parsers.CompilationTimeStamp;

/**
 * class type (TTCN-3)
 * 
 * @author Miklos Magyari
 */

public final class Class_Type extends Type {
	private static final String SE_FINALVSABSTRACT = "A final class cannot be abstract.";
	private static final String SE_BADBASECLASS = "Incorrect base class";
	
	private static final String CLASSTYPE_NAME = "class";
	
	private final Location modifierLoc;
	private final Reference extClass;
	private final Reference runsOnRef;
	private final Reference mtcRef;
	private final Reference systemRef;
	private final List<ClassModifier> modifiers;
	private final StatementBlock finallyBlock;

	private NamedBridgeScope bridgeScope = null;
	
	public Class_Type(List<ClassModifier> modifiers, final Location modifierLoc, Reference extClass, 
			final Reference runsOnRef, final Reference mtcRef, final Reference systemRef,
			StatementBlock finallyBlock) {
		this.modifiers = modifiers;
		this.modifierLoc = modifierLoc;		
		this.extClass = extClass;
		this.runsOnRef = runsOnRef;
		this.mtcRef = mtcRef;
		this.systemRef = systemRef;
		this.finallyBlock = finallyBlock;
		
		if (runsOnRef != null) {
			runsOnRef.setFullNameParent(this);
		}
		if (mtcRef != null) {
			mtcRef.setFullNameParent(this);
		}
		if (systemRef != null) {
			systemRef.setFullNameParent(this);
		}
		if (finallyBlock != null) {
			finallyBlock.setFullNameParent(this);
		}
	}
	
    @Override
    /** {@inheritDoc} */
    public Type_type getTypetype() {
    	return Type_type.TYPE_CLASS;
    }
    
    @Override
    /** {@inheritDoc} */
    public Type_type getTypetypeTtcn3() {
    	if (isErroneous) {
    		return Type_type.TYPE_UNDEFINED;
    	}
    	return getTypetype();
    }
    
    @Override
    public boolean checkThisTemplate(final CompilationTimeStamp timestamp, final ITTCN3Template template,
    		final boolean isModified, final boolean implicitOmit, final Assignment lhs) {
    	registerUsage(template);
    	
    	return false;
    }
    
    @Override
    public String getGenNameValue(final JavaGenData aData, final StringBuilder source) {
    	aData.addBuiltinTypeImport("TitanClass");
    	
    	return "TitanClass";
    }
    
    @Override
    public void generateCode(final JavaGenData aData, final StringBuilder source) {
    	if (lastTimeGenerated != null && !lastTimeGenerated.isLess(aData.getBuildTimstamp())) {
			return;
		}
    	
    	lastTimeGenerated = aData.getBuildTimstamp();
    	final StringBuilder sb = new StringBuilder();
    	
    	final String ownName = getGenNameOwn();
    	List<ClassModifier> modifiers = getModifiers();
    	String modifier = "";
    	
    	if (modifiers.contains(ClassModifier.Final))
    		modifier = "final";
    	if (modifiers.contains(ClassModifier.Abstract))
    		modifier = "abstract";
    	
    	sb.append(MessageFormat.format("\tpublic {0} class {1} ", modifier, ownName));
    	if (extClass != null) {
    		sb.append(MessageFormat.format("extends {0}", extClass.getSubreferences().get(0).getId()));
    	}
    	
    	sb.append(" {\n");
    	
    	if (finallyBlock != null) {
    		sb.append("\n\t\t@Override");
    		sb.append("\n\t\tpublic void finalize() {\n");
    		finallyBlock.generateCode(aData, sb);
    		sb.append("\t\t}\n");
    	}
    	
    	sb.append("\n\t}\n");
    	
    	source.append(sb);
    }
    
    @Override
	/** {@inheritDoc} */
	public String getTypename() {
		return CLASSTYPE_NAME;
	}

	@Override
	public String getOutlineIcon() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	/** {@inheritDoc} */
	public IType getFieldType(final CompilationTimeStamp timestamp, final Reference reference, final int actualSubReference,
			final Expected_Value_type expectedIndex, final IReferenceChain refChain, final boolean interruptIfOptional) {
		final List<ISubReference> subreferences = reference.getSubreferences();
		if (subreferences.size() <= actualSubReference) {
			return this;
		}

		final ISubReference subreference = subreferences.get(actualSubReference);
		switch (subreference.getReferenceType()) {
		case arraySubReference:
			subreference.getLocation().reportSemanticError(MessageFormat.format(ArraySubReference.INVALIDSUBREFERENCE, getTypename()));
			return null;
		case fieldSubReference:
			subreference.getLocation().reportSemanticError(
					MessageFormat.format(FieldSubReference.INVALIDSUBREFERENCE, ((FieldSubReference) subreference).getId().getDisplayName(),
							getTypename()));
			return null;
		case parameterisedSubReference:
			subreference.getLocation().reportSemanticError(
					MessageFormat.format(FieldSubReference.INVALIDSUBREFERENCE, ((ParameterisedSubReference) subreference).getId().getDisplayName(),
							getTypename()));
			return null;
		default:
			subreference.getLocation().reportSemanticError(ISubReference.INVALIDSUBREFERENCE);
			return null;
		}
	}

	@Override
	public boolean isCompatible(CompilationTimeStamp timestamp, IType otherType, TypeCompatibilityInfo info,
			Chain leftChain, Chain rightChain) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean generatesOwnClass(JavaGenData aData, StringBuilder source) {
		return true;
	}

	@Override
	public String getGenNameTemplate(JavaGenData aData, StringBuilder source) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String getGenNameTypeDescriptor(JavaGenData aData, StringBuilder source) {
		// TODO Auto-generated method stub
		return null;
	}
	
	@Override
	/** {@inheritDoc} */
	public void check(final CompilationTimeStamp timestamp) {
		if (lastTimeChecked != null && !lastTimeChecked.isLess(timestamp)) {
			return;
		}
		
		if (modifiers.contains(ClassModifier.Final) && modifiers.contains(ClassModifier.Abstract)) {
			modifierLoc.reportSemanticError(SE_FINALVSABSTRACT);
		}
		
		if (extClass != null) {
			ISubReference sub = extClass.getSubreferences().get(0);
			
			Subreference_type type = sub.getReferenceType(); 
			if (type == null) {
				extClass.getLocation().reportSemanticError(SE_BADBASECLASS);
			}
		}
		
		if (finallyBlock != null) {
			finallyBlock.check(timestamp);
			finallyBlock.postCheck();
			finallyBlock.setCodeSection(CodeSectionType.CS_INLINE);
		}
	}
	
	public List<ClassModifier> getModifiers() {
		return modifiers; 
	}
	
	@Override
	/** {@inheritDoc} */
	public void setMyScope(final Scope scope) {
		super.setMyScope(scope);
		if (runsOnRef != null) {
			runsOnRef.setMyScope(scope);
		}
		if (mtcRef != null) {
			mtcRef.setMyScope(scope);
		}
		if (systemRef != null) {
			systemRef.setMyScope(scope);
		}
		if (finallyBlock != null) {
			bridgeScope = new NamedBridgeScope();
			bridgeScope.setParentScope(scope);
			finallyBlock.setMyScope(bridgeScope);
			scope.addSubScope(finallyBlock.getLocation(), finallyBlock);
		}
	}
	
	@Override
	/** {@inheritDoc} */
	public void findReferences(final ReferenceFinder referenceFinder, final List<Hit> foundIdentifiers) {
		super.findReferences(referenceFinder, foundIdentifiers);
	
		if (runsOnRef != null) {
			runsOnRef.findReferences(referenceFinder, foundIdentifiers);
		}
		if (finallyBlock != null) {
			finallyBlock.findReferences(referenceFinder, foundIdentifiers);
		}
	}
}
