#!/usr/bin/perl
###############################################################################
# Copyright (c) 2000-2016 Ericsson Telecom AB
# All rights reserved. This program and the accompanying materials
# are made available under the terms of the Eclipse Public License v1.0
# which accompanies this distribution, and is available at
# http://www.eclipse.org/legal/epl-v10.html
#
# Contributors:
#   Lovassy, Arpad
#
###############################################################################

###############################################################################
# Generates ...LexerLogUtil.java implements ILexerLogUtil
# for all ...Lexer.java under the current directory
# to determine token name from token index.
# This is used for logging purpose,
# see ParserLogger.java and TokenNameResolver.java
#
# Prerequisites: ANTLR lexer .g4 files must be compiled,
#   because generated java files must exist.
#
# Example usage (to create ...LexerLogUtil.java for all the ...Lexer.java
# in titan.EclipsePlug-ins project):
#   cd <titan.EclipsePlug-ins project root>
#   Tools/antlr4_generate_lexerlogutil.pl
###############################################################################

# to read the whole file at once, not just line-by-line
# http://www.kichwa.com/quik_ref/spec_variables.html
undef $/;

$fileindex = 0;

# Parses a generated ...Lexer.java, and collects (token type name, token type index) pairs into hash (associative array)
sub parseLexer {
    # lexer filename
    my $filename = shift;
    print("Source file: $filename\n");
    open(IN, $filename);
    my $whole_file = <IN>;
    close(IN);
    # associative array of lexer tokens, where key is token name, value is token index
    my %t;
    if ( $whole_file =~ /public static final int\r?\n\s*(.*?);/gs ) {
        my $const_defs = $1; #comma separated constant definition: WS=1, LINE_COMMENT=2, ..., MACRO12=451
#        print "\$const_defs == $const_defs\n";
        my @list = split(/,\s*/, $const_defs);
        foreach my $const_def (@list) {
#            print "\$const_def == \"$const_def\"\n";
            if ( $const_def =~ /(^[A-Za-z][A-Za-z0-9_]*)=([0-9]+)$/ ) {
                my $const_name = $1;
                my $const_value = $2;
#                print "\$const_name == \"$const_name\"\n";
#                print "\$const_value == \"$const_value\"\n";
                $t{$const_name} = $const_value;
            }
            else {
                print "ERROR: $const_def does NOT match!\n";
            }
        }
    }
    else {
        print "ERROR: NO match: public static final int\n";
    }
    return \%t;
}

# recursive function to load all the specified directories and files recursively from a directory
sub load {
    my $filename = shift; # 1st parameter
    my $f = "F".$fileindex++;
    if ( opendir( $f, $filename ) ) {
        # $filename is a directory
        while ( local $newfilename = readdir( $f ) ) {
            if ( !( $newfilename =~ /^\.(\.|git)?$/ ) ) {  # filter . .. .git
                local $longfilename = $filename."/".$newfilename; # for Unix
                #local $longfilename = $filename."\\".$newfilename; # for DOS/Windows

                load($longfilename);
            }
        }
        closedir($f);
    }
    else {
        # $filename is a file
        if ( $filename =~ /^(.*\/)Ttcn3BaseLexer\.java$/ ) {
            my $t_ref = parseLexer( $filename );
            # global variable to store the parsed result for later comparison
            %baseTokens = %$t_ref;
        }
        elsif ( $filename =~ /^(.*\/)Ttcn3Lexer\.java$/ ) {
            my $t_ref = parseLexer( $filename );
            # global variable to store the parsed result for later comparison
            %ttcn3Tokens = %$t_ref;
        }
    }
}

#----------
# MAIN PART

# processing all the files from ..
load("..");

# comparing the result hashes, compared hashes are modified: common element are removed

my $error = 0;
foreach my $ttcn3_key ( keys %ttcn3Tokens ) {
    if ( exists $baseTokens{$ttcn3_key} && $ttcn3Tokens{$ttcn3_key} eq $baseTokens{$ttcn3_key} ) {
        delete $ttcn3Tokens{$ttcn3_key};
        delete $baseTokens{$ttcn3_key};
    }
}

if ( %ttcn3Tokens ) {
    $error = 1;
    print"ERROR: There are token types, which exists only in Ttcn3Lexer:\n";
    print "  $_ $ttcn3Tokens{$_}\n" for ( keys %ttcn3Tokens );
}

if ( %baseTokens ) {
    $error = 1;
    print"ERROR: There are token types, which exists only in Ttcn3BaseLexer:\n";
    print "  $_ $baseTokens{$_}\n" for ( keys %baseTokens );
}

exit( $error );

