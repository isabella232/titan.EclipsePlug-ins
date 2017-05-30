package org.eclipse.titan.designer.compiler;

import java.util.HashMap;
import java.util.Set;
import java.util.TreeSet;

/**
 * Helper class for java code generation.
 * The info is collected here before it is written out to the java files
 * @author Arpad Lovassy
 */
public class JavaGenData {

	/** the java source file without the import part */
	private StringBuilder mSrc;
	
	/** the contents of pre_init */
	private StringBuilder preInit;
	
	/** the contents of post_init */
	private StringBuilder postInit;
	
	/** the contents of init_comp_type */
	private StringBuilder initComp;

	/** The imports with short class names */
	private Set<String> mImports;
	
	/** The imports with short class names */
	private Set<String> mInternalImports;

	/** The imports with short class names */
	private Set<String> mInterModuleImports;

	private HashMap<String, StringBuilder> types;

	/**
	 * true for debug mode: debug info is written as comments in the generated code
	 */
	private boolean mDebug;
	
	/**
	 * internal variable used to generate temporal helper variables with unique names.
	 * */
	private int tempVariableCounter = 0;

	public JavaGenData() {
		mSrc = new StringBuilder();
		preInit = new StringBuilder();
		postInit = new StringBuilder();
		initComp = new StringBuilder();

		// TreeSet keeps elements in natural order (alphabetical)
		mImports = new TreeSet<String>();
		mInternalImports = new TreeSet<String>();
		mInterModuleImports = new TreeSet<String>();
		mDebug = false;
		types = new HashMap<String, StringBuilder>();
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
}
