package org.eclipse.titan.designer.actions;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.titan.designer.consoles.TITANConsole;
import org.eclipse.titan.designer.consoles.TITANDebugConsole;
import org.eclipse.titan.designer.core.TITANJob;
import org.eclipse.titan.designer.core.makefile.InternalMakefileGenerator;
import org.eclipse.titan.designer.core.makefile.ModuleStruct;
import org.eclipse.titan.designer.core.makefile.OtherFileStruct;
import org.eclipse.titan.designer.core.makefile.UserStruct;
import org.eclipse.ui.IObjectActionDelegate;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.handlers.HandlerUtil;

public class MakeCliArchive extends AbstractHandler implements IObjectActionDelegate  {
	
	private static final String JOB_TITLE = "Creating archive file";
	private static final String COMMAND = "tar -cvzf ./%s_%s.tar.gz ./bin && rm -fr ./bin";
	
	private static final String BIN_SUBPATH = File.separator + "bin";
	private static final String BACKUP_SUBPATH = File.separator + "backup";
	private static final String BACKUP_BIN_SUBPATH = BACKUP_SUBPATH + File.separator + "bin";
	
	private ISelection selection;
		
	private InternalMakefileGenerator makefileGenerator;
	
	@Override
	/** {@inheritDoc} */
	public void run(final IAction action) {
		generateCLIArchive(selection);
	}

	@Override
	/** {@inheritDoc} */
	public void selectionChanged(final IAction action, final ISelection selection) {
		this.selection = selection;
	}

	@Override
	/** {@inheritDoc} */
	public void setActivePart(final IAction action, final IWorkbenchPart targetPart) {
		// Do nothing
	}

	@Override
	/** {@inheritDoc} */
	public Object execute(final ExecutionEvent event) throws ExecutionException {
		selection = HandlerUtil.getActiveWorkbenchWindow(event).getActivePage().getSelection();

		generateCLIArchive(selection);

		return null;
	}

	/** 
	 * This is the main entry point of the feature. It uses the internal makefile generator's
	 * infrastructure to collect the files to a working directory which will be compressed 
	 * later to an archive.
	 * 
	 * @param selection The selected project in the Project explorer
	 */
	
	public void generateCLIArchive(ISelection selection) {
		if (selection instanceof IStructuredSelection) {
			final IStructuredSelection structSelection = (IStructuredSelection) selection;
			IProject singleSelectedProject;
			final List<?> selectionList = structSelection.toList();
			if (selectionList.size() == 1) {
				if (selectionList.get(0) instanceof IProject) {
					singleSelectedProject = (IProject) selectionList.get(0);
					makefileGenerator = new InternalMakefileGenerator(singleSelectedProject);
					
					makefileGenerator.gatherInformation();
					
					final File binDir = new File( singleSelectedProject.getLocation().toFile(), BIN_SUBPATH );
					final File backupBinDir = new File(binDir, BACKUP_BIN_SUBPATH);
					if ((backupBinDir.exists()) || (!backupBinDir.exists() && backupBinDir.mkdirs())) {
							
						File f;
						
						Iterator<ModuleStruct> ttcn3Files = makefileGenerator.getTtcn3Modules().iterator();
						while (ttcn3Files.hasNext()) {
							ModuleStruct filedesc = ttcn3Files.next();
							f = new File(filedesc.getOriginalLocation());
							copyFile(f.toPath(), backupBinDir.toPath().resolve(f.getName()));
						}
						
						Iterator<ModuleStruct> ttcn3PPFiles = makefileGenerator.getTtcnppModules().iterator();
						while (ttcn3PPFiles.hasNext()) {
							ModuleStruct filedesc = ttcn3PPFiles.next();
							f = new File(filedesc.getOriginalLocation());
							copyFile(f.toPath(), backupBinDir.toPath().resolve(f.getName()));
						}
						
						Iterator<ModuleStruct> ASN1Files = makefileGenerator.getAsn1modules().iterator();
						while (ASN1Files.hasNext()) {
							ModuleStruct filedesc = ASN1Files.next();
							f = new File(filedesc.getOriginalLocation());
							copyFile(f.toPath(), backupBinDir.toPath().resolve(f.getName()));
						}
						
						Iterator<UserStruct> userFiles = makefileGenerator.getUserFiles().iterator();
						while (userFiles.hasNext()) {
							UserStruct filedesc = userFiles.next();
							f = new File(filedesc.getOriginalSourceLocation());
							copyFile(f.toPath(), backupBinDir.toPath().resolve(f.getName()));
							String absPath = f.getAbsolutePath();
							String pathWithoutExt = absPath.substring(0, absPath.lastIndexOf('.')+1);
							
							File hFile = new File(pathWithoutExt+"h");
							if (hFile.exists()) {
								copyFile(hFile.toPath(), backupBinDir.toPath().resolve(hFile.getName()));
							}
							
							File hhFile = new File(pathWithoutExt+"hh");
							if (hhFile.exists()) {
								copyFile(hhFile.toPath(), backupBinDir.toPath().resolve(hhFile.getName()));
							}
						}
						
						Iterator<OtherFileStruct> otherFiles = makefileGenerator.getOtherFiles().iterator();
						while (otherFiles.hasNext()) {
							OtherFileStruct filedesc = otherFiles.next();
							String loc;
							if(filedesc.getFileName().equals("Makefile")) {
								loc = binDir + File.separator +filedesc.getFileName();
								f = new File(loc);
								copyFile(f.toPath(), backupBinDir.toPath().resolve(f.getName()+".orig"));
							} else {
								loc = filedesc.getOriginalLocation();
								f = new File(loc);
								copyFile(f.toPath(), backupBinDir.toPath().resolve(f.getName()));
							}
						}
						
						//Create README file
						try {
							createReadme(backupBinDir);
						} catch (IOException e) {
							e.printStackTrace();
							TITANConsole.println("Unable to create README file");
						}
						
						//Creating the archive
						final File backupDir = new File(binDir, BACKUP_SUBPATH);
						createArchive(backupDir, singleSelectedProject);
						
					}
				} else {
					TITANConsole.println("Make CLI archive works only for single selected project");
				}
			} else {
				TITANConsole.println("Make CLI archive works only for single selected project");
			}
		    
		}
		
	}
	
	/**
	 * Create the README file for explaining how to deal with the archive
	 */
	private void createReadme(File backupBinDir) throws IOException {
		StringBuffer buff = new StringBuffer();
		
		buff.append("This archive contains the files of one or more Eclipe Titan projects.\n");
		buff.append("\n");
		buff.append("The name of the file itself can be interpreted as follows:\n");
		buff.append("<Project>_<Date>_<time>.tar.gz\n");
		buff.append("\n");
		buff.append("To build it the Makefile has to be generated from sratch using the\n");
		buff.append("\"makefilegen ./*\" command or manually created.\n");
		buff.append("\n");
		buff.append("The original Makefile used to compile at the source system\n");
		buff.append("is also included in this archive named \"Makefile.orig\"\n");
				
		File f = new File(backupBinDir, "README");
		f.createNewFile();
		FileWriter outfile = new FileWriter(f);
		BufferedWriter writer = new BufferedWriter(outfile);
		writer.write(buff.toString());
		writer.close();
	}
	
	private Path copyFile(Path from, Path to) {
		try {
			TITANDebugConsole.println("Copying from "+from.toString()+" to "+to.toString());
			
			return Files.copy(from, to, StandardCopyOption.REPLACE_EXISTING );
		} catch (IOException e) { 
			e.printStackTrace();
			return null;
		}
	}
	
	/**
	 * This method creates the needed {@link TITANJob} and schedules it.
	 * <p>
	 * The actual work:
	 * <ul>
	 * <li>creates a TITANJob for invoking the "tar" command and redirecting the results
	 * <li>schedules the job.
	 * </ul>
	 */
	private void createArchive(File workingDir, IProject project) {
		final TITANJob titanJob = new TITANJob( JOB_TITLE, new HashMap<String, IFile>(), workingDir, project );
		titanJob.setPriority( Job.DECORATE );
		titanJob.setUser( true );
		titanJob.setRule( project );
		String dateTime = new SimpleDateFormat("yyyyMMdd_HHmmss").format(Calendar.getInstance().getTime());
		
		final List<String> command = new ArrayList<String>();
		command.add(String.format(COMMAND, project.getName(), dateTime));
		titanJob.addCommand( command, JOB_TITLE );
		titanJob.schedule();
	}
	
}
