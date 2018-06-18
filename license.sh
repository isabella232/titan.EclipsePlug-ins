###############################################################################
# Copyright (c) 2000-2018 Ericsson Telecom AB
# All rights reserved. This program and the accompanying materials
# are made available under the terms of the Eclipse Public License v2.0
# which accompanies this distribution, and is available at
# https://www.eclipse.org/org/documents/epl-2.0/EPL-2.0.html
###############################################################################
# switch on sending of usage statistics in titan eclipse
perl -i -p -e 's/(^[^\S\n]*public static final boolean USAGE_STAT_SENDING = )(false|true);/\1true;/g' org.eclipse.titan.common/src/org/eclipse/titan/common/product/ProductConstants.java 

# switch on license file checking in titan eclipse
perl -i -p -e 's/(^[^\S\n]*public static final boolean LICENSE_NEEDED = )(false|true);/\1true;/g' org.eclipse.titan.common/src/org/eclipse/titan/common/product/ProductConstants.java 
