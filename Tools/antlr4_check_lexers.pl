#!/usr/bin/perl
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
# in the generated lexers.
# Lexers must contain the same amount of tokens to make sure, that they are synchronized properly.
# Token index of the same token must be the same in all of the lexers.
#
# Prerequisites: ANTLR lexer .g4 files must be compiled,
#   because generated java files must exist.
#
# Example usage:
#   cd <titan.EclipsePlug-ins project root>
#   Tools/antlr4_check_lexers.pl
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
    open(IN, $filename) || die("ERROR: Opening $filename failed");
    my $whole_file = <IN>;
    close(IN);
    # hash (associative array) of lexer tokens, where key is token name, value is token index
    my %t;
    if ( $whole_file =~ /public static final int\r?\n\s*(.*?);/gs ) {
        my $const_defs = $1; #comma separated constant definition: WS=1, LINE_COMMENT=2, ..., MACRO12=451
#        print "\$const_defs == $const_defs\n";
        my @list = split(/,\s*/, $const_defs);
        foreach my $const_def (@list) {
#            print "\$const_def == \"$const_def\"\n";
            if ( $const_def =~ /^([A-Za-z][A-Za-z0-9_]*)=([0-9]+)$/ ) {
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

#----------
# MAIN PART

# processing files
my $filename1 = shift; # 1st parameter
my $filename2 = shift; # 2nd parameter
my %tokens1 = %{ parseLexer( $filename1 ) };
my %tokens2 = %{ parseLexer( $filename2 ) };

# comparing the result hashes, compared hashes are modified: common elements are removed

my $error = 0;
foreach my $key ( keys %tokens1 ) {
    if ( exists $tokens2{$key} && $tokens1{$key} eq $tokens2{$key} ) {
        delete $tokens1{$key};
        delete $tokens2{$key};
    }
}

if ( %tokens1 ) {
    $error = 1;
    print"ERROR: There are token types, which exist only in $filename1:\n";
    my @keys = sort { $tokens1{$a} <=> $tokens1{$b} or "\L$a" cmp "\L$b" } keys %tokens1;
    print "  $_ $tokens1{$_}\n" for ( @keys );
}

if ( %tokens2 ) {
    $error = 1;
    print"ERROR: There are token types, which exist only in $filename2:\n";
    my @keys = sort { $tokens2{$a} <=> $tokens2{$b} or "\L$a" cmp "\L$b" } keys %tokens2;
    print "  $_ $tokens2{$_}\n" for ( @keys );
}

if ( $error == 0 ) {
    print"$filename1 and $filename2 have the same tokens, OK\n";
}

exit( $error );

