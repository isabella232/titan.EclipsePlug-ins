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
import org.eclipse.titan.runtime.core.Base_Type;
import org.eclipse.titan.runtime.core.LoggingParam.logging_param_t;
import org.eclipse.titan.runtime.core.LoggingParam.logging_param_type;
import org.eclipse.titan.runtime.core.LoggingParam.logging_setting_t;
import org.eclipse.titan.runtime.core.Module_List;
import org.eclipse.titan.runtime.core.Module_Param_Length_Restriction;
import org.eclipse.titan.runtime.core.Param_Types.Module_Param_Any;
import org.eclipse.titan.runtime.core.Param_Types.Module_Param_AnyOrNone;
import org.eclipse.titan.runtime.core.Param_Types.Module_Param_Asn_Null;
import org.eclipse.titan.runtime.core.Param_Types.Module_Param_Assignment_List;
import org.eclipse.titan.runtime.core.Param_Types.Module_Param_Bitstring;
import org.eclipse.titan.runtime.core.Param_Types.Module_Param_Bitstring_Template;
import org.eclipse.titan.runtime.core.Param_Types.Module_Param_Boolean;
import org.eclipse.titan.runtime.core.Param_Types.Module_Param_Charstring;
import org.eclipse.titan.runtime.core.Param_Types.Module_Param_ComplementList_Template;
import org.eclipse.titan.runtime.core.Param_Types.Module_Param_Hexstring;
import org.eclipse.titan.runtime.core.Param_Types.Module_Param_Hexstring_Template;
import org.eclipse.titan.runtime.core.Param_Types.Module_Param_Id;
import org.eclipse.titan.runtime.core.Param_Types.Module_Param_Indexed_List;
import org.eclipse.titan.runtime.core.Param_Types.Module_Param_Enumerated;
import org.eclipse.titan.runtime.core.Param_Types.Module_Param_Expression;
import org.eclipse.titan.runtime.core.Param_Types.Module_Param_FieldName;
import org.eclipse.titan.runtime.core.Param_Types.Module_Param_Float;
import org.eclipse.titan.runtime.core.Param_Types.Module_Param_FloatRange;
import org.eclipse.titan.runtime.core.Param_Types.Module_Param_Index;
import org.eclipse.titan.runtime.core.Param_Types.Module_Param_Integer;
import org.eclipse.titan.runtime.core.Param_Types.Module_Param_IntRange;
import org.eclipse.titan.runtime.core.Param_Types.Module_Param_List_Template;
import org.eclipse.titan.runtime.core.Param_Types.Module_Param_Name;
import org.eclipse.titan.runtime.core.Param_Types.Module_Param_NotUsed;
import org.eclipse.titan.runtime.core.Param_Types.Module_Param_Objid;
import org.eclipse.titan.runtime.core.Param_Types.Module_Param_Omit;
import org.eclipse.titan.runtime.core.Param_Types.Module_Param_Octetstring;
import org.eclipse.titan.runtime.core.Param_Types.Module_Param_Octetstring_Template;
import org.eclipse.titan.runtime.core.Param_Types.Module_Param_Pattern;
import org.eclipse.titan.runtime.core.Param_Types.Module_Param_Permutation_Template;
import org.eclipse.titan.runtime.core.Param_Types.Module_Param_StringRange;
import org.eclipse.titan.runtime.core.Param_Types.Module_Param_Subset_Template;
import org.eclipse.titan.runtime.core.Param_Types.Module_Param_Superset_Template;
import org.eclipse.titan.runtime.core.Param_Types.Module_Param_Ttcn_Null;
import org.eclipse.titan.runtime.core.Param_Types.Module_Param_Ttcn_mtc;
import org.eclipse.titan.runtime.core.Param_Types.Module_Param_Ttcn_system;
import org.eclipse.titan.runtime.core.Param_Types.Module_Param_Universal_Charstring;
import org.eclipse.titan.runtime.core.Param_Types.Module_Param_Value_List;
import org.eclipse.titan.runtime.core.Param_Types.Module_Param_Verdict;
import org.eclipse.titan.runtime.core.Param_Types.Module_Parameter;
import org.eclipse.titan.runtime.core.Param_Types.Module_Parameter.expression_operand_t;
import org.eclipse.titan.runtime.core.Param_Types.Module_Parameter.operation_type_t;
import org.eclipse.titan.runtime.core.TTCN_Logger;
import org.eclipse.titan.runtime.core.TTCN_Logger.component_id_t;
import org.eclipse.titan.runtime.core.TTCN_Logger.component_id_selector_enum;
import org.eclipse.titan.runtime.core.TTCN_Logger.disk_full_action_type_t;
import org.eclipse.titan.runtime.core.TTCN_Logger.emergency_logging_behaviour_t;
import org.eclipse.titan.runtime.core.TTCN_Logger.log_event_types_t;
import org.eclipse.titan.runtime.core.TTCN_Logger.Logging_Bits;
import org.eclipse.titan.runtime.core.TTCN_Logger.matching_verbosity_t;
import org.eclipse.titan.runtime.core.TTCN_Logger.Severity;
import org.eclipse.titan.runtime.core.TTCN_Logger.source_info_format_t;
import org.eclipse.titan.runtime.core.TTCN_Logger.timestamp_format_t;
import org.eclipse.titan.runtime.core.TitanCharString;
import org.eclipse.titan.runtime.core.TitanComponent;
import org.eclipse.titan.runtime.core.TitanInteger;
import org.eclipse.titan.runtime.core.TitanPort;
import org.eclipse.titan.runtime.core.TitanUniversalChar;
import org.eclipse.titan.runtime.core.TitanUniversalCharString;
import org.eclipse.titan.runtime.core.TitanVerdictType;
import org.eclipse.titan.runtime.core.TitanVerdictType.VerdictTypeEnum;
import org.eclipse.titan.runtime.core.TtcnError;
import org.eclipse.titan.runtime.core.cfgparser.ExecuteSectionHandler.ExecuteItem;

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

	private int mLine = 1;
	private int mOffset = 0;

	private ComponentSectionHandler componentSectionHandler = new ComponentSectionHandler();
	private GroupSectionHandler groupSectionHandler = new GroupSectionHandler();
	private MCSectionHandler mcSectionHandler = new MCSectionHandler();
	private ExternalCommandSectionHandler externalCommandsSectionHandler = new ExternalCommandSectionHandler();
	private ExecuteSectionHandler executeSectionHandler = new ExecuteSectionHandler();

	private void reportWarning(TITANMarker marker){
		//TODO: implement
	}

	public void setActualFile(File file) {
		mActualFile = file;
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
	private TITANMarker createMarker( final String aMessage, final Token aStartToken, final Token aEndToken, final int aSeverity, final int aPriority ) {
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
	private void reportError( final String aMessage, final Token aStartToken, final Token aEndToken ) {
		TITANMarker marker = createError( aMessage, aStartToken, aEndToken );
		//TODO: implement
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
	private TITANMarker createError( final String aMessage, final Token aStartToken, final Token aEndToken ) {
		final TITANMarker marker = createMarker( aMessage, aStartToken, aEndToken, SEVERITY_ERROR, PRIORITY_NORMAL );
		return marker;
	}

	/**
	 * Converts timestamp format string to timestamp format enum
	 * @param timestamp_str timestamp format string
	 * @return timestamp format enum
	 */
	private timestamp_format_t to_timestamp_format(final String timestamp_str) {
		if (null == timestamp_str) {
			return null;
		}
		if ("time".equalsIgnoreCase(timestamp_str)) {
			return timestamp_format_t.TIMESTAMP_TIME;
		} else if ("datetime".equalsIgnoreCase(timestamp_str)) {
			return timestamp_format_t.TIMESTAMP_DATETIME;
		} else if ("seconds".equalsIgnoreCase(timestamp_str)) {
			return timestamp_format_t.TIMESTAMP_SECONDS;
		}
		return null;
	}

	/**
	 * Sets a module parameter
	 * @param new module parameter
	 */
	private void set_param(Module_Parameter param) {
		Module_List.set_param(param);
	}

	/**
	 * Logs error during the process
	 * @param errorMsg error message
	 */
	private void config_process_error(final String errorMsg)	{
		//TODO: implement
	}
}

pr_ConfigFile:
	pr_Section+
	EOF
;

pr_Section:
(	pr_MainControllerSection
|	pr_IncludeSection
|	pr_OrderedIncludeSection
|	pr_ExecuteSection
|	pr_DefineSection
|	pr_ExternalCommandsSection
|	pr_TestportParametersSection
|	pr_GroupsSection
|	pr_ModuleParametersSection
|	pr_ComponentsSection
|	pr_LoggingSection
|	pr_ProfilerSection
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
			mcSectionHandler.setKillTimer( $k.number );
		}
	}
;

pr_MainControllerItemLocalAddress:
	LOCALADDRESS
	ASSIGNMENTCHAR
	l = pr_HostName
	SEMICOLON?
	{	mcSectionHandler.setLocalAddress( $l.text );
	}
;

pr_MainControllerItemNumHcs:
	NUMHCS
	ASSIGNMENTCHAR
	n = pr_IntegerValueExpression
	SEMICOLON?
	{	if ( $n.integer != null ) {
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
			mcSectionHandler.setTcpPort( $t.integer );
		}
	}
;

pr_IncludeSection:
	INCLUDE_SECTION
	(	INCLUDE_FILENAME
	)*
;

pr_OrderedIncludeSection:
	ORDERED_INCLUDE_SECTION
	(	ORDERED_INCLUDE_FILENAME
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
	{	executeSectionHandler.getExecuteitems().add( item );
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
	(	pr_MacroAssignment
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

pr_TestportParameter
@init {
}:
	comp = pr_ComponentID
	DOT
	pn = pr_TestportName	{	final String portName = "*".equals($pn.text) ? null : $pn.text;	}
	DOT
	paramname = pr_Identifier
	ASSIGNMENTCHAR
	value = pr_StringValue
{	TitanPort.add_parameter( $comp.comp, portName, $paramname.identifier, $value.string );
}
;

pr_GroupsSection:
	GROUPS_SECTION
	(	pr_GroupItem SEMICOLON?
	)*
;

pr_ModuleParametersSection:
	MODULE_PARAMETERS_SECTION
	(	pr_ModuleParam
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
		{	//TODO: error, CFG file cannot contain macros after preparsing
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
		{	//TODO: error, CFG file cannot contain macros after preparsing
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
	component_id_t comp = new component_id_t();
}:
	(	cn = pr_ComponentID DOT
		{	componentName = $cn.text;
			comp = $cn.comp;
		}
	)?
	LOGGERPLUGINS
	ASSIGNMENTCHAR
	BEGINCHAR
	pr_LoggerPluginsList[comp]
	ENDCHAR
;

pr_LoggerPluginsList [component_id_t comp]:
	pr_LoggerPluginEntry[$comp]
	(	COMMA
		pr_LoggerPluginEntry[$comp]
	)*
;

pr_LoggerPluginEntry [component_id_t comp]
@init {
	String pluginFilename = "";
}:
	i = pr_Identifier
	(	ASSIGNMENTCHAR
		s = pr_StringValue	{	pluginFilename = $s.string;	}
	)?
	{
//TODO: implement
//		TTCN_Logger.register_plugin( $comp, $i.identifier, pluginFilename );

//		final logging_setting_t logging_setting = new logging_setting_t();
//		logging_setting.component = $comp;
//		logging_setting.pluginId = $i.identifier;
//		final logging_param_t logging_param = new logging_param_t();
//		logging_param.log_param_selection = logging_param_type.LP_UNKNOWN;
//		logging_setting.logparam = logging_param;
//		TTCN_Logger.add_parameter(logging_setting);
	}
;

pr_PlainLoggingParam
@init {
	String componentName = "*";
	String pluginName = "*";
	component_id_t comp = new component_id_t();
}:
(	cn = pr_ComponentID DOT
		{	componentName = $cn.text;
			comp = $cn.comp;
		}
)?
(	STAR DOT
|	pn = pr_Identifier DOT { pluginName = $pn.text; }
)?
(	FILEMASK ASSIGNMENTCHAR fileMask = pr_LoggingBitMask
		{	TTCN_Logger.set_file_mask(comp, $fileMask.loggingBitMask);
		}
|	CONSOLEMASK ASSIGNMENTCHAR consoleMask = pr_LoggingBitMask
		{	TTCN_Logger.set_console_mask(comp, $consoleMask.loggingBitMask);
		}
|	DISKFULLACTION ASSIGNMENTCHAR pr_DiskFullActionValue
|	LOGFILENUMBER ASSIGNMENTCHAR lfn = pr_NaturalNumber
		{	TTCN_Logger.set_file_number( $lfn.integer.getIntegerValue() );
		}
|	LOGFILESIZE ASSIGNMENTCHAR lfs = pr_NaturalNumber
		{	TTCN_Logger.set_file_size( $lfs.integer.getIntegerValue() );
		}
|	LOGFILENAME ASSIGNMENTCHAR f = pr_LogfileName
		{	TTCN_Logger.set_file_name( $f.string, true );
		}
|	TIMESTAMPFORMAT ASSIGNMENTCHAR ttv = pr_TimeStampValue
		{	TTCN_Logger.set_timestamp_format( to_timestamp_format( $ttv.text ) );
		}
|	CONSOLETIMESTAMPFORMAT ASSIGNMENTCHAR ttv = pr_TimeStampValue
		{	//TODO: add TTCN_Logger.set_console_timestamp_format(timestamp_format_t)
			//TTCN_Logger.set_console_timestamp_format( to_timestamp_format( $ttv.text ) );
		}
|	SOURCEINFOFORMAT ASSIGNMENTCHAR
	(	pr_SourceInfoValue
	|	b = pr_YesNoOrBoolean
		{	TTCN_Logger.set_source_info_format( $b.bool ? source_info_format_t.SINFO_SINGLE : source_info_format_t.SINFO_NONE);
		}
	)
|	APPENDFILE ASSIGNMENTCHAR af = pr_YesNoOrBoolean
	{	TTCN_Logger.set_append_file( $af.bool );
	}
|	LOGEVENTTYPES ASSIGNMENTCHAR pr_LogEventTypesValue
|	LOGENTITYNAME ASSIGNMENTCHAR len = pr_YesNoOrBoolean
	{	//TODO: change to TTCN_Logger.set_log_entity_name(boolean)
		//TTCN_Logger.set_log_entity_name( $len.bool );
	}
|	MATCHINGHINTS ASSIGNMENTCHAR pr_MatchingHintsValue
|	o1 = pr_PluginSpecificParamName ASSIGNMENTCHAR o2 = pr_StringValue
	{	final logging_setting_t logging_setting = new logging_setting_t();
		logging_setting.component = comp;
		logging_setting.pluginId = pluginName;
		final logging_param_t logging_param = new logging_param_t();
		logging_param.log_param_selection = logging_param_type.LP_PLUGIN_SPECIFIC;
		logging_param.param_name = $o1.text;
		logging_param.str_val = $o2.string;
		logging_setting.logparam = logging_param;
		TTCN_Logger.add_parameter(logging_setting);
	}
|	EMERGENCYLOGGING ASSIGNMENTCHAR el = pr_NaturalNumber
	{	TTCN_Logger.set_emergency_logging( $el.integer.getIntegerValue() );
	}
|	EMERGENCYLOGGINGBEHAVIOUR ASSIGNMENTCHAR pr_BufferAllOrMasked
|	EMERGENCYLOGGINGMASK ASSIGNMENTCHAR elm = pr_LoggingBitMask
	{	TTCN_Logger.set_emergency_logging_mask(comp, $elm.loggingBitMask);
	}
)
;

pr_TimeStampValue:
	TIMESTAMPVALUE
;

pr_SourceInfoValue:
	SOURCEINFOVALUE_NONE	{	TTCN_Logger.set_source_info_format(source_info_format_t.SINFO_NONE);	}
|	SOURCEINFOVALUE_SINGLE	{	TTCN_Logger.set_source_info_format(source_info_format_t.SINFO_SINGLE);	}
|	SOURCEINFOVALUE_STACK	{	TTCN_Logger.set_source_info_format(source_info_format_t.SINFO_STACK);	}
;

pr_PluginSpecificParamName:
	TTCN3IDENTIFIER
;

pr_BufferAllOrMasked:
	BUFFERALL		{	TTCN_Logger.set_emergency_logging_behaviour(emergency_logging_behaviour_t.BUFFER_ALL);	}
|	BUFFERMASKED	{	TTCN_Logger.set_emergency_logging_behaviour(emergency_logging_behaviour_t.BUFFER_MASKED);	}
;

pr_DiskFullActionValue:
(	DISKFULLACTIONVALUE_ERROR	{	TTCN_Logger.set_disk_full_action(disk_full_action_type_t.DISKFULL_ERROR);	}
|	DISKFULLACTIONVALUE_STOP	{	TTCN_Logger.set_disk_full_action(disk_full_action_type_t.DISKFULL_STOP);	}
|	DISKFULLACTIONVALUE_DELETE	{	TTCN_Logger.set_disk_full_action(disk_full_action_type_t.DISKFULL_DELETE);	}
|	DISKFULLACTIONVALUE_RETRY
	{	int retry_interval = 30;	// default retry interval
	}
	(	LPAREN
		n = NATURAL_NUMBER
			{	try {
					retry_interval = Integer.parseInt( $n.text );
				} catch ( Exception e ) {
					// do nothing
				}
			}
		RPAREN
	)?
	{	TTCN_Logger.set_disk_full_action(disk_full_action_type_t.DISKFULL_RETRY, retry_interval);	}
)
;

pr_ComponentID returns [component_id_t comp]
@init {
	$comp = new component_id_t();
}:
(	i = pr_Identifier
		{	$comp.id_selector = component_id_selector_enum.COMPONENT_ID_NAME;
			$comp.id_name = $i.text;
		}
|	n = pr_NaturalNumber
		{	$comp.id_selector = component_id_selector_enum.COMPONENT_ID_COMPREF;
			$comp.id_compref = $n.integer.getIntegerValue();
		}
|	MTCKEYWORD
		{	$comp.id_selector = component_id_selector_enum.COMPONENT_ID_COMPREF;
			$comp.id_compref = TitanComponent.MTC_COMPREF;
		}
|	STAR
		{	$comp.id_selector = component_id_selector_enum.COMPONENT_ID_ALL;
			$comp.id_name = null;
		}
|	SYSTEMKEYWORD
		{	$comp.id_selector = component_id_selector_enum.COMPONENT_ID_SYSTEM;
			$comp.id_name = null;
		}
)
;

pr_LoggingBitMask returns [ Logging_Bits loggingBitMask ]
@init {
	$loggingBitMask = new Logging_Bits();
}:
	pr_LoggingMaskElement [ $loggingBitMask ]
	(	LOGICALOR	pr_LoggingMaskElement [ $loggingBitMask ]
	)*
;

pr_LoggingMaskElement [ Logging_Bits loggingBitMask ]:
	pr_LogEventType [ $loggingBitMask ]
|	pr_LogEventTypeSet [ $loggingBitMask ]
|	pr_deprecatedEventTypeSet [ $loggingBitMask ]
;

pr_LogfileName returns [String string]:
	s = pr_StringValue { $string = $s.string; }
;

pr_YesNoOrBoolean returns [Boolean bool]:
	YESNO { $bool = "yes".equalsIgnoreCase( $YESNO.text ); }
|	b = pr_Boolean { $bool = $b.bool; }
;

pr_LogEventTypesValue:
	b = pr_YesNoOrBoolean	{	TTCN_Logger.set_log_event_types( $b.bool ?
									log_event_types_t.LOGEVENTTYPES_YES : log_event_types_t.LOGEVENTTYPES_NO );	}
|	pr_Detailed				{	TTCN_Logger.set_log_event_types( log_event_types_t.LOGEVENTTYPES_SUBCATEGORIES );	}
;

pr_MatchingHintsValue:
	COMPACT		{	TTCN_Logger.set_matching_verbosity( matching_verbosity_t.VERBOSITY_COMPACT );	}
|	DETAILED	{	TTCN_Logger.set_matching_verbosity( matching_verbosity_t.VERBOSITY_FULL );	}
;

pr_LogEventType [ Logging_Bits loggingBitMask ]:
(  a1 = ACTION_UNQUALIFIED		{ loggingBitMask.add(Severity.ACTION_UNQUALIFIED); }
|  a2 = DEBUG_ENCDEC			{ loggingBitMask.add(Severity.DEBUG_ENCDEC); }
|  a3 = DEBUG_TESTPORT			{ loggingBitMask.add(Severity.DEBUG_TESTPORT); }
|  a4 = DEBUG_UNQUALIFIED		{ loggingBitMask.add(Severity.DEBUG_UNQUALIFIED); }
|  a5 = DEFAULTOP_ACTIVATE		{ loggingBitMask.add(Severity.DEFAULTOP_ACTIVATE); }
|  a6 = DEFAULTOP_DEACTIVATE	{ loggingBitMask.add(Severity.DEFAULTOP_DEACTIVATE); }
|  a7 = DEFAULTOP_EXIT			{ loggingBitMask.add(Severity.DEFAULTOP_EXIT); }
|  a8 = DEFAULTOP_UNQUALIFIED	{ loggingBitMask.add(Severity.DEFAULTOP_UNQUALIFIED); }
|  a9 = ERROR_UNQUALIFIED		{ loggingBitMask.add(Severity.ERROR_UNQUALIFIED); }
|  a10 = EXECUTOR_COMPONENT		{ loggingBitMask.add(Severity.EXECUTOR_COMPONENT); }
|  a11 = EXECUTOR_CONFIGDATA	{ loggingBitMask.add(Severity.EXECUTOR_CONFIGDATA); }
|  a12 = EXECUTOR_EXTCOMMAND	{ loggingBitMask.add(Severity.EXECUTOR_EXTCOMMAND); }
|  a13 = EXECUTOR_LOGOPTIONS	{ loggingBitMask.add(Severity.EXECUTOR_LOGOPTIONS); }
|  a14 = EXECUTOR_RUNTIME		{ loggingBitMask.add(Severity.EXECUTOR_RUNTIME); }
|  a15 = EXECUTOR_UNQUALIFIED	{ loggingBitMask.add(Severity.EXECUTOR_UNQUALIFIED); }
|  a16 = FUNCTION_RND			{ loggingBitMask.add(Severity.FUNCTION_RND); }
|  a17 = FUNCTION_UNQUALIFIED	{ loggingBitMask.add(Severity.FUNCTION_UNQUALIFIED); }
|  a18 = MATCHING_DONE			{ loggingBitMask.add(Severity.MATCHING_DONE); }
|  a19 = MATCHING_MCSUCCESS		{ loggingBitMask.add(Severity.MATCHING_MCSUCCESS); }
|  a20 = MATCHING_MCUNSUCC		{ loggingBitMask.add(Severity.MATCHING_MCUNSUCC); }
|  a21 = MATCHING_MMSUCCESS		{ loggingBitMask.add(Severity.MATCHING_MMSUCCESS); }
|  a22 = MATCHING_MMUNSUCC		{ loggingBitMask.add(Severity.MATCHING_MMUNSUCC); }
|  a23 = MATCHING_PCSUCCESS		{ loggingBitMask.add(Severity.MATCHING_PCSUCCESS); }
|  a24 = MATCHING_PCUNSUCC		{ loggingBitMask.add(Severity.MATCHING_PCUNSUCC); }
|  a25 = MATCHING_PMSUCCESS		{ loggingBitMask.add(Severity.MATCHING_PMSUCCESS); }
|  a26 = MATCHING_PMUNSUCC		{ loggingBitMask.add(Severity.MATCHING_PMUNSUCC); }
|  a27 = MATCHING_PROBLEM		{ loggingBitMask.add(Severity.MATCHING_PROBLEM); }
|  a28 = MATCHING_TIMEOUT		{ loggingBitMask.add(Severity.MATCHING_TIMEOUT); }
|  a29 = MATCHING_UNQUALIFIED	{ loggingBitMask.add(Severity.MATCHING_UNQUALIFIED); }
|  a30 = PARALLEL_PORTCONN		{ loggingBitMask.add(Severity.PARALLEL_PORTCONN); }
|  a31 = PARALLEL_PORTMAP		{ loggingBitMask.add(Severity.PARALLEL_PORTMAP); }
|  a32 = PARALLEL_PTC			{ loggingBitMask.add(Severity.PARALLEL_PTC); }
|  a33 = PARALLEL_UNQUALIFIED	{ loggingBitMask.add(Severity.PARALLEL_UNQUALIFIED); }
|  a34 = PORTEVENT_DUALRECV		{ loggingBitMask.add(Severity.PORTEVENT_DUALRECV); }
|  a35 = PORTEVENT_DUALSEND		{ loggingBitMask.add(Severity.PORTEVENT_DUALSEND); }
|  a36 = PORTEVENT_MCRECV		{ loggingBitMask.add(Severity.PORTEVENT_MCRECV); }
|  a37 = PORTEVENT_MCSEND		{ loggingBitMask.add(Severity.PORTEVENT_MCSEND); }
|  a38 = PORTEVENT_MMRECV		{ loggingBitMask.add(Severity.PORTEVENT_MMRECV); }
|  a39 = PORTEVENT_MMSEND		{ loggingBitMask.add(Severity.PORTEVENT_MMSEND); }
|  a40 = PORTEVENT_MQUEUE		{ loggingBitMask.add(Severity.PORTEVENT_MQUEUE); }
|  a41 = PORTEVENT_PCIN			{ loggingBitMask.add(Severity.PORTEVENT_PCIN); }
|  a42 = PORTEVENT_PCOUT		{ loggingBitMask.add(Severity.PORTEVENT_PCOUT); }
|  a43 = PORTEVENT_PMIN			{ loggingBitMask.add(Severity.PORTEVENT_PMIN); }
|  a44 = PORTEVENT_PMOUT		{ loggingBitMask.add(Severity.PORTEVENT_PMOUT); }
|  a45 = PORTEVENT_PQUEUE		{ loggingBitMask.add(Severity.PORTEVENT_PQUEUE); }
|  a46 = PORTEVENT_STATE		{ loggingBitMask.add(Severity.PORTEVENT_STATE); }
|  a47 = PORTEVENT_UNQUALIFIED	{ loggingBitMask.add(Severity.PORTEVENT_UNQUALIFIED); }
|  a48 = STATISTICS_UNQUALIFIED	{ loggingBitMask.add(Severity.STATISTICS_UNQUALIFIED); }
|  a49 = STATISTICS_VERDICT		{ loggingBitMask.add(Severity.STATISTICS_VERDICT); }
|  a50 = TESTCASE_FINISH		{ loggingBitMask.add(Severity.TESTCASE_FINISH); }
|  a51 = TESTCASE_START			{ loggingBitMask.add(Severity.TESTCASE_START); }
|  a52 = TESTCASE_UNQUALIFIED	{ loggingBitMask.add(Severity.TESTCASE_UNQUALIFIED); }
|  a53 = TIMEROP_GUARD			{ loggingBitMask.add(Severity.TIMEROP_GUARD); }
|  a54 = TIMEROP_READ			{ loggingBitMask.add(Severity.TIMEROP_READ); }
|  a55 = TIMEROP_START			{ loggingBitMask.add(Severity.TIMEROP_START); }
|  a56 = TIMEROP_STOP			{ loggingBitMask.add(Severity.TIMEROP_STOP); }
|  a57 = TIMEROP_TIMEOUT		{ loggingBitMask.add(Severity.TIMEROP_TIMEOUT); }
|  a58 = TIMEROP_UNQUALIFIED	{ loggingBitMask.add(Severity.TIMEROP_UNQUALIFIED); }
|  a59 = USER_UNQUALIFIED		{ loggingBitMask.add(Severity.USER_UNQUALIFIED); }
|  a60 = VERDICTOP_FINAL		{ loggingBitMask.add(Severity.VERDICTOP_FINAL); }
|  a61 = VERDICTOP_GETVERDICT	{ loggingBitMask.add(Severity.VERDICTOP_GETVERDICT); }
|  a62 = VERDICTOP_SETVERDICT	{ loggingBitMask.add(Severity.VERDICTOP_SETVERDICT); }
|  a63 = VERDICTOP_UNQUALIFIED	{ loggingBitMask.add(Severity.VERDICTOP_UNQUALIFIED); }
|  a64 = WARNING_UNQUALIFIED	{ loggingBitMask.add(Severity.WARNING_UNQUALIFIED); }
)
;

pr_LogEventTypeSet [ Logging_Bits loggingBitMask ]:
(  a1 = TTCN_EXECUTOR2		{ loggingBitMask.add(Severity.EXECUTOR_UNQUALIFIED); }
|  a2 = TTCN_ERROR2			{ loggingBitMask.add(Severity.ERROR_UNQUALIFIED); }
|  a3 = TTCN_WARNING2		{ loggingBitMask.add(Severity.WARNING_UNQUALIFIED); }
|  a4 = TTCN_PORTEVENT2		{ loggingBitMask.add(Severity.PORTEVENT_UNQUALIFIED); }
|  a5 = TTCN_TIMEROP2		{ loggingBitMask.add(Severity.TIMEROP_UNQUALIFIED); }
|  a6 = TTCN_VERDICTOP2		{ loggingBitMask.add(Severity.VERDICTOP_UNQUALIFIED); }
|  a7 = TTCN_DEFAULTOP2		{ loggingBitMask.add(Severity.DEFAULTOP_UNQUALIFIED); }
|  a8 = TTCN_ACTION2		{ loggingBitMask.add(Severity.ACTION_UNQUALIFIED); }
|  a9 = TTCN_TESTCASE2		{ loggingBitMask.add(Severity.TESTCASE_UNQUALIFIED); }
|  a10 = TTCN_FUNCTION2		{ loggingBitMask.add(Severity.FUNCTION_UNQUALIFIED); }
|  a11 = TTCN_USER2			{ loggingBitMask.add(Severity.USER_UNQUALIFIED); }
|  a12 = TTCN_STATISTICS2	{ loggingBitMask.add(Severity.STATISTICS_UNQUALIFIED); }
|  a13 = TTCN_PARALLEL2		{ loggingBitMask.add(Severity.PARALLEL_UNQUALIFIED); }
|  a14 = TTCN_MATCHING2		{ loggingBitMask.add(Severity.MATCHING_UNQUALIFIED); }
|  a15 = TTCN_DEBUG2		{ loggingBitMask.add(Severity.DEBUG_UNQUALIFIED); }
|  a16 = LOG_ALL			{ loggingBitMask.addBitmask(Logging_Bits.log_all); }
|  a17 = LOG_NOTHING		{ loggingBitMask.addBitmask(Logging_Bits.log_nothing); }
)
;

pr_deprecatedEventTypeSet [ Logging_Bits loggingBitMask ]:
(  a1 = TTCN_EXECUTOR1		{ loggingBitMask.add(Severity.EXECUTOR_UNQUALIFIED); }
|  a2 = TTCN_ERROR1			{ loggingBitMask.add(Severity.ERROR_UNQUALIFIED); }
|  a3 = TTCN_WARNING1		{ loggingBitMask.add(Severity.WARNING_UNQUALIFIED); }
|  a4 = TTCN_PORTEVENT1		{ loggingBitMask.add(Severity.PORTEVENT_UNQUALIFIED); }
|  a5 = TTCN_TIMEROP1		{ loggingBitMask.add(Severity.TIMEROP_UNQUALIFIED); }
|  a6 = TTCN_VERDICTOP1		{ loggingBitMask.add(Severity.VERDICTOP_UNQUALIFIED); }
|  a7 = TTCN_DEFAULTOP1		{ loggingBitMask.add(Severity.DEFAULTOP_UNQUALIFIED); }
|  a8 = TTCN_ACTION1		{ loggingBitMask.add(Severity.ACTION_UNQUALIFIED); }
|  a9 = TTCN_TESTCASE1		{ loggingBitMask.add(Severity.TESTCASE_UNQUALIFIED); }
|  a10 = TTCN_FUNCTION1		{ loggingBitMask.add(Severity.FUNCTION_UNQUALIFIED); }
|  a11 = TTCN_USER1			{ loggingBitMask.add(Severity.USER_UNQUALIFIED); }
|  a12 = TTCN_STATISTICS1	{ loggingBitMask.add(Severity.STATISTICS_UNQUALIFIED); }
|  a13 = TTCN_PARALLEL1		{ loggingBitMask.add(Severity.PARALLEL_UNQUALIFIED); }
|  a14 = TTCN_MATCHING1		{ loggingBitMask.add(Severity.MATCHING_UNQUALIFIED); }
|  a15 = TTCN_DEBUG1		{ loggingBitMask.add(Severity.DEBUG_UNQUALIFIED); }
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
	(	h = pr_HostName {	component.setHostName( $h.text );
						}
	|	i = pr_HostNameIpV6	{	component.setHostName( $i.text );
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
|	MACRO_HOSTNAME
	{	// runtime cfg parser should have resolved the macros already, so raise error
		config_process_error("Macro is not resolved");
	}
|	macro2 = MACRO
	{	// runtime cfg parser should have resolved the macros already, so raise error
		config_process_error("Macro is not resolved");
	}
)
;

pr_HostNameIpV6:
	IPV6
;

pr_MacroAssignment:
(	TTCN3IDENTIFIER
	ASSIGNMENTCHAR
	pr_DefinitionRValue
)
;

pr_DefinitionRValue:
(	pr_SimpleValue+
|	pr_StructuredValue
)
;

pr_SimpleValue:
(	TTCN3IDENTIFIER
|	MACRORVALUE
|	(	MACRO_ID
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
	)
	{	// runtime cfg parser should have resolved the macros already, so raise error
		config_process_error("Macro is not resolved");
	}
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

pr_TestportName:
(	pr_Identifier
	(	SQUAREOPEN pr_IntegerValueExpression SQUARECLOSE
		//TODO: it can be changed to pr_IndexItemIndex, also in config_process.y
	)*
|	STAR
)
;

pr_Identifier returns [String identifier]:
(	macro = MACRO_ID
	{	// runtime cfg parser should have resolved the macros already, so raise error
		config_process_error("Macro is not resolved");
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
|	macro = pr_MacroNaturalNumber
|	TTCN3IDENTIFIER // module parameter name
		{	$integer = new CFGNumber( "1" ); // value is unknown yet, but it should not be null
		}//TODO: incorrect behaviour
)
;

pr_MPNaturalNumber returns [TitanInteger integer]:
(	a = NATURAL_NUMBER	{$integer = new TitanInteger($a.text);}
|	pr_MacroNaturalNumber
)
;

pr_MacroNaturalNumber:
(	MACRO_INT
|	MACRO
)
{	// runtime cfg parser should have resolved the macros already, so raise error
	config_process_error("Macro is not resolved");
}
;

pr_StringValue returns [String string]
@init {
	$string = "";
}:
	a = pr_CString
		{	if ( $a.string != null ) {
				$string = $a.string;
			}
		}
	(	STRINGOP
		b = pr_CString
			{	if ( $b.string != null ) {
					$string = $string + $b.string;
				}
			}
	)*
;

pr_CString returns [String string]:
(	cs = STRING
		{
			final CharstringExtractor cse = new CharstringExtractor( $cs.text );
			$string = cse.getExtractedString();
			if ( cse.isErroneous() ) {
				config_process_error( cse.getErrorMessage() );
			}
		}
|	pr_MacroCString
|	pr_MacroExpliciteCString
|	TTCN3IDENTIFIER // module parameter name
		{	$string = ""; // value is unknown yet, but it should not be null
		}
)
;

pr_MPCString returns [String string]:
(	cs = STRING
		{
			final CharstringExtractor cse = new CharstringExtractor( $cs.text );
			$string = cse.getExtractedString();
			if ( cse.isErroneous() ) {
				config_process_error( cse.getErrorMessage() );
			}
		}
|	(	pr_MacroCString
	|	pr_MacroExpliciteCString
	)
	{	// runtime cfg parser should have resolved the macros already, so raise error
		config_process_error("Macro is not resolved");
	}
)
;

pr_MacroCString:
	MACRO
	{	// runtime cfg parser should have resolved the macros already, so raise error
		config_process_error("Macro is not resolved");
	}
;

pr_MacroExpliciteCString:
	MACRO_EXP_CSTR
	{	// runtime cfg parser should have resolved the macros already, so raise error
		config_process_error("Macro is not resolved");
	}
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
{	group.setGroupName( $a.text );
	groupSectionHandler.getGroups().add( group );
}
;

pr_DNSName:
(	NATURAL_NUMBER
|	FLOAT
|	DNSNAME
)
;

pr_ModuleParam
@init {
	operation_type_t operation;
}:
	name = pr_ParameterName
	(	ASSIGNMENTCHAR	{	operation = operation_type_t.OT_ASSIGN;	}
	|	CONCATCHAR	{	operation = operation_type_t.OT_CONCAT;	}
	)
	param = pr_ParameterValue
		{	final Module_Parameter mp = $param.moduleparameter;
			if (mp != null) {
				mp.set_id(new Module_Param_Name($name.names));
				mp.set_operation_type(operation);
				set_param(mp);
			}
		}
;

pr_ParameterName returns [List<String> names]
@init {
	$names = new ArrayList<String>();
}:
(	id1 = pr_ParameterNamePart	{	$names.add($id1.text);	}
	(	separator = pr_Dot
		id2 = pr_ParameterNameTail[$names]
	|
	)
|	star = pr_StarModuleName
	DOT
	id3 = pr_ParameterNamePart	{	$names.add($id3.text);	}
)
;

// One part of the parameter name which are separated by dots
pr_ParameterNamePart:
	pr_Identifier
	pr_IndexItemIndex*
;

// rest of the parameter name after the first dot
// this is handled as parameter (2nd column) in the cfg editor on module parameters tab
pr_ParameterNameTail [List<String> names]:
	n = pr_ParameterNamePart	{	$names.add($n.text);	}
	(	pr_Dot
		n = pr_ParameterNamePart	{	$names.add($n.text);	}
	)*
;

pr_Dot:
	DOT
;

pr_StarModuleName:
	STAR
;

pr_ParameterValue returns [Module_Parameter moduleparameter]:
	pe = pr_ParameterExpression	{	$moduleparameter = $pe.moduleparameter;	}
	(	lm = pr_LengthMatch	{	$moduleparameter.set_length_restriction($lm.length_restriction);	}
	)?
	(	IFPRESENTKEYWORD	{	$moduleparameter.set_ifpresent();	}
	)?
;

//module parameter expression, it can contain previously defined module parameters
pr_ParameterExpression returns [Module_Parameter moduleparameter]:
	pe = pr_MPAddExpression	{	$moduleparameter = $pe.moduleparameter;	}
;

pr_MPAddExpression returns[Module_Parameter moduleparameter]:
(	pe1 = pr_MPMulExpression { $moduleparameter = $pe1.moduleparameter; }
	(	{	expression_operand_t operand;	}
		(	PLUS	{	operand = expression_operand_t.EXPR_ADD;	}
		|	MINUS	{	operand = expression_operand_t.EXPR_SUBTRACT;	}
		|	STRINGOP	{	operand = expression_operand_t.EXPR_CONCATENATE;	}
		)
		pe2 = pr_MPMulExpression
		{	$moduleparameter = new Module_Param_Expression(operand, $moduleparameter, $pe2.moduleparameter);
		}
	)*
);

pr_MPMulExpression returns[Module_Parameter moduleparameter]:
(	pe1 = pr_MPUnaryExpression { $moduleparameter = $pe1.moduleparameter; }
	(	{	expression_operand_t operand;	}
		(	STAR	{	operand = expression_operand_t.EXPR_MULTIPLY;	}
		|	SLASH	{	operand = expression_operand_t.EXPR_DIVIDE;	}
		)
		pe2 = pr_MPUnaryExpression
		{	$moduleparameter = new Module_Param_Expression(operand, $moduleparameter, $pe2.moduleparameter);
		}
	)*
);

pr_MPUnaryExpression returns [Module_Parameter moduleparameter]:
(	PLUS
	ue = pr_MPUnaryExpression	{	$moduleparameter = $ue.moduleparameter;	}
|	MINUS
	ue = pr_MPUnaryExpression	{	$moduleparameter = new Module_Param_Expression($ue.moduleparameter);	}
|	LPAREN
	pe = pr_ParameterExpression
	RPAREN
	{	$moduleparameter = $pe.moduleparameter;	}
|	pv = pr_MPPrimaryValue		{	$moduleparameter = $pv.moduleparameter;	}
);

pr_MPPrimaryValue returns [Module_Parameter moduleparameter]:
(	spv = pr_SimpleParameterValue	{	$moduleparameter = $spv.moduleparameter;	}
|	pr = pr_ParameterReference		{	$moduleparameter = $pr.moduleparameter;	}
);

pr_LengthMatch returns [Module_Param_Length_Restriction length_restriction]
@init {
	$length_restriction = new Module_Param_Length_Restriction();
}:
	LENGTHKEYWORD
	LPAREN
	(	single = pr_LengthBound	{	$length_restriction.set_single($single.integer.getIntegerValue());	}
	|	min = pr_LengthBound	{	$length_restriction.set_min($min.integer.getIntegerValue());	}
		DOTDOT
		(	max = pr_LengthBound
			{
				if ($min.integer.getIntegerValue() > $max.integer.getIntegerValue()) {
					config_process_error("invalid length restriction: lower bound > upper bound");
				}
				$length_restriction.set_max($max.integer.getIntegerValue());
			}
		|	INFINITYKEYWORD
		)
	)
	RPAREN
;

pr_LengthBound returns [CFGNumber integer]:
	i = pr_IntegerValueExpression	{	$integer = $i.integer;	}
;

pr_SimpleParameterValue returns [Module_Parameter moduleparameter]
@init {
	$moduleparameter = null;
}:
(	i = pr_MPNaturalNumber			{	$moduleparameter = new Module_Param_Integer($i.integer);	}
|	f = pr_MPFloat					{	$moduleparameter = new Module_Param_Float($f.floatnum);	}
|	bool = pr_Boolean				{	$moduleparameter = new Module_Param_Boolean($bool.bool);	}
|	oi = pr_ObjIdValue
	{	final List<TitanInteger> cs = $oi.components;
		final int size = cs.size();
		$moduleparameter = new Module_Param_Objid(size, cs.toArray(new TitanInteger[size]));
	}
|	verdict = pr_VerdictValue		{	$moduleparameter = new Module_Param_Verdict($verdict.verdict);	}
|	bstr = pr_BStringValue			{	$moduleparameter = new Module_Param_Bitstring($bstr.string);	}
|	hstr = pr_HStringValue			{	$moduleparameter = new Module_Param_Hexstring($hstr.string);	}
|	ostr = pr_OStringValue			{	$moduleparameter = new Module_Param_Octetstring($ostr.string);	}
|	cs = pr_MPCString				{	$moduleparameter = new Module_Param_Charstring(new TitanCharString($cs.string));	}
|	ucs = pr_Quadruple				{	$moduleparameter = new Module_Param_Universal_Charstring($ucs.ucstr);	}
|	OMITKEYWORD						{	$moduleparameter = new Module_Param_Omit();	}
|	nulltext = pr_NULLKeyword
	{	if ("null".equals($nulltext.text)) {
			$moduleparameter = new Module_Param_Ttcn_Null();
		} else {
			$moduleparameter = new Module_Param_Asn_Null();
		}
	}
|	MTCKEYWORD						{	$moduleparameter = new Module_Param_Ttcn_mtc();	}
|	SYSTEMKEYWORD					{	$moduleparameter = new Module_Param_Ttcn_system();	}
|	cv = pr_CompoundValue			{	$moduleparameter = $cv.moduleparameter;	}
|	ANYVALUE						{	$moduleparameter = new Module_Param_Any();	}
|	STAR							{	$moduleparameter = new Module_Param_AnyOrNone();	}
|	ir = pr_IntegerRange
	{	$moduleparameter = new Module_Param_IntRange(
			$ir.min != null ? new TitanInteger($ir.min.getIntegerValue()) : null,
			$ir.max != null ? new TitanInteger($ir.max.getIntegerValue()) : null,
			$ir.min_exclusive, $ir.max_exclusive );
	}
|	fr = pr_FloatRange
	{	$moduleparameter = new Module_Param_FloatRange(
			$fr.min != null ? $fr.min.getValue() : 0,
			$fr.min != null,
			$fr.max != null ? $fr.max.getValue() : 0,
			$fr.max != null,
			$fr.min_exclusive, $fr.max_exclusive );
	}
|	sr = pr_StringRange	{	$moduleparameter = $sr.stringrange;	}
|	PATTERNKEYWORD pcl = pr_PatternChunkList
	{
		//TODO: handle nocase
		boolean nocase = false;
		$moduleparameter = new Module_Param_Pattern($pcl.ucstr.to_utf(), nocase);
	}
|	bsm = pr_BStringMatch			{	$moduleparameter = new Module_Param_Bitstring_Template($bsm.string);	}
|	hsm = pr_HStringMatch			{	$moduleparameter = new Module_Param_Hexstring_Template($hsm.string);	}
|	osm = pr_OStringMatch			{	$moduleparameter = new Module_Param_Octetstring_Template($osm.string);	}
)
;

pr_ParameterReference returns [Module_Parameter moduleparameter]:
	// enumerated values are also treated as references by the parser,
	// these will be sorted out later during set_param()
	pns = pr_ParameterNameSegment
	{	// no references allowed in RT1, so the name segment must be an enumerated value
    	// (which means it can only contain 1 name)
    	if ($pns.names == null || $pns.names.size() != 1) {
			config_process_error("Module parameter references are not allowed in the Load Test Runtime.");
		}
		$moduleparameter = ($pns.names == null || $pns.names.size() == 0) ? null : new Module_Param_Enumerated($pns.names.get(0));
	}
;

pr_ParameterNameSegment returns [List<String> names]:
	pns = pr_ParameterNameSegment
	pr_Dot
	i = pr_Identifier
	{	$names = $pns.names;
		$names.add($i.identifier);
	}
|	pns = pr_ParameterNameSegment
	iii = pr_IndexItemIndex
	{	$names = $pns.names;
		int size = $names.size();
		final String last = $names.get(size - 1);
		$names.set(size - 1, last + $iii.text);
	}
|	i = pr_Identifier
	{	$names = new ArrayList<String>();
		$names.add($i.identifier);
	}
;

pr_IndexItemIndex returns [CFGNumber integer]:
	SQUAREOPEN
	i = pr_IntegerValueExpression	{	$integer = $i.integer;	}
	SQUARECLOSE
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
(	a = pr_Float	{$number = $a.floatnum;}
|	b = pr_NaturalNumber	{$number = $b.integer;}
|	LPAREN c = pr_ArithmeticAddExpression RPAREN {$number = $c.number;}
)
;

pr_Float returns [CFGNumber floatnum]:
(	a = FLOAT {$floatnum = new CFGNumber($a.text);}
|	MACRO_FLOAT
	{	// runtime cfg parser should have resolved the macros already, so raise error
		config_process_error("Macro is not resolved");
	}
|	TTCN3IDENTIFIER // module parameter name
		{	$floatnum = new CFGNumber( "1.0" ); // value is unknown yet, but it should not be null
		}//TODO: incorrect behaviour
)
;

pr_MPFloat returns [double floatnum]:
(	a = FLOAT {$floatnum = Double.parseDouble($a.text);}
|	NANKEYWORD	{	$floatnum = Double.NaN;	}
|	INFINITYKEYWORD	{	$floatnum = Double.POSITIVE_INFINITY;	}
|	MACRO_FLOAT
	{	// runtime cfg parser should have resolved the macros already, so raise error
		config_process_error("Macro is not resolved");
	}
)
;

pr_Boolean returns [Boolean bool]:
(	t = TRUE { $bool = true; }
|	f = FALSE { $bool = false; }
|	MACRO_BOOL
	{	// runtime cfg parser should have resolved the macros already, so raise error
		config_process_error("Macro is not resolved");
	}
)
;

pr_ObjIdValue returns[List<TitanInteger> components]
@init{
	$components = new ArrayList<TitanInteger>();
}:
	OBJIDKEYWORD
	BEGINCHAR
	(	c = pr_ObjIdComponent { $components.add($c.integer);}
	)+
	ENDCHAR
;

pr_ObjIdComponent returns [TitanInteger integer]:
(	n = pr_NaturalNumber	{	$integer = new TitanInteger($n.integer.getIntegerValue());	}
|	pr_Identifier LPAREN n = pr_NaturalNumber RPAREN	{	$integer = new TitanInteger($n.integer.getIntegerValue());	}
)
;

pr_VerdictValue returns [TitanVerdictType verdict]:
(	NONE_VERDICT	{	$verdict = new TitanVerdictType(VerdictTypeEnum.NONE);	}
|	PASS_VERDICT	{	$verdict = new TitanVerdictType(VerdictTypeEnum.PASS);	}
|	INCONC_VERDICT	{	$verdict = new TitanVerdictType(VerdictTypeEnum.INCONC);	}
|	FAIL_VERDICT	{	$verdict = new TitanVerdictType(VerdictTypeEnum.FAIL);	}
|	ERROR_VERDICT	{	$verdict = new TitanVerdictType(VerdictTypeEnum.ERROR);	}
)
;

pr_BStringValue returns [String string]:
	s = pr_BString	{	$string = $s.string;	}
	(	STRINGOP
		s = pr_BString	{	$string += $s.string;	}
	)*
;

pr_BString returns [String string]:
(	b = BITSTRING
	{	final String temp = $b.text;
		if ( temp != null ) {
			$string = temp.replaceAll("^\'|\'B$|\\s+", "");
		}
	}
|	MACRO_BSTR
	{	// runtime cfg parser should have resolved the macros already, so raise error
		config_process_error("Macro is not resolved");
	}
)
;

pr_HStringValue returns [String string]:
	s = pr_HString	{	$string = $s.string;	}
	(	STRINGOP
		s = pr_HString	{	$string += $s.string;	}
	)*
;

pr_HString returns [String string]:
(	h = HEXSTRING
	{	final String temp = $h.text;
		if ( temp != null ) {
			$string = temp.replaceAll("^\'|\'H$|\\s+", "");
		}
	}
|	MACRO_HSTR
	{	// runtime cfg parser should have resolved the macros already, so raise error
		config_process_error("Macro is not resolved");
	}
)
;

pr_OStringValue returns [String string]:
	s = pr_OString	{	$string = $s.string;	}
	(	STRINGOP
		s = pr_OString	{	$string += $s.string;	}
	)*
;

pr_OString returns [String string]:
(	o = OCTETSTRING
	{	final String temp = $o.text;
		if ( temp != null ) {
			$string = temp.replaceAll("^\'|\'O$|\\s+", "");
		}
	}
|	MACRO_OSTR
	{	// runtime cfg parser should have resolved the macros already, so raise error
		config_process_error("Macro is not resolved");
	}
|	MACRO_BINARY
	{	// runtime cfg parser should have resolved the macros already, so raise error
		config_process_error("Macro is not resolved");
	}
)
;

//returns TitanCharString or TitanUniversalCharString
pr_UniversalOrNotStringValue returns [Base_Type cstr]:
(	c = pr_CString	{ 	$cstr = new TitanCharString($c.string);	}
|	q = pr_Quadruple	{ 	$cstr = $q.ucstr;	}
)
(	STRINGOP
	(	c = pr_CString
		{	if ($cstr instanceof TitanCharString) {
				final TitanCharString cs = (TitanCharString)$cstr;
				$cstr = cs.operator_concatenate($c.string);
			} else {
				final TitanUniversalCharString ucs = (TitanUniversalCharString)$cstr;
				$cstr = ucs.operator_concatenate($c.string);
			}
		}
	|	q = pr_Quadruple
		{	if ($cstr instanceof TitanCharString) {
				final TitanCharString cs = (TitanCharString)$cstr;
				$cstr = cs.operator_concatenate($q.ucstr);
			} else {
				final TitanUniversalCharString ucs = (TitanUniversalCharString)$cstr;
				$cstr = ucs.operator_concatenate($q.ucstr);
			}
		}
	)
)*
;

pr_Quadruple returns [TitanUniversalCharString ucstr]:
	CHARKEYWORD
	LPAREN
	i1 = pr_IntegerValueExpression
	COMMA
	i2 = pr_IntegerValueExpression
	COMMA
	i3 = pr_IntegerValueExpression
	COMMA
	i4 = pr_IntegerValueExpression
	RPAREN
	{	$ucstr = new TitanUniversalCharString(	(char)$i1.integer.getIntegerValue().intValue(),
												(char)$i2.integer.getIntegerValue().intValue(),
												(char)$i3.integer.getIntegerValue().intValue(),
												(char)$i4.integer.getIntegerValue().intValue()	);
	}
;

pr_EnumeratedValue returns [String identifier]:
	i = pr_Identifier	{	$identifier = $i.identifier;	}
;

pr_NULLKeyword:
	NULLKEYWORD
;

pr_CompoundValue returns [Module_Parameter moduleparameter]:
(	BEGINCHAR
	(	/* empty */	{	$moduleparameter = new Module_Param_Value_List();	}
	|	fv = pr_FieldValue
		{	$moduleparameter = new Module_Param_Assignment_List();
			$moduleparameter.add_elem($fv.moduleparameter);
		}
		(	COMMA
			fv = pr_FieldValue	{	$moduleparameter.add_elem($fv.moduleparameter);	}
		)*
	|	ai = pr_ArrayItem
		{	$moduleparameter = new Module_Param_Value_List();
			$ai.moduleparameter.set_id(new Module_Param_Index($moduleparameter.get_size(),false));
			$moduleparameter.add_elem($ai.moduleparameter);
		}
		(	COMMA
			ai = pr_ArrayItem
			{	$ai.moduleparameter.set_id(new Module_Param_Index($moduleparameter.get_size(),false));
				$moduleparameter.add_elem($ai.moduleparameter);
			}
		)*
	|	iv = pr_IndexValue
		{	$moduleparameter = new Module_Param_Indexed_List();
			$moduleparameter.add_elem($iv.moduleparameter);
		}
		(	COMMA
			iv = pr_IndexValue	{	$moduleparameter.add_elem($iv.moduleparameter);	}
		)*
	)
	ENDCHAR
|	LPAREN
	/* at least 2 elements to avoid shift/reduce conflicts with pr_IntegerValueExpression and pr_FloatValueExpression rules */
	pv = pr_ParameterValue
	{	$moduleparameter = new Module_Param_List_Template();
		$pv.moduleparameter.set_id(new Module_Param_Index($moduleparameter.get_size(),false));
		$moduleparameter.add_elem($pv.moduleparameter);
	}
	COMMA
	til = pr_TemplateItemList	{	$moduleparameter.add_list_with_implicit_ids($til.mplist);	}
	RPAREN
|	COMPLEMENTKEYWORD
	LPAREN
	til = pr_TemplateItemList
	{	$moduleparameter = new Module_Param_ComplementList_Template();
		$moduleparameter.add_list_with_implicit_ids($til.mplist);
	}
	RPAREN
|	SUPERSETKEYWORD
	LPAREN
	til = pr_TemplateItemList
	{	$moduleparameter = new Module_Param_Superset_Template();
		$moduleparameter.add_list_with_implicit_ids($til.mplist);
	}
	RPAREN
|	SUBSETKEYWORD
	LPAREN
	til = pr_TemplateItemList
	{	$moduleparameter = new Module_Param_Subset_Template();
		$moduleparameter.add_list_with_implicit_ids($til.mplist);
	}
	RPAREN
)
;

pr_FieldValue returns [Module_Parameter moduleparameter]:
	fn = pr_FieldName
	ASSIGNMENTCHAR
	pv = pr_ParameterValueOrNotUsedSymbol
	{
		$moduleparameter = $pv.moduleparameter;
		$moduleparameter.set_id(new Module_Param_FieldName($fn.identifier));
	}
;

pr_FieldName returns [String identifier]:
	i = pr_Identifier	{	$identifier = $i.identifier; }
;

pr_ParameterValueOrNotUsedSymbol returns [Module_Parameter moduleparameter]:
	MINUS	{	$moduleparameter = new Module_Param_NotUsed();	}
|	pv = pr_ParameterValue	{	$moduleparameter = $pv.moduleparameter;	}
;

pr_ArrayItem returns [Module_Parameter moduleparameter]:
	pv = pr_ParameterValueOrNotUsedSymbol	{	$moduleparameter = $pv.moduleparameter;	}
|	PERMUTATIONKEYWORD LPAREN til = pr_TemplateItemList RPAREN
	{	$moduleparameter = new Module_Param_Permutation_Template();
		$moduleparameter.add_list_with_implicit_ids($til.mplist);
	}
;

pr_TemplateItemList returns [List<Module_Parameter> mplist]:
	pv = pr_ParameterValue
	{	$mplist = new ArrayList<Module_Parameter>();
		$mplist.add($pv.moduleparameter);
	}
	(	COMMA
		pv = pr_ParameterValue	{	$mplist.add($pv.moduleparameter);	}
	)*
;

// config_process.y/IndexItem
pr_IndexValue returns [Module_Parameter moduleparameter]:
	iii = pr_IndexItemIndex ASSIGNMENTCHAR pv = pr_ParameterValue
	{	$moduleparameter = $pv.moduleparameter;
		$moduleparameter.set_id(new Module_Param_Index($iii.integer.getIntegerValue(),true));
	}
;

pr_IntegerRange returns [CFGNumber min, CFGNumber max, boolean min_exclusive, boolean max_exclusive]
@init {
	$min = null;
	$max = null;
	$min_exclusive = false;
	$max_exclusive = false;
}:
	LPAREN
	(	EXCLUSIVE	{	$min_exclusive = true;	}
	)?
	(	i1 = pr_IntegerValueExpression	{	$min = $i1.integer;	}
	|	MINUS	INFINITYKEYWORD
	)
	DOTDOT
	(	EXCLUSIVE	{	$max_exclusive = true;	}
	)?
	(	i2 = pr_IntegerValueExpression	{	$max = $i2.integer;	}
	|	INFINITYKEYWORD
	)
	RPAREN
;

pr_FloatRange returns [CFGNumber min, CFGNumber max, boolean min_exclusive, boolean max_exclusive]
@init {
	$min = null;
	$max = null;
	$min_exclusive = false;
	$max_exclusive = false;
}:
	LPAREN
	(	EXCLUSIVE	{	$min_exclusive = true;	}
	)?
	(	f1 = pr_FloatValueExpression	{	$min = $f1.floatnum;	}
	|	MINUS	INFINITYKEYWORD
	)
	DOTDOT
	(	EXCLUSIVE	{	$max_exclusive = true;	}
	)?
	(	f2 = pr_FloatValueExpression	{	$max = $f2.floatnum;	}
	|	INFINITYKEYWORD
	)
	RPAREN
;

pr_FloatValueExpression returns [CFGNumber floatnum]:
	a = pr_FloatAddExpression	{	$floatnum = $a.floatnum;	}
;

pr_FloatAddExpression returns [CFGNumber floatnum]:
	a = pr_FloatMulExpression	{	$floatnum = $a.floatnum;	}
	(	(	PLUS	b1 = pr_FloatMulExpression	{	$floatnum.add($b1.floatnum);	}
		|	MINUS	b2 = pr_FloatMulExpression	{	$b2.floatnum.mul(-1); $floatnum.add($b2.floatnum);	}
		)
	)*
;

pr_FloatMulExpression returns [CFGNumber floatnum]:
	a = pr_FloatUnaryExpression	{	$floatnum = $a.floatnum;	}
	(	STAR	b1 = pr_FloatUnaryExpression	{	$floatnum.mul($b1.floatnum);	}
	|	SLASH	b2 = pr_FloatUnaryExpression
		{	try {
				$floatnum.div($b2.floatnum);
			} catch ( ArithmeticException e ) {
				// division by 0
				reportError( e.getMessage(), $a.start, $b2.stop );
				$floatnum = new CFGNumber( "0" );
			}
		}
	)*
;

pr_FloatUnaryExpression returns [CFGNumber floatnum]:
{	boolean negate = false;
}
	(	PLUS
	|	MINUS	{	negate = !negate;	}
	)*
	a = pr_FloatPrimaryExpression
		{	$floatnum = $a.floatnum;
			if ( negate ) {
				$floatnum.mul( -1 );
			}
		}
;

pr_FloatPrimaryExpression returns [CFGNumber floatnum]:
(	a = pr_Float	{	$floatnum = $a.floatnum;	}
|	LPAREN b = pr_FloatAddExpression RPAREN	{	$floatnum = $b.floatnum;	}
)
;

pr_StringRange returns [Module_Param_StringRange stringrange]
@init {
	TitanUniversalChar lower = new TitanUniversalChar((char)0, (char)0, (char)0, (char)0);
	TitanUniversalChar upper = new TitanUniversalChar((char)0, (char)0, (char)0, (char)0);
	boolean min_exclusive = false;
	boolean max_exclusive = false;
}:
	LPAREN
	(	EXCLUSIVE	{	min_exclusive = true;	}
	)?
	s1 = pr_UniversalOrNotStringValue
	{	if ($s1.cstr instanceof TitanCharString) {
			final TitanCharString cs = (TitanCharString)$s1.cstr;
			if (cs.lengthof().operator_not_equals(1)) {
				config_process_error("Lower bound of char range must be 1 character only");
			} else {
				lower = new TitanUniversalChar((char)0, (char)0, (char)0, cs.get_value().charAt(0));
			}
		} else {
			final TitanUniversalCharString ucs = (TitanUniversalCharString)$s1.cstr;
			if (ucs.lengthof().operator_not_equals(1)) {
				config_process_error("Lower bound of char range must be 1 character only");
			} else {
				lower = ucs.get_value().get(0);
			}
		}
	}
	DOTDOT
	(	EXCLUSIVE	{	max_exclusive = true;	}
	)?
	s2 = pr_UniversalOrNotStringValue
	{	if ($s2.cstr instanceof TitanCharString) {
			final TitanCharString cs = (TitanCharString)$s2.cstr;
			if (cs.lengthof().operator_not_equals(1)) {
				config_process_error("Upper bound of char range must be 1 character only");
			} else {
				upper = new TitanUniversalChar((char)0, (char)0, (char)0, cs.get_value().charAt(0));
			}
		} else {
			final TitanUniversalCharString ucs = (TitanUniversalCharString)$s2.cstr;
			if (ucs.lengthof().operator_not_equals(1)) {
				config_process_error("Upper bound of char range must be 1 character only");
			} else {
				upper = ucs.get_value().get(0);
			}
		}
	}
	RPAREN
{
	if (upper.is_less_than(lower)) {
		config_process_error("Lower bound is larger than upper bound in the char range");
		lower = upper;
	}
	$stringrange = new Module_Param_StringRange(lower, upper, min_exclusive, max_exclusive);
}
;


pr_PatternChunkList returns [TitanUniversalCharString ucstr]:
	p = pr_PatternChunk	{	$ucstr = $p.ucstr;	}
	(	AND
		p = pr_PatternChunk	{	$ucstr.operator_concatenate($p.ucstr);	}
	)*
;

pr_PatternChunk returns [TitanUniversalCharString ucstr]:
	cstr = pr_CString
	// pr_CString.text is used instead of pr_CString.string,
	// so the original text is used instead of the unescaped return value.
	// This is done this way, because pattern string escape handling is done differently.
	// But beginning and ending quotes must be removed.
	{	if ( $cstr.text != null ) {
			$ucstr = new TitanUniversalCharString($cstr.text.replaceAll("^\"|\"$", ""));
		}
	}
|	q = pr_Quadruple	{	$ucstr = $q.ucstr;	}
;

pr_BStringMatch returns [String string]:
	b = BITSTRINGMATCH
	{	final String temp = $b.text;
		if ( temp != null ) {
			$string = temp.replaceAll("^\'|\'B$|\\s+", "");
		}
	}
;

pr_HStringMatch returns [String string]:
	h = HEXSTRINGMATCH
	{	final String temp = $h.text;
		if ( temp != null ) {
			$string = temp.replaceAll("^\'|\'H$|\\s+", "");
		}
	}
;

pr_OStringMatch returns [String string]:
	o = OCTETSTRINGMATCH
	{	final String temp = $o.text;
		if ( temp != null ) {
			$string = temp.replaceAll("^\'|\'O$|\\s+", "");
		}
	}
;
