parser grammar RuntimeCfgParser;

/*
 ******************************************************************************
 * Copyright (c) 2000-2018 Ericsson Telecom AB
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/org/documents/epl-2.0/EPL-2.0.html
 ******************************************************************************
*/

options{
	tokenVocab = RuntimeCfgLexer;
}

@header {
import org.eclipse.titan.runtime.core.cfgparser.ExecuteSectionHandler.ExecuteItem;
import org.eclipse.titan.runtime.core.cfgparser.LoggingSectionHandler.LogParamEntry;

import java.io.File;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
}

@members{
	// format strings for error messages if definition (macro or environment variable) cannot be resolved
	// %s : definition
	private static final String DEFINITION_NOT_FOUND_STRING  = "Could not resolve definition: %s using \"\" as a replacement.";
	private static final String DEFINITION_NOT_FOUND_BSTR    = "Could not resolve definition: %s using ''B as a replacement.";
	private static final String DEFINITION_NOT_FOUND_HSTR    = "Could not resolve definition: %s using ''H as a replacement.";
	private static final String DEFINITION_NOT_FOUND_OSTR    = "Could not resolve definition: %s using ''O as a replacement.";
	private static final String DEFINITION_NOT_FOUND_INT     = "Could not resolve integer definition: %s using 0 as replacement.";
	private static final String DEFINITION_NOT_FOUND_FLOAT   = "No macro or environmental variable defined %s could be found, using 0.0 as a replacement value.";
	private static final String DEFINITION_NOT_FOUND_BOOLEAN = "Could not resolve definition: %s using \"true\" as a replacement.";

	private final static int SEVERITY_INFO    = 0;
	private final static int SEVERITY_WARNING = 1;
	private final static int SEVERITY_ERROR   = 2;
	
	private final static int PRIORITY_LOW     = 0;
	private final static int PRIORITY_NORMAL  = 1;
	private final static int PRIORITY_HIGH    = 2;

	// pattern for matching macro string, for example: \$a, \${a}
	private final static Pattern PATTERN_MACRO = Pattern.compile("\\$\\s*\\{?\\s*([A-Za-z][A-Za-z0-9_]*)\\s*\\}?");

	// pattern for matching typed macro string, for example: ${a, float}
	private final static Pattern PATTERN_TYPED_MACRO = Pattern.compile("\\$\\s*\\{\\s*([A-Za-z][A-Za-z0-9_]*)\\s*,\\s*[A-Za-z][A-Za-z0-9_]*\\s*\\}");

	private File mActualFile = null;

	private Map<String, String> mEnvVariables;

	private CfgParseResult mCfgParseResult = new CfgParseResult();

	private int mLine = 1;
	private int mOffset = 0;

	private ModuleParameterSectionHandler moduleParametersHandler = new ModuleParameterSectionHandler();
	private TestportParameterSectionHandler testportParametersHandler = new TestportParameterSectionHandler();
	private ComponentSectionHandler componentSectionHandler = new ComponentSectionHandler();
	private GroupSectionHandler groupSectionHandler = new GroupSectionHandler();
	private MCSectionHandler mcSectionHandler = new MCSectionHandler();
	private ExternalCommandSectionHandler externalCommandsSectionHandler = new ExternalCommandSectionHandler();
	private ExecuteSectionHandler executeSectionHandler = new ExecuteSectionHandler();
	private IncludeSectionHandler includeSectionHandler = new IncludeSectionHandler();
	private IncludeSectionHandler orderedIncludeSectionHandler = new IncludeSectionHandler();
	private DefineSectionHandler defineSectionHandler = new DefineSectionHandler();
	private LoggingSectionHandler loggingSectionHandler = new LoggingSectionHandler();

	/**
	 * Adds a new definition
	 * @param aName name
	 * @param aValue definition value
	 * @param aToken token of the definition for getting its location
	 */
	private void addDefinition( final String aName, final String aValue, final Token aToken ) {
		final ArrayList<CfgLocation> locations = new ArrayList<CfgLocation>();
		locations.add( new CfgLocation( mActualFile, aToken, aToken ) );
		mCfgParseResult.getDefinitions().put( aName, new CfgDefinitionInformation( aValue, locations ) );
	}

	/**
	 * Adds a new macro reference
	 * @param aMacroName name
	 * @param aMacroToken token of the macro for getting its location
	 * @param aErrorMarker error marker, it will be shown after parsing
	 *                     if the definition referenced macro is not found
	 */
	private void addMacro( final String aMacroName, final Token aMacroToken, final TITANMarker aErrorMarker ) {
		mCfgParseResult.addMacro( aMacroName, aMacroToken, mActualFile, aErrorMarker );
	}

	public void reportWarning(TITANMarker marker){
		mCfgParseResult.getWarningsAndErrors().add(marker);
	}

	public void setActualFile(File file) {
		mActualFile = file;
	}

	public CfgParseResult getCfgParseResult() {
		return mCfgParseResult;
	}

	public void setEnvironmentalVariables(Map<String, String> aEnvVariables){
		mEnvVariables = aEnvVariables;
	}

	public ModuleParameterSectionHandler getModuleParametersHandler() {
		return moduleParametersHandler;
	}

	public TestportParameterSectionHandler getTestportParametersHandler() {
		return testportParametersHandler;
	}

	public ComponentSectionHandler getComponentSectionHandler() {
		return componentSectionHandler;
	}

	public GroupSectionHandler getGroupSectionHandler() {
		return groupSectionHandler;
	}

	public MCSectionHandler getMcSectionHandler() {
		return mcSectionHandler;
	}

	public ExternalCommandSectionHandler getExternalCommandsSectionHandler() {
		return externalCommandsSectionHandler;
	}

	public ExecuteSectionHandler getExecuteSectionHandler() {
		return executeSectionHandler;
	}

	public IncludeSectionHandler getIncludeSectionHandler() {
		return includeSectionHandler;
	}

	public IncludeSectionHandler getOrderedIncludeSectionHandler() {
		return orderedIncludeSectionHandler;
	}

	public DefineSectionHandler getDefineSectionHandler() {
		return defineSectionHandler;
	}

	public LoggingSectionHandler getLoggingSectionHandler() {
		return loggingSectionHandler;
	}

	/**
	 * Creates a marker.
	 * Locations of input tokens are not moved by offset and line yet, this function does this conversion.
	 * @param aMessage marker message
	 * @param aStartToken the 1st token, its line and start position will be used for the location
	 *                  NOTE: start position is the column index of the tokens 1st character.
	 *                        Column index starts with 0.
	 * @param aEndToken the last token, its end position will be used for the location.
	 *                  NOTE: end position is the column index after the token's last character.
	 * @param aSeverity severity (info/warning/error)
	 * @param aPriority priority (low/normal/high)
	 * @return new marker
	 */
	public TITANMarker createMarker( final String aMessage, final Token aStartToken, final Token aEndToken, final int aSeverity, final int aPriority ) {
		TITANMarker marker = new TITANMarker(
			aMessage,
			(aStartToken != null) ? mLine - 1 + aStartToken.getLine() : -1,
			(aStartToken != null) ? mOffset + aStartToken.getStartIndex() : -1,
			(aEndToken != null) ? mOffset + aEndToken.getStopIndex() + 1 : -1,
			aSeverity, aPriority );
		return marker;
	}

	/**
	 * Adds an error marker.
	 * Locations of input tokens are not moved by offset and line yet, this function does this conversion.
	 * @param aMessage marker message
	 * @param aStartToken the 1st token, its line and start position will be used for the location
	 *                  NOTE: start position is the column index of the tokens 1st character.
	 *                        Column index starts with 0.
	 * @param aEndToken the last token, its end position will be used for the location.
	 *                  NOTE: end position is the column index after the token's last character.
	 */
	public void reportError( final String aMessage, final Token aStartToken, final Token aEndToken ) {
		TITANMarker marker = createError( aMessage, aStartToken, aEndToken );
		mCfgParseResult.getWarningsAndErrors().add(marker);
	}

	/**
	 * Creates an error marker.
	 * Locations of input tokens are not moved by offset and line yet, this function does this conversion.
	 * @param aMessage marker message
	 * @param aStartToken the 1st token, its line and start position will be used for the location
	 *                  NOTE: start position is the column index of the tokens 1st character.
	 *                        Column index starts with 0.
	 * @param aEndToken the last token, its end position will be used for the location.
	 *                  NOTE: end position is the column index after the token's last character.
	 * @return the created error marker
	 */
	public TITANMarker createError( final String aMessage, final Token aStartToken, final Token aEndToken ) {
		final TITANMarker marker = createMarker( aMessage, aStartToken, aEndToken, SEVERITY_ERROR, PRIORITY_NORMAL );
		return marker;
	}

	/**
	 * Gets the value of a macro or an environment variable
	 * @param aDefinition macro or environment variable
	 * @return macro or environment variable value, or null if there is no such definition
	 */
	private String getDefinitionValue(String aDefinition){
		final Map< String, CfgDefinitionInformation > definitions = mCfgParseResult.getDefinitions();
		if ( definitions != null && definitions.containsKey( aDefinition ) ) {
			return definitions.get( aDefinition ).getValue();
		} else if ( mEnvVariables != null && mEnvVariables.containsKey( aDefinition ) ) {
			return mEnvVariables.get( aDefinition );
		} else {
			return null;
		}
	}

	/**
	 * Extracts macro name from macro string
	 * @param aMacroString macro string, for example: \$a, \${a}
	 * @return extracted macro name without extra characters, for example: a
	 */
	private String getMacroName( final String aMacroString ) {
		final Matcher m = PATTERN_MACRO.matcher( aMacroString );
		if ( m.find() ) {
			return m.group(1);
		} else {
			return null;
		}
	}

	/**
	 * Extracts macro name from typed macro string
	 * @param aMacroString macro string, for example: \${a, float}
	 * @return extracted macro name without extra characters, for example: a
	 */
	private String getTypedMacroName( final String aMacroString ) {
		final Matcher m = PATTERN_TYPED_MACRO.matcher( aMacroString );
		if ( m.find() ) {
			return m.group(1);
		} else {
			return null;
		}
	}

	/**
	 * Gets the macro value string of a macro (without type)
	 * @param aMacroToken the macro token
	 * @param aErrorFormatStr format strings for error messages if definition (macro or environment variable) cannot be resolved
	 *                        %s : definition
	 * @return the macro value string
	 *         or "" if macro is invalid. In this case an error marker is also created
	 */
	private String getMacroValue( final Token aMacroToken, final String aErrorFormatStr ) {
		final String definition = getMacroName( aMacroToken.getText() );
		final String errorMsg = String.format( aErrorFormatStr, definition );
		final String value = getDefinitionValue( definition );
		if ( value == null ) {
			// macro errors are processed later
			final TITANMarker errorMarker = createError( errorMsg, aMacroToken, aMacroToken );
			addMacro( definition, aMacroToken, errorMarker );
			return "";
		}
		return value;
	}

	/**
	 * Gets the macro value string of a macro (with type)
	 * @param aMacroToken the macro token
	 * @param aErrorFormatStr format strings for error messages if definition (macro or environment variable) cannot be resolved
	 *                        %s : definition
	 * @return the macro value string
	 *         or "" if macro is invalid. In this case an error marker is also created
	 */
	private String getTypedMacroValue( Token aMacroToken, String aErrorFormatStr ) {
		final String definition = getTypedMacroName( aMacroToken.getText() );
		final String errorMsg = String.format( aErrorFormatStr, definition );
		final String value = getDefinitionValue( definition );
		if ( value == null ) {
			// macro errors are processed later
			final TITANMarker errorMarker = createError( errorMsg, aMacroToken, aMacroToken );
			addMacro( definition, aMacroToken, errorMarker );
			return "";
		}
		return value;
	}
}

pr_ConfigFile:
	(	pr_Section
	)+
	EOF
;

pr_Section:
(	mc = pr_MainControllerSection
|	i = pr_IncludeSection
|	oi = pr_OrderedIncludeSection
|	e = pr_ExecuteSection
|	d = pr_DefineSection
|	ec = pr_ExternalCommandsSection
|	tp = pr_TestportParametersSection
|	g = pr_GroupsSection
|	mp = pr_ModuleParametersSection
|	c = pr_ComponentsSection
|	l = pr_LoggingSection
|	p = pr_ProfilerSection
)
;

pr_MainControllerSection:
MAIN_CONTROLLER_SECTION
(	pr_MainControllerItem SEMICOLON?
)*
;

pr_MainControllerItem:
(	pr_MainControllerItemUnixDomainSocket
|	pr_MainControllerItemKillTimer
|	pr_MainControllerItemLocalAddress
|	pr_MainControllerItemNumHcs
|	pr_MainControllerItemTcpPort
)+
;

pr_MainControllerItemUnixDomainSocket:
	UNIXSOCKETS
	ASSIGNMENTCHAR
	u = pr_MainControllerItemUnixDomainSocketValue
	SEMICOLON?
	{	if ( $u.text != null ) {
			boolean value = "yes".equalsIgnoreCase( $u.text );
			mCfgParseResult.setUnixDomainSocket( value );
			//TODO: remove one of them, it is redundant
			mcSectionHandler.setUnixDomainSocket( value );
		}
	}
;

pr_MainControllerItemUnixDomainSocketValue:
	(YES | NO)
;

pr_MainControllerItemKillTimer:
	KILLTIMER
	ASSIGNMENTCHAR
	k = pr_ArithmeticValueExpression
	SEMICOLON?
	{	if ( $k.number != null ) {
			mCfgParseResult.setKillTimer( $k.number.getValue() );
			//TODO: remove one of them, it is redundant
			mcSectionHandler.setKillTimer( $k.number );
		}
	}
;

pr_MainControllerItemLocalAddress:
	LOCALADDRESS
	ASSIGNMENTCHAR
	l = pr_HostName
	SEMICOLON?
	{	mCfgParseResult.setLocalAddress( $l.text );
		//TODO: remove one of them, it is redundant
		mcSectionHandler.setLocalAddress( $l.text );
	}
;

pr_MainControllerItemNumHcs:
	NUMHCS
	ASSIGNMENTCHAR
	n = pr_IntegerValueExpression
	SEMICOLON?
	{	if ( $n.integer != null ) {
			mCfgParseResult.setNumHcs( $n.integer.getIntegerValue() );
			//TODO: remove one of them, it is redundant
			mcSectionHandler.setNumHCsText( $n.integer );
		}
	}
;

pr_MainControllerItemTcpPort:
	TCPPORT
	ASSIGNMENTCHAR
	t = pr_IntegerValueExpression
	SEMICOLON?
	{	if ( $t.integer != null ) {
			mCfgParseResult.setTcpPort( $t.integer.getIntegerValue() );
			//TODO: remove one of them, it is redundant
			mcSectionHandler.setTcpPort( $t.integer );
		}
	}
;

pr_IncludeSection:
	INCLUDE_SECTION
	( f = INCLUDE_FILENAME
		{	String fileName = $f.getText().substring( 1, $f.getText().length() - 1 );
			mCfgParseResult.getIncludeFiles().add( fileName );
			//TODO: remove one of them, it is redundant
			includeSectionHandler.getFiles().add( fileName );
		}
	)*
;

pr_OrderedIncludeSection:
	ORDERED_INCLUDE_SECTION
	( f = ORDERED_INCLUDE_FILENAME
		{	String fileName = $f.getText().substring( 1, $f.getText().length() - 1 );
			mCfgParseResult.getIncludeFiles().add( fileName );
			//TODO: remove one of them, it is redundant
			orderedIncludeSectionHandler.getFiles().add( fileName );
		}
	)*
;

pr_ExecuteSection:
	EXECUTE_SECTION
	pr_ExecuteSectionItem*
;

pr_ExecuteSectionItem
@init {
	String executeElement = "";
	ExecuteItem item = new ExecuteItem();
}:
	module = pr_ExecuteSectionItemModuleName
		{
			executeElement += $module.name;
			item.setModuleName( $module.name );
		}
	(	DOT
		testcase = pr_ExecuteSectionItemTestcaseName
			{
				executeElement += $testcase.name;
				item.setTestcaseName( $testcase.name );
			}
	)?
	//TODO: remove one of them, it is redundant
	{	mCfgParseResult.getExecuteElements().add( executeElement );
		executeSectionHandler.getExecuteitems().add( item );
	}
	SEMICOLON?
;

pr_ExecuteSectionItemModuleName returns [ String name ]:
	t = TTCN3IDENTIFIER
{	$name = $t.text != null ? $t.text : "";
};

pr_ExecuteSectionItemTestcaseName returns [ String name ]:
	t = ( TTCN3IDENTIFIER | STAR )
{	$name = $t.text != null ? $t.text : "";
};

pr_DefineSection:
	DEFINE_SECTION
	(	def = pr_MacroAssignment
			{	if ( $def.definition != null ) {
					defineSectionHandler.getDefinitions().add( $def.definition );
				}
			}
	)*
;

pr_ExternalCommandsSection:
	EXTERNAL_COMMANDS_SECTION
	(	pr_ExternalCommand
		SEMICOLON?
	)*
;

pr_ExternalCommand:
	(	BEGINCONTROLPART
		ASSIGNMENTCHAR
		v = pr_ExternalCommandValue
			{	externalCommandsSectionHandler.setBeginControlPart( $v.text );
			}
	|	ENDCONTROLPART
		ASSIGNMENTCHAR
		v = pr_ExternalCommandValue
			{	externalCommandsSectionHandler.setEndControlPart( $v.text );
			}
	|	BEGINTESTCASE
		ASSIGNMENTCHAR
		v = pr_ExternalCommandValue
			{	externalCommandsSectionHandler.setBeginTestcase( $v.text );
			}
	|	ENDTESTCASE
		ASSIGNMENTCHAR
		v = pr_ExternalCommandValue
			{	externalCommandsSectionHandler.setEndTestcase( $v.text );
			}
	)
;

pr_ExternalCommandValue:
	pr_StringValue
;

pr_TestportParametersSection:
	TESTPORT_PARAMETERS_SECTION
	(	pr_TestportParameter
		SEMICOLON?
	)*
;

pr_TestportParameter:
	a = pr_ComponentID
	DOT
	b = pr_TestportName
	DOT
	c = pr_Identifier
	ASSIGNMENTCHAR
	d = pr_StringValue
{	TestportParameterSectionHandler.TestportParameter parameter = new TestportParameterSectionHandler.TestportParameter();
	parameter.setComponentName( $a.ctx );
	parameter.setTestportName( $b.ctx );
	parameter.setParameterName( $c.ctx );
	parameter.setValue( $d.ctx );
	testportParametersHandler.getTestportParameters().add( parameter );
}
;

pr_GroupsSection:
	GROUPS_SECTION
	(	pr_GroupItem SEMICOLON?
	)*
;

pr_ModuleParametersSection:
	MODULE_PARAMETERS_SECTION
	(	param = pr_ModuleParam
			{	if ( $param.parameter != null ) {
					moduleParametersHandler.getModuleParameters().add( $param.parameter );
				}
			}
		SEMICOLON?
	)*
;

pr_ComponentsSection:
	COMPONENTS_SECTION
	(	pr_ComponentItem SEMICOLON?
	)*
;

pr_LoggingSection:
	LOGGING_SECTION
	(	pr_LoggingParam	SEMICOLON?
	)*
;

pr_ProfilerSection:
	PROFILER_SECTION
	(	pr_ProfilerSetting SEMICOLON?
	)*
;

pr_ProfilerSetting:
(	pr_DisableProfiler
|	pr_DisableCoverage
|	pr_DatabaseFile
|	pr_AggregateData
|	pr_StatisticsFile
|	pr_DisableStatistics
|	pr_StatisticsFilter
|	pr_StartAutomatically
|	pr_NetLineTimes
|	pr_NetFunctionTimes
)
;

pr_DisableProfiler:
	DISABLEPROFILER
	ASSIGNMENTCHAR
	(	TRUE
	|	FALSE
	)
;

pr_DisableCoverage:
	DISABLECOVERAGE
	ASSIGNMENTCHAR
	(	TRUE
	|	FALSE
	)
;

pr_DatabaseFile:
	DATABASEFILE
	ASSIGNMENTCHAR
	pr_DatabaseFilePart
	(	AND
		pr_DatabaseFilePart
	)*
;

pr_DatabaseFilePart:
(	STRING
|	macro = MACRO
		{	String value = getMacroValue( $macro, DEFINITION_NOT_FOUND_STRING );
			//TODO: implement: use value if needed
		}
);

pr_AggregateData:
	AGGREGATEDATA
	ASSIGNMENTCHAR
	(	TRUE
	|	FALSE
	)
;

pr_StatisticsFile:
	STATISTICSFILE
	ASSIGNMENTCHAR
	pr_StatisticsFilePart
	(	AND
		pr_StatisticsFilePart
	)*
;

// currently it is the same as pr_DatabaseFilePart,
// but it will be different if value is used
pr_StatisticsFilePart:
(	STRING
|	macro = MACRO
		{	String value = getMacroValue( $macro, DEFINITION_NOT_FOUND_STRING );
			//TODO: implement: use value if needed
		}
);

pr_DisableStatistics:
	DISABLESTATISTICS
	ASSIGNMENTCHAR
	(	TRUE
	|	FALSE
	)
;

pr_StatisticsFilter:
	STATISTICSFILTER
	(	ASSIGNMENTCHAR
	|	CONCATCHAR
	)
	pr_StatisticsFilterEntry
	(	(	LOGICALOR
		|	AND
		)
		pr_StatisticsFilterEntry
	)*
;

pr_StatisticsFilterEntry:
(	NUMBEROFLINES
|	LINEDATARAW
|	FUNCDATARAW
|	LINEAVGRAW
|	FUNCAVGRAW
|	LINETIMESSORTEDBYMOD
|	FUNCTIMESSORTEDBYMOD
|	LINETIMESSORTEDTOTAL
|	FUNCTIMESSORTEDTOTAL
|	LINECOUNTSORTEDBYMOD
|	FUNCCOUNTSORTEDBYMOD
|	LINECOUNTSORTEDTOTAL
|	FUNCCOUNTSORTEDTOTAL
|	LINEAVGSORTEDBYMOD
|	FUNCAVGSORTEDBYMOD
|	LINEAVGSORTEDTOTAL
|	FUNCAVGSORTEDTOTAL
|	TOP10LINETIMES
|	TOP10FUNCTIMES
|	TOP10LINECOUNT
|	TOP10FUNCCOUNT
|	TOP10LINEAVG
|	TOP10FUNCAVG
|	UNUSEDLINES
|	UNUSEDFUNC
|	ALLRAWDATA
|	LINEDATASORTEDBYMOD
|	FUNCDATASORTEDBYMOD
|	LINEDATASORTEDTOTAL
|	FUNCDATASORTEDTOTAL
|	LINEDATASORTED
|	FUNCDATASORTED
|	ALLDATASORTED
|	TOP10LINEDATA
|	TOP10FUNCDATA
|	TOP10ALLDATA
|	UNUSEDATA
|	ALL
|	HEXFILTER
)
;

pr_StartAutomatically:
	STARTAUTOMATICALLY
	ASSIGNMENTCHAR
	(	TRUE
	|	FALSE
	)
;

pr_NetLineTimes:
	NETLINETIMES
	ASSIGNMENTCHAR
	(	TRUE
	|	FALSE
	)
;

pr_NetFunctionTimes:
	NETFUNCTIONTIMES
	ASSIGNMENTCHAR
	(	TRUE
	|	FALSE
	)
;

pr_LoggingParam:
	pr_ComponentSpecificLoggingParam
;

pr_ComponentSpecificLoggingParam:
	pr_LoggerPluginsPart
|	pr_PlainLoggingParam
;

pr_LoggerPluginsPart
@init {
	String componentName = "*";
}:
	(	cn = pt_TestComponentID DOT { componentName = $cn.text; }
	)?
	LOGGERPLUGINS
	ASSIGNMENTCHAR
	BEGINCHAR
	lpl = pr_LoggerPluginsList
	ENDCHAR
{
	for (LoggingSectionHandler.LoggerPluginEntry item : $lpl.entries) {
		LoggingSectionHandler.LogParamEntry lpe = loggingSectionHandler.componentPlugin(componentName, item.getName());
		lpe.setPluginPath(item.getPath());
	}
	LoggingSectionHandler.LoggerPluginsEntry entry = new LoggingSectionHandler.LoggerPluginsEntry();
	entry.setPlugins( new HashMap<String, LoggingSectionHandler.LoggerPluginEntry>( $lpl.entries.size() ) );
	for ( LoggingSectionHandler.LoggerPluginEntry item : $lpl.entries ) {
		entry.getPlugins().put(item.getName(), item);
	}
	loggingSectionHandler.getLoggerPluginsTree().put(componentName, entry);
}
;

pr_LoggerPluginsList returns [ List<LoggingSectionHandler.LoggerPluginEntry> entries ]
@init {
	$entries = new ArrayList<LoggingSectionHandler.LoggerPluginEntry>();
}:
	lpe = pr_LoggerPluginEntry { $entries.add( $lpe.entry ); }
	(	COMMA lpe = pr_LoggerPluginEntry { $entries.add( $lpe.entry ); }
	)*
;

pr_PlainLoggingParam
@init {
	String componentName = "*";
	String pluginName = "*";
}:
(	cn = pt_TestComponentID DOT { componentName = $cn.text; }
)?
(	STAR DOT
|	pn = pr_Identifier DOT { pluginName = $pn.text; }
)?
{	LogParamEntry logParamEntry = loggingSectionHandler.componentPlugin(componentName, pluginName);
}
(	FILEMASK ASSIGNMENTCHAR fileMask = pr_LoggingBitMask
		{	logParamEntry.setFileMaskBits( $fileMask.loggingBitMask );
		}
|	CONSOLEMASK ASSIGNMENTCHAR consoleMask = pr_LoggingBitMask
		{	logParamEntry.setConsoleMaskBits( $consoleMask.loggingBitMask );
		}
|	DISKFULLACTION ASSIGNMENTCHAR dfa = pr_DiskFullActionValue
		{	logParamEntry.setDiskFullAction( $dfa.text );
		}
|	LOGFILENUMBER ASSIGNMENTCHAR lfn = pr_NaturalNumber
		{	logParamEntry.setLogfileNumber( $lfn.integer );
		}
|	LOGFILESIZE ASSIGNMENTCHAR lfs = pr_NaturalNumber
		{	logParamEntry.setLogfileSize( $lfs.integer );
		}
|	LOGFILENAME ASSIGNMENTCHAR f = pr_LogfileName
	{	mCfgParseResult.setLogFileDefined( true );
		String logFileName = $f.text;
		//TODO: remove one of them, it is redundant
		if ( logFileName != null ) {
			// remove quotes
			logFileName = logFileName.replaceAll("^\"|\"$", "");
			mCfgParseResult.setLogFileName( logFileName );
		}
		logParamEntry.setLogFile( $f.text );
	}
|	TIMESTAMPFORMAT ASSIGNMENTCHAR ttv = pr_TimeStampValue
	{	logParamEntry.setTimestampFormat( $ttv.text );
	}
|	CONSOLETIMESTAMPFORMAT ASSIGNMENTCHAR ttv = pr_TimeStampValue
	{	logParamEntry.setConsoleTimestampFormat( $ttv.text );
	}
|	SOURCEINFOFORMAT ASSIGNMENTCHAR
	(	siv1 = pr_SourceInfoValue
		{	logParamEntry.setSourceInfoFormat( $siv1.text );
		}
	|	siv2 = pr_YesNoOrBoolean
		{	logParamEntry.setSourceInfoFormat( $siv2.text );
		}
	)
|	APPENDFILE ASSIGNMENTCHAR af = pr_YesNoOrBoolean
	{	logParamEntry.setAppendFile( $af.bool );
	}
|	LOGEVENTTYPES ASSIGNMENTCHAR let = pr_LogEventTypesValue
	{	logParamEntry.setLogeventTypes( $let.text );
	}
|	LOGENTITYNAME ASSIGNMENTCHAR len = pr_YesNoOrBoolean
	{	logParamEntry.setLogEntityName( $len.bool );
	}
|	MATCHINGHINTS ASSIGNMENTCHAR mh = pr_MatchingHintsValue
	{	logParamEntry.setMatchingHints( $mh.text );
	}
|	o1 = pr_PluginSpecificParamName ASSIGNMENTCHAR o2 = pr_StringValue
	{	logParamEntry.getPluginSpecificParam().add(
			new LoggingSectionHandler.PluginSpecificParam( $o1.text, $o2.text ) );
	}
|	EMERGENCYLOGGING ASSIGNMENTCHAR el = pr_NaturalNumber
	{	logParamEntry.setEmergencyLogging( $el.integer );
	}
|	EMERGENCYLOGGINGBEHAVIOUR ASSIGNMENTCHAR elb = pr_BufferAllOrMasked
	{	logParamEntry.setEmergencyLoggingBehaviour( $elb.text );
	}
|	EMERGENCYLOGGINGMASK ASSIGNMENTCHAR elm = pr_LoggingBitMask
	{	logParamEntry.setEmergencyLoggingMask( $elm.loggingBitMask );
	}
)
;

pr_TimeStampValue:
	TIMESTAMPVALUE
;

pr_SourceInfoValue:
	SOURCEINFOVALUE
;

pr_PluginSpecificParamName:
	TTCN3IDENTIFIER
;

pr_BufferAllOrMasked:
	BUFFERALLORBUFFERMASKED
;

pr_DiskFullActionValue:
(	DISKFULLACTIONVALUE
|	DISKFULLACTIONVALUERETRY ( LPAREN NATURAL_NUMBER RPAREN )?
)
;

pr_LoggerPluginEntry returns [ LoggingSectionHandler.LoggerPluginEntry entry ]
@init {
	$entry = new LoggingSectionHandler.LoggerPluginEntry();
}:
	i = pr_Identifier {	$entry.setName( $i.identifier );
						$entry.setPath("");	}
	(	ASSIGNMENTCHAR
		s = pr_StringValue { $entry.setPath( $s.string ); }
	)?
;

pt_TestComponentID:
(	pr_Identifier
|	pr_NaturalNumber
|	MTCKEYWORD
|	STAR
)
;

pr_LoggingBitMask returns [ List<LoggingBit> loggingBitMask ]
@init {
	$loggingBitMask = new ArrayList<LoggingBit>();
}:
	pr_LoggingMaskElement [ $loggingBitMask ]
	(	LOGICALOR	pr_LoggingMaskElement [ $loggingBitMask ]
	)*
;

pr_LoggingMaskElement [ List<LoggingBit> loggingBitMask ]:
	pr_LogEventType [ $loggingBitMask ]
|	pr_LogEventTypeSet [ $loggingBitMask ]
|	pr_deprecatedEventTypeSet [ $loggingBitMask ]
;

pr_LogfileName:
	pr_StringValue
;

pr_YesNoOrBoolean returns [Boolean bool]:
	YESNO { $bool = "yes".equalsIgnoreCase( $YESNO.text ); }
|	b = pr_Boolean { $bool = $b.bool; }
;

pr_LogEventTypesValue:
	pr_YesNoOrBoolean
|	pr_Detailed
;

pr_MatchingHintsValue:
	COMPACT
|	DETAILED
;

pr_LogEventType [ List<LoggingBit> loggingBitMask ]:
(  a1 = ACTION_UNQUALIFIED		{ loggingBitMask.add(LoggingBit.ACTION_UNQUALIFIED); }
|  a2 = DEBUG_ENCDEC			{ loggingBitMask.add(LoggingBit.DEBUG_ENCDEC); }
|  a3 = DEBUG_TESTPORT			{ loggingBitMask.add(LoggingBit.DEBUG_TESTPORT); }
|  a4 = DEBUG_UNQUALIFIED		{ loggingBitMask.add(LoggingBit.DEBUG_UNQUALIFIED); }
|  a5 = DEFAULTOP_ACTIVATE		{ loggingBitMask.add(LoggingBit.DEFAULTOP_ACTIVATE); }
|  a6 = DEFAULTOP_DEACTIVATE	{ loggingBitMask.add(LoggingBit.DEFAULTOP_DEACTIVATE); }
|  a7 = DEFAULTOP_EXIT			{ loggingBitMask.add(LoggingBit.DEFAULTOP_EXIT); }
|  a8 = DEFAULTOP_UNQUALIFIED	{ loggingBitMask.add(LoggingBit.DEFAULTOP_UNQUALIFIED); }
|  a9 = ERROR_UNQUALIFIED		{ loggingBitMask.add(LoggingBit.ERROR_UNQUALIFIED); }
|  a10 = EXECUTOR_COMPONENT		{ loggingBitMask.add(LoggingBit.EXECUTOR_COMPONENT); }
|  a11 = EXECUTOR_CONFIGDATA	{ loggingBitMask.add(LoggingBit.EXECUTOR_CONFIGDATA); }
|  a12 = EXECUTOR_EXTCOMMAND	{ loggingBitMask.add(LoggingBit.EXECUTOR_EXTCOMMAND); }
|  a13 = EXECUTOR_LOGOPTIONS	{ loggingBitMask.add(LoggingBit.EXECUTOR_LOGOPTIONS); }
|  a14 = EXECUTOR_RUNTIME		{ loggingBitMask.add(LoggingBit.EXECUTOR_RUNTIME); }
|  a15 = EXECUTOR_UNQUALIFIED	{ loggingBitMask.add(LoggingBit.EXECUTOR_UNQUALIFIED); }
|  a16 = FUNCTION_RND			{ loggingBitMask.add(LoggingBit.FUNCTION_RND); }
|  a17 = FUNCTION_UNQUALIFIED	{ loggingBitMask.add(LoggingBit.FUNCTION_UNQUALIFIED); }
|  a18 = MATCHING_DONE			{ loggingBitMask.add(LoggingBit.MATCHING_DONE); }
|  a19 = MATCHING_MCSUCCESS		{ loggingBitMask.add(LoggingBit.MATCHING_MCSUCCESS); }
|  a20 = MATCHING_MCUNSUCC		{ loggingBitMask.add(LoggingBit.MATCHING_MCUNSUCC); }
|  a21 = MATCHING_MMSUCCESS		{ loggingBitMask.add(LoggingBit.MATCHING_MMSUCCESS); }
|  a22 = MATCHING_MMUNSUCC		{ loggingBitMask.add(LoggingBit.MATCHING_MMUNSUCC); }
|  a23 = MATCHING_PCSUCCESS		{ loggingBitMask.add(LoggingBit.MATCHING_PCSUCCESS); }
|  a24 = MATCHING_PCUNSUCC		{ loggingBitMask.add(LoggingBit.MATCHING_PCUNSUCC); }
|  a25 = MATCHING_PMSUCCESS		{ loggingBitMask.add(LoggingBit.MATCHING_PMSUCCESS); }
|  a26 = MATCHING_PMUNSUCC		{ loggingBitMask.add(LoggingBit.MATCHING_PMUNSUCC); }
|  a27 = MATCHING_PROBLEM		{ loggingBitMask.add(LoggingBit.MATCHING_PROBLEM); }
|  a28 = MATCHING_TIMEOUT		{ loggingBitMask.add(LoggingBit.MATCHING_TIMEOUT); }
|  a29 = MATCHING_UNQUALIFIED	{ loggingBitMask.add(LoggingBit.MATCHING_UNQUALIFIED); }
|  a30 = PARALLEL_PORTCONN		{ loggingBitMask.add(LoggingBit.PARALLEL_PORTCONN); }
|  a31 = PARALLEL_PORTMAP		{ loggingBitMask.add(LoggingBit.PARALLEL_PORTMAP); }
|  a32 = PARALLEL_PTC			{ loggingBitMask.add(LoggingBit.PARALLEL_PTC); }
|  a33 = PARALLEL_UNQUALIFIED	{ loggingBitMask.add(LoggingBit.PARALLEL_UNQUALIFIED); }
|  a34 = PORTEVENT_DUALRECV		{ loggingBitMask.add(LoggingBit.PORTEVENT_DUALRECV); }
|  a35 = PORTEVENT_DUALSEND		{ loggingBitMask.add(LoggingBit.PORTEVENT_DUALSEND); }
|  a36 = PORTEVENT_MCRECV		{ loggingBitMask.add(LoggingBit.PORTEVENT_MCRECV); }
|  a37 = PORTEVENT_MCSEND		{ loggingBitMask.add(LoggingBit.PORTEVENT_MCSEND); }
|  a38 = PORTEVENT_MMRECV		{ loggingBitMask.add(LoggingBit.PORTEVENT_MMRECV); }
|  a39 = PORTEVENT_MMSEND		{ loggingBitMask.add(LoggingBit.PORTEVENT_MMSEND); }
|  a40 = PORTEVENT_MQUEUE		{ loggingBitMask.add(LoggingBit.PORTEVENT_MQUEUE); }
|  a41 = PORTEVENT_PCIN			{ loggingBitMask.add(LoggingBit.PORTEVENT_PCIN); }
|  a42 = PORTEVENT_PCOUT		{ loggingBitMask.add(LoggingBit.PORTEVENT_PCOUT); }
|  a43 = PORTEVENT_PMIN			{ loggingBitMask.add(LoggingBit.PORTEVENT_PMIN); }
|  a44 = PORTEVENT_PMOUT		{ loggingBitMask.add(LoggingBit.PORTEVENT_PMOUT); }
|  a45 = PORTEVENT_PQUEUE		{ loggingBitMask.add(LoggingBit.PORTEVENT_PQUEUE); }
|  a46 = PORTEVENT_STATE		{ loggingBitMask.add(LoggingBit.PORTEVENT_STATE); }
|  a47 = PORTEVENT_UNQUALIFIED	{ loggingBitMask.add(LoggingBit.PORTEVENT_UNQUALIFIED); }
|  a48 = STATISTICS_UNQUALIFIED	{ loggingBitMask.add(LoggingBit.STATISTICS_UNQUALIFIED); }
|  a49 = STATISTICS_VERDICT		{ loggingBitMask.add(LoggingBit.STATISTICS_VERDICT); }
|  a50 = TESTCASE_FINISH		{ loggingBitMask.add(LoggingBit.TESTCASE_FINISH); }
|  a51 = TESTCASE_START			{ loggingBitMask.add(LoggingBit.TESTCASE_START); }
|  a52 = TESTCASE_UNQUALIFIED	{ loggingBitMask.add(LoggingBit.TESTCASE_UNQUALIFIED); }
|  a53 = TIMEROP_GUARD			{ loggingBitMask.add(LoggingBit.TIMEROP_GUARD); }
|  a54 = TIMEROP_READ			{ loggingBitMask.add(LoggingBit.TIMEROP_READ); }
|  a55 = TIMEROP_START			{ loggingBitMask.add(LoggingBit.TIMEROP_START); }
|  a56 = TIMEROP_STOP			{ loggingBitMask.add(LoggingBit.TIMEROP_STOP); }
|  a57 = TIMEROP_TIMEOUT		{ loggingBitMask.add(LoggingBit.TIMEROP_TIMEOUT); }
|  a58 = TIMEROP_UNQUALIFIED	{ loggingBitMask.add(LoggingBit.TIMEROP_UNQUALIFIED); }
|  a59 = USER_UNQUALIFIED		{ loggingBitMask.add(LoggingBit.USER_UNQUALIFIED); }
|  a60 = VERDICTOP_FINAL		{ loggingBitMask.add(LoggingBit.VERDICTOP_FINAL); }
|  a61 = VERDICTOP_GETVERDICT	{ loggingBitMask.add(LoggingBit.VERDICTOP_GETVERDICT); }
|  a62 = VERDICTOP_SETVERDICT	{ loggingBitMask.add(LoggingBit.VERDICTOP_SETVERDICT); }
|  a63 = VERDICTOP_UNQUALIFIED	{ loggingBitMask.add(LoggingBit.VERDICTOP_UNQUALIFIED); }
|  a64 = WARNING_UNQUALIFIED	{ loggingBitMask.add(LoggingBit.WARNING_UNQUALIFIED); }
)
;

pr_LogEventTypeSet [ List<LoggingBit> loggingBitMask ]:
(  a1 = TTCN_EXECUTOR2		{ loggingBitMask.add(LoggingBit.EXECUTOR); }
|  a2 = TTCN_ERROR2			{ loggingBitMask.add(LoggingBit.ERROR); }
|  a3 = TTCN_WARNING2		{ loggingBitMask.add(LoggingBit.WARNING); }
|  a4 = TTCN_PORTEVENT2		{ loggingBitMask.add(LoggingBit.PORTEVENT); }
|  a5 = TTCN_TIMEROP2		{ loggingBitMask.add(LoggingBit.TIMEROP); }
|  a6 = TTCN_VERDICTOP2		{ loggingBitMask.add(LoggingBit.VERDICTOP); }
|  a7 = TTCN_DEFAULTOP2		{ loggingBitMask.add(LoggingBit.DEFAULTOP); }
|  a8 = TTCN_ACTION2		{ loggingBitMask.add(LoggingBit.ACTION); }
|  a9 = TTCN_TESTCASE2		{ loggingBitMask.add(LoggingBit.TESTCASE); }
|  a10 = TTCN_FUNCTION2		{ loggingBitMask.add(LoggingBit.FUNCTION); }
|  a11 = TTCN_USER2			{ loggingBitMask.add(LoggingBit.USER); }
|  a12 = TTCN_STATISTICS2	{ loggingBitMask.add(LoggingBit.STATISTICS); }
|  a13 = TTCN_PARALLEL2		{ loggingBitMask.add(LoggingBit.PARALLEL); }
|  a14 = TTCN_MATCHING2		{ loggingBitMask.add(LoggingBit.MATCHING); }
|  a15 = TTCN_DEBUG2		{ loggingBitMask.add(LoggingBit.DEBUG); }
|  a16 = LOG_ALL			{ loggingBitMask.add(LoggingBit.LOG_ALL); }
|  a17 = LOG_NOTHING		{ loggingBitMask.add(LoggingBit.LOG_NOTHING); }
)
;

pr_deprecatedEventTypeSet [ List<LoggingBit> loggingBitMask ]:
(  a1 = TTCN_EXECUTOR1		{ loggingBitMask.add(LoggingBit.EXECUTOR); }
|  a2 = TTCN_ERROR1			{ loggingBitMask.add(LoggingBit.ERROR); }
|  a3 = TTCN_WARNING1		{ loggingBitMask.add(LoggingBit.WARNING); }
|  a4 = TTCN_PORTEVENT1		{ loggingBitMask.add(LoggingBit.PORTEVENT); }
|  a5 = TTCN_TIMEROP1		{ loggingBitMask.add(LoggingBit.TIMEROP); }
|  a6 = TTCN_VERDICTOP1		{ loggingBitMask.add(LoggingBit.VERDICTOP); }
|  a7 = TTCN_DEFAULTOP1		{ loggingBitMask.add(LoggingBit.DEFAULTOP); }
|  a8 = TTCN_ACTION1		{ loggingBitMask.add(LoggingBit.ACTION); }
|  a9 = TTCN_TESTCASE1		{ loggingBitMask.add(LoggingBit.TESTCASE); }
|  a10 = TTCN_FUNCTION1		{ loggingBitMask.add(LoggingBit.FUNCTION); }
|  a11 = TTCN_USER1			{ loggingBitMask.add(LoggingBit.USER); }
|  a12 = TTCN_STATISTICS1	{ loggingBitMask.add(LoggingBit.STATISTICS); }
|  a13 = TTCN_PARALLEL1		{ loggingBitMask.add(LoggingBit.PARALLEL); }
|  a14 = TTCN_MATCHING1		{ loggingBitMask.add(LoggingBit.MATCHING); }
|  a15 = TTCN_DEBUG1		{ loggingBitMask.add(LoggingBit.DEBUG); }
)
{	reportWarning(new TITANMarker("Deprecated logging option " + $start.getText(), $start.getLine(),
		$start.getStartIndex(), $start.getStopIndex(), SEVERITY_WARNING, PRIORITY_NORMAL));
}
;

pr_Detailed:
	DETAILED
|	SUBCATEGORIES
;

pr_ComponentItem:
{	ComponentSectionHandler.Component component = new ComponentSectionHandler.Component();
}
	n = pr_ComponentName
		{	component.setComponentName( $n.text );	}
	ASSIGNMENTCHAR
	(	h = pr_HostName {	mCfgParseResult.getComponents().put( $n.text, $h.text );
							component.setHostName( $h.text );
						}
	|	i = pr_HostNameIpV6	{	mCfgParseResult.getComponents().put( $n.text, $i.text );
								component.setHostName( $i.text );
							}
	)
{	componentSectionHandler.getComponents().add( component );
}
;

pr_ComponentName:
(	pr_Identifier
|	STAR
)
;

pr_HostName:
(	pr_DNSName
|	TTCN3IDENTIFIER
|	macro1 = MACRO_HOSTNAME
		{	String value = getTypedMacroValue( $macro1, DEFINITION_NOT_FOUND_STRING );
			//TODO: implement: use value if needed
		}
|	macro2 = MACRO
		{	String value = getMacroValue( $macro2, DEFINITION_NOT_FOUND_STRING );
			//TODO: implement: use value if needed
		}
)
;

pr_HostNameIpV6:
	IPV6
;

pr_MacroAssignment returns [ DefineSectionHandler.Definition definition ]
@init {
	$definition = null;
	String name = null;
	String value = null;
}:
(	col = TTCN3IDENTIFIER { name = $col.getText(); }
	ASSIGNMENTCHAR
	endCol = pr_DefinitionRValue { value = $endCol.text; }
)
{	if(name != null && value != null) {
		addDefinition( name, value, $col );
	}
	//TODO: remove one of them, it is redundant
	$definition = new DefineSectionHandler.Definition();
	$definition.setDefinitionName($col.text);
	$definition.setDefinitionValue($endCol.text);
}
;

pr_DefinitionRValue:
(	pr_SimpleValue+
|	pr_StructuredValue
)
;

pr_SimpleValue:
(	TTCN3IDENTIFIER
|	MACRORVALUE
|	MACRO_ID
|	MACRO_INT
|	MACRO_BOOL
|	MACRO_FLOAT
|	MACRO_EXP_CSTR
|	MACRO_BSTR
|	MACRO_HSTR
|	MACRO_OSTR
|	MACRO_BINARY
|	MACRO_HOSTNAME
|	MACRO
|	IPV6
|	STRING
|	BITSTRING
|	HEXSTRING
|	OCTETSTRING
|	BITSTRINGMATCH
|	HEXSTRINGMATCH
|	OCTETSTRINGMATCH
)
;

pr_StructuredValue:
	BEGINCHAR
	(pr_StructuredValue | pr_StructuredValue2)
	ENDCHAR
;

pr_StructuredValue2:
(	pr_MacroAssignment
|	pr_SimpleValue
)?
;

pr_ComponentID:
(	pr_Identifier
|	pr_NaturalNumber
|	MTC
|	SYSTEM
|	STAR
)
;

pr_TestportName:
(	pr_Identifier
	(	SQUAREOPEN pr_IntegerValueExpression SQUARECLOSE
	)*
|	STAR
)
;

pr_Identifier returns [String identifier]:
(	macro = MACRO_ID
		{	String value = getTypedMacroValue( $macro, DEFINITION_NOT_FOUND_STRING );
			$identifier = value;
		}
|	a = TTCN3IDENTIFIER
		{	$identifier = $a.getText();	}
)
;

pr_IntegerValueExpression returns [CFGNumber integer]:
	a = pr_IntegerAddExpression	{	$integer = $a.integer;	}
;

pr_IntegerAddExpression returns [CFGNumber integer]:
	a = pr_IntegerMulExpression	{	$integer = $a.integer;	}
	(	PLUS	b1 = pr_IntegerMulExpression	{	$integer.add($b1.integer);	}
	|	MINUS	b2 = pr_IntegerMulExpression	{	$b2.integer.mul(-1); $integer.add($b2.integer);	}
	)*
;

pr_IntegerMulExpression returns [CFGNumber integer]:
	a = pr_IntegerUnaryExpression	{	$integer = $a.integer;	}
	(	STAR	b1 = pr_IntegerUnaryExpression	{	$integer.mul($b1.integer);	}
	|	SLASH	b2 = pr_IntegerUnaryExpression
		{	try {
				$integer.div($b2.integer);
			} catch ( ArithmeticException e ) {
				// division by 0
				reportError( e.getMessage(), $a.start, $b2.stop );
				$integer = new CFGNumber( "0" );
			}
		}
	)*
;

pr_IntegerUnaryExpression returns [CFGNumber integer]:
{	boolean negate = false;
}
	(	PLUS
	|	MINUS	{	negate = !negate;	}
	)*
	a = pr_IntegerPrimaryExpression
		{	$integer = $a.integer;
			if ( negate ) {
				$integer.mul( -1 );
			}
		}
;

pr_IntegerPrimaryExpression returns [CFGNumber integer]:
(	a = pr_NaturalNumber	{	$integer = $a.integer;	}
|	LPAREN b = pr_IntegerAddExpression RPAREN	{	$integer = $b.integer;	}
)
;

pr_NaturalNumber returns [CFGNumber integer]:
(	a = NATURAL_NUMBER	{$integer = new CFGNumber($a.text);}
|	macro = pr_MacroNaturalNumber { $integer = $macro.integer; }
|	TTCN3IDENTIFIER // module parameter name
		{	$integer = new CFGNumber( "1" ); // value is unknown yet, but it should not be null
		}//TODO: incorrect behaviour
)
;

pr_MacroNaturalNumber returns [CFGNumber integer]:
(	macro1 = MACRO_INT
		{	String value = getTypedMacroValue( $macro1, DEFINITION_NOT_FOUND_INT );
			$integer = new CFGNumber( value.length() > 0 ? value : "0" );
		}
|	macro2 = MACRO
		{	String value = getMacroValue( $macro2, DEFINITION_NOT_FOUND_INT );
			$integer = new CFGNumber( value.length() > 0 ? value : "0" );
		}
)
;

pr_StringValue returns [String string]
@init {
	$string = "";
}:
	a = pr_CString
		{	if ( $a.string != null ) {
				$string = $a.string.replaceAll("^\"|\"$", "");
			}
		}
	(	STRINGOP
		b = pr_CString
			{	if ( $b.string != null ) {
					$string = $string + $b.string.replaceAll("^\"|\"$", "");
				}
			}
	)*
	{	if ( $string != null ) {
			$string = "\"" + $string + "\"";
		}
	}
;

pr_CString returns [String string]:
(	a = STRING
		{
			$string = $a.text;
		}
|	macro2 = pr_MacroCString			{	$string = "\"" + $macro2.string + "\"";	}
|	macro1 = pr_MacroExpliciteCString	{	$string = "\"" + $macro1.string + "\"";	}
|	TTCN3IDENTIFIER // module parameter name
		{	$string = "\"\""; // value is unknown yet, but it should not be null
		}
)
;

pr_MacroCString returns [String string]:
	macro = MACRO
		{	$string = getMacroValue( $macro, DEFINITION_NOT_FOUND_STRING );	}
;

pr_MacroExpliciteCString returns [String string]:
	macro = MACRO_EXP_CSTR
		{	$string = getTypedMacroValue( $macro, DEFINITION_NOT_FOUND_STRING );	}
;

pr_GroupItem:
{	List<String> memberlist = new ArrayList<String>();
	GroupSectionHandler.Group group = new GroupSectionHandler.Group();
}
(	a = pr_Identifier
	ASSIGNMENTCHAR
	(	STAR {	memberlist.add("*");	}
	|	(	c = pr_DNSName	{	memberlist.add( $c.text );
								group.getGroupItems().add( new GroupSectionHandler.GroupItem( $c.text ) );
							}
		|	d = pr_Identifier	{	memberlist.add( $d.text );
									group.getGroupItems().add( new GroupSectionHandler.GroupItem( $d.text ) );
								}
		)
		(	COMMA
			(	e = pr_DNSName	{	memberlist.add( $e.text );
									group.getGroupItems().add( new GroupSectionHandler.GroupItem( $e.text ) );
								}
			|	f = pr_Identifier	{	memberlist.add( $f.text );
										group.getGroupItems().add( new GroupSectionHandler.GroupItem( $f.text ) );
									}
			)
		)*
	)
)
{	mCfgParseResult.getGroups().put( $a.text, memberlist.toArray( new String[ memberlist.size() ] ) );
	//TODO: remove one of them, it is redundant
	group.setGroupName( $a.text );
	groupSectionHandler.getGroups().add( group );
}
;

pr_DNSName:
(	NATURAL_NUMBER
|	FLOAT
|	DNSNAME
)
;

pr_ModuleParam returns[ModuleParameterSectionHandler.ModuleParameter parameter]
@init {
	$parameter = null;
}:
	name = pr_ParameterName	{$parameter = $name.parameter;}
	(	ASSIGNMENTCHAR
		val1 = pr_ParameterValue	{$parameter.setValue($val1.text);}
	|	CONCATCHAR
		val2 = pr_ParameterValue	{$parameter.setValue($val2.text);}
	)
;

pr_ParameterName returns[ModuleParameterSectionHandler.ModuleParameter parameter]:
{	$parameter = new ModuleParameterSectionHandler.ModuleParameter();
}
(	id1 = pr_ParameterNamePart
	(	separator = pr_Dot
		id2 = pr_ParameterNameTail
			{	$parameter.setModuleName( $id1.text );
				$parameter.setSeparator( $separator.text );
				$parameter.setParameterName( $id2.text );
			}
	|	{	$parameter.setParameterName( $id1.text );
		}
	)
|	star = pr_StarModuleName
	DOT
	id3 = pr_ParameterNamePart
	{	$parameter.setModuleName($star.text);
		$parameter.setParameterName($id3.text);
	}
)
;

// One part of the parameter name which are separated by dots
pr_ParameterNamePart:
	pr_Identifier
	pr_IndexItemIndex*
;

// rest of the parameter name after the first dot
// this is handled as parameter (2nd column) in the cfg editor on module parameters tab
pr_ParameterNameTail:
	pr_ParameterNamePart
	(	pr_Dot
		pr_ParameterNamePart
	)*
;

pr_Dot:
	DOT
;

pr_StarModuleName:
	STAR
;

pr_ParameterValue:
	pr_ParameterExpression pr_LengthMatch? IFPRESENTKEYWORD?
;

//module parameter expression, it can contain previously defined module parameters
pr_ParameterExpression:
	pr_SimpleParameterValue
|	pr_ParameterReference
|	pr_ParameterExpression
	(	(	PLUS
		|	MINUS
		|	STAR
		|	SLASH
		|	STRINGOP
		)
		pr_ParameterExpression
	)+
|	(	PLUS
	|	MINUS
	)
	pr_ParameterExpression
|	LPAREN
	pr_ParameterExpression
	RPAREN
;

pr_LengthMatch:
	LENGTHKEYWORD LPAREN pr_LengthBound
	(	RPAREN
	|	DOTDOT
		(	pr_LengthBound | INFINITYKEYWORD	)
		RPAREN
	)
;

pr_SimpleParameterValue:
(	pr_ArithmeticValueExpression
|	pr_Boolean
|	pr_ObjIdValue
|	pr_VerdictValue
|	pr_BStringValue
|	pr_HStringValue
|	pr_OStringValue
|	pr_UniversalOrNotStringValue
|	OMITKEYWORD
|	pr_EnumeratedValue
|	pr_NULLKeyword
|	MTCKEYWORD
|	SYSTEMKEYWORD
|	pr_CompoundValue
|	ANYVALUE
|	STAR
|	pr_IntegerRange
|	pr_FloatRange
|	pr_StringRange
|	PATTERNKEYWORD pr_PatternChunkList
|	pr_BStringMatch
|	pr_HStringMatch
|	pr_OStringMatch
)
;
pr_ParameterReference:
	// enumerated values are also treated as references by the parser,
	// these will be sorted out later during set_param()
	pr_ParameterNameSegment
;

pr_ParameterNameSegment:
	pr_ParameterNameSegment
	pr_Dot
	pr_Identifier
|	pr_ParameterNameSegment
	pr_IndexItemIndex
|	pr_Identifier
;

pr_IndexItemIndex:
	SQUAREOPEN
	pr_IntegerValueExpression
	SQUARECLOSE
;

pr_LengthBound:
	pr_IntegerValueExpression
;

pr_ArithmeticValueExpression returns [CFGNumber number]:
	a = pr_ArithmeticAddExpression	{	$number = $a.number;	}
;

pr_ArithmeticAddExpression returns [CFGNumber number]:
	a = pr_ArithmeticMulExpression	{	$number = $a.number;	}
	(	PLUS	b1 = pr_ArithmeticMulExpression	{	$number.add($b1.number);	}
	|	MINUS	b2 = pr_ArithmeticMulExpression	{	$b2.number.mul(-1); $number.add($b2.number);	}
	)*
;

pr_ArithmeticMulExpression returns [CFGNumber number]:
	a = pr_ArithmeticUnaryExpression	{	$number = $a.number;	}
	(	STAR	b1 = pr_ArithmeticUnaryExpression	{	$number.mul($b1.number);	}
	|	SLASH	b2 = pr_ArithmeticUnaryExpression
		{	try {
				$number.div($b2.number);
			} catch ( ArithmeticException e ) {
				// division by 0
				reportError( e.getMessage(), $a.start, $b2.stop );
				$number = new CFGNumber( "0.0" );
			}
		}
	)*
;

pr_ArithmeticUnaryExpression returns [CFGNumber number]:
{	boolean negate = false;
}
	(	PLUS
	|	MINUS	{	negate = !negate;	}
	)*
	a = pr_ArithmeticPrimaryExpression
		{	$number = $a.number;
			if ( negate ) {
				$number.mul( -1 );
			}
		}
;

pr_ArithmeticPrimaryExpression returns [CFGNumber number]:
(	a = pr_Float	{$number = $a.number;}
|	b = pr_NaturalNumber	{$number = $b.integer;}
|	LPAREN c = pr_ArithmeticAddExpression RPAREN {$number = $c.number;}
)
;

pr_Float returns [CFGNumber number]:
(	a = FLOAT {$number = new CFGNumber($a.text);}
|	macro = MACRO_FLOAT
		{	String value = getTypedMacroValue( $macro, DEFINITION_NOT_FOUND_FLOAT );
			$number = new CFGNumber( value.length() > 0 ? value : "0.0" );
		}
|	TTCN3IDENTIFIER // module parameter name
		{	$number = new CFGNumber( "1.0" ); // value is unknown yet, but it should not be null
		}
)
;

pr_Boolean returns [Boolean bool]:
(	t = TRUE { $bool = true; }
|	f = FALSE { $bool = false; }
|	macro = MACRO_BOOL
		{	String value = getTypedMacroValue( $macro, DEFINITION_NOT_FOUND_BOOLEAN );
			$bool = "true".equalsIgnoreCase( value );
		}
)
;

pr_ObjIdValue:
	OBJIDKEYWORD	BEGINCHAR	pr_ObjIdComponent+	ENDCHAR
;

pr_ObjIdComponent:
(	pr_NaturalNumber
|	pr_Identifier LPAREN pr_NaturalNumber RPAREN
)
;

pr_VerdictValue:
(	NONE_VERDICT
|	PASS_VERDICT
|	INCONC_VERDICT
|	FAIL_VERDICT
|	ERROR_VERDICT
)
;

pr_BStringValue:
	pr_BString	(	STRINGOP pr_BString	)*
;

pr_BString returns [String string]:
(	b = BITSTRING { $string = $b.getText(); }
|	macro = MACRO_BSTR
		{	String value = getTypedMacroValue( $macro, DEFINITION_NOT_FOUND_BSTR );
			$string = "'" + value + "'B";
		}
)
;

pr_HStringValue:
	pr_HString	(	STRINGOP pr_HString	)*
;

pr_HString returns [String string]:
(	h = HEXSTRING { $string = $h.getText(); }
|	macro = MACRO_HSTR
		{	String value = getTypedMacroValue( $macro, DEFINITION_NOT_FOUND_HSTR );
			$string = "'" + value + "'H";
		}
)
;

pr_OStringValue:
	pr_OString	(	STRINGOP pr_OString	)*
;

pr_OString returns [String string]:
(	o = OCTETSTRING { $string = $o.getText(); }
|	macro = MACRO_OSTR
		{	String value = getTypedMacroValue( $macro, DEFINITION_NOT_FOUND_OSTR );
			$string = "'" + value + "'0";
		}
|	macro_bin = MACRO_BINARY
		{	String value = getTypedMacroValue( $macro_bin, DEFINITION_NOT_FOUND_STRING );
			$string = value;
		}
)
;

pr_UniversalOrNotStringValue:
(	pr_CString
|	pr_Quadruple
)
(	STRINGOP
	(	pr_CString
	|	pr_Quadruple
	)
)*
;

pr_Quadruple:
	CHARKEYWORD
	LPAREN
	pr_IntegerValueExpression COMMA pr_IntegerValueExpression COMMA pr_IntegerValueExpression COMMA pr_IntegerValueExpression
	RPAREN
;

pr_EnumeratedValue:
	pr_Identifier
;

pr_NULLKeyword:
	NULLKEYWORD
;

pr_CompoundValue:
(	BEGINCHAR
	(	/* empty */
	|	pr_FieldValue	(	COMMA pr_FieldValue	)*
	|	pr_ArrayItem	(	COMMA pr_ArrayItem	)*
	|	pr_IndexValue	(	COMMA pr_IndexValue	)*
	)
	ENDCHAR
|	LPAREN
	/* at least 2 elements to avoid shift/reduce conflicts with pr_IntegerValueExpression and pr_FloatValueExpression rules */
	pr_ParameterValue (COMMA pr_ParameterValue)+
	RPAREN
|	COMPLEMENTKEYWORD LPAREN pr_ParameterValue (COMMA pr_ParameterValue)* RPAREN
|	SUPERSETKEYWORD LPAREN pr_ParameterValue (COMMA pr_ParameterValue)* RPAREN
|	SUBSETKEYWORD LPAREN pr_ParameterValue (COMMA pr_ParameterValue)* RPAREN
)
;

pr_FieldValue:
	pr_FieldName ASSIGNMENTCHAR pr_ParameterValueOrNotUsedSymbol
;

pr_FieldName:
	pr_Identifier
;

pr_ParameterValueOrNotUsedSymbol:
	MINUS
|	pr_ParameterValue
;

pr_ArrayItem:
	pr_ParameterValueOrNotUsedSymbol
|	PERMUTATIONKEYWORD LPAREN pr_TemplateItemList RPAREN
;

pr_TemplateItemList:
	pr_ParameterValue
	(	COMMA pr_ParameterValue
	)*
;

pr_IndexValue:
	SQUAREOPEN pr_IntegerValueExpression SQUARECLOSE ASSIGNMENTCHAR pr_ParameterValue
;

pr_IntegerRange:
	LPAREN
	(	MINUS INFINITYKEYWORD DOTDOT (pr_IntegerValueExpression | INFINITYKEYWORD)
	|	pr_IntegerValueExpression DOTDOT (pr_IntegerValueExpression | INFINITYKEYWORD)
	)
	RPAREN
;

pr_FloatRange:
	LPAREN
	(	MINUS INFINITYKEYWORD DOTDOT (pr_FloatValueExpression | INFINITYKEYWORD)
	|	pr_FloatValueExpression DOTDOT (pr_FloatValueExpression | INFINITYKEYWORD)
	)
	RPAREN
;

pr_FloatValueExpression:
	pr_FloatAddExpression
;

pr_FloatAddExpression:
	pr_FloatMulExpression
	(	(	PLUS
		|	MINUS
		)
		pr_FloatMulExpression
	)*
;

pr_FloatMulExpression:
	pr_FloatUnaryExpression
	(	(	STAR
		|	SLASH
		)
		pr_FloatUnaryExpression
	)*
;

pr_FloatUnaryExpression:
	(	PLUS
	|	MINUS
	)*
	pr_FloatPrimaryExpression
;

pr_FloatPrimaryExpression:
(	pr_Float
|	LPAREN pr_FloatAddExpression RPAREN
)
;

pr_StringRange:
	LPAREN pr_UniversalOrNotStringValue DOTDOT pr_UniversalOrNotStringValue RPAREN
;

pr_PatternChunkList:
	pr_PatternChunk (AND pr_PatternChunk)*
;

pr_PatternChunk:
	pr_CString
|	pr_Quadruple
;

pr_BStringMatch:
	BITSTRINGMATCH
;

pr_HStringMatch:
	HEXSTRINGMATCH
;

pr_OStringMatch:
	OCTETSTRINGMATCH
;
