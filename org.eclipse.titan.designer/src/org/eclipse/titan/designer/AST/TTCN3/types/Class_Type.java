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
import java.util.Map;

import org.eclipse.titan.designer.AST.ArraySubReference;
import org.eclipse.titan.designer.AST.Assignment;
import org.eclipse.titan.designer.AST.FieldSubReference;
import org.eclipse.titan.designer.AST.GovernedSimple.CodeSectionType;
import org.eclipse.titan.designer.AST.IReferenceChain;
import org.eclipse.titan.designer.AST.ISubReference;
import org.eclipse.titan.designer.AST.ReferenceFinder.Hit;
import org.eclipse.titan.designer.AST.IType;
import org.eclipse.titan.designer.AST.ITypeWithComponents;
import org.eclipse.titan.designer.AST.Identifier;
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
import org.eclipse.titan.designer.AST.TTCN3.definitions.OopVisibilityModifier;
import org.eclipse.titan.designer.AST.TTCN3.statements.StatementBlock;
import org.eclipse.titan.designer.AST.TTCN3.templates.ITTCN3Template;
import org.eclipse.titan.designer.compiler.JavaGenData;
import org.eclipse.titan.designer.declarationsearch.Declaration;
import org.eclipse.titan.designer.parsers.CompilationTimeStamp;

/**
 * class type (TTCN-3)
 * 
 * @author Miklos Magyari
 */

public final class Class_Type extends Type implements ITypeWithComponents {
	private static final String SE_FINALVSABSTRACT = "A final class cannot be abstract.";
	private static final String SE_BADBASECLASS = "Only a class type can be used after 'extends'";
	private static final String SE_PUBLICFIELD = "Class fields cannot be public";
	private static final String SE_DUPLICATEFIELD = "{0} shadows inherited member {1}";
	
	private static final String CLASSTYPE_NAME = "class";

	private boolean isExternal;
	private final Location modifierLoc;
	private final Type extClass;
	private final Reference runsOnRef;
	private final Reference mtcRef;
	private final Reference systemRef;
	private final List<ClassModifier> modifiers;
	private final StatementBlock finallyBlock;
	private final CompFieldMap compFieldMap;

	private NamedBridgeScope bridgeScope = null;
	
	public Class_Type(final boolean isExternal, final List<ClassModifier> modifiers, final Location modifierLoc, Type extClass, 
			final Reference runsOnRef, final Reference mtcRef, final Reference systemRef,
			StatementBlock finallyBlock, CompFieldMap compFieldMap) {
		this.isExternal = isExternal;
		this.modifiers = modifiers;
		this.modifierLoc = modifierLoc;		
		this.extClass = extClass;
		this.runsOnRef = runsOnRef;
		this.mtcRef = mtcRef;
		this.systemRef = systemRef;
		this.finallyBlock = finallyBlock;
		
		this.compFieldMap = compFieldMap;
		compFieldMap.setMyType(this);
		compFieldMap.setFullNameParent(this);
	
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
    		sb.append(MessageFormat.format("extends {0}", extClass.getGenNameOwn()));
    	}
    	
    	if (finallyBlock != null) {
    		sb.append(" implements AutoCloseable ");
    	}
    	
    	sb.append(" {\n");
    	
    	if (finallyBlock != null) {
    		sb.append("\n\t\t@Override");
    		sb.append("\n\t\tpublic void close () throws Exception {\n");
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
		
		for (Map.Entry<String, CompField> entry : compFieldMap.getComponentFieldMap(timestamp).entrySet()) {
			CompField c = entry.getValue();
			if (c.getType().getTypetype() != Type_type.TYPE_REFERENCED) {
				if (c.getVisibility() == OopVisibilityModifier.Public) {
					c.getVisibilityLocation().reportSemanticError(SE_PUBLICFIELD);
				}
			}
		}
		
		if (extClass instanceof Referenced_Type) {
			Reference parentRef = ((Referenced_Type)extClass).getReference(); 
			Declaration d = parentRef.getReferencedDeclaration(parentRef.getSubreferences(0, 0).get(0));
			Assignment assignment = d.getAssignment();
			IType etype = assignment.getType(timestamp);
			if (! (etype instanceof Class_Type)) {
				extClass.getLocation().reportSemanticError(SE_BADBASECLASS);
			} else {
				Class_Type parent = (Class_Type)etype;
				CompFieldMap cfm = parent.getCompFieldMap();
				for (Map.Entry<String, CompField> entry : cfm.getComponentFieldMap(timestamp).entrySet()) {
					CompField existing = compFieldMap.getCompWithName(entry.getKey());
					CompField inherited = entry.getValue();
					if (existing == null) {
						inherited.setInherited(true);
						compFieldMap.addComp(inherited);
					} else {
						if (existing.isInherited() == false) {
							if (inherited.isAbstract() == false) {
								existing.getLocation().reportSemanticError(MessageFormat.format(SE_DUPLICATEFIELD, 
										existing.getFullName(), inherited.getFullName()));
							} 
						}
					}
				}
			}
		} 
		
		if (modifiers.contains(ClassModifier.Final) && modifiers.contains(ClassModifier.Abstract)) {
			modifierLoc.reportSemanticError(SE_FINALVSABSTRACT);
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
		if (extClass != null) {
			extClass.setMyScope(scope.getParentScope());
		}
		if (finallyBlock != null) {
			bridgeScope = new NamedBridgeScope();
			bridgeScope.setParentScope(scope);
			finallyBlock.setMyScope(bridgeScope);
			scope.addSubScope(finallyBlock.getLocation(), finallyBlock);
		}
		if (compFieldMap != null) {
			compFieldMap.setMyScope(scope);
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

	@Override
	/** {@inheritDoc} */
	public Identifier getComponentIdentifierByName(final Identifier identifier) {
		if(identifier == null){
			return null;
		}
		final CompField cf = getComponentByName(identifier.getName());
		return cf == null ? null : cf.getIdentifier();
	}
	
	/**
	 * Returns the element with the specified name.
	 *
	 * @param name the name of the element to return
	 * @return the element with the specified name in this list, or null if none was found
	 */
	public final CompField getComponentByName(final String name) {
		if (compFieldMap.componentFieldMap == null) {
			return null;
		}

		return compFieldMap.componentFieldMap.get(name);
	}
	
	@Override
	/** {@inheritDoc} */
	public final IType getFieldType(final CompilationTimeStamp timestamp, final Reference reference, final int actualSubReference,
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
			final Identifier id = subreference.getId();
			final CompField compField = compFieldMap.getCompWithName(id);
			if (compField == null) {
				subreference.getLocation().reportSemanticError(
						MessageFormat.format(FieldSubReference.NONEXISTENTSUBREFERENCE, ((FieldSubReference) subreference).getId().getDisplayName(),
								getTypename()));
				return null;
			}

			final IType fieldType = compField.getType();
			if (fieldType == null) {
				return null;
			}

			if (interruptIfOptional && compField.isOptional()) {
				return null;
			}

			final Expected_Value_type internalExpectation =
					expectedIndex == Expected_Value_type.EXPECTED_TEMPLATE ? Expected_Value_type.EXPECTED_DYNAMIC_VALUE : expectedIndex;
			//This is the recursive function call:
			return fieldType.getFieldType(timestamp, reference, actualSubReference + 1, internalExpectation, refChain, interruptIfOptional);
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
	
	public CompFieldMap getCompFieldMap() {
		return compFieldMap;
	}
}
