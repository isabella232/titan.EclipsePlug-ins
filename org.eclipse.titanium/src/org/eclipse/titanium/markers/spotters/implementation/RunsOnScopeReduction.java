package org.eclipse.titanium.markers.spotters.implementation;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.eclipse.titan.designer.AST.ASTVisitor;
import org.eclipse.titan.designer.AST.Assignment;
import org.eclipse.titan.designer.AST.IVisitableNode;
import org.eclipse.titan.designer.AST.Identifier;
import org.eclipse.titan.designer.AST.Reference;
import org.eclipse.titan.designer.AST.TTCN3.definitions.Def_Altstep;
import org.eclipse.titan.designer.AST.TTCN3.definitions.Def_Function;
import org.eclipse.titan.designer.AST.TTCN3.definitions.Def_Testcase;
import org.eclipse.titan.designer.AST.TTCN3.types.ComponentTypeBody;
import org.eclipse.titan.designer.AST.TTCN3.types.Component_Type;
import org.eclipse.titan.designer.parsers.CompilationTimeStamp;
import org.eclipse.titanium.markers.spotters.BaseModuleCodeSmellSpotter;
import org.eclipse.titanium.markers.types.CodeSmellType;

/**
 * @author Farkas Izabella Ingrid
 * */
public class RunsOnScopeReduction extends BaseModuleCodeSmellSpotter{

	public RunsOnScopeReduction() {
		super(CodeSmellType.RUNS_ON_SCOPE_REDUCTION);
	}

	@Override
	protected void process(IVisitableNode node, Problems problems) {
 		final Set<Identifier> definitions = new HashSet<Identifier>();
		final Identifier componentIdentifier;
		final CompilationTimeStamp timestamp = CompilationTimeStamp.getBaseTimestamp();
		final Identifier identifier;
		boolean isTestCase = false;

		if (node instanceof Def_Function) {
			final Def_Function variable = (Def_Function) node;
			final Component_Type componentType = variable.getRunsOnType(timestamp); 
			if (componentType == null) {
				return;
			}
			componentIdentifier = componentType.getComponentBody().getIdentifier();
			identifier = variable.getIdentifier();
		} else if (node instanceof Def_Altstep) {
			final Def_Altstep variable = (Def_Altstep) node;
			componentIdentifier = variable.getRunsOnType(timestamp).getComponentBody().getIdentifier();
			identifier = variable.getIdentifier();
		} else {
			final Def_Testcase variable = (Def_Testcase) node;
			componentIdentifier = variable.getRunsOnType(timestamp).getComponentBody().getIdentifier();
			identifier = variable.getIdentifier();
			isTestCase = true;
		}

		final ReferenceCheck chek = new ReferenceCheck();
		node.accept(chek);
		definitions.addAll(chek.getIdentifiers());

		if (definitions.isEmpty()) {
			if(isTestCase){
				problems.report(identifier.getLocation(), MessageFormat.format("The runs on component `{0}'' seems to be never used. Use empty component.",componentIdentifier.getDisplayName()));
			} else {
				problems.report(identifier.getLocation(), MessageFormat.format("The runs on component `{0}'' seems to be never used, it is erasable.",componentIdentifier.getDisplayName()));
			}
		} else if (!definitions.contains(componentIdentifier)) {
			ArrayList<Identifier> list = new ArrayList<Identifier>(definitions);
			if (definitions.size() == 1){
				problems.report(identifier.getLocation(), MessageFormat.format("The runs on component `{0}'' seems to be never used. Use `{1}'' component.",
						componentIdentifier.getName(),list.get(0).getDisplayName()));
			} else {
				//FIXME: implement other cases
				problems.report(identifier.getLocation(), MessageFormat.format("The runs on component `{0}'' seems to be never used.",componentIdentifier.getDisplayName()));
			}
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

	@Override
	public List<Class<? extends IVisitableNode>> getStartNode() { 
		final List<Class<? extends IVisitableNode>> ret = new ArrayList<Class<? extends IVisitableNode>>(4);
		ret.add(Def_Altstep.class); 
		ret.add(Def_Function.class);
		ret.add(Def_Testcase.class);
		return ret;
	}
}
