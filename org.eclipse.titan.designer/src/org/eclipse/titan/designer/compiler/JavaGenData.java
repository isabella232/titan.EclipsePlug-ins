package org.eclipse.titan.designer.compiler;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;
import java.util.TreeSet;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.QualifiedName;
import org.eclipse.titan.common.logging.ErrorReporter;
import org.eclipse.titan.designer.AST.Location;
import org.eclipse.titan.designer.AST.NULL_Location;
import org.eclipse.titan.designer.properties.data.ProjectBuildPropertyData;
import org.eclipse.titan.designer.properties.data.TITANFlagsOptionsData;

/**
 * Helper class for java code generation.
 * The info is collected here before it is written out to the java files
 * @author Arpad Lovassy
 */
public class JavaGenData {

	/** the java source file without the import part */
	private StringBuilder mSrc;

	/** the extra module level variables (global in original, in java public static) */
	private HashSet<String> mGlobalVariablesGenerated;
	private StringBuilder mGlobalVariables;

	/** the contents of pre_init */
	private StringBuilder preInit;

	/** the contents of post_init */
	private StringBuilder postInit;

	/** the contents of set_module_param */
	private StringBuilder setModuleParameters;

	/** the contents of init_comp_type */
	private StringBuilder initComp;

	/** The imports with short class names */
	private Set<String> mImports;

	/** The imports with short class names */
	private Set<String> mInternalImports;

	/** The imports with short class names */
	private Set<String> mInterModuleImports;

	private HashMap<String, StringBuilder> types;

	/** are omits allowed in value list (legacy mode) */
	private boolean allowOmitInValueList = false;

	/** is legacy codec handling needed */
	private boolean legacyCodecHandling = false;

	/** was raw encoding disabled for the runtime */
	private boolean rawDisabled = false;

	/**
	 * true for debug mode: debug info is written as comments in the generated code
	 */
	private boolean mDebug;

	/**
	 * internal variable used to generate temporal helper variables with unique names.
	 * */
	private int tempVariableCounter = 0;

	public BuildTimestamp buildTimestamp;

	public JavaGenData(final BuildTimestamp timestamp) {
		buildTimestamp = timestamp;

		mSrc = new StringBuilder();
		mGlobalVariablesGenerated = new HashSet<String>();
		mGlobalVariables = new StringBuilder();
		preInit = new StringBuilder();
		postInit = new StringBuilder();
		setModuleParameters = new StringBuilder();
		initComp = new StringBuilder();

		// TreeSet keeps elements in natural order (alphabetical)
		mImports = new TreeSet<String>();
		mInternalImports = new TreeSet<String>();
		mInterModuleImports = new TreeSet<String>();
		mDebug = false;
		types = new HashMap<String, StringBuilder>();
	}

	public void collectProjectSettings(final Location location) {
		if(location == null || (location instanceof NULL_Location)) {
			return;
		}

		final IResource f = location.getFile();
		if( f == null) {
			return;
		}

		final IProject project = f.getProject();
		if(project == null) {
			return;
		}

		try {
			String s= project.getPersistentProperty(new QualifiedName(ProjectBuildPropertyData.QUALIFIER,TITANFlagsOptionsData.ALLOW_OMIT_IN_VALUELIST_TEMPLATE_PROPERTY));
			allowOmitInValueList = s == null || "true".equals(s);

			s = project.getPersistentProperty(new QualifiedName(ProjectBuildPropertyData.QUALIFIER,TITANFlagsOptionsData.DISABLE_RAW_PROPERTY));
			rawDisabled = s == null || "true".equals(s);

			// Legacy codec handling is not supported in the Java code generator
			//s = project.getPersistentProperty(new QualifiedName(ProjectBuildPropertyData.QUALIFIER,TITANFlagsOptionsData.ENABLE_LEGACY_ENCODING_PROPERTY));
			//legacyCodecHandling = s == null || "true".equals(s);
			legacyCodecHandling = false;
		} catch (CoreException e) {
			ErrorReporter.logExceptionStackTrace(e);
			return;
		}
	}

	public StringBuilder getCodeForType(final String typeName) {
		if(types.containsKey(typeName)) {
			return types.get(typeName);
		}

		StringBuilder temp = new StringBuilder();
		types.put(typeName, temp);
		return temp;
	}

	/**
	 * @return the string where new java code is written
	 */
	public StringBuilder getSrc() {
		return mSrc;
	}

	public boolean hasGlobalVariable(final String name) {
		return mGlobalVariablesGenerated.contains(name);
	}

	/**
	 * Inserts a new global variable into the code to be generated.
	 *
	 * @param name the name of the parameter to be inserted
	 * @param code the code representing the creation of the global variable.
	 */
	public void addGlobalVariable(final String name, final String code) {
		mGlobalVariablesGenerated.add(name);
		mGlobalVariables.append(code);
	}

	StringBuilder getGlobalVariables() {
		return mGlobalVariables;
	}

	/**
	 * @return the string where new pre init code is written
	 */
	public StringBuilder getPreInit() {
		return preInit;
	}

	/**
	 * @return the string where new post init code is written
	 */
	public StringBuilder getPostInit() {
		return postInit;
	}

	/**
	 * @return the string where the module parameter setting code is written
	 */
	public StringBuilder getSetModuleParameters () {
		return setModuleParameters;
	}

	/**
	 * @return the string where new init comp code is written
	 */
	public StringBuilder getInitComp() {
		return initComp;
	}

	/**
	 * Adds a new import
	 * @param aNewImport the new import with short class name. It is ignored in case of duplicate.
	 */
	public void addImport( final String aNewImport ) {
		mImports.add( aNewImport );
	}

	/**
	 * Adds a new built in type import
	 * @param aNewImport the new import with short class name. It is ignored in case of duplicate.
	 */
	public void addBuiltinTypeImport( final String aNewImport ) {
		mInternalImports.add( aNewImport );
	}

	/**
	 * Adds a new common library import
	 * @param aNewImport the new import with short class name. It is ignored in case of duplicate.
	 */
	public void addCommonLibraryImport( final String aNewImport ) {
		mInternalImports.add( aNewImport );
	}

	/**
	 * Adds a new import
	 * @param aNewImport the new import with short class name. It is ignored in case of duplicate.
	 */
	public void addInterModuleImport( final String aNewImport ) {
		mInterModuleImports.add( aNewImport );
	}

	/**
	 * @return the imports with short class names in alphabetical order
	 */
	public Set<String> getImports() {
		return mImports;
	}

	public HashMap<String, StringBuilder> getTypes() {
		return types;
	}

	/**
	 * @return the internal imports with short class names in alphabetical order
	 */
	public Set<String> getInternalImports() {
		return mInternalImports;
	}

	/**
	 * @return the internal imports with short class names in alphabetical order
	 */
	public Set<String> getInterModuleImports() {
		return mInterModuleImports;
	}

	public boolean isDebug() {
		return mDebug;
	}

	public void setDebug( final boolean aDebug ) {
		mDebug = aDebug;
	}

	public BuildTimestamp getBuildTimstamp() {
		return buildTimestamp;
	}

	/**
	 * Returns an identifier used for temporary Java objects,
	 *  which is unique in the module
	 *  
	 * Module::get_temporary_id() in the compiler
	 * 
	 * TODO rethink in the compiler, should not be part of the semantic structure
	 * */
	public String getTemporaryVariableName() {
		StringBuilder builder = new StringBuilder("tmp_");
		tempVariableCounter++;
		builder.append(tempVariableCounter);

		return builder.toString();
	}

	/**
	 * @return if omit is allowed in a value list.
	 */
	public boolean getAllowOmitInValueList() {
		return allowOmitInValueList;
	}

	/**
	 * @return true if raw encoding is enabled
	 */
	public boolean getEnableRaw() {
		return !rawDisabled;
	}

	/**
	 * @return false as this feature is not supported
	 */
	public boolean getLegacyCodecHandling() {
		return legacyCodecHandling;
	}
}
