parser grammar RuntimeCfgPreParser;

/*
 ******************************************************************************
 * Copyright (c) 2000-2018 Ericsson Telecom AB
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/org/documents/epl-2.0/EPL-2.0.html
 ******************************************************************************
*/

options {
	tokenVocab = RuntimeCfgLexer;
}

@header {
}

@members {

	private DefineSectionHandler defineSectionHandler = new DefineSectionHandler((CommonTokenStream)getTokenStream());

	public DefineSectionHandler getDefineSectionHandler() {
		return defineSectionHandler;
	}
}

pr_ConfigFile:
	(	pr_Section
	)+
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
	pr_MainControllerItemUnixDomainSocketValue
	SEMICOLON?
;

pr_MainControllerItemUnixDomainSocketValue:
	(YES | NO)
;

pr_MainControllerItemKillTimer:
	KILLTIMER
	ASSIGNMENTCHAR
	pr_ArithmeticValueExpression
	SEMICOLON?
;

pr_MainControllerItemLocalAddress:
	LOCALADDRESS
	ASSIGNMENTCHAR
	pr_HostName
	SEMICOLON?
;

pr_MainControllerItemNumHcs:
	NUMHCS
	ASSIGNMENTCHAR
	pr_IntegerValueExpression
	SEMICOLON?
;

pr_MainControllerItemTcpPort:
	TCPPORT
	ASSIGNMENTCHAR
	pr_IntegerValueExpression
	SEMICOLON?
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

pr_ExecuteSectionItem:
	pr_ExecuteSectionItemModuleName
	(	DOT
		pr_ExecuteSectionItemTestcaseName
	)?
	SEMICOLON?
;

pr_ExecuteSectionItemModuleName:
	TTCN3IDENTIFIER
;

pr_ExecuteSectionItemTestcaseName:
(	TTCN3IDENTIFIER
|	STAR
)
;

pr_DefineSection:
	DEFINE_SECTION
	(	def = pr_MacroAssignment
			{	if ( $def.name != null ) {
					defineSectionHandler.addDefinition( $def.name, $def.value );
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
		pr_ExternalCommandValue
	|	ENDCONTROLPART
		ASSIGNMENTCHAR
		pr_ExternalCommandValue
	|	BEGINTESTCASE
		ASSIGNMENTCHAR
		pr_ExternalCommandValue
	|	ENDTESTCASE
		ASSIGNMENTCHAR
		pr_ExternalCommandValue
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
	pr_ComponentID
	DOT
	pr_TestportName
	DOT
	pr_Identifier
	ASSIGNMENTCHAR
	pr_StringValue
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
|	MACRO
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
|	MACRO
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

pr_LoggerPluginsPart:
	(	pr_ComponentID DOT
	)?
	LOGGERPLUGINS
	ASSIGNMENTCHAR
	BEGINCHAR
	pr_LoggerPluginsList
	ENDCHAR
;

pr_LoggerPluginsList:
	pr_LoggerPluginEntry
	(	COMMA
		pr_LoggerPluginEntry
	)*
;

pr_LoggerPluginEntry:
	pr_Identifier
	(	ASSIGNMENTCHAR
		pr_StringValue
	)?
;

pr_PlainLoggingParam:
(	pr_ComponentID DOT
)?
(	STAR DOT
|	pr_Identifier DOT
)?
(	FILEMASK ASSIGNMENTCHAR	pr_LoggingBitMask
|	CONSOLEMASK ASSIGNMENTCHAR	pr_LoggingBitMask
|	DISKFULLACTION ASSIGNMENTCHAR pr_DiskFullActionValue
|	LOGFILENUMBER ASSIGNMENTCHAR	pr_NaturalNumber
|	LOGFILESIZE ASSIGNMENTCHAR	pr_NaturalNumber
|	LOGFILENAME ASSIGNMENTCHAR	pr_LogfileName
|	TIMESTAMPFORMAT ASSIGNMENTCHAR	pr_TimeStampValue
|	CONSOLETIMESTAMPFORMAT ASSIGNMENTCHAR	pr_TimeStampValue
|	SOURCEINFOFORMAT ASSIGNMENTCHAR
	(	pr_SourceInfoValue
	|	pr_YesNoOrBoolean
	)
|	APPENDFILE ASSIGNMENTCHAR	pr_YesNoOrBoolean
|	LOGEVENTTYPES ASSIGNMENTCHAR pr_LogEventTypesValue
|	LOGENTITYNAME ASSIGNMENTCHAR pr_LogEventTypesValue
|	MATCHINGHINTS ASSIGNMENTCHAR pr_MatchingHintsValue
|	pr_PluginSpecificParamName ASSIGNMENTCHAR	pr_StringValue
|	EMERGENCYLOGGING ASSIGNMENTCHAR	pr_NaturalNumber
|	EMERGENCYLOGGINGBEHAVIOUR ASSIGNMENTCHAR pr_BufferAllOrMasked
|	EMERGENCYLOGGINGMASK ASSIGNMENTCHAR	pr_LoggingBitMask
)
;

pr_TimeStampValue:
	TIMESTAMPVALUE
;

pr_SourceInfoValue:
	SOURCEINFOVALUE_NONE
|	SOURCEINFOVALUE_SINGLE
|	SOURCEINFOVALUE_STACK
;

pr_PluginSpecificParamName:
	TTCN3IDENTIFIER
;

pr_BufferAllOrMasked:
	BUFFERALL
|	BUFFERMASKED
;

pr_DiskFullActionValue:
(	DISKFULLACTIONVALUE_ERROR
|	DISKFULLACTIONVALUE_STOP
|	DISKFULLACTIONVALUE_DELETE
|	DISKFULLACTIONVALUE_RETRY
	(	LPAREN
		NATURAL_NUMBER
		RPAREN
	)?
)
;

pr_ComponentID:
(	pr_Identifier
|	pr_NaturalNumber
|	MTCKEYWORD
|	STAR
|	SYSTEMKEYWORD
)
;

pr_LoggingBitMask:
	pr_LoggingMaskElement
	(	LOGICALOR	pr_LoggingMaskElement
	)*
;

pr_LoggingMaskElement:
	pr_LogEventType
|	pr_LogEventTypeSet
|	pr_deprecatedEventTypeSet
;

pr_LogfileName:
	pr_StringValue
;

pr_YesNoOrBoolean:
	YESNO
|	pr_Boolean
;

pr_LogEventTypesValue:
	pr_YesNoOrBoolean
|	pr_Detailed
;

pr_MatchingHintsValue:
	COMPACT
|	DETAILED
;

pr_LogEventType:
(  ACTION_UNQUALIFIED
|  DEBUG_ENCDEC
|  DEBUG_TESTPORT
|  DEBUG_UNQUALIFIED
|  DEFAULTOP_ACTIVATE
|  DEFAULTOP_DEACTIVATE
|  DEFAULTOP_EXIT
|  DEFAULTOP_UNQUALIFIED
|  ERROR_UNQUALIFIED
|  EXECUTOR_COMPONENT
|  EXECUTOR_CONFIGDATA
|  EXECUTOR_EXTCOMMAND
|  EXECUTOR_LOGOPTIONS
|  EXECUTOR_RUNTIME
|  EXECUTOR_UNQUALIFIED
|  FUNCTION_RND
|  FUNCTION_UNQUALIFIED
|  MATCHING_DONE
|  MATCHING_MCSUCCESS
|  MATCHING_MCUNSUCC
|  MATCHING_MMSUCCESS
|  MATCHING_MMUNSUCC
|  MATCHING_PCSUCCESS
|  MATCHING_PCUNSUCC
|  MATCHING_PMSUCCESS
|  MATCHING_PMUNSUCC
|  MATCHING_PROBLEM
|  MATCHING_TIMEOUT
|  MATCHING_UNQUALIFIED
|  PARALLEL_PORTCONN
|  PARALLEL_PORTMAP
|  PARALLEL_PTC
|  PARALLEL_UNQUALIFIED
|  PORTEVENT_DUALRECV
|  PORTEVENT_DUALSEND
|  PORTEVENT_MCRECV
|  PORTEVENT_MCSEND
|  PORTEVENT_MMRECV
|  PORTEVENT_MMSEND
|  PORTEVENT_MQUEUE
|  PORTEVENT_PCIN
|  PORTEVENT_PCOUT
|  PORTEVENT_PMIN
|  PORTEVENT_PMOUT
|  PORTEVENT_PQUEUE
|  PORTEVENT_STATE
|  PORTEVENT_UNQUALIFIED
|  STATISTICS_UNQUALIFIED
|  STATISTICS_VERDICT
|  TESTCASE_FINISH
|  TESTCASE_START
|  TESTCASE_UNQUALIFIED
|  TIMEROP_GUARD
|  TIMEROP_READ
|  TIMEROP_START
|  TIMEROP_STOP
|  TIMEROP_TIMEOUT
|  TIMEROP_UNQUALIFIED
|  USER_UNQUALIFIED
|  VERDICTOP_FINAL
|  VERDICTOP_GETVERDICT
|  VERDICTOP_SETVERDICT
|  VERDICTOP_UNQUALIFIED
|  WARNING_UNQUALIFIED
)
;

pr_LogEventTypeSet:
(  TTCN_EXECUTOR2
|  TTCN_ERROR2
|  TTCN_WARNING2
|  TTCN_PORTEVENT2
|  TTCN_TIMEROP2
|  TTCN_VERDICTOP2
|  TTCN_DEFAULTOP2
|  TTCN_ACTION2
|  TTCN_TESTCASE2
|  TTCN_FUNCTION2
|  TTCN_USER2
|  TTCN_STATISTICS2
|  TTCN_PARALLEL2
|  TTCN_MATCHING2
|  TTCN_DEBUG2
|  LOG_ALL
|  LOG_NOTHING
)
;

pr_deprecatedEventTypeSet:
(  TTCN_EXECUTOR1
|  TTCN_ERROR1
|  TTCN_WARNING1
|  TTCN_PORTEVENT1
|  TTCN_TIMEROP1
|  TTCN_VERDICTOP1
|  TTCN_DEFAULTOP1
|  TTCN_ACTION1
|  TTCN_TESTCASE1
|  TTCN_FUNCTION1
|  TTCN_USER1
|  TTCN_STATISTICS1
|  TTCN_PARALLEL1
|  TTCN_MATCHING1
|  TTCN_DEBUG1
)
;

pr_Detailed:
	DETAILED
|	SUBCATEGORIES
;

pr_ComponentItem:
	pr_ComponentName
	ASSIGNMENTCHAR
	(	pr_HostName
	|	pr_HostNameIpV6
	)
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
|	MACRO
)
;

pr_HostNameIpV6:
	IPV6
;

pr_MacroAssignment returns [ String name, ParserRuleContext value ]:
(	n = TTCN3IDENTIFIER { $name = $n.getText(); }
	ASSIGNMENTCHAR
	v = pr_DefinitionRValue { $value = $v.ctx; }
)
;

pr_DefinitionRValue:
(	pr_SimpleValue+
|	BEGINCHAR
	pr_StructuredValueList?
	ENDCHAR
)
;

pr_StructuredValueList:
	(	pr_DefinitionRValue
	|	pr_MacroAssignment	)
	(	COMMA
		(	pr_DefinitionRValue
		|	pr_MacroAssignment	)
	)*
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
|	FSTRING
)
;

pr_TestportName:
(	pr_Identifier
	(	SQUAREOPEN pr_IntegerValueExpression SQUARECLOSE
		//TODO: it can be changed to pr_IndexItemIndex, also in config_process.y
	)*
|	STAR
)
;

pr_Identifier:
(	MACRO_ID
|	TTCN3IDENTIFIER
)
;

pr_IntegerValueExpression:
	pr_IntegerAddExpression
;

pr_IntegerAddExpression:
	pr_IntegerMulExpression
	(	PLUS	pr_IntegerMulExpression
	|	MINUS	pr_IntegerMulExpression
	)*
;

pr_IntegerMulExpression:
	pr_IntegerUnaryExpression
	(	STAR	pr_IntegerUnaryExpression
	|	SLASH	pr_IntegerUnaryExpression
	)*
;

pr_IntegerUnaryExpression:
	(	PLUS
	|	MINUS
	)*
	pr_IntegerPrimaryExpression
;

pr_IntegerPrimaryExpression:
(	pr_NaturalNumber
|	LPAREN pr_IntegerAddExpression RPAREN
)
;

pr_NaturalNumber:
(	NATURAL_NUMBER
|	pr_MacroNaturalNumber
|	TTCN3IDENTIFIER // module parameter name
)
;

pr_MPNaturalNumber:
(	NATURAL_NUMBER
|	pr_MacroNaturalNumber
)
;

pr_MacroNaturalNumber:
(	MACRO_INT
|	MACRO
)
;

pr_StringValue:
	pr_CString
	(	STRINGOP
		pr_CString
	)*
;

pr_CString:
(	STRING
|	pr_MacroCString
|	pr_MacroExpliciteCString
|	TTCN3IDENTIFIER // module parameter name
)
;

pr_MPCString:
(	STRING
|	(	pr_MacroCString
	|	pr_MacroExpliciteCString
	)
)
;

pr_MacroCString:
	MACRO
;

pr_MacroExpliciteCString:
	MACRO_EXP_CSTR
;

pr_GroupItem:
(	pr_Identifier
	ASSIGNMENTCHAR
	(	STAR
	|	(	pr_DNSName
		|	pr_Identifier
		)
		(	COMMA
			(	pr_DNSName
			|	pr_Identifier
			)
		)*
	)
)
;

pr_DNSName:
(	NATURAL_NUMBER
|	FLOAT
|	DNSNAME
)
;

pr_ModuleParam:
	pr_ParameterName
	(	ASSIGNMENTCHAR
	|	CONCATCHAR
	)
	pr_ParameterValue
;

pr_ParameterName:
(	pr_ParameterNamePart
	(	pr_Dot
		pr_ParameterNameTail
	|
	)
|	pr_StarModuleName
	DOT
	pr_ParameterNamePart
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
	pr_ParameterExpression
	(	pr_LengthMatch
	)?
	(	IFPRESENTKEYWORD
	)?
;

//module parameter expression, it can contain previously defined module parameters
pr_ParameterExpression:
	pr_MPAddExpression
;

pr_MPAddExpression:
(	pr_MPMulExpression
	(
		(	PLUS
		|	MINUS
		|	STRINGOP
		)
		pr_MPMulExpression
	)*
);

pr_MPMulExpression:
(	pr_MPUnaryExpression
	(
		(	STAR
		|	SLASH
		)
		pr_MPUnaryExpression
	)*
);

pr_MPUnaryExpression:
(	PLUS
	pr_MPUnaryExpression
|	MINUS
	pr_MPUnaryExpression
|	LPAREN
	pr_ParameterExpression
	RPAREN
|	pr_MPPrimaryValue
);

pr_MPPrimaryValue:
(	pr_SimpleParameterValue
|	pr_ParameterReference
);

pr_LengthMatch:
	LENGTHKEYWORD
	LPAREN
	(	pr_LengthBound
	|	pr_LengthBound
		DOTDOT
		(	pr_LengthBound
		|	INFINITYKEYWORD
		)
	)
	RPAREN
;

pr_LengthBound:
	pr_IntegerValueExpression
;

pr_SimpleParameterValue:
(	pr_MPNaturalNumber
|	pr_MPFloat
|	pr_Boolean
|	pr_ObjIdValue
|	pr_VerdictValue
|	pr_BStringValue
|	pr_HStringValue
|	pr_OStringValue
|	pr_MPCString
|	pr_Quadruple
|	OMITKEYWORD
|	pr_NULLKeyword
|	MTCKEYWORD
|	SYSTEMKEYWORD
|	pr_CompoundValue
|	ANYVALUE
|	STAR
|	pr_IntegerRange
|	pr_FloatRange
|	pr_StringRange
|	PATTERNKEYWORD	NOCASEKEYWORD?	pr_PatternChunkList
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

pr_ArithmeticValueExpression:
	pr_ArithmeticAddExpression
;

pr_ArithmeticAddExpression:
	pr_ArithmeticMulExpression
	(	PLUS	pr_ArithmeticMulExpression
	|	MINUS	pr_ArithmeticMulExpression
	)*
;

pr_ArithmeticMulExpression:
	pr_ArithmeticUnaryExpression
	(	STAR	pr_ArithmeticUnaryExpression
	|	SLASH	pr_ArithmeticUnaryExpression
	)*
;

pr_ArithmeticUnaryExpression:
	(	PLUS
	|	MINUS
	)*
	pr_ArithmeticPrimaryExpression
;

pr_ArithmeticPrimaryExpression:
(	pr_Float
|	pr_NaturalNumber
|	LPAREN	pr_ArithmeticAddExpression RPAREN
)
;

pr_Float:
(	FLOAT
|	MACRO_FLOAT
|	TTCN3IDENTIFIER // module parameter name
)
;

pr_MPFloat:
(	FLOAT
|	NANKEYWORD
|	INFINITYKEYWORD
|	MACRO_FLOAT
)
;

pr_Boolean:
(	TRUE
|	FALSE
|	MACRO_BOOL
)
;

pr_ObjIdValue:
	OBJIDKEYWORD
	BEGINCHAR
	pr_ObjIdComponent+
	ENDCHAR
;

pr_ObjIdComponent:
(	pr_NaturalNumber
|	pr_Identifier LPAREN	pr_NaturalNumber RPAREN
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
	pr_BString
	(	STRINGOP
		pr_BString
	)*
;

pr_BString:
(	BITSTRING
|	MACRO_BSTR
)
;

pr_HStringValue:
	pr_HString
	(	STRINGOP
		pr_HString
	)*
;

pr_HString:
(	HEXSTRING
|	MACRO_HSTR
)
;

pr_OStringValue:
	pr_OString
	(	STRINGOP
		pr_OString
	)*
;

pr_OString:
(	OCTETSTRING
|	MACRO_OSTR
|	macro_bin = MACRO_BINARY
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
	pr_IntegerValueExpression
	COMMA
	pr_IntegerValueExpression
	COMMA
	pr_IntegerValueExpression
	COMMA
	pr_IntegerValueExpression
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
	|	pr_FieldValue
		(	COMMA
			pr_FieldValue
		)*
	|	pr_ArrayItem
		(	COMMA
			pr_ArrayItem
		)*
	|	pr_IndexValue
		(	COMMA
			pr_IndexValue
		)*
	)
	ENDCHAR
|	LPAREN
	/* at least 2 elements to avoid shift/reduce conflicts with pr_IntegerValueExpression and pr_FloatValueExpression rules */
	pr_ParameterValue
	COMMA
	pr_TemplateItemList
	RPAREN
|	COMPLEMENTKEYWORD
	LPAREN
	pr_TemplateItemList
	RPAREN
|	SUPERSETKEYWORD
	LPAREN
	pr_TemplateItemList
	RPAREN
|	SUBSETKEYWORD
	LPAREN
	pr_TemplateItemList
	RPAREN
)
;

pr_FieldValue:
	pr_FieldName
	ASSIGNMENTCHAR
	pr_ParameterValueOrNotUsedSymbol
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
|	PERMUTATIONKEYWORD LPAREN	pr_TemplateItemList RPAREN
;

pr_TemplateItemList:
	pr_ParameterValue
	(	COMMA
		pr_ParameterValue
	)*
;

// config_process.y/IndexItem
pr_IndexValue:
	pr_IndexItemIndex
	ASSIGNMENTCHAR
	pr_ParameterValue
;

pr_IntegerRange:
	LPAREN
	EXCLUSIVE?
	(	pr_IntegerValueExpression
	|	MINUS	INFINITYKEYWORD
	)
	DOTDOT
	EXCLUSIVE?
	(	pr_IntegerValueExpression
	|	INFINITYKEYWORD
	)
	RPAREN
;

pr_FloatRange:
	LPAREN
	EXCLUSIVE?
	(	pr_FloatValueExpression
	|	MINUS	INFINITYKEYWORD
	)
	DOTDOT
	EXCLUSIVE?
	(	pr_FloatValueExpression
	|	INFINITYKEYWORD
	)
	RPAREN
;

pr_FloatValueExpression:
	pr_FloatAddExpression
;

pr_FloatAddExpression:
	pr_FloatMulExpression
	(	(	PLUS	pr_FloatMulExpression
		|	MINUS	pr_FloatMulExpression
		)
	)*
;

pr_FloatMulExpression:
	pr_FloatUnaryExpression
	(	STAR	pr_FloatUnaryExpression
	|	SLASH	pr_FloatUnaryExpression
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
	LPAREN
	EXCLUSIVE?
	pr_UniversalOrNotStringValue
	DOTDOT
	EXCLUSIVE?
	pr_UniversalOrNotStringValue
	RPAREN
;

pr_PatternChunkList:
	pr_PatternChunk
	(	AND
		pr_PatternChunk
	)*
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
