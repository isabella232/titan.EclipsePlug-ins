/******************************************************************************
 * Copyright (c) 2000-2018 Ericsson Telecom AB
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/org/documents/epl-2.0/EPL-2.0.html
 ******************************************************************************/
package org.eclipse.titan.runtime.core.cfgparser;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Stores temporary config editor data of the logging section
 * @author Kristof Szabados
 * @author Arpad Lovassy
 */
public final class LoggingSectionHandler {

	public static final class PluginSpecificParam {
		private String param = null;
		private String value = null;

		public PluginSpecificParam(final String param, final String value) {
			this.setParam(param);
			this.setValue(value);
		}

		@Override
		public boolean equals(final Object o) {
			if (this == o) {
				return true;
			}

			if (o == null) {
				return false;
			}

			if (o.getClass() != getClass()) {
				return false;
			}

			final PluginSpecificParam p = (PluginSpecificParam) o;
			return getParam().equals(p.getParam()) && getValue().equals(p.getValue());
		}

		@Override
		public int hashCode() {
			return getParam().hashCode() + 31 * getValue().hashCode();
		}

		public String getParam() {
			return param;
		}

		public void setParam(final String param) {
			this.param = param;
		}

		public String getValue() {
			return value;
		}

		public void setValue(final String value) {
			this.value = value;
		}
	}

	public static class LogParamEntry {
		private String logFile = null;
		private Boolean appendFile = null;
		private String timestampFormat = null;
		private String consoleTimestampFormat = null;
		private String logeventTypes = null;
		private String sourceInfoFormat = null;
		private Boolean logEntityName = null;
		private String matchingHints = null;

		//TODO: change type to integer
		private CFGNumber logfileNumber = null;
		//TODO: change type to integer
		private CFGNumber logfileSize = null;
		private String diskFullAction = null;

		private List<LoggingBit> fileMaskBits = new ArrayList<LoggingBit>();

		private List<LoggingBit> consoleMaskBits = new ArrayList<LoggingBit>();

		private List<PluginSpecificParam> pluginSpecificParam = new ArrayList<PluginSpecificParam>();

		private String pluginPath = null;

		//TODO: change type to integer
		private CFGNumber emergencyLogging = null;
		private String emergencyLoggingBehaviour = null;
		private List<LoggingBit> emergencyLoggingMask = null;

		public String getLogFile() {
			return logFile;
		}

		public void setLogFile(final String logFile) {
			this.logFile = logFile;
		}

		public Boolean getAppendFile() {
			return appendFile;
		}

		public void setAppendFile(final Boolean appendFile) {
			this.appendFile = appendFile;
		}

		public String getTimestampFormat() {
			return timestampFormat;
		}

		public void setTimestampFormat(final String timestampFormat) {
			this.timestampFormat = timestampFormat;
		}
		
		public String getConsoleTimestampFormat() {
			return consoleTimestampFormat;
		}

		public void setConsoleTimestampFormat(final String timestampFormat) {
			this.consoleTimestampFormat = timestampFormat;
		}

		public String getLogeventTypes() {
			return logeventTypes;
		}

		public void setLogeventTypes(final String logeventTypes) {
			this.logeventTypes = logeventTypes;
		}

		public String getSourceInfoFormat() {
			return sourceInfoFormat;
		}

		public void setSourceInfoFormat(final String sourceInfoFormat) {
			this.sourceInfoFormat = sourceInfoFormat;
		}

		public Boolean getLogEntityName() {
			return logEntityName;
		}

		public void setLogEntityName(final Boolean logEntityName) {
			this.logEntityName = logEntityName;
		}

		public String getMatchingHints() {
			return matchingHints;
		}

		public void setMatchingHints(final String matchingHints) {
			this.matchingHints = matchingHints;
		}

		public CFGNumber getLogfileNumber() {
			return logfileNumber;
		}

		public void setLogfileNumber(final CFGNumber logfileNumber) {
			this.logfileNumber = logfileNumber;
		}

		public CFGNumber getLogfileSize() {
			return logfileSize;
		}

		public void setLogfileSize(final CFGNumber logfileSize) {
			this.logfileSize = logfileSize;
		}

		public String getDiskFullAction() {
			return diskFullAction;
		}

		public void setDiskFullAction(final String diskFullAction) {
			this.diskFullAction = diskFullAction;
		}

		public List<LoggingBit> getFileMaskBits() {
			return fileMaskBits;
		}

		public void setFileMaskBits(final List<LoggingBit> fileMaskBits) {
			this.fileMaskBits = fileMaskBits;
		}

		public List<LoggingBit> getConsoleMaskBits() {
			return consoleMaskBits;
		}

		public void setConsoleMaskBits(final List<LoggingBit> consoleMaskBits) {
			this.consoleMaskBits = consoleMaskBits;
		}

		public List<PluginSpecificParam> getPluginSpecificParam() {
			return pluginSpecificParam;
		}

		public void setPluginSpecificParam(final List<PluginSpecificParam> pluginSpecificParam) {
			this.pluginSpecificParam = pluginSpecificParam;
		}

		public String getPluginPath() {
			return pluginPath;
		}

		public void setPluginPath(final String pluginPath) {
			this.pluginPath = pluginPath;
		}

		public CFGNumber getEmergencyLogging() {
			return emergencyLogging;
		}

		public void setEmergencyLogging(final CFGNumber emergencyLogging) {
			this.emergencyLogging = emergencyLogging;
		}

		public String getEmergencyLoggingBehaviour() {
			return emergencyLoggingBehaviour;
		}

		public void setEmergencyLoggingBehaviour(final String emergencyLoggingBehaviour) {
			this.emergencyLoggingBehaviour = emergencyLoggingBehaviour;
		}

		public List<LoggingBit> getEmergencyLoggingMask() {
			return emergencyLoggingMask;
		}

		public void setEmergencyLoggingMask(final List<LoggingBit> emergencyLoggingMask) {
			this.emergencyLoggingMask = emergencyLoggingMask;
		}
	}

	public static class LoggerPluginEntry {
		private String name = null;
		private String path = null;

		public String getName() {
			return name;
		}

		public void setName(final String name) {
			this.name = name;
		}

		public String getPath() {
			return path;
		}

		public void setPath(final String path) {
			this.path = path;
		}
	}

	public static class LoggerPluginsEntry {
		private Map<String, LoggerPluginEntry> plugins = null;

		public Map<String, LoggerPluginEntry> getPlugins() {
			return plugins;
		}

		public void setPlugins(final Map<String, LoggerPluginEntry> plugins) {
			this.plugins = plugins;
		}
	}

	private Map<String, LoggerPluginsEntry> loggerPluginsTree = new HashMap<String, LoggingSectionHandler.LoggerPluginsEntry>();

	// component/plugin hashmap
	private Map<String,HashMap<String,LogParamEntry>> loggerTree = new HashMap<String,HashMap<String,LogParamEntry>>();

	public Set<String> getComponents() {
		return loggerTree.keySet();
	}

	public Set<String> getPlugins(final String componentName) {
		final Map<String,LogParamEntry> pluginsMap = loggerTree.get(componentName);
		if (pluginsMap==null) {
			return new HashSet<String>();
		}

		return pluginsMap.keySet();
	}

	/*
	 * if a key does not exist it will be automatically created
	 */
	public LogParamEntry componentPlugin(final String componentName, final String pluginName) {
		String tempComponentName = componentName;
		if (componentName==null) {
			tempComponentName = "*";
		}
		String tempPluginName = pluginName;
		if (pluginName==null) {
			tempPluginName = "*";
		}
		if (!loggerTree.containsKey(tempComponentName)) {
			loggerTree.put(tempComponentName, new HashMap<String,LogParamEntry>());
		}

		final Map<String,LogParamEntry> pluginMap = loggerTree.get(tempComponentName);
		if (!pluginMap.containsKey(tempPluginName)) {
			pluginMap.put(tempPluginName, new LogParamEntry());
		}
		return pluginMap.get(tempPluginName);
	}

	/*
	 * helper class for the SWT tree providers
	 */
	public static class LoggerTreeElement {
		private LoggingSectionHandler lsh = null;
		private String componentName = null;
		private String pluginName = null;
		public LoggerTreeElement(final LoggingSectionHandler lsh, final String componentName, final String pluginName) {
			this.lsh = lsh;
			this.componentName = componentName;
			this.pluginName = pluginName;
		}
		public LoggerTreeElement(final LoggingSectionHandler lsh, final String componentName) {
			this.lsh = lsh;
			this.componentName = componentName;
			this.pluginName = null;
		}
		public void writeNamePrefix(final StringBuilder name) {
			name.append(componentName).append('.');
			if (pluginName!=null) {
				name.append(pluginName).append('.');
			}
		}

		public LoggingSectionHandler getLsh() {
			return lsh;
		}

		public void setLsh(final LoggingSectionHandler lsh) {
			this.lsh = lsh;
		}

		public String getComponentName() {
			return componentName;
		}

		public void setComponentName(final String componentName) {
			this.componentName = componentName;
		}

		public String getPluginName() {
			return pluginName;
		}

		public void setPluginName(final String pluginName) {
			this.pluginName = pluginName;
		}
	}

	public void removeTreeElement(final LoggerTreeElement lte) {
		if (lte.pluginName==null) {
			loggerTree.remove(lte.componentName);
		} else {
			loggerTree.get(lte.componentName).remove(lte.pluginName);
		}
	}

	public Object[] getComponentsTreeElementArray() {
		final List<LoggerTreeElement> rv = new ArrayList<LoggerTreeElement>();
		for (final String s : loggerTree.keySet()) {
			rv.add(new LoggerTreeElement(this,s));
		}

		return rv.toArray();
	}

	public Object[] getPluginsTreeElementArray(final String componentName) {
		final List<LoggerTreeElement> rv = new ArrayList<LoggerTreeElement>();
		final Map<String,LogParamEntry> pluginsMap = loggerTree.get(componentName);
		if (pluginsMap==null) {
			return new Object[] {};
		}

		for (final String s : pluginsMap.keySet()) {
			rv.add(new LoggerTreeElement(this,componentName,s));
		}
		return rv.toArray();
	}

	public Map<String, LoggerPluginsEntry> getLoggerPluginsTree() {
		return loggerPluginsTree;
	}

	public void setLoggerPluginsTree(final Map<String, LoggerPluginsEntry> loggerPluginsTree) {
		this.loggerPluginsTree = loggerPluginsTree;
	}
}
