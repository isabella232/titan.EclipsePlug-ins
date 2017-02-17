package org.eclipse.titan.designer.compiler;

import java.io.ByteArrayInputStream;
import java.io.InputStream;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.titan.designer.AST.Module;

/**
 * This is project level root of all java compiler related activities.
 * @author Arpad Lovassy
 */
public class ProjectSourceCompiler {

	/** the root package of the generated java source */
	private final static String PACKAGE_GENERATED_ROOT = "org.eclipse.titan.generated";
	private final static String PACKAGE_RUNTIME_ROOT = "org.eclipse.titan.runtime";
	private final static String PACKAGE_RUNTIME_TYPES = PACKAGE_RUNTIME_ROOT + ".types";

	/** the root folder of the generated java source */
	private final static String DIR_GENERATED_ROOT = "src/org/eclipse/titan/generated";

	/**
	 * Generates java code for a module
	 * @param aModule module to compile
	 * @param aDebug true: debug info is added to the source code 
	 * @throws CoreException
	 */
	public static void compile( final Module aModule, final boolean aDebug ) throws CoreException {
		JavaGenData data = new JavaGenData();
		data.setDebug( aDebug );
		aModule.generateJava( data );

		//write imports
		StringBuilder headerSb = new StringBuilder();
		writeHeader( headerSb, data );

		//write src file body
		IProject project  = aModule.getProject();
		IFolder folder = project.getFolder( DIR_GENERATED_ROOT );
		IFile file = folder.getFile( aModule.getName() + ".java");
		createDir( folder );

		//write to file
		final InputStream headerStream = new ByteArrayInputStream( headerSb.toString().getBytes() );
		if (file.exists()) {
			file.setContents( headerStream, IResource.FORCE | IResource.KEEP_HISTORY, null );
		} else {
			file.create( headerStream, IResource.FORCE, null );
		}
		final InputStream bodyStream = new ByteArrayInputStream( data.getSrc().toString().getBytes() );
		file.appendContents( bodyStream, IResource.FORCE | IResource.KEEP_HISTORY, null );
	}

	/**
	 * RECURSIVE
	 * Creates full directory path
	 * @param aFolder directory to create
	 * @throws CoreException
	 */
	private static void createDir( IFolder aFolder ) throws CoreException {
		if (!aFolder.exists()) {
			createDir( (IFolder) aFolder.getParent() );
			aFolder.create( true, true, new NullProgressMonitor() );
		}
	}

	/**
	 * Builds header part of the java source file.
	 * <ul>
	 *   <li> header comment
	 *   <li> package
	 *   <li> includes
	 * </ul>
	 * @param aSb string buffer, where the result is written
	 * @param aData data collected during code generation, we need the include files form it
	 */
	private static void writeHeader( final StringBuilder aSb, final JavaGenData aData ) {
		aSb.append( "package " );
		aSb.append( PACKAGE_GENERATED_ROOT );
		aSb.append( ";\n\n" );
		for ( String importName : aData.getImports() ) {
			writeImport( aSb, importName );
		}
		aSb.append( "\n" );
	}

	/**
	 * Writes an import to the header
	 * @param aSb string buffer, where the result is written
	 * @param aImportName short class name to import. This function knows the package of all the runtime classes.
	 */
	private static void writeImport( final StringBuilder aSb, final String aImportName ) {
		if ( "TitanCharString".equals( aImportName ) ||
			 "TitanInteger".equals( aImportName ) ) {
			aSb.append( "import " );
			aSb.append( PACKAGE_RUNTIME_TYPES );
			aSb.append( "." );
			aSb.append( aImportName );
			aSb.append( ";\n" );
		} else if ( "TtcnError".equals( aImportName ) ) {
			aSb.append( "import " );
			aSb.append( PACKAGE_RUNTIME_ROOT );
			aSb.append( "." );
			aSb.append( aImportName );
			aSb.append( ";\n" );
		} else {
			aSb.append( "//TODO: unknown import: " );
			aSb.append( aImportName );
			aSb.append( "\n" );
		}
	}
}
