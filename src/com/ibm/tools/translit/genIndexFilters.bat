@rem = '--*-Perl-*--
@echo off
if "%OS%" == "Windows_NT" goto WinNT
perl -W -x -S "%0" %1 %2 %3 %4 %5 %6 %7 %8 %9
goto endofperl
:WinNT
perl -W -x -S "%0" %*
if NOT "%COMSPEC%" == "%SystemRoot%\system32\cmd.exe" goto endofperl
if %errorlevel% == 9009 echo You do not have Perl in your PATH.
goto endofperl
@rem ';
#!perl
#line 14

# This perl script updates the filters in the transliterator index file.
# It does so in a dumb way:
#
#  Latin-X   NFD lower
#  X-Latin   NFD
#
# For transliterators using NFKD, or not using Lower in this way, you
# will have to hand-edit the index file.
#
# This script writes a new index file.  The new file has to then be
# hand-edited and checked before use; it contains comments indicating
# old lines that were replaced.
#
# Alan Liu 11/29/01

use Getopt::Long;

my $DIR = "../../text/resources";
my $CLASSES = "../../../../../classes";

#GetOptions('dir=s' => \$DIR,
#           'id=s' => \$ID,
#           '<>' => \&usage) || die;

#usage() if (@ARGV);

#$ID =~ s/-/_/;
if (! -d $DIR) {
    print STDERR "$DIR is not a directory\n";
    usage();
}

#sub usage {
#    my $me = $0;
#    $me =~ s|.+[/\\]||;
#    print "Usage: $me [-dir <dir>] [-id <id>]\n";
#    print " --dir <dir> Specify the directory containing the\n";
#    print "             Transliterator_*.txt files\n";
#    print " --id <id>   Specify a single ID to transform, e.g.\n";
#    print "             Fullwidth-Halfwidth\n";
#    die;
#}

convertIndex();

######################################################################
# Convert the index file from Java to C format
sub convertIndex {
    $IN = "Transliterator_index.txt";
    $OUT = "$IN.new";
    open(IN, "$DIR/$IN") or die;
    open(OUT, ">$DIR/$OUT") or die;
    
    while (<IN>) {
        if (/^([^:]+):alias:\[.+?;(NF[^;]+)/) {
            my $id = $1;
            my $NFXD = $2;
            print STDERR "$id\n";
            my $lower = '';
            $lower = 'lower' if ($id =~ /^Latin-/i);
            my $set = getSourceSet($id, $NFXD, $lower);
            my $line = $_;
            $line =~ s/^([^:]+:alias:)\[.+?(;NF)/$1$set$2/;
            print OUT $line;
            $_ = "## $_";
        }
        print OUT;
    }

    close(IN);
    close(OUT);
    print STDERR "Wrote $DIR/$OUT\n";
}

######################################################################
# Get the source set (call out to Java), optionally with a closure.
sub getSourceSet {
    my $ID = shift;
    my $NFXD = shift;
    my $lower = shift;
    my $set = `java -classpath $CLASSES com.ibm.tools.translit.genIndexFilters $ID $NFXD $lower`;
    chomp($set);
    $set;
}

__END__
:endofperl
