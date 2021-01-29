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
import org.eclipse.jdt.core.*;
import org.eclipse.jdt.launching.IJavaLaunchConfigurationConstants;
import org.eclipse.titan.common.logging.ErrorReporter;
import org.eclipse.titan.common.parsers.cfg.ConfigFileHandler;
import org.eclipse.titan.designer.consoles.TITANConsole;
import org.eclipse.titan.executor.designerconnection.DynamicLinkingHelper;
import org.eclipse.titan.executor.views.executormonitor.ExecutorMonitorView;

/**
 * @author Adam Knapp
 * */
@SuppressWarnings("restriction")
public final class LaunchConfigurationUtil {

	private static final String MAIN_SINGLE = "org.eclipse.titan.{0}.generated.Single_main";
	private static final String MAIN_PARALLEL = "org.eclipse.titan.{0}.generated.Parallel_main";
	private static final String GROUP_LAUNCH_CONFIGURATION_ID = "org.eclipse.debug.core.groups.GroupLaunchConfigurationType";
	private static final String LAUNCH_CONFIGURATION_LIST_ATTR = PLUGIN_ID + ".linkedLaunchConfigurations";

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
		wc.setAttribute(IJavaLaunchConfigurationConstants.ATTR_MODULE_NAME, getModuleName(type));
		wc.setAttribute(IJavaLaunchConfigurationConstants.ATTR_USE_ARGFILE, false);
		wc.setAttribute(IJavaLaunchConfigurationConstants.ATTR_USE_CLASSPATH_ONLY_JAR, false);
		wc.setAttribute(IJavaLaunchConfigurationConstants.ATTR_SHOW_CODEDETAILS_IN_EXCEPTION_MESSAGES, true);
		wc.setAttribute(IJavaLaunchConfigurationConstants.ATTR_USE_START_ON_FIRST_THREAD, true);
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

	public static ILaunchConfiguration findGroupLaunchConfigurationByName(final String configurationName) throws CoreException {
		return findLaunchConfigurationByName(configurationName, getGroupLaunchConfigurationType());
	}

	public static ILaunchConfiguration findJavaAppLaunchConfigurationByName(final String configurationName) throws CoreException {
		return findLaunchConfigurationByName(configurationName, getJavaAppLaunchConfigurationType());
	}

	public static ILaunchConfiguration findLaunchConfigurationByName(final String configurationName) throws CoreException {
		return findLaunchConfigurationByName(configurationName, null);
	}

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
			TITANConsole.println("\"TCPPort\" parameter is missing from configuration file: " 
					+ configFileName);
			TITANConsole.println("Cannot continue");
			return "";
		}

		return MessageFormat.format("{0} {1,number,#}", localAddress, tcpPort);
	}

	public static String getArgsForParallelLaunch(final IProject project, final String configFileName) {
		return getArgsForParallelLaunch(project.getFile(configFileName));
	}

	public static ILaunchConfigurationType getGroupLaunchConfigurationType() {
		return getLaunchManager().getLaunchConfigurationType(GROUP_LAUNCH_CONFIGURATION_ID);
	}
	
	public static ILaunchConfigurationType getJavaAppLaunchConfigurationType() {
		return getLaunchManager().getLaunchConfigurationType(IJavaLaunchConfigurationConstants.ID_JAVA_APPLICATION);
	}
	
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

	public static String getLinkedGroupLaunchConfigurationName(final ILaunchConfiguration configuration) throws CoreException {
		List<String> list = getLinkedLaunchConfigurations(configuration);
		if (list != null && list.size() == 2) {
			return list.get(1);
		}
		return null;
	}

	public static String getLinkedJavaAppLaunchConfigurationName(final ILaunchConfiguration configuration) throws CoreException {
		List<String> list = getLinkedLaunchConfigurations(configuration);
		if (list != null && !list.isEmpty()) {
			return list.get(0);
		}
		return null;
	}

	public static List<String> getLinkedLaunchConfigurations(final ILaunchConfiguration configuration) throws CoreException {
		return configuration.getAttribute(LAUNCH_CONFIGURATION_LIST_ATTR, new ArrayList<String>(0));
	}
	
	public static void setLinkedLaunchConfigurations(ILaunchConfigurationWorkingCopy configuration, List<String> list) {
		if (list == null || list.isEmpty()) {
			return;
		}
		configuration.setAttribute(LAUNCH_CONFIGURATION_LIST_ATTR, list);
	}
	
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

	private static String getModuleName(IType type) {
		IJavaElement javaElement = type.getAncestor(IJavaElement.PACKAGE_FRAGMENT_ROOT);
		if (javaElement instanceof IPackageFragmentRoot) {
			IModuleDescription moduleDescription = ((IPackageFragmentRoot) (javaElement)).getModuleDescription();
			if (moduleDescription != null) {
				return moduleDescription.getElementName();
			}
		}
		return ""; //$NON-NLS-1$
	}

}
