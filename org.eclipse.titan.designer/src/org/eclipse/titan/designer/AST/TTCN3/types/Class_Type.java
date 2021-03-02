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

import org.eclipse.titan.designer.AST.Assignment;
import org.eclipse.titan.designer.AST.IReferenceChain;
import org.eclipse.titan.designer.AST.ISubReference;
import org.eclipse.titan.designer.AST.ISubReference.Subreference_type;
import org.eclipse.titan.designer.AST.IType;
import org.eclipse.titan.designer.AST.Identifier;
import org.eclipse.titan.designer.AST.Location;
import org.eclipse.titan.designer.AST.Reference;
import org.eclipse.titan.designer.AST.Type;
import org.eclipse.titan.designer.AST.TypeCompatibilityInfo;
import org.eclipse.titan.designer.AST.TypeCompatibilityInfo.Chain;
import org.eclipse.titan.designer.AST.TTCN3.Expected_Value_type;
import org.eclipse.titan.designer.AST.TTCN3.definitions.ClassModifier;
import org.eclipse.titan.designer.AST.TTCN3.templates.ITTCN3Template;
import org.eclipse.titan.designer.compiler.JavaGenData;
import org.eclipse.titan.designer.declarationsearch.Declaration;
import org.eclipse.titan.designer.parsers.CompilationTimeStamp;

/**
 * class type (TTCN-3).
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
	private final List<ClassModifier> modifiers;
	
	public Class_Type(List<ClassModifier> modifiers, final Location modifierLoc, Reference extClass, final Reference runsOnRef) {
		this.modifiers = modifiers;
		this.modifierLoc = modifierLoc;		
		this.extClass = extClass;
		this.runsOnRef = runsOnRef;
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
    	Identifier id = getIdentifier();
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
	public IType getFieldType(CompilationTimeStamp timestamp, Reference reference, int actualSubReference,
			Expected_Value_type expectedIndex, IReferenceChain refChain, boolean interruptIfOptional) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public boolean isCompatible(CompilationTimeStamp timestamp, IType otherType, TypeCompatibilityInfo info,
			Chain leftChain, Chain rightChain) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean generatesOwnClass(JavaGenData aData, StringBuilder source) {
		// TODO Auto-generated method stub
		return false;
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
	}
	
	public List<ClassModifier> getModifiers() {
		return modifiers; 
	}
}
