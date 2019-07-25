#!/bin/bash
###############################################################################
# Copyright (c) 2000-2019 Ericsson Telecom AB
# All rights reserved. This program and the accompanying materials
# are made available under the terms of the Eclipse Public License v2.0
# which accompanies this distribution, and is available at
# https://www.eclipse.org/org/documents/epl-2.0/EPL-2.0.html
#
# Contributors:
#   Lovassy, Arpad
#
###############################################################################

###############################################################################
# Checks if the token type names and token type indexes correspond to each other
# in the generated Ttcn3 lexers.
# Ttcn3 lexers must contain the same amount of tokens to make sure, that they are synchronized properly.
# Token index of the same token must be the same in all of the Ttcn3 lexers, otherwise code completion
# will not work properly, because Ttcn3ReferenceParser uses Ttcn3KeywordlessLexer (based on Ttcn3BaseLexer).
# So if a new token is added to Ttcn3Lexer, the same token must be added also to Ttcn3BaseLexer as unused token
# (see "tokens" section in Ttcn3BaseLexer.g4).
#
# Example usage:
#   cd <titan.EclipsePlug-ins project root>
#   Tools/antlr4_check_ttcn3_lexers.sh
###############################################################################
set -e
set -o pipefail

# script directory
# http://stackoverflow.com/questions/59895/can-a-bash-script-tell-which-directory-it-is-stored-in
DIR="$( cd "$( dirname "${BASH_SOURCE[0]}" )" && pwd )"

WORKSPACE_PATH=$DIR/..

# Check if lexers have the same tokens
cd $WORKSPACE_PATH

$DIR/antlr4_check_lexers.pl \
org.eclipse.titan.designer/src/org/eclipse/titan/designer/parsers/ttcn3parser/Ttcn3BaseLexer.java \
org.eclipse.titan.designer/src/org/eclipse/titan/designer/parsers/ttcn3parser/Ttcn3Lexer.java

$DIR/antlr4_check_lexers.pl \
org.eclipse.titan.designer/src/org/eclipse/titan/designer/parsers/ttcn3parser/Ttcn3BaseLexer.java \
org.eclipse.titan.designer/src/org/eclipse/titan/designer/parsers/ttcn3parser/Ttcn3KeywordlessLexer.java

