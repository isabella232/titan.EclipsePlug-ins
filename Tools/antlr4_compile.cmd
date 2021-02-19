@rem ###############################################################################
@rem # Copyright (c) 2000-2021 Ericsson Telecom AB
@rem # All rights reserved. This program and the accompanying materials
@rem # are made available under the terms of the Eclipse Public License v2.0
@rem # which accompanies this distribution, and is available at
@rem # https://www.eclipse.org/org/documents/epl-2.0/EPL-2.0.html
@rem #
@rem # Contributors:
@rem #   Lovassy, Arpad
@rem #   Magyari, Miklos
@rem #
@rem ###############################################################################

@rem ###############################################################################
@rem # Script for Windows to compile all the .g4 files in the Titan EclipsePlug-ins 
@rem # repo. Can be used to invoke antlr manually if the eclipse antlr plugin does
@rem # not work properly.
@rem # The script also builds helper classes for parser debuggig (these are not part
@rem # of the Titan build as they are only useful for parser developers).
@rem #
@rem # Example usage:
@rem #   cd <titan.EclipsePlug-ins project root>
@rem #   Tools/antlr4_compile.cmd 
@rem ###############################################################################

@echo off

@set ANTLR_VERSION=4.3

:loop
@if not "%1"=="" (
    @if "%1"=="-av" (
        @set ANTLR_VERSION=%2
        @shift
    )
    @shift
    @goto :loop
)

@echo Using antlr version %ANTLR_VERSION%

@set CURDIR=%cd%
@set DIR=%~dp0
@set WORKSPACE_PATH=%DIR%\..
@set ANTLR=-cp %HOMEDRIVE%%HOMEPATH%\lib\\antlr-%ANTLR_VERSION%-complete.jar org.antlr.v4.Tool

@cd %WORKSPACE_PATH%\org.eclipse.titan.runtime\src\org\eclipse\titan\runtime\core\cfgparser\
@java %ANTLR% RuntimeCfgLexer.g4 -no-listener -no-visitor -encoding UTF-8 -package org.eclipse.titan.runtime.core.cfgparser
@java %ANTLR% RuntimeCfgParser.g4 -no-listener -no-visitor -encoding UTF-8 -package org.eclipse.titan.runtime.core.cfgparser
@java %ANTLR% RuntimeCfgPreParser.g4 -no-listener -no-visitor -encoding UTF-8 -package org.eclipse.titan.runtime.core.cfgparser

@REM Titan Common
@echo Compiling common
@cd %WORKSPACE_PATH%\org.eclipse.titan.common\src\org\eclipse\titan\common\parsers\cfg\

@java %ANTLR% CfgLexer.g4 -no-listener -no-visitor -encoding UTF-8 -package org.eclipse.titan.common.parsers.cfg
@java %ANTLR% CfgParser.g4 -no-listener -no-visitor -encoding UTF-8 -package org.eclipse.titan.common.parsers.cfg

@rem Titan Designer
@REM ASN1
@echo Compiling ASN1
@cd %WORKSPACE_PATH%\org.eclipse.titan.designer\src\org\eclipse\titan\designer\parsers\asn1parser\
@java %ANTLR% Asn1Lexer.g4 -no-listener -no-visitor -encoding UTF-8 -package org.eclipse.titan.designer.parsers.asn1parser
@java %ANTLR% Asn1Parser.g4 -no-listener -no-visitor -encoding UTF-8 -package org.eclipse.titan.designer.parsers.asn1parser

@REM TTCN3
@echo Compiling ttcn3
@cd %WORKSPACE_PATH%\org.eclipse.titan.designer\src\org\eclipse\titan\designer\parsers\ttcn3parser\
@java %ANTLR% PreprocessorDirectiveLexer.g4 -no-listener -no-visitor -encoding UTF-8 -package org.eclipse.titan.designer.parsers.ttcn3parser
@java %ANTLR% PreprocessorDirectiveParser.g4 -no-listener -no-visitor -encoding UTF-8 -package org.eclipse.titan.designer.parsers.ttcn3parser
@java %ANTLR% Ttcn3Lexer.g4 -no-listener -no-visitor -encoding UTF-8 -package org.eclipse.titan.designer.parsers.ttcn3parser
@java %ANTLR% Ttcn3KeywordlessLexer.g4 -no-listener -no-visitor -encoding UTF-8 -package org.eclipse.titan.designer.parsers.ttcn3parser
@java %ANTLR% Ttcn3CharstringLexer.g4 -no-listener -no-visitor -encoding UTF-8 -package org.eclipse.titan.designer.parsers.ttcn3parser
@java %ANTLR% Ttcn3Parser.g4 -no-listener -no-visitor -encoding UTF-8 -package org.eclipse.titan.designer.parsers.ttcn3parser
@java %ANTLR% Ttcn3Reparser.g4 -no-listener -no-visitor -encoding UTF-8 -package org.eclipse.titan.designer.parsers.ttcn3parser
@java %ANTLR% PatternStringLexer.g4 -no-listener -no-visitor -encoding UTF-8 -package org.eclipse.titan.designer.parsers.ttcn3parser

@REM Extension attribute
@echo Compiling ext attr
@cd %WORKSPACE_PATH%\org.eclipse.titan.designer\src\org\eclipse\titan\designer\parsers\extensionattributeparser\
@java %ANTLR% ExtensionAttributeLexer.g4 -no-listener -no-visitor -encoding UTF-8 -package org.eclipse.titan.designer.parsers.extensionattributeparser
@java %ANTLR% ExtensionAttributeParser.g4 -no-listener -no-visitor -encoding UTF-8 -package org.eclipse.titan.designer.parsers.extensionattributeparser

@REM Variant attribute
@echo Compiling var attr
@cd %WORKSPACE_PATH%\org.eclipse.titan.designer\src\org\eclipse\titan\designer\parsers\variantattributeparser\
@java %ANTLR% VariantAttributeLexer.g4 -no-listener -no-visitor -encoding UTF-8 -package org.eclipse.titan.designer.parsers.variantattributeparser
@java %ANTLR% VariantAttributeParser.g4 -no-listener -no-visitor -encoding UTF-8 -package org.eclipse.titan.designer.parsers.variantattributeparser

@echo Generating ...LexerLogUtil.java files from ...Lexer.java files for resolving token names (OPTIONAL)
@cd %DIR%
@perl antlr4_generate_lexerlogutil.pl

@cd %CURDIR%

