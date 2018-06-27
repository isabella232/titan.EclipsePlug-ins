/******************************************************************************
 * Copyright (c) 2000-2018 Ericsson Telecom AB
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/org/documents/epl-2.0/EPL-2.0.html
 ******************************************************************************/
package org.eclipse.titan.designer.AST.TTCN3.definitions;

import java.math.BigInteger;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.titan.common.logging.ErrorReporter;
import org.eclipse.titan.designer.AST.ASTVisitor;
import org.eclipse.titan.designer.AST.ArraySubReference;
import org.eclipse.titan.designer.AST.GovernedSimple.CodeSectionType;
import org.eclipse.titan.designer.AST.INamedNode;
import org.eclipse.titan.designer.AST.IReferenceChain;
import org.eclipse.titan.designer.AST.ISubReference;
import org.eclipse.titan.designer.AST.ISubReference.Subreference_type;
import org.eclipse.titan.designer.AST.IType.Type_type;
import org.eclipse.titan.designer.AST.IValue;
import org.eclipse.titan.designer.AST.IValue.Value_type;
import org.eclipse.titan.designer.AST.Identifier;
import org.eclipse.titan.designer.AST.Location;
import org.eclipse.titan.designer.AST.NamingConventionHelper;
import org.eclipse.titan.designer.AST.Reference;
import org.eclipse.titan.designer.AST.ReferenceChain;
import org.eclipse.titan.designer.AST.ReferenceFinder;
import org.eclipse.titan.designer.AST.ReferenceFinder.Hit;
import org.eclipse.titan.designer.AST.Scope;
import org.eclipse.titan.designer.AST.Value;
import org.eclipse.titan.designer.AST.TTCN3.Expected_Value_type;
import org.eclipse.titan.designer.AST.TTCN3.types.ComponentTypeBody;
import org.eclipse.titan.designer.AST.TTCN3.values.ArrayDimension;
import org.eclipse.titan.designer.AST.TTCN3.values.ArrayDimensions;
import org.eclipse.titan.designer.AST.TTCN3.values.Integer_Value;
import org.eclipse.titan.designer.AST.TTCN3.values.Real_Value;
import org.eclipse.titan.designer.AST.TTCN3.values.SequenceOf_Value;
import org.eclipse.titan.designer.AST.TTCN3.values.expressions.ExpressionStruct;
import org.eclipse.titan.designer.compiler.JavaGenData;
import org.eclipse.titan.designer.editors.ProposalCollector;
import org.eclipse.titan.designer.editors.actions.DeclarationCollector;
import org.eclipse.titan.designer.parsers.CompilationTimeStamp;
import org.eclipse.titan.designer.parsers.ttcn3parser.IIdentifierReparser;
import org.eclipse.titan.designer.parsers.ttcn3parser.IdentifierReparser;
import org.eclipse.titan.designer.parsers.ttcn3parser.ReParseException;
import org.eclipse.titan.designer.parsers.ttcn3parser.TTCN3ReparseUpdater;
import org.eclipse.titan.designer.parsers.ttcn3parser.Ttcn3Lexer;
import org.eclipse.titan.designer.preferences.PreferenceConstants;

/**
 * The Def_Timer class represents TTCN3 timer definitions.
 * <p>
 * Timers in TTCN3 does not have a type.
 *
 * @author Kristof Szabados
 * @author Farkas Izabella Ingrid
 * */
public final class Def_Timer extends Definition {
	private static final String NEGATIVDURATIONERROR = "A non-negative float value was expected as timer duration instead of {0}";
	private static final String INFINITYDURATIONERROR = "{0} can not be used as the default timer duration";
	private static final String OPERANDERROR = "The default timer duration should be a float value";

	private static final String FULLNAMEPART1 = ".<dimensions>";
	private static final String FULLNAMEPART2 = ".<default_duration>";
	private static final String KIND = "timer";

	private final ArrayDimensions dimensions;
	private final Value defaultDuration;

	public Def_Timer(final Identifier identifier, final ArrayDimensions dimensions, final Value defaultDuration) {
		super(identifier);
		this.dimensions = dimensions;
		this.defaultDuration = defaultDuration;

		if (dimensions != null) {
			dimensions.setFullNameParent(this);
		}
		if (defaultDuration != null) {
			defaultDuration.setFullNameParent(this);
		}
	}

	@Override
	/** {@inheritDoc} */
	public Assignment_type getAssignmentType() {
		return Assignment_type.A_TIMER;
	}

	@Override
	/** {@inheritDoc} */
	public void setMyScope(final Scope scope) {
		super.setMyScope(scope);
		if (dimensions != null) {
			dimensions.setMyScope(scope);
		}
		if (defaultDuration != null) {
			defaultDuration.setMyScope(scope);
		}
	}

	@Override
	/** {@inheritDoc} */
	public StringBuilder getFullName(final INamedNode child) {
		final StringBuilder builder = super.getFullName(child);

		if (dimensions == child) {
			return builder.append(FULLNAMEPART1);
		} else if (defaultDuration == child) {
			return builder.append(FULLNAMEPART2);
		}

		return builder;
	}

	@Override
	/** {@inheritDoc} */
	public String getAssignmentName() {
		return "timer";
	}

	@Override
	/** {@inheritDoc} */
	public String getDescription() {
		final StringBuilder builder = new StringBuilder();
		builder.append(getAssignmentName()).append(" `");

		if (isLocal()) {
			builder.append(identifier.getDisplayName());
		} else {
			builder.append(getFullName());
		}

		builder.append('\'');
		return builder.toString();
	}

	@Override
	/** {@inheritDoc} */
	public String getOutlineIcon() {
		return "timer.gif";
	}

	@Override
	/** {@inheritDoc} */
	public String getProposalKind() {
		return KIND;
	}

	public ArrayDimensions getDimensions() {
		return dimensions;
	}

	/**
	 * Returns false if it is sure that the timer referred by array indices
	 * reference does not have a default duration. Otherwise it returns
	 * true.
	 *
	 * @param timestamp
	 *                the timestamp of the actual semantic check cycle
	 * @param reference
	 *                might be NULL when examining a single timer.
	 *
	 * @return true if the timer has a default duration, false otherwise.
	 * */
	public boolean hasDefaultDuration(final CompilationTimeStamp timestamp, final Reference reference) {
		if (defaultDuration == null) {
			return false;
		} else if (dimensions == null || reference == null) {
			return true;
		}

		IValue v = defaultDuration;
		final List<ISubReference> subreferences = reference.getSubreferences();
		final int nofDimensions = dimensions.size();
		final int nofReferences = subreferences.size() - 1;
		final int upperLimit = (nofDimensions < nofReferences) ? nofDimensions : nofReferences;
		for (int i = 0; i < upperLimit; i++) {
			v = v.getValueRefdLast(timestamp, null);
			if (Value_type.SEQUENCEOF_VALUE.equals(v.getValuetype())) {
				final ISubReference ref = subreferences.get(i + 1);
				if (!Subreference_type.arraySubReference.equals(ref.getReferenceType())) {
					return true;
				}

				final IValue index = ((ArraySubReference) ref).getValue();
				if (!Value_type.INTEGER_VALUE.equals(index.getValuetype())) {
					return true;
				}

				final long realIndex = ((Integer_Value) index).getValue() - dimensions.get(i).getOffset();
				if (realIndex >= 0 && realIndex < ((SequenceOf_Value) v).getNofComponents()) {
					v = ((SequenceOf_Value) v).getValueByIndex((int) realIndex);
				}
			}
		}
		return !Value_type.NOTUSED_VALUE.equals(v.getValuetype());
	}

	@Override
	/** {@inheritDoc} */
	public void check(final CompilationTimeStamp timestamp) {
		check(timestamp, null);
	}

	@Override
	/** {@inheritDoc} */
	public void check(final CompilationTimeStamp timestamp, final IReferenceChain refChain) {
		if (lastTimeChecked != null && !lastTimeChecked.isLess(timestamp)) {
			return;
		}
		lastTimeChecked = timestamp;
		isUsed = false;

		if (getMyScope() instanceof ComponentTypeBody) {
			NamingConventionHelper.checkConvention(PreferenceConstants.REPORTNAMINGCONVENTION_COMPONENT_TIMER, identifier, this);
		} else if (isLocal()) {
			NamingConventionHelper.checkConvention(PreferenceConstants.REPORTNAMINGCONVENTION_LOCAL_TIMER, identifier, this);
		} else {
			NamingConventionHelper.checkConvention(PreferenceConstants.REPORTNAMINGCONVENTION_GLOBAL_TIMER, identifier, this);
		}
		NamingConventionHelper.checkNameContents(identifier, getMyScope().getModuleScope().getIdentifier(), getDescription());

		if (dimensions != null) {
			dimensions.check(timestamp);
		}

		if (defaultDuration != null) {
			if (dimensions == null) {
				defaultDuration.setLoweridToReference(timestamp);
				final Type_type tempType = defaultDuration.getExpressionReturntype(timestamp,
						isLocal() ? Expected_Value_type.EXPECTED_DYNAMIC_VALUE : Expected_Value_type.EXPECTED_STATIC_VALUE);

				switch (tempType) {
				case TYPE_REAL:
					final IValue last = defaultDuration.getValueRefdLast(timestamp, null);
					if (!last.isUnfoldable(timestamp)) {
						final Real_Value real = (Real_Value) last;
						final double value = real.getValue();
						if (value < 0.0f) {
							defaultDuration.getLocation().reportSemanticError(
									MessageFormat.format(NEGATIVDURATIONERROR, value));
						} else if (real.isPositiveInfinity()) {
							final String message = MessageFormat.format(INFINITYDURATIONERROR, real.createStringRepresentation());
							defaultDuration.getLocation().reportSemanticError(message);
						}
					}
					return;
				default:
					defaultDuration.getLocation().reportSemanticError(OPERANDERROR);
				}
			} else {
				checkArrayDuration(timestamp,defaultDuration, 0);
			}

			defaultDuration.setCodeSection(CodeSectionType.CS_POST_INIT);
		}

		if (withAttributesPath != null) {
			withAttributesPath.checkGlobalAttributes(timestamp, false);
			withAttributesPath.checkAttributes(timestamp);
		}

		lastTimeChecked = timestamp;
	}

	private void checkSingleDuration(final CompilationTimeStamp timestamp, final IValue duration){
		final IReferenceChain referenceChain = ReferenceChain.getInstance(IReferenceChain.CIRCULARREFERENCE, true);
		final Value v = (Value) duration.getValueRefdLast(timestamp, referenceChain);
		referenceChain.release();

		if (v.getValuetype() == Value_type.REAL_VALUE) {
			final Real_Value value = (Real_Value) v;
			final double valueReal = value.getValue();
			if (valueReal < 0.0 || value.isSpecialFloat()) {
				duration.getLocation().reportSemanticError("A non-negative float value was expected as timer duration instead of" + valueReal);
			}
		} else {
			duration.getLocation().reportSemanticError("Value is not real");
		}
	}

	private void checkArrayDuration(final CompilationTimeStamp timestamp, final IValue duration, final int startDimension) {
		final ArrayDimension dim = dimensions.get(startDimension);
		final boolean arraySizeKnown = !dim.getIsErroneous(timestamp);
		int arraySize = 0;
		if (arraySizeKnown) {
			arraySize = (int) dim.getSize();
		}

		final IReferenceChain referenceChain = ReferenceChain.getInstance(IReferenceChain.CIRCULARREFERENCE, true);
		final Value v = (Value) duration.getValueRefdLast(timestamp, referenceChain);
		referenceChain.release();

		if(v.getIsErroneous(timestamp)) {
			//error
			return;
		}

		if (v.getValuetype() == Value_type.SEQUENCEOF_VALUE) {
			final SequenceOf_Value value = (SequenceOf_Value) v;
			final int nofComp = value.getNofComponents();

			// Value-list notation.
			if (!value.isIndexed()) {
				if (arraySizeKnown) {
					if (arraySize > nofComp) {
						duration.getLocation().reportSemanticError("Too few elements in the default duration of timer array: "
													+ arraySize + " was expected instead of " + nofComp);
					} else if (arraySize < nofComp) {
						duration.getLocation().reportSemanticError("Too many elements in the default duration of timer array: "
								+ arraySize + " was expected instead of " + nofComp );
					}
				}

				final boolean last_dim = startDimension + 1 >= dimensions.size();
				for (int i = 0; i < nofComp; ++i) {
					final IValue array_v = value.getValueByIndex(i);
					if (array_v.getValuetype() == Value_type.NOTUSED_VALUE) {
						continue;
					}
					if (last_dim) {
						checkSingleDuration(timestamp, array_v);
					} else {
						checkArrayDuration(timestamp, array_v, startDimension + 1);
					}
				}
			} else {
				// Indexed-notation.
				final boolean last_dim = startDimension + 1 >= dimensions.size();
				final Map<Integer, Integer> indexMap = new HashMap<Integer, Integer>();

				for (int i = 0; i < nofComp; ++i) {
					final IValue array_v = value.getValueByIndex(i);
					if (array_v.getValuetype() == Value_type.NOTUSED_VALUE) {
						continue;
					}
					if (last_dim) {
						checkSingleDuration(timestamp, array_v);
					} else {
						checkArrayDuration(timestamp, array_v, startDimension + 1);
					}

					final IValue array_index = value.getIndexByIndex(i);
					dim.checkIndex(timestamp, array_index, Expected_Value_type.EXPECTED_DYNAMIC_VALUE);
					final IValue tmp = array_index.getValueRefdLast(timestamp, referenceChain);
					if (tmp.getValuetype() == Value_type.INTEGER_VALUE) {
						final BigInteger index = ((Integer_Value) tmp).getValueValue();
						if (index.compareTo(BigInteger.valueOf( Integer.MAX_VALUE)) > 0) {
							array_index.getLocation().reportSemanticError(MessageFormat.format("An integer value less than {0} was expected for indexing timer array instead of {1}", Integer.MAX_VALUE, index));
							array_index.setIsErroneous(true);
						} else {
							final int IndexValue =  index.intValue();
							if (indexMap.containsKey(IndexValue)) {
								array_index.getLocation().reportSemanticError(MessageFormat.format("Duplicate index value {0} for timer array elements {1} and {2}", index, i+1, indexMap.get(IndexValue)));
								array_index.setIsErroneous(true);
							} else {
								indexMap.put(IndexValue, i+1);
							}
						}
					}
				}
				// It's not possible to have "indexMap.size() > arraySize", since we
		        // add only correct constant-index values into the map.  It's possible
		        // to create partially initialized timer arrays.
				indexMap.clear();
			}
		} else {
			if (arraySizeKnown) {
				duration.getLocation().reportSemanticError("An array value (with " + arraySize + " elements) was expected as default duration of timer array");
			} else {
				duration.getLocation().reportSemanticError("An array value was expected as default duration of timer array");
			}
			duration.setIsErroneous(true);
		}
	}

	@Override
	/** {@inheritDoc} */
	public boolean checkIdentical(final CompilationTimeStamp timestamp, final Definition definition) {
		check(timestamp);
		definition.check(timestamp);

		if (!Assignment_type.A_TIMER.semanticallyEquals(definition.getAssignmentType())) {
			location.reportSemanticError(MessageFormat.format(
					"Local definition `{0}'' is a timer, but the definition inherited from component type `{1}'' is a {2}",
					identifier.getDisplayName(), definition.getMyScope().getFullName(), definition.getAssignmentName()));
			return false;
		}

		final Def_Timer otherTimer = (Def_Timer) definition;
		if (dimensions != null) {
			if (otherTimer.dimensions != null) {
				if (!dimensions.isIdenticial(timestamp, otherTimer.dimensions)) {
					location.reportSemanticError(MessageFormat
							.format("Local timer `{0}'' and the timer inherited from component type `{1}'' have different array dimensions",
									identifier.getDisplayName(), otherTimer.getMyScope().getFullName()));
					return false;
				}
			} else {
				location.reportSemanticError(MessageFormat
						.format("Local definition `{0}'' is a timer array, but the definition inherited from component type `{1}'' is a single timer",
								identifier.getDisplayName(), otherTimer.getMyScope().getFullName()));
				return false;
			}
		} else if (otherTimer.dimensions != null) {
			location.reportSemanticError(MessageFormat
					.format("Local definition `{0}'' is a single timer, but the definition inherited from component type `{1}'' is a timer array",
							identifier.getDisplayName(), otherTimer.getMyScope().getFullName()));
			return false;
		}

		if (defaultDuration != null) {
			if (otherTimer.defaultDuration != null) {
				if (!defaultDuration.isUnfoldable(timestamp) && !otherTimer.defaultDuration.isUnfoldable(timestamp)
						&& !defaultDuration.checkEquality(timestamp, otherTimer.defaultDuration)) {
					final String message = MessageFormat
							.format("Local timer `{0}'' and the timer inherited from component type `{1}'' have different default durations",
									identifier.getDisplayName(), otherTimer.getMyScope().getFullName());
					defaultDuration.getLocation().reportSemanticWarning(message);
				}
			} else {
				final String message = MessageFormat
						.format("Local timer `{0}'' has default duration, but the timer inherited from component type `{1}'' does not",
								identifier.getDisplayName(), otherTimer.getMyScope().getFullName());
				defaultDuration.getLocation().reportSemanticWarning(message);
			}
		} else if (otherTimer.defaultDuration != null) {
			location.reportSemanticWarning(MessageFormat.format(
					"Local timer `{0}'' does not have default duration, but the timer inherited from component type `{1}'' has",
					identifier.getDisplayName(), otherTimer.getMyScope().getFullName()));
		}

		return true;
	}

	@Override
	/** {@inheritDoc} */
	public void addProposal(final ProposalCollector propCollector, final int i) {
		final List<ISubReference> subrefs = propCollector.getReference().getSubreferences();
		if (subrefs.size() <= i || !Subreference_type.fieldSubReference.equals(subrefs.get(i).getReferenceType())) {
			return;
		}

		if (subrefs.size() == i + 1 && identifier.getName().toLowerCase().startsWith(subrefs.get(i).getId().getName().toLowerCase())) {
			super.addProposal(propCollector, i);
		}
		if (identifier.getName().equals(subrefs.get(i).getId().getName())) {
			// perfect match
			// do as if timers had a type
			Timer.addProposal(propCollector, i + 1);
		}
	}

	@Override
	/** {@inheritDoc} */
	public void addDeclaration(final DeclarationCollector declarationCollector, final int i) {
		final List<ISubReference> subrefs = declarationCollector.getReference().getSubreferences();
		if (subrefs.size() > i && identifier.getName().equals(subrefs.get(i).getId().getName())) {
			if (subrefs.size() == i + 1 && Subreference_type.fieldSubReference.equals(subrefs.get(i).getReferenceType())) {
				declarationCollector.addDeclaration(this);
			}
		}
	}

	@Override
	/** {@inheritDoc} */
	public List<Integer> getPossibleExtensionStarterTokens() {
		final List<Integer> result = super.getPossibleExtensionStarterTokens();

		if (defaultDuration == null) {
			result.add(Ttcn3Lexer.ASSIGNMENTCHAR);
		}

		return result;
	}

	@Override
	/** {@inheritDoc} */
	public void updateSyntax(final TTCN3ReparseUpdater reparser, final boolean isDamaged) throws ReParseException {
		if (isDamaged) {
			lastTimeChecked = null;
			int result = 1;
			final Location tempIdentifier = identifier.getLocation();
			if (reparser.envelopsDamage(tempIdentifier) || reparser.isExtending(tempIdentifier)) {
				reparser.extendDamagedRegion(tempIdentifier);
				final IIdentifierReparser r = new IdentifierReparser(reparser);
				result = r.parseAndSetNameChanged();
				identifier = r.getIdentifier();
				if (result != 0) {
					throw new ReParseException(result);
				}

				if (dimensions != null) {
					dimensions.updateSyntax(reparser, false);
				}

				if (defaultDuration != null) {
					defaultDuration.updateSyntax(reparser, false);
					reparser.updateLocation(defaultDuration.getLocation());
				}

				if (withAttributesPath != null) {
					withAttributesPath.updateSyntax(reparser, false);
					reparser.updateLocation(withAttributesPath.getLocation());
				}

				return;
			}

			throw new ReParseException();
		}

		reparser.updateLocation(identifier.getLocation());
		if (dimensions != null) {
			dimensions.updateSyntax(reparser, false);
		}

		if (defaultDuration != null) {
			defaultDuration.updateSyntax(reparser, false);
			reparser.updateLocation(defaultDuration.getLocation());
		}

		if (withAttributesPath != null) {
			withAttributesPath.updateSyntax(reparser, false);
			reparser.updateLocation(withAttributesPath.getLocation());
		}
	}

	@Override
	/** {@inheritDoc} */
	public void findReferences(final ReferenceFinder referenceFinder, final List<Hit> foundIdentifiers) {
		super.findReferences(referenceFinder, foundIdentifiers);
		if (dimensions != null) {
			dimensions.findReferences(referenceFinder, foundIdentifiers);
		}
		if (defaultDuration != null) {
			defaultDuration.findReferences(referenceFinder, foundIdentifiers);
		}
	}

	@Override
	/** {@inheritDoc} */
	protected boolean memberAccept(final ASTVisitor v) {
		if (!super.memberAccept(v)) {
			return false;
		}
		if (dimensions != null && !dimensions.accept(v)) {
			return false;
		}
		if (defaultDuration != null && !defaultDuration.accept(v)) {
			return false;
		}
		return true;
	}

	@Override
	/** {@inheritDoc} */
	public void generateCode( final JavaGenData aData, final boolean cleanUp ) {
		final String genName = getGenName();
		final StringBuilder sb = aData.getSrc();
		final StringBuilder initComp = aData.getInitComp();
		final StringBuilder source = new StringBuilder();
		if ( !isLocal() ) {
			if(VisibilityModifier.Private.equals(getVisibilityModifier())) {
				source.append( "private" );
			} else {
				source.append( "public" );
			}
			source.append( " static " );
		}

		aData.addBuiltinTypeImport( "TitanTimer" );

		if (getMyScope() instanceof ComponentTypeBody) {
			if(dimensions == null) {
				// single timer instance
				if (defaultDuration == null) {
					source.append(MessageFormat.format("ThreadLocal<TitanTimer> {0} = new ThreadLocal<TitanTimer>() '{'\n", genName));
					source.append("@Override\n" );
					source.append("protected TitanTimer initialValue() {\n");
					source.append(MessageFormat.format("return new TitanTimer(\"{0}\");\n", identifier.getDisplayName()));
					source.append("}\n");
					source.append("};\n");
				} else {
					if (defaultDuration.canGenerateSingleExpression()) {
						//known in compile time
						source.append(MessageFormat.format("ThreadLocal<TitanTimer> {0} = new ThreadLocal<TitanTimer>() '{'\n", genName));
						source.append("@Override\n" );
						source.append("protected TitanTimer initialValue() {\n");
						source.append(MessageFormat.format("return new TitanTimer(\"{1}\", {2});\n", genName, identifier.getDisplayName(), defaultDuration.generateSingleExpression(aData)));
						source.append("}\n");
						source.append("};\n");
					} else {
						source.append(MessageFormat.format("ThreadLocal<TitanTimer> {0} = new ThreadLocal<TitanTimer>() '{'\n", genName));
						source.append("@Override\n" );
						source.append("protected TitanTimer initialValue() {\n");
						source.append(MessageFormat.format("return new TitanTimer(\"{1}\");\n", genName, identifier.getDisplayName()));
						source.append("}\n");
						source.append("};\n");

						final ExpressionStruct expression = new ExpressionStruct();
						expression.expression.append(genName);
						expression.expression.append(".get().setDefaultDuration(");

						defaultDuration.generateCodeExpression(aData, expression, true);

						expression.expression.append(')');
						expression.mergeExpression(aData.getPostInit());
					}
				}


				if ( defaultDuration != null ) {
					defaultDuration.generateCodeInit(aData, initComp, genName + ".get()" );
				} else if (cleanUp) {
					initComp.append(genName);
					initComp.append(".get().cleanUp();\n");
				}

			} else {
				aData.addBuiltinTypeImport("TitanTimerArray");

				final ArrayList<String> classNames= new ArrayList<String>();
				final ExpressionStruct expression = new ExpressionStruct();
				final String elementName = generateClassCode(aData, sb, classNames);

				source.append(MessageFormat.format("ThreadLocal<{0}> {1} = new ThreadLocal<{0}>() '{'\n", elementName, genName));
				source.append("@Override\n" );
				source.append(MessageFormat.format("protected {0} initialValue() '{'\n", elementName));
				source.append(MessageFormat.format("return new {0}();\n",elementName));
				source.append("}\n");
				source.append("};\n");

				if (defaultDuration != null) {
					generateCodeArrayDuration(aData, initComp, genName + ".get()", classNames, defaultDuration, 0);
				}

				expression.expression.append(genName);
				expression.expression.append(".get().setName(\"");
				expression.expression.append(identifier.getDisplayName());
				expression.expression.append("\");\n");

				expression.mergeExpression(aData.getPreInit());
			}
		} else {
			if(dimensions == null) {
				// single timer instance
				if (defaultDuration == null) {
					source.append(MessageFormat.format("TitanTimer {0} = new TitanTimer(\"{1}\");\n", genName, identifier.getDisplayName()));
				} else {
					if (defaultDuration.canGenerateSingleExpression()) {
						//known in compile time
						source.append(MessageFormat.format("TitanTimer {0} = new TitanTimer(\"{1}\", {2});\n", genName, identifier.getDisplayName(), defaultDuration.generateSingleExpression(aData)));
					} else {
						source.append(MessageFormat.format("TitanTimer {0} = new TitanTimer(\"{1}\");\n", genName, identifier.getDisplayName()));

						final ExpressionStruct expression = new ExpressionStruct();
						expression.expression.append(genName);
						expression.expression.append(".setDefaultDuration(");

						defaultDuration.generateCodeExpression(aData, expression, true);

						expression.expression.append(')');
						expression.mergeExpression(aData.getPostInit());
					}
				}


				if ( defaultDuration != null ) {
					defaultDuration.generateCodeInit(aData, initComp, genName );
				} else if (cleanUp) {
					initComp.append(genName);
					initComp.append(".cleanUp();\n");
				}

			} else {
				final ArrayList<String> classNames= new ArrayList<String>();
				final ExpressionStruct expression = new ExpressionStruct();
				aData.addBuiltinTypeImport("TitanTimerArray");
				final String elementName = generateClassCode(aData, sb, classNames);
				source.append(MessageFormat.format(" {0} {1} = new {0}();\n",elementName, genName));

				if (defaultDuration != null) {
					generateCodeArrayDuration(aData, initComp, genName, classNames, defaultDuration, 0);
				}

				expression.expression.append(genName);
				expression.expression.append(".setName(\"");
				expression.expression.append(identifier.getDisplayName());
				expression.expression.append("\");\n");

				expression.mergeExpression(aData.getPreInit());
			}
		}

		sb.append(source);

	}

	@Override
	/** {@inheritDoc} */
	public void generateCodeString(final JavaGenData aData, final StringBuilder source) {
		final String genName = getGenName();
		aData.addBuiltinTypeImport( "TitanTimer" );

		if (defaultDuration != null) {
			defaultDuration.setGenNameRecursive(getGenName());
		}

		if(dimensions == null) {
			// single timer instance
			if (defaultDuration == null) {
				source.append(MessageFormat.format("TitanTimer {0} = new TitanTimer(\"{1}\");\n", genName, identifier.getDisplayName()));
			} else {
				if (defaultDuration.canGenerateSingleExpression()) {
					//known in compile time
					source.append(MessageFormat.format("TitanTimer {0} = new TitanTimer(\"{1}\", {2});\n", genName, identifier.getDisplayName(), defaultDuration.generateSingleExpression(aData)));
				} else {
					source.append(MessageFormat.format("TitanTimer {0} = new TitanTimer(\"{1}\");\n", genName, identifier.getDisplayName()));

					final ExpressionStruct expression = new ExpressionStruct();
					expression.expression.append(genName);
					expression.expression.append(".setDefaultDuration(");

					defaultDuration.generateCodeExpression(aData, expression, true);

					expression.expression.append(')');
					expression.mergeExpression(source);
				}
			}
		} else {
			final ArrayList<String> classNames = new ArrayList<String>();
			aData.addBuiltinTypeImport("TitanTimerArray");

			final StringBuilder sb = aData.getCodeForType(genName);
			final String elementName = generateClassCode(aData, sb, classNames);
			source.append(MessageFormat.format(" {0} {1} = new {0}();\n",elementName, genName));


			if (defaultDuration != null) {
				generateCodeArrayDuration(aData, source, genName, classNames, defaultDuration, 0);
			}

			source.append(genName).append(".setName(\"").append(identifier.getDisplayName()).append("\");\n");
		}
	}

	private void generateCodeArrayDuration(final JavaGenData aData, final StringBuilder source, final String genName, final ArrayList<String> classNames, final Value defaultDuration2,final int startDim) {
		final ArrayDimension dim = dimensions.get(startDim);
		final int dim_size = (int) dim.getSize();

		final IReferenceChain referenceChain = ReferenceChain.getInstance(IReferenceChain.CIRCULARREFERENCE, true);
		final Value v = (Value) defaultDuration2.getValueRefdLast(CompilationTimeStamp.getBaseTimestamp(), referenceChain);
		referenceChain.release();

		if (v.getValuetype() != Value_type.SEQUENCEOF_VALUE) {
			// FIXME: throw FATAL_ERROR("Def_Timer::generate_code_array_duration()"); ErrorReporter.INTERNAL_ERROR()
			return;
		}

		final SequenceOf_Value value = (SequenceOf_Value) v;
		if (value.getNofComponents() != dim_size && !value.isIndexed()) {
			ErrorReporter.INTERNAL_ERROR("Code generator reached erroneous definition `" + getFullName() + "''");
			return;
		}

		// Value-list notation.
		if (!value.isIndexed()) {
			if (startDim + 1 < dimensions.size()) {
				// There are more dimensions, the elements of "value" are arrays a
				// temporary reference shall be introduced if the next dimension has more than 1 elements.
				// boolean temp_ref_needed = dimensions.get(startDim + 1).getSize() > 1;
				for (int i = 0; i < dim_size; i++) {
					final IValue v_elem = value.getValueByIndex(i);// get_comp_byIndex(i);
					if (v_elem.getValuetype() == Value_type.NOTUSED_VALUE) {
						continue;
					}
					final String embeddedName = MessageFormat.format("{0}.getAt({1})", genName, i + dim.getOffset());
					generateCodeArrayDuration(aData, source, embeddedName, classNames, (Value) v_elem, startDim + 1);
				}
			} else {
				// We are in the last dimension, the elements of "value" are floats.
				for (int i = 0; i < dim_size; i++) {
					final IValue v_elem = value.getValueByIndex(i);
					if (v_elem.getValuetype() == Value_type.NOTUSED_VALUE) {
						continue;
					}
					final ExpressionStruct expression = new ExpressionStruct();
					expression.expression.append(genName);
					expression.expression.append(".getAt(").append(i + dim.getOffset()).append(")");
					expression.expression.append(".assign("); // originally set_default_duration(obj_name, i)

					v_elem.generateCodeExpression(aData, expression, true);

					expression.expression.append(')');
					expression.mergeExpression(source);
				}
			}
			// Indexed-list notation.
		} else {
			if (startDim + 1 < dimensions.size()) {
				// boolean temp_ref_needed = dimensions.get(startDim + 1).getSize() > 1;
				for (int i = 0; i < value.getNofComponents(); ++i) {
					final IValue v_elem = value.getValueByIndex(i);
					final IValue index = value.getIndexByIndex(i);

					if (v_elem.getValuetype() == Value_type.NOTUSED_VALUE) {
						continue;
					}

					final String tempId1 = aData.getTemporaryVariableName();
					final String tempIdX = aData.getTemporaryVariableName();
					source.append("{\n");
					source.append(MessageFormat.format("final TitanInteger {0} = new TitanInteger();\n", tempIdX));
					index.generateCodeInit(aData, source, tempIdX);

					source.append(MessageFormat.format("final {0} {1} = {2}.getAt({3});\n", classNames.get(classNames.size() - startDim - 2), tempId1, genName, tempIdX));
					generateCodeArrayDuration(aData, source, tempId1, classNames, (Value) v_elem, startDim + 1);
					source.append("}\n");
				}
			} else {
				for (int i = 0; i < value.getNofComponents(); ++i) {
					final IValue v_elem = value.getValueByIndex(i);
					final IValue v_elemIndex = value.getIndexByIndex(i);
					if (v_elem.getValuetype() == Value_type.NOTUSED_VALUE) {
						continue;
					}

					final ExpressionStruct expression = new ExpressionStruct();
					final String tempIdX = aData.getTemporaryVariableName();
					source.append("{\n");
					source.append(MessageFormat.format("final TitanInteger {0} = new TitanInteger();\n", tempIdX));
					v_elemIndex.generateCodeInit(aData, source, tempIdX);

					final String embeddedName = MessageFormat.format("{0}.getAt(", genName);
					expression.expression.append(embeddedName).append(tempIdX).append(")");
					expression.expression.append(".assign("); // originally set_default_duration(obj_name, i)

					v_elem.generateCodeExpression(aData, expression, true);

					expression.expression.append(')');
					expression.mergeExpression(source);
					source.append("}\n");
				}
			}
		}

		return;
	}

	private String generateClassCode(final JavaGenData aData, final StringBuilder sb, final ArrayList<String> list) {
		String tempId1 = "TitanTimer";
		for (int i = 0; i < dimensions.size(); ++i) {
			final ArrayDimension dim = dimensions.get(dimensions.size() - i - 1);
			final String tempId2 = aData.getTemporaryVariableName();
			list.add(tempId2);
			sb.append(MessageFormat.format("public static class {0} extends TitanTimerArray<{1}> '{'\n", tempId2, tempId1));
			sb.append(MessageFormat.format("public {0}() '{'\n", tempId2));
			sb.append(MessageFormat.format("super({0}.class, {1}, {2});\n", tempId1, dim.getSize(), dim.getOffset()));
			sb.append("}\n");
			sb.append(MessageFormat.format("public {0}({0} otherValue) '{'\n", tempId2));
			sb.append("super(otherValue);\n");
			sb.append("}\n }\n\n");
			tempId1 = tempId2;
		}
		return tempId1;
	}

	@Override
	/** {@inheritDoc} */
	public void generateCodeInitComp(final JavaGenData aData, final StringBuilder initComp, final Definition definition) {
		if (defaultDuration == null) {
			return;
		}

		if (!(definition instanceof Def_Timer)) {
			ErrorReporter.INTERNAL_ERROR("Code generator reached erroneous definition `" + getFullName() + "''");
			return;
		}

		final Def_Timer baseTimerDefinition = (Def_Timer) definition;
		if (baseTimerDefinition.defaultDuration == null) {
			ErrorReporter.INTERNAL_ERROR("Code generator reached erroneous definition `" + getFullName() + "''");
			return;
		}

		// initializer is not needed if the default durations are the same
		// constants in both timers
		if (defaultDuration.isUnfoldable(CompilationTimeStamp.getBaseTimestamp())
				|| baseTimerDefinition.defaultDuration.isUnfoldable(CompilationTimeStamp.getBaseTimestamp())
				|| defaultDuration.checkEquality(CompilationTimeStamp.getBaseTimestamp(), baseTimerDefinition.defaultDuration)) {
			if (dimensions == null) {
				final ExpressionStruct expression = new ExpressionStruct();
				expression.expression.append(baseTimerDefinition.getGenNameFromScope(aData, initComp, myScope, ""));
				expression.expression.append(".setDefaultDuration(");

				defaultDuration.generateCodeExpression(aData, expression, true);

				expression.expression.append(')');
				expression.mergeExpression(initComp);
			} else {
				generateCodeArrayDuration(aData, initComp, baseTimerDefinition.getGenNameFromScope(aData, initComp, myScope, ""), new ArrayList<String>(),  baseTimerDefinition.defaultDuration, 0);

			}
		}
	}
}
