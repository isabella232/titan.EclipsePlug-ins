#!/usr/bin/perl
###############################################################################
# Copyright (c) 2000-2020 Ericsson Telecom AB
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
# Prerequisites: ANTLR parser .g4 files must be compiled,
#   because generated java files must exist.
#
# Example usage:
#   cd <titan.EclipsePlug-ins project root>
#   Tools/antlr4_check_parsers.pl
###############################################################################

# to read the whole file at once, not just line-by-line
# http://www.kichwa.com/quik_ref/spec_variables.html
undef $/;

$fileindex = 0;

# Parses a generated ...Parser.java, and collects parser rule names
sub parseParser {
    # parser filename
    my $filename = shift;
    print("Source file: $filename\n");
    open(IN, $filename) || die("ERROR: Opening $filename failed");
    my $whole_file = <IN>;
    close(IN);
    # hash (associative array) of parser rules, where key is rule name, value is number of occurrence
    my %t;
    if ( $whole_file =~ /public static final String\[\] ruleNames = \{\r?\n\s*(.*?)\r?\n\s*\};/gs ) {
        my $rule_defs = $1; #comma separated constant definition: WS=1, LINE_COMMENT=2, ..., MACRO12=451
#        print "\$rule_defs == $rule_defs\n";
        my @list = split(/,\s*/, $rule_defs);
        foreach my $rule_def (@list) {
#            print "\$rule_def == \"$rule_def\"\n";
            if ( $rule_def =~ /^\"([A-Za-z][A-Za-z0-9_]*)\"$/ ) {
                my $rule_name = $1;
#                print "\$rule_name == \"$rule_name\"\n";
                $t{$rule_name}++;
            }
            else {
                print "ERROR: $rule_def does NOT match!\n";
            }
        }
    }
    else {
        print "ERROR: NO match: public static final int\n";
    }
    return \%t;
}

#----------
# MAIN PART

# processing files
my $filename1 = shift; # 1st parameter
my $filename2 = shift; # 2nd parameter
my %rules1 = %{ parseParser( $filename1 ) };
my %rules2 = %{ parseParser( $filename2 ) };

# comparing the result hashes, compared hashes are modified: common elements are removed

my $error = 0;
foreach my $key ( keys %rules1 ) {
    if ( exists $rules2{$key} && $rules1{$key} eq $rules2{$key} ) {
        delete $rules1{$key};
        delete $rules2{$key};
    }
}

if ( %rules1 ) {
    $error = 1;
    print"ERROR: There are parser rules, which exist only in $filename1:\n";
    my @keys = sort { $rules1{$a} <=> $rules1{$b} or "\L$a" cmp "\L$b" } keys %rules1;
    print "  $_\n" for ( @keys );
}

if ( %rules2 ) {
    $error = 1;
    print"ERROR: There are parser rules, which exist only in $filename2:\n";
    my @keys = sort { $rules2{$a} <=> $rules2{$b} or "\L$a" cmp "\L$b" } keys %rules2;
    print "  $_\n" for ( @keys );
}

if ( $error == 0 ) {
    print"$filename1 and $filename2 have the same parser rules, OK\n";
}

exit( $error );

