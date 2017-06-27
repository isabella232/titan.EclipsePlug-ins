package org.eclipse.titanium.refactoring.fieldordering;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.NavigableSet;
import java.util.TreeSet;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.ltk.core.refactoring.Change;
import org.eclipse.ltk.core.refactoring.Refactoring;
import org.eclipse.ltk.core.refactoring.TextFileChange;
import org.eclipse.text.edits.MultiTextEdit;
import org.eclipse.text.edits.ReplaceEdit;
import org.eclipse.titan.common.logging.ErrorReporter;
import org.eclipse.titan.designer.AST.ASTVisitor;
import org.eclipse.titan.designer.AST.ILocateableNode;
import org.eclipse.titan.designer.AST.IType;
import org.eclipse.titan.designer.AST.IValue;
import org.eclipse.titan.designer.AST.IValue.Value_type;
import org.eclipse.titan.designer.AST.IVisitableNode;
import org.eclipse.titan.designer.AST.Identifier;
import org.eclipse.titan.designer.AST.Module;
import org.eclipse.titan.designer.AST.ASN1.types.ASN1_Sequence_Type;
import org.eclipse.titan.designer.AST.ASN1.types.ASN1_Set_Type;
import org.eclipse.titan.designer.AST.TTCN3.types.Referenced_Type;
import org.eclipse.titan.designer.AST.TTCN3.types.TTCN3_Sequence_Type;
import org.eclipse.titan.designer.AST.TTCN3.types.TTCN3_Set_Type;
import org.eclipse.titan.designer.AST.TTCN3.values.Integer_Value;
import org.eclipse.titan.designer.AST.TTCN3.values.NamedValue;
import org.eclipse.titan.designer.AST.TTCN3.values.SequenceOf_Value;
import org.eclipse.titan.designer.AST.TTCN3.values.Sequence_Value;
import org.eclipse.titan.designer.parsers.CompilationTimeStamp;
import org.eclipse.titan.designer.parsers.GlobalParser;
import org.eclipse.titan.designer.parsers.ProjectSourceParser;

/**
 * This class is only instantiated by the {@link OrderFieldNamesRefactoring} once per each refactoring operation.
 * <p>
 * By passing the selection through the constructor and calling {@link ChangeCreator#perform()}, this class
 *  creates a {@link Change} object, which can be returned by the standard
 *  {@link Refactoring#createChange(IProgressMonitor)} method in the refactoring class.
 *
 * @author Zsolt Tabi
 */
class ChangeCreator {
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
		final NavigableSet<ILocateableNode> nodes = vis.getLocations();

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

		final String fileContents = loadFileContent(toVisit);

		for (ILocateableNode node : nodes) {
			
			if (node instanceof Sequence_Value) {
				orderSequence_Value(fileContents, (Sequence_Value) node, rootEdit);
			} else if (node instanceof SequenceOf_Value) {
				orderSequenceOf_Value(fileContents, (SequenceOf_Value) node, rootEdit);
			}
				
		}

		if (!rootEdit.hasChildren()) {
			return null;
		}

		return tfc;
	}
	

	private static void orderSequence_Value(final String fileContents, final Sequence_Value sequence_Value, final MultiTextEdit rootEdit) {
		if (sequence_Value.getNofComponents() == 0) {
			return;
		}
		
		if (sequence_Value.getSeqValueByIndex(0) == null) { // record with unnamed fields
			return;
		}

		IType type = sequence_Value.getMyGovernor();
		if (!(type instanceof Referenced_Type)) {
			return;
		}

		IType refType = type.getTypeRefdLast(CompilationTimeStamp.getBaseTimestamp());
		StringBuilder builder = new StringBuilder();
		ArrayList<Identifier> fieldNamesOrdered = new ArrayList<Identifier>();
		if (refType instanceof TTCN3_Set_Type) {
			TTCN3_Set_Type setType = (TTCN3_Set_Type) refType;

			for (int i = 0; i < setType.getNofComponents(); ++i) {
				
				Identifier identifier = setType.getComponentIdentifierByIndex(i);
				fieldNamesOrdered.add(identifier);
			}
		} else if (refType instanceof TTCN3_Sequence_Type) {
			TTCN3_Sequence_Type sequenceType = (TTCN3_Sequence_Type) refType;

			for (int i = 0; i < sequenceType.getNofComponents(); ++i) {
				
				Identifier identifier = sequenceType.getComponentIdentifierByIndex(i);
				fieldNamesOrdered.add(identifier);
			}
		} else if (refType instanceof ASN1_Set_Type) {
			ASN1_Set_Type setType = (ASN1_Set_Type) refType;

			for (int i = 0; i < setType.getNofComponents(CompilationTimeStamp.getBaseTimestamp()); ++i) {
				
				Identifier identifier = setType.getComponentIdentifierByIndex(i);
				fieldNamesOrdered.add(identifier);
			}
		} else if (refType instanceof ASN1_Sequence_Type) {
			ASN1_Sequence_Type sequenceType = (ASN1_Sequence_Type) refType;
			for (int i = 0; i < sequenceType.getNofComponents(CompilationTimeStamp.getBaseTimestamp()); ++i) {
				
				Identifier identifier = sequenceType.getComponentIdentifierByIndex(i);
				fieldNamesOrdered.add(identifier);
			}
		}

		boolean isFirst = true;
		for (int i = 0; i < fieldNamesOrdered.size() ; i++) {
			Identifier identifier = fieldNamesOrdered.get(i);
			NamedValue componentByName = sequence_Value.getComponentByName(identifier);
			
			if (componentByName == null) { // no value defined
				continue;
			}
			
			if (isFirst) { // we don't always have 0-indexed element
				isFirst = false;
			} else {
				builder.append(",\n");
			}

			IValue value = componentByName.getValue();
			int start = value.getLocation().getOffset();
			int end = value.getLocation().getEndOffset();
			builder.append(identifier.getDisplayName()).append(" := ").append(fileContents.substring(start, end));
		}

		int seqStartOffset = sequence_Value.getLocation().getOffset() + 1;
		int seqEndOffset = sequence_Value.getLocation().getEndOffset() - 1;
		rootEdit.addChild(new ReplaceEdit(seqStartOffset, seqEndOffset - seqStartOffset, builder.toString()));
	}
	
	private static void orderSequenceOf_Value(final String fileContents, final SequenceOf_Value sequenceOf_Value, final MultiTextEdit rootEdit) {

		if (!sequenceOf_Value.isIndexed()) { // ordered
			return;
		}

		if (sequenceOf_Value.getNofComponents() == 0) {
			return;
		}

		StringBuilder builder = new StringBuilder();
		boolean isFirst = true;
		Long maxIndex = getIndexUpperBound(sequenceOf_Value);
		if (maxIndex == null) {
			return;
		}
		for (long i = 0; i < maxIndex; ++i) {

			long realIndex = i + 1;
			IValue indexedValueByRealIndex = sequenceOf_Value.getValues().getIndexedValueByRealIndex((int) realIndex);

			if (indexedValueByRealIndex == null) { // no value defined
				continue;
			}

			if (isFirst) { // we don't always have 0-indexed element
				isFirst = false;
			} else {
				builder.append(", ");
			}

			int start = indexedValueByRealIndex.getLocation().getOffset();
			int end = indexedValueByRealIndex.getLocation().getEndOffset();
			builder.append('[').append(realIndex).append("] := ").append(fileContents.substring(start, end));

		}

		int seqStartOffset = sequenceOf_Value.getLocation().getOffset() + 1;
		int seqEndOffset = sequenceOf_Value.getLocation().getEndOffset() - 1;
		rootEdit.addChild(new ReplaceEdit(seqStartOffset, seqEndOffset - seqStartOffset, builder.toString()));
	}
	
	private static Long getIndexUpperBound(SequenceOf_Value sequenceOf_Value) {
		long result = 0;
		for (int i = 0; i < sequenceOf_Value.getNofComponents(); i++) {
			
			IValue indexByIndex = sequenceOf_Value.getIndexByIndex(i);
			if (indexByIndex.getValuetype() != Value_type.INTEGER_VALUE) {
				return null;
			}
			
			long value = ((Integer_Value)indexByIndex).getValue();
			if (value > result) {
				result = value;
			}
		}
		return result;
	}
	
	/**
	 * Collects the locations of all the definitions in a module where the visibility modifier
	 *  is not yet minimal.
	 * <p>
	 * Call on modules.
	 * */

	private static class DefinitionVisitor extends ASTVisitor {

		private final NavigableSet<ILocateableNode> locations;

		DefinitionVisitor() {
			locations = new TreeSet<ILocateableNode>(new LocationComparator());
		}

		private NavigableSet<ILocateableNode> getLocations() {
			return locations;
		}

		@Override
		public int visit(final IVisitableNode node) {
						
			if (node instanceof Sequence_Value) {
				if (needsOrdering((Sequence_Value) node)) {
					locations.add((Sequence_Value) node);
				}
			} else if (node instanceof SequenceOf_Value) {
				if (needsOrdering((SequenceOf_Value) node)) {
					locations.add((SequenceOf_Value) node);
				}
			}
		
			
			return V_CONTINUE;
		}

		/**
		 * @return true if the value can and needs to be ordered, false otherwise
		 * */
		private boolean needsOrdering(final Sequence_Value sequence_Value) {
			IType type = sequence_Value.getMyGovernor();
			if (!(type instanceof Referenced_Type)) {
				return false;
			}
			
			if (sequence_Value.getNofComponents() == 0) {
				return false;
			}
			
			if (sequence_Value.getSeqValueByIndex(0) == null) { // record with unnamed fields
				return false;
			}

			IType refType = type.getTypeRefdLast(CompilationTimeStamp.getBaseTimestamp());
			if (refType instanceof TTCN3_Set_Type) {
				TTCN3_Set_Type setType = (TTCN3_Set_Type) refType;
				//check if already in order
				boolean inOrder = true;
				for (int i = 0; i < setType.getNofComponents() && inOrder; ++i) {
					Identifier identifier = setType.getComponentIdentifierByIndex(i);
					NamedValue namedValue = sequence_Value.getSeqValueByIndex(i);
					inOrder = identifier.equals(namedValue.getName());
				}
				return !inOrder;
			} else if (refType instanceof TTCN3_Sequence_Type) {
				TTCN3_Sequence_Type sequenceType = (TTCN3_Sequence_Type) refType;
				//check if already in order
				boolean inOrder = true;
				for (int i = 0; i < sequenceType.getNofComponents() && inOrder; ++i) {
					Identifier identifier = sequenceType.getComponentIdentifierByIndex(i);
					NamedValue namedValue = sequence_Value.getSeqValueByIndex(i);
					inOrder = identifier.equals(namedValue.getName());
				}
				return !inOrder;
			} else if (refType instanceof ASN1_Set_Type) {
				ASN1_Set_Type setType = (ASN1_Set_Type) refType;
				//check if already in order
				boolean inOrder = true;
				for (int i = 0; i < setType.getNofComponents(CompilationTimeStamp.getBaseTimestamp()) && inOrder; ++i) {
					Identifier identifier = setType.getComponentIdentifierByIndex(i);
					NamedValue namedValue = sequence_Value.getSeqValueByIndex(i);
					inOrder = identifier.equals(namedValue.getName());
				}
				return !inOrder;
			} else if (refType instanceof ASN1_Sequence_Type) {
				ASN1_Sequence_Type sequenceType = (ASN1_Sequence_Type) refType;
				//check if already in order
				boolean inOrder = true;
				for (int i = 0; i < sequenceType.getNofComponents(CompilationTimeStamp.getBaseTimestamp()) && inOrder; ++i) {
					Identifier identifier = sequenceType.getComponentIdentifierByIndex(i);
					NamedValue namedValue = sequence_Value.getSeqValueByIndex(i);
					inOrder = identifier.equals(namedValue.getName());
				}
				return !inOrder;
			}

			return false;
		}

		/**
		 * @return true if the value can and needs to be ordered, false otherwise
		 * */
		private boolean needsOrdering(final SequenceOf_Value sequenceOf_Value) {
			if (!sequenceOf_Value.isIndexed()) { // ordered
				return false;
			}

			if (sequenceOf_Value.getNofComponents() == 0) {
				return false;
			}

			int lastIndex = -1;
			for (int i = 0; i < sequenceOf_Value.getNofComponents(); i++) {
				IValue index = sequenceOf_Value.getIndexByIndex(i);
				if (!(index instanceof Integer_Value)) {
					return false;
				}
				if (((Integer_Value)index).intValue() > lastIndex) {
					lastIndex = ((Integer_Value)index).intValue();
				} else {
					return true;
				}
			}

			return false;
		}
	}

	/**
	 * Compares {@link ILocateableNode}s by comparing the file paths as strings.
	 * If the paths are equal, the two offset integers are compared.
	 * */
	private static class LocationComparator implements Comparator<ILocateableNode> {

		@Override
		public int compare(final ILocateableNode arg0, final ILocateableNode arg1) {
			final IResource f0 = arg0.getLocation().getFile();
			final IResource f1 = arg1.getLocation().getFile();
			if (!f0.equals(f1)) {
				return f0.getFullPath().toString().compareTo(f1.getFullPath().toString());
			}

			final int o0 = arg0.getLocation().getOffset();
			final int o1 = arg1.getLocation().getOffset();
			return (o0 < o1) ? -1 : ((o0 == o1) ? 0 : 1);//TODO update with Java 1.7 to Integer.compare
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
