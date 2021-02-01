package org.eclipse.titan.executor.executors;

import static org.eclipse.titan.common.utils.StringUtils.isNullOrEmpty;
import static org.eclipse.titan.executor.Activator.PLUGIN_ID;
import static org.eclipse.titan.executor.GeneralConstants.CONFIGFILEPATH;
import static org.eclipse.titan.executor.GeneralConstants.PROJECTNAME;
import static org.eclipse.titan.executor.GeneralConstants.SINGLEMODEJAVAEXECUTOR;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchConfigurationType;
import org.eclipse.debug.core.ILaunchConfigurationWorkingCopy;
import org.eclipse.debug.core.ILaunchManager;
import org.eclipse.debug.internal.core.groups.GroupLaunchConfigurationDelegate;
import org.eclipse.debug.internal.core.groups.GroupLaunchElement;
import org.eclipse.debug.ui.IDebugUIConstants;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.launching.IJavaLaunchConfigurationConstants;
import org.eclipse.titan.common.logging.ErrorReporter;
import org.eclipse.titan.common.parsers.cfg.ConfigFileHandler;
import org.eclipse.titan.designer.consoles.TITANConsole;
import org.eclipse.titan.executor.designerconnection.DynamicLinkingHelper;
import org.eclipse.titan.executor.views.executormonitor.ExecutorMonitorView;

/**
 * Utility class for different operations related to launch configurations
 * @author Adam Knapp
 * */
@SuppressWarnings("restriction")
public final class LaunchConfigurationUtil {

	private static final String MAIN_SINGLE = "org.eclipse.titan.{0}.generated.Single_main";
	private static final String MAIN_PARALLEL = "org.eclipse.titan.{0}.generated.Parallel_main";
	private static final String GROUP_LAUNCH_CONFIGURATION_ID = "org.eclipse.debug.core.groups.GroupLaunchConfigurationType";
	private static final String LAUNCH_CONFIGURATION_LIST_ATTR = PLUGIN_ID + ".linkedLaunchConfigurations";

	/**
	 * Creates a new or overwrites the existing launch group configuration for Titan Native Java launcher.
	 * If the Main Controller has a linked launch group configuration, then it will be overwritten.
	 * @param configMC Launch configuration of the Main Controller
	 * @param configHC Launch configuration of the Host Controller
	 * @return The newly created or the overwritten launch group configuration. It returns {@code null}, 
	 * if the configuration of the Main Controller is {@code null} or not properly filled.
	 * @throws CoreException
	 */
	public static ILaunchConfiguration createGroupLaunchConfiguration(
			final ILaunchConfiguration configMC, final ILaunchConfiguration configHC) throws CoreException {
		if (configMC == null || configHC == null) {
			return null;
		}
		final boolean singleMode = configMC.getAttribute(SINGLEMODEJAVAEXECUTOR, false);
		final String projectName = configMC.getAttribute(PROJECTNAME, "");
		final String configFile = configMC.getAttribute(CONFIGFILEPATH, "");
		if (singleMode || projectName.isEmpty() || configFile.isEmpty()) {
			return null;
		}
		String configurationName = getLinkedGroupLaunchConfigurationName(configMC);
		if (configurationName == null) {
			configurationName = projectName + "-" + configFile.replace("/", "-") + "-Parallel-Java";
		}
		ILaunchConfiguration config = findGroupLaunchConfigurationByName(configurationName);
		ILaunchConfigurationWorkingCopy wc = null;
		if (config != null) {
			wc = config.getWorkingCopy();
		} else {
			wc = getGroupLaunchConfigurationType().newInstance(null, 
					getLaunchManager().generateLaunchConfigurationName(configurationName));
		}
		final IProject project = DynamicLinkingHelper.getProject(projectName);
		wc.setMappedResources(new IResource[] {project});
		List<String> list = new ArrayList<String>(1);
		list.add(IDebugUIConstants.ID_RUN_LAUNCH_GROUP);
		wc.setAttribute(IDebugUIConstants.ATTR_FAVORITE_GROUPS, list);
		List<GroupLaunchElement> configList = createGroupLaunchElements(configMC.getName(), configHC.getName(), true);
		GroupLaunchConfigurationDelegate.storeLaunchElements(wc, configList);

		config = wc.doSave();

		return config;
	}

	/**
	 * Creates a new or overwrites the existing Java app launch configuration for Titan Native Java launcher.
	 * If the Main Controller has a linked Java app launch configuration, then it will be overwritten.
	 * @param configuration Launch configuration of the Main Controller
	 * @return The newly created or the overwritten Java app launch configuration. It returns {@code null}, 
	 * if the configuration of the Main Controller is {@code null} or not properly filled.
	 * @throws CoreException
	 */
	public static ILaunchConfiguration createJavaAppLaunchConfiguration(
			final ILaunchConfiguration configuration) throws CoreException {
		if (configuration == null) {
			return null;
		}
		final boolean singleMode = configuration.getAttribute(SINGLEMODEJAVAEXECUTOR, false);
		final String projectName = configuration.getAttribute(PROJECTNAME, "");
		final String configFile = configuration.getAttribute(CONFIGFILEPATH, "");
		if (projectName.isEmpty() || configFile.isEmpty()) {
			return null;
		}
		final IProject project = DynamicLinkingHelper.getProject(projectName);
		if (project == null) {
			return null;
		}
		final IJavaProject javaProject = JavaCore.create(project);
		String mainType = "";
		IType type = null;
		String configurationName = getLinkedJavaAppLaunchConfigurationName(configuration);
		if (singleMode) {
			if (configurationName == null) {
				configurationName = projectName + "-" + configFile.replace("/", "-") + "-Single-Java";
			}
			mainType = MessageFormat.format(MAIN_SINGLE, projectName);
			type = javaProject.findType(mainType);
		} else {
			if (configurationName == null) {
				configurationName = projectName + "-" + configFile.replace("/", "-") + "-HC-Java";
			}
			mainType = MessageFormat.format(MAIN_PARALLEL, projectName);
			type = javaProject.findType(mainType);
		}

		ILaunchConfiguration config = findJavaAppLaunchConfigurationByName(configurationName);
		ILaunchConfigurationWorkingCopy wc = null;
		if (config != null) {
			wc = config.getWorkingCopy();
		} else {
			wc = getJavaAppLaunchConfigurationType().newInstance(null, 
					getLaunchManager().generateLaunchConfigurationName(configurationName));
		}

		wc.setAttribute(IJavaLaunchConfigurationConstants.ATTR_MAIN_TYPE_NAME, type.getFullyQualifiedName());
		wc.setAttribute(IJavaLaunchConfigurationConstants.ATTR_PROJECT_NAME, projectName);
		if (singleMode) {
			wc.setAttribute(IJavaLaunchConfigurationConstants.ATTR_PROGRAM_ARGUMENTS, configFile);
			List<String> list = new ArrayList<String>(1);
			list.add(IDebugUIConstants.ID_RUN_LAUNCH_GROUP);
			wc.setAttribute(IDebugUIConstants.ATTR_FAVORITE_GROUPS, list);
		} else {
			final String args = getArgsForParallelLaunch(project, configFile);
			wc.setAttribute(IJavaLaunchConfigurationConstants.ATTR_PROGRAM_ARGUMENTS, args);
		}
		wc.setMappedResources(new IResource[] {type.getUnderlyingResource()});

		config = wc.doSave();
		return config;
	}

	/**
	 * Overwrites the existing linked launch group configuration and disables the Host Controller.
	 * It might be necessary, if the Main Controller is set to single mode. 
	 * Thus running the launch group will only start the Main Controller.
	 * @param configuration Launch configuration of the Main Controller
	 * @return Returns whether the operation was successful or not
	 * @throws CoreException
	 */
	public static boolean disableHCinGroupLaunchConfiguration(final ILaunchConfigurationWorkingCopy configuration) throws CoreException {
		if (configuration == null) {
			return false;
		}
		ILaunchConfiguration confGroup = findGroupLaunchConfigurationByName(getLinkedGroupLaunchConfigurationName(configuration));
		if (confGroup == null) {
			return false;
		}
		ILaunchConfiguration confHC = findJavaAppLaunchConfigurationByName(getLinkedJavaAppLaunchConfigurationName(configuration));
		if (confHC == null) {
			return false;
		}
		List<GroupLaunchElement> configList = createGroupLaunchElements(configuration.getName(), confHC.getName(), false);
		ILaunchConfigurationWorkingCopy wc = confGroup.getWorkingCopy();
		GroupLaunchConfigurationDelegate.storeLaunchElements(wc, configList);
		confGroup = wc.doSave();
		return true;
	}

	/**
	 * Looks for the launch group configuration with the given name in the workspace.
	 * @param configurationName Name of the launch group configuration
	 * @return Launch group configuration. It returns {@code null} if the {@code configurationName}
	 * is {@code null} or empty or no launch group configuration was found with this name in the workspace.
	 * @throws CoreException
	 */
	public static ILaunchConfiguration findGroupLaunchConfigurationByName(final String configurationName) throws CoreException {
		return findLaunchConfigurationByName(configurationName, getGroupLaunchConfigurationType());
	}

	/**
	 * Looks for the Java app launch configuration with the given name in the workspace.
	 * @param configurationName Name of the Java app launch configuration
	 * @return Java app launch configuration. It returns {@code null} if the {@code configurationName}
	 * is {@code null} or empty or no Java app launch configuration was found with this name in the workspace.
	 * @throws CoreException
	 */
	public static ILaunchConfiguration findJavaAppLaunchConfigurationByName(final String configurationName) throws CoreException {
		return findLaunchConfigurationByName(configurationName, getJavaAppLaunchConfigurationType());
	}

	/**
	 * Looks for any type of launch configuration with the given name in the workspace.
	 * @param configurationName Name of the launch configuration
	 * @return Launch configuration. It returns {@code null} if the {@code configurationName}
	 * is {@code null} or empty or no launch configuration was found with this name in the workspace.
	 * @throws CoreException
	 */
	public static ILaunchConfiguration findLaunchConfigurationByName(final String configurationName) throws CoreException {
		return findLaunchConfigurationByName(configurationName, null);
	}

	/**
	 * Looks for the launch configuration of the specified type with the given name in the workspace.
	 * @param configurationName Name of the launch configuration
	 * @param type Type of the launch configuration. Might be {@code null}
	 * @return Launch configuration. It returns {@code null} if the {@code configurationName}
	 * is {@code null} or empty or no launch configuration was found with this name in the workspace.
	 * @throws CoreException
	 */
	public static ILaunchConfiguration findLaunchConfigurationByName(final String configurationName, 
			final ILaunchConfigurationType type) throws CoreException {
		if (configurationName == null || configurationName.isEmpty()) {
			return null;
		}

		ILaunchConfiguration[] configs = null;
		if (type == null) {
			configs = getLaunchManager().getLaunchConfigurations();
		} else {
			configs = getLaunchManager().getLaunchConfigurations(type);
		}

		for (ILaunchConfiguration config : configs) {
			if (config.getName().equals(configurationName)) {
				return config;
			}
		}

		return null;
	}

	/**
	 * Extracts the {@code localAddress} and {@code tcpPort} from the specified Titan configuration file.
	 * @param file Titan configuration file
	 * @return Return the the {@code localAddress} and {@code tcpPort} parameters as a concatenated string.
	 * It returns an empty string if the file is {@code null} or not exist or the {@code tcpPort} parameter is not valid.
	 * Never returns {@code null}
	 */
	public static String getArgsForParallelLaunch(final IFile file) {
		if (file == null || !file.exists()) {
			return "";
		}
		final String configFileName = file.getLocation().toOSString();
		final ConfigFileHandler configHandler = new ConfigFileHandler();
		configHandler.readFromFile(configFileName);
		
		String localAddress = configHandler.getLocalAddress();
		if (isNullOrEmpty(localAddress)) {
			localAddress = "127.0.0.1";
			TITANConsole.println("\"LocalAddress\" parameter is missing from configuration file: " 
					+ configFileName);
			TITANConsole.println("Using default: " + localAddress);
		}

		final int tcpPort = configHandler.getTcpPort();
		if (tcpPort == 0) {
			ErrorReporter.logError("\"TCPPort\" parameter is missing from configuration file: " 
					+ configFileName);
			ErrorReporter.parallelErrorDisplayInMessageDialog(
					"Error while launching the project " + file.getProject().getName(), 
					"\"TCPPort\" parameter is missing from configuration file: " + file.getFullPath().toOSString() +
					"\nCannot continue");
			TITANConsole.println("\"TCPPort\" parameter is missing from configuration file: " 
					+ configFileName);
			TITANConsole.println("Cannot continue");
			return "";
		}

		return MessageFormat.format("{0} {1,number,#}", localAddress, tcpPort);
	}

	/**
	 * Extracts the {@code localAddress} and {@code tcpPort} from the specified Titan project and file.
	 * @param project Titan project
	 * @param configFileName Name of the Titan configuration file
	 * @return Return the the {@code localAddress} and {@code tcpPort} parameters as a concatenated string.
	 * It returns an empty string if the file is {@code null} or not exist or the {@code tcpPort} parameter is not valid.
	 * Never returns {@code null}
	 */
	public static String getArgsForParallelLaunch(final IProject project, final String configFileName) {
		return getArgsForParallelLaunch(project.getFile(configFileName));
	}

	/**
	 * Returns the launch configuration type extension for launch group configurations.
	 * @return The launch configuration type extension for launch group configurations
	 * @see org.eclipse.debug.core.ILaunchManager#getLaunchConfigurationType
	 */
	public static ILaunchConfigurationType getGroupLaunchConfigurationType() {
		return getLaunchManager().getLaunchConfigurationType(GROUP_LAUNCH_CONFIGURATION_ID);
	}

	/**
	 * Returns the launch configuration type extension for Java app launch configurations.
	 * @return The launch configuration type extension for Java app launch configurations
	 * @see org.eclipse.debug.core.ILaunchManager#getLaunchConfigurationType
	 */
	public static ILaunchConfigurationType getJavaAppLaunchConfigurationType() {
		return getLaunchManager().getLaunchConfigurationType(IJavaLaunchConfigurationConstants.ID_JAVA_APPLICATION);
	}

	/**
	 * Returns the launch configuration type extension for Java Main Controller launch configurations.
	 * @return The launch configuration type extension for Java Main Controller launch configurations
	 * @see org.eclipse.debug.core.ILaunchManager#getLaunchConfigurationType
	 */
	public static ILaunchConfigurationType getJavaMctrLaunchConfigurationType() {
		return getLaunchManager().getLaunchConfigurationType(ExecutorMonitorView.NATIVE_JAVA_LAUNCHCONFIGURATION_ID);
	}

	/**
	 * Returns the singleton launch manager.
	 *
	 * @return launch manager
	 */
	public static ILaunchManager getLaunchManager() {
		return DebugPlugin.getDefault().getLaunchManager();
	}

	/**
	 * Returns the name of the linked launch group configuration.
	 * @param configuration Launch configuration of the Main Controller
	 * @return The name of the linked launch group configuration or {@code null} if no launch group configuration is linked
	 * @throws CoreException
	 */
	public static String getLinkedGroupLaunchConfigurationName(final ILaunchConfiguration configuration) throws CoreException {
		List<String> list = getLinkedLaunchConfigurations(configuration);
		if (list != null && list.size() == 2) {
			return list.get(1);
		}
		return null;
	}

	/**
	 * Returns the name of the linked java app launch configuration.
	 * @param configuration Launch configuration of the Main Controller
	 * @return The name of the linked java app launch configuration or {@code null} if no java app launch configuration is linked
	 * @throws CoreException
	 */
	public static String getLinkedJavaAppLaunchConfigurationName(final ILaunchConfiguration configuration) throws CoreException {
		List<String> list = getLinkedLaunchConfigurations(configuration);
		if (list != null && !list.isEmpty()) {
			return list.get(0);
		}
		return null;
	}

	/**
	 * Returns the list of the linked launch configurations.
	 * @param configuration Launch configuration of the Main Controller
	 * @return The list of the linked launch configurations or an empty list if no launch configuration is linked.
	 * Never returns {@code null}
	 * @throws CoreException
	 */
	public static List<String> getLinkedLaunchConfigurations(final ILaunchConfiguration configuration) throws CoreException {
		return configuration.getAttribute(LAUNCH_CONFIGURATION_LIST_ATTR, new ArrayList<String>(0));
	}
	
	/**
	 * Stores the specified list of launch configurations as linked launch configurations
	 * in the specified launch configuration of the Main Controller.
	 * @param configuration Launch configuration of the Main Controller
	 * @param list List of names of linked launch configurations 
	 */
	public static void setLinkedLaunchConfigurations(ILaunchConfigurationWorkingCopy configuration, List<String> list) {
		if (list == null || list.isEmpty()) {
			return;
		}
		configuration.setAttribute(LAUNCH_CONFIGURATION_LIST_ATTR, list);
	}

	/**
	 * Creates the list of {@code GroupLaunchElements} for launch group configurations.
	 * @param MCName Name of launch configuration related to the Main Controller
	 * @param HCName Name of launch configuration related to the Host Controller
	 * @param enableHC If {@code true}, the Host Controller is enabled in launch group configuration, disabled otherwise
	 * @return The list of {@code GroupLaunchElements} for launch group configurations
	 * @see org.eclipse.debug.internal.core.groups.GroupLaunchElement
	 */
	private static List<GroupLaunchElement> createGroupLaunchElements(final String MCName, final String HCName, final boolean enableHC) {
		List<GroupLaunchElement> configList = new ArrayList<GroupLaunchElement>(2);
		if (MCName == null || MCName.isEmpty() || HCName == null || HCName.isEmpty()) {
			return configList;
		}
		GroupLaunchElement elMC = new GroupLaunchElement();
		elMC.index = 0;
		elMC.name = MCName;
		configList.add(elMC);
		GroupLaunchElement elHC = new GroupLaunchElement();
		elHC.index = 1;
		elHC.name = HCName;
		elHC.action = GroupLaunchElement.GroupElementPostLaunchAction.OUTPUT_REGEXP;
		elHC.actionParam = new String("Listening on IP address");
		elHC.enabled = enableHC;
		configList.add(elHC);
		return configList;
	}

}
