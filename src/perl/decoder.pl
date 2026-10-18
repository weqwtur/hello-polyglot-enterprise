#!/usr/bin/perl
# Stage 4: reads "KEY <k>" and encoded codes, XORs them back and prints plain codes.
use strict;
use warnings;

sub read_input {
    my @lines = grep { /\S/ } <STDIN>;
    die "expected KEY line and data line\n" if @lines < 2;
    chomp @lines;
    my ($key) = $lines[0] =~ /^KEY (\d+)$/ or die "malformed KEY line: $lines[0]\n";
    my @values = split /\s+/, $lines[1];
    return ($key, @values);
}

sub decode_all {
    my ($key, @values) = @_;
    # Force numeric context: Perl XORs strings bytewise when both operands are strings.
    return map { (0 + $_) ^ (0 + $key) } @values;
}

sub main {
    my ($key, @values) = read_input();
    my @decoded = decode_all($key, @values);
    print join(' ', @decoded), "\n";
    print STDERR "[perl][INFO ] decoded " . scalar(@decoded) . " codes with key $key\n";
    return 0;
}

exit main();
