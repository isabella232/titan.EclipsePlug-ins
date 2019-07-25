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
# Checks if the parsers have the same rules
# in the generated parsers.
#
# Example usage:
#   cd <titan.EclipsePlug-ins project root>
#   Tools/antlr4_check_cfg_parsers.sh
###############################################################################
set -e
set -o pipefail

# script directory
# http://stackoverflow.com/questions/59895/can-a-bash-script-tell-which-directory-it-is-stored-in
DIR="$( cd "$( dirname "${BASH_SOURCE[0]}" )" && pwd )"

WORKSPACE_PATH=$DIR/..

# Check if the parsers have the same rules
cd $WORKSPACE_PATH

$DIR/antlr4_check_parsers.pl \
org.eclipse.titan.runtime/src/org/eclipse/titan/runtime/core/cfgparser/RuntimeCfgParser.java \
org.eclipse.titan.runtime/src/org/eclipse/titan/runtime/core/cfgparser/RuntimeCfgPreParser.java

$DIR/antlr4_check_parsers.pl \
org.eclipse.titan.runtime/src/org/eclipse/titan/runtime/core/cfgparser/RuntimeCfgParser.java \
org.eclipse.titan.common/src/org/eclipse/titan/common/parsers/cfg/CfgParser.java

