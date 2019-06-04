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
import org.eclipse.titan.designer.AST.Module;
import org.eclipse.titan.designer.AST.NULL_Location;
import org.eclipse.titan.designer.properties.data.ProjectBuildPropertyData;
import org.eclipse.titan.designer.properties.data.TITANFlagsOptionsData;

/**
 * Helper class for java code generation.
 * The info is collected here before it is written out to the java files
 * @author Arpad Lovassy
 */
public class JavaGenData {

	/** the module where the code is being generated */
	private final Module module;

	/** the java source file without the import part */
	private final StringBuilder mSrc;

	/** the header of the generated class */
	private final StringBuilder classHeader;

	/** the extra module level variables (global in original, in java public static) */
	private final HashSet<String> mGlobalVariablesGenerated;
	private final StringBuilder mGlobalVariables;

	/** the constructor of the generated class */
	private final StringBuilder constructor;

	/** the contents of pre_init */
	private final StringBuilder preInit;

	/** the contents of post_init */
	private final StringBuilder postInit;

	/** the contents of set_module_param */
	private final StringBuilder setModuleParameters;

	/** the contents of start_function if needed */
	private final StringBuilder startPTCFunction;

	/** the contents of the execute_testcase function */
	private final StringBuilder executeTestcase;

	/** the contents of the execute_all_testcases function */
	private final StringBuilder executeAllTestcases;
	
	/** the contents of init_comp_type */
	private final StringBuilder initComp;

	/** the contents of init_system_port*/
	private final StringBuilder initSystemPort;

	/** the contents of list_testcases */
	private final StringBuilder listTestcases;

	/** the contents of list_modulepars */
	private final StringBuilder listModulePars;

	/** The imports with short class names */
	private final Set<String> mImports;

	/** The imports with short class names */
	private final Set<String> mInternalImports;

	/** The imports with short class names */
	private final Set<String> mInterModuleImports;

	private final HashMap<String, StringBuilder> types;

	/** are omits allowed in value list (legacy mode) */
	private boolean allowOmitInValueList = false;

	/** is legacy codec handling needed */
	private boolean legacyCodecHandling = false;

	/** was raw encoding disabled for the runtime */
	private boolean rawDisabled = false;

	/** is generating source code line info needed */
	private boolean addSourceInfo = false;

	/** is generating seof types fully, forced? */
	private boolean forceGenSeof = false;

	/**
	 * RAW attribute registry is used to generate a static RAW attribute
	 * only once.
	 * */
	public HashMap<String, String> RAW_attibute_registry;

	/**
	 * encoding registry is used to generate a static default encoding value
	 * only once.
	 * */
	public HashMap<String, String> encoding_registry;

	/**
	 * true for debug mode: debug info is written as comments in the generated code
	 */
	private boolean mDebug;

	/**
	 * internal variable used to generate temporal helper variables with unique names.
	 * */
	private int tempVariableCounter = 0;

	public BuildTimestamp buildTimestamp;

	public JavaGenData(final Module module, final BuildTimestamp timestamp) {
		this.module = module;
		buildTimestamp = timestamp;

		mSrc = new StringBuilder();
		classHeader = new StringBuilder();
		mGlobalVariablesGenerated = new HashSet<String>();
		mGlobalVariables = new StringBuilder();
		RAW_attibute_registry = new HashMap<String, String>();
		encoding_registry = new HashMap<String, String>();
		constructor = new StringBuilder();
		preInit = new StringBuilder();
		postInit = new StringBuilder();
		setModuleParameters = new StringBuilder();
		startPTCFunction = new StringBuilder();
		executeTestcase = new StringBuilder();
		executeAllTestcases = new StringBuilder();
		initComp = new StringBuilder();
		initSystemPort = new StringBuilder();
		listTestcases = new StringBuilder();
		listModulePars = new StringBuilder();

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

			s= project.getPersistentProperty(new QualifiedName(ProjectBuildPropertyData.QUALIFIER,TITANFlagsOptionsData.FORCE_GEN_SEOF));
			forceGenSeof = s != null && "true".equals(s);

			s = project.getPersistentProperty(new QualifiedName(ProjectBuildPropertyData.QUALIFIER,TITANFlagsOptionsData.DISABLE_RAW_PROPERTY));
			rawDisabled = s == null || "true".equals(s);

			// Legacy codec handling is not supported in the Java code generator
			//s = project.getPersistentProperty(new QualifiedName(ProjectBuildPropertyData.QUALIFIER,TITANFlagsOptionsData.ENABLE_LEGACY_ENCODING_PROPERTY));
			//legacyCodecHandling = s == null || "true".equals(s);
			legacyCodecHandling = false;

			s = project.getPersistentProperty(new QualifiedName(ProjectBuildPropertyData.QUALIFIER,TITANFlagsOptionsData.ADD_SOURCELINEINFO_PROPERTY));
			addSourceInfo = s == null || "true".equals(s);
		} catch (CoreException e) {
			ErrorReporter.logExceptionStackTrace(e);
			return;
		}
	}

	public StringBuilder getCodeForType(final String typeName) {
		if(types.containsKey(typeName)) {
			return types.get(typeName);
		}

		final StringBuilder temp = new StringBuilder();
		types.put(typeName, temp);
		return temp;
	}

	/**
	 * @return the module for which the code is being generated.
	 * */
	public Module getModuleScope() {
		return module;
	}

	/**
	 * @return the string where new java code is written
	 */
	public StringBuilder getSrc() {
		return mSrc;
	}

	/**
	 * @return the string where the class header of the generated class is written
	 */
	public StringBuilder getClassHeader() {
		return classHeader;
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
	 * @return the string where the constructor of the generated class is written
	 */
	public StringBuilder getConstructor() {
		return constructor;
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
	 * @return the string where new start function code is written
	 */
	public StringBuilder getStartPTCFunction() {
		return startPTCFunction;
	}

	/**
	 * @return the string where new execute_testcase code is written
	 */
	public StringBuilder getExecuteTestcase() {
		return executeTestcase;
	}

	/**
	 * @return the string where new execute_all_testcases code is written
	 */
	public StringBuilder getExecuteAllTestcase() {
		return executeAllTestcases;
	}

	/**
	 * @return the string where new init comp code is written
	 */
	public StringBuilder getInitComp() {
		return initComp;
	}

	/**
	 * @return the string where new init system port code is written.
	 * */
	public StringBuilder getInitSystemPort() {
		return initSystemPort;
	}

	/**
	 * @return the string where testcase listing code is written.
	 * */
	public StringBuilder getListTestcases() {
		return listTestcases;
	}

	/**
	 * @return the string where module parameter listing code is written.
	 * */
	public StringBuilder getListModulePars() {
		return listModulePars;
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
		final StringBuilder builder = new StringBuilder("tmp_");
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
	 * @return if seof types must be fully generated.
	 */
	public boolean getForceGenSeof() {
		return forceGenSeof;
	}

	/**
	 * @return true if raw encoding is enabled
	 */
	public boolean getEnableRaw() {
		return !rawDisabled;
	}

	/**
	 * @return true if source code line information should be generated
	 * */
	public boolean getAddSourceInfo() {
		return addSourceInfo;
	}

	/**
	 * @return false as this feature is not supported
	 */
	public boolean getLegacyCodecHandling() {
		return legacyCodecHandling;
	}
}
