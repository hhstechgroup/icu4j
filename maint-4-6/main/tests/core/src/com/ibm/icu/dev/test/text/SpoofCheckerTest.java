/*
 *******************************************************************************
 * Copyright (C) 2009-2011, International Business Machines Corporation and    *
 * others. All Rights Reserved.                                                *
 *******************************************************************************
 */
package com.ibm.icu.dev.test.text;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.Reader;
import java.text.ParseException;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.ibm.icu.dev.test.TestFmwk;
import com.ibm.icu.dev.test.TestUtil;
import com.ibm.icu.impl.Utility;
import com.ibm.icu.text.Normalizer2;
import com.ibm.icu.text.SpoofChecker;
import com.ibm.icu.text.UnicodeSet;
import com.ibm.icu.util.ULocale;

public class SpoofCheckerTest extends TestFmwk {

    public static void main(String[] args) throws Exception {
        new SpoofCheckerTest().run(args);
    }

    void TEST_ASSERT(boolean expr) {
        if ((expr) == false) {
            errln("Assertion Failure.\n");
        }
    }

    void TEST_ASSERT_EQ(int a, int b) {
        if (a != b) {
            errln(String.format("Test Failure: %d != %d\n", a, b));
        }
    }

    void TEST_ASSERT_NE(Object a, Object b) {
        if (a == b) {
            errln(String.format("Test Failure: (%s) == (%s) \n", a.toString(), b.toString()));
        }
    }

    /*
     * setup() and teardown() macros to handle the boilerplate around setting up test case. Put arbitrary test code
     * between SETUP and TEARDOWN. "sc" is the ready-to-go SpoofChecker for use in the tests.
     */
    SpoofChecker sc;
    SpoofChecker.Builder builder;

    void setup() {
        builder = new SpoofChecker.Builder();
        sc = builder.build();
    }

    void teardown() {
        sc = null;
    }

    /*
     * Identifiers for verifying that spoof checking is minimally alive and working.
     */
    char[] goodLatinChars = { (char) 0x75, (char) 0x7a };
    String goodLatin = new String(goodLatinChars); /* "uz", all ASCII */
    /* (not confusable) */
    char[] scMixedChars = { (char) 0x73, (char) 0x0441 };
    String scMixed = new String(scMixedChars); /* "sc", with Cyrillic 'c' */
    /* (mixed script, confusable */

    char[] scLatinChars = { (char) 0x73, (char) 0x63 };
    String scLatin = new String(scLatinChars); /* "sc", plain ascii. */
    char[] goodCyrlChars = { (char) 0x438, (char) 0x43B };
    String goodCyrl = new String(goodCyrlChars); /*
                                                  * Plain lower case Cyrillic letters, no latin confusables
                                                  */

    char[] goodGreekChars = { (char) 0x3c0, (char) 0x3c6 };
    String goodGreek = new String(goodGreekChars); /* Plain lower case Greek letters */

    char[] lll_Latin_aChars = { (char) 0x6c, (char) 0x49, (char) 0x31 };
    String lll_Latin_a = new String(lll_Latin_aChars); /* lI1, all ASCII */

    /* Full-width I, Small Roman Numeral fifty, Latin Cap Letter IOTA */
    char[] lll_Latin_bChars = { (char) 0xff29, (char) 0x217c, (char) 0x196 };
    String lll_Latin_b = new String(lll_Latin_bChars);

    char[] lll_CyrlChars = { (char) 0x0406, (char) 0x04C0, (char) 0x31 };
    String lll_Cyrl = new String(lll_CyrlChars);

    /* The skeleton transform for all of thes 'lll' lookalikes is all ascii lower case letter l. */
    char[] lll_SkelChars = { (char) 0x6c, (char) 0x6c, (char) 0x6c };
    String lll_Skel = new String(lll_SkelChars);

    /*
     * Test basic constructor.
     */
    public void TestUSpoof() {
        setup();
        teardown();
    }

    /*
     * Test build from source rules.
     */
    public void TestOpenFromSourceRules() {
        setup();
        String fileName;
        Reader confusables;
        Reader confusablesWholeScript;

        try {
            fileName = "unicode/confusables.txt";
            confusables = TestUtil.getDataReader(fileName, "UTF-8");
            fileName = "unicode/confusablesWholeScript.txt";
            confusablesWholeScript = TestUtil.getDataReader(fileName, "UTF-8");

            SpoofChecker rsc = builder.setData(confusables, confusablesWholeScript).build();
            if (rsc == null) {
                errln("FAIL: null SpoofChecker");
            }
        } catch (java.io.IOException e) {
            errln(e.toString());
        } catch (ParseException e) {
            errln(e.toString());
        }
        teardown();
    }

    /*
     * Set & Get Check Flags
     */
    public void TestGetSetChecks1() {
        setup();
        int t;
        sc = builder.setChecks(SpoofChecker.ALL_CHECKS).build();
        t = sc.getChecks();
        TEST_ASSERT_EQ(t, SpoofChecker.ALL_CHECKS);

        sc = builder.setChecks(0).build();
        t = sc.getChecks();
        TEST_ASSERT_EQ(0, t);

        int checks = SpoofChecker.WHOLE_SCRIPT_CONFUSABLE | SpoofChecker.MIXED_SCRIPT_CONFUSABLE
                | SpoofChecker.ANY_CASE;
        sc = builder.setChecks(checks).build();
        t = sc.getChecks();
        TEST_ASSERT_EQ(checks, t);
        teardown();
    }

    /*
     * get & setAllowedChars
     */
    public void TestGetSetAllowedChars() {
        setup();
        UnicodeSet us;
        UnicodeSet uset;

        uset = sc.getAllowedChars();
        TEST_ASSERT(uset.isFrozen());
        us = new UnicodeSet((int) 0x41, (int) 0x5A); /* [A-Z] */
        sc = builder.setAllowedChars(us).build();
        TEST_ASSERT_NE(us, sc.getAllowedChars());
        TEST_ASSERT(us.equals(sc.getAllowedChars()));
        teardown();
    }

    /*
     * get & set Checks
     */
    public void TestGetSetChecks() {
        setup();
        int checks;
        int checks2;
        boolean checkResults;

        checks = sc.getChecks();
        TEST_ASSERT_EQ(SpoofChecker.ALL_CHECKS, checks);

        checks &= ~(SpoofChecker.SINGLE_SCRIPT | SpoofChecker.MIXED_SCRIPT_CONFUSABLE);
        sc = builder.setChecks(checks).build();
        checks2 = sc.getChecks();
        TEST_ASSERT_EQ(checks, checks2);

        /*
         * The checks that were disabled just above are the same ones that the "scMixed" test fails. So with those tests
         * gone checking that Identifier should now succeed
         */
        checkResults = sc.check(scMixed);
        TEST_ASSERT(false == checkResults);
        teardown();
    }

    /*
     * AllowedLoacles
     */
    public void TestAllowedLoacles() {
        setup();
        Set<ULocale> allowedLocales = new LinkedHashSet<ULocale>();
        boolean checkResults;

        /* Default allowed locales list should be empty */
        allowedLocales = sc.getAllowedLocales();
        TEST_ASSERT(allowedLocales.isEmpty());

        /* Allow en and ru, which should enable Latin and Cyrillic only to pass */
        ULocale enloc = new ULocale("en");
        ULocale ruloc = new ULocale("ru_RU");
        allowedLocales.add(enloc);
        allowedLocales.add(ruloc);
        sc = builder.setAllowedLocales(allowedLocales).build();
        allowedLocales = sc.getAllowedLocales();
        TEST_ASSERT(allowedLocales.contains(enloc));
        TEST_ASSERT(allowedLocales.contains(ruloc));

        /*
         * Limit checks to SpoofChecker.CHAR_LIMIT. Some of the test data has whole script confusables also, which we
         * don't want to see in this test.
         */
        sc = builder.setChecks(SpoofChecker.CHAR_LIMIT).build();

        SpoofChecker.CheckResult result = new SpoofChecker.CheckResult();
        checkResults = sc.check(goodLatin);
        TEST_ASSERT(false == checkResults);

        checkResults = sc.check(goodGreek, result);
        TEST_ASSERT_EQ(SpoofChecker.CHAR_LIMIT, result.checks);

        checkResults = sc.check(goodCyrl);
        TEST_ASSERT(false == checkResults);

        /* Reset with an empty locale list, which should allow all characters to pass */
        allowedLocales = new LinkedHashSet<ULocale>();
        sc = builder.setAllowedLocales(allowedLocales).build();

        checkResults = sc.check(goodGreek);
        TEST_ASSERT(false == checkResults);
        teardown();
    }

    /*
     * AllowedChars set/get the UnicodeSet of allowed characters.
     */
    public void TestAllowedChars() {
        setup();
        UnicodeSet set;
        UnicodeSet tmpSet;
        boolean checkResults;

        /* By default, we should see no restriction; the UnicodeSet should allow all characters. */
        set = sc.getAllowedChars();
        tmpSet = new UnicodeSet(0, 0x10ffff);
        TEST_ASSERT(tmpSet.equals(set));

        /* Setting the allowed chars should enable the check. */
        sc = builder.setChecks(SpoofChecker.ALL_CHECKS & ~SpoofChecker.CHAR_LIMIT).build();

        /* Remove a character that is in our good Latin test identifier from the allowed chars set. */
        tmpSet.remove(goodLatin.charAt(1));
        sc = builder.setAllowedChars(tmpSet).build();

        /* Latin Identifier should now fail; other non-latin test cases should still be OK */
        SpoofChecker.CheckResult result = new SpoofChecker.CheckResult();
        checkResults = sc.check(goodLatin, result);
        TEST_ASSERT(checkResults);
        TEST_ASSERT_EQ(SpoofChecker.CHAR_LIMIT, result.checks);

        checkResults = sc.check(goodGreek, result);
        TEST_ASSERT(checkResults);
        TEST_ASSERT_EQ(SpoofChecker.WHOLE_SCRIPT_CONFUSABLE, result.checks);
        teardown();
    }

    public void TestCheck() {
        setup();
        SpoofChecker.CheckResult result = new SpoofChecker.CheckResult();
        boolean checkResults;

        result.position = 666;
        checkResults = sc.check(goodLatin, result);
        TEST_ASSERT(false == checkResults);
        TEST_ASSERT_EQ(666, result.position);

        checkResults = sc.check(goodCyrl, result);
        TEST_ASSERT(false == checkResults);

        result.position = 666;
        checkResults = sc.check(scMixed, result);
        TEST_ASSERT(true == checkResults);
        TEST_ASSERT_EQ(SpoofChecker.MIXED_SCRIPT_CONFUSABLE | SpoofChecker.SINGLE_SCRIPT, result.checks);
        TEST_ASSERT_EQ(2, result.position);
        teardown();
    }

    public void TestAreConfusable1() {
        setup();
        int checkResults;
        checkResults = sc.areConfusable(scLatin, scMixed);
        TEST_ASSERT_EQ(SpoofChecker.MIXED_SCRIPT_CONFUSABLE, checkResults);

        checkResults = sc.areConfusable(goodGreek, scLatin);
        TEST_ASSERT_EQ(0, checkResults);

        checkResults = sc.areConfusable(lll_Latin_a, lll_Latin_b);
        TEST_ASSERT_EQ(SpoofChecker.SINGLE_SCRIPT_CONFUSABLE, checkResults);
        teardown();
    }

    public void TestGetSkeleton() {
        setup();
        String dest;
        dest = sc.getSkeleton(SpoofChecker.ANY_CASE, lll_Latin_a);
        TEST_ASSERT(lll_Skel.equals(dest));
        TEST_ASSERT_EQ(lll_Skel.length(), dest.length());
        TEST_ASSERT_EQ(3, dest.length());
        teardown();
    }

    /**
     * IntlTestSpoof is the top level test class for the Unicode Spoof detection tests
     */

    // Test the USpoofDetector API functions that require C++
    // The pure C part of the API, which is most of it, is tested in cintltst
    /**
     * IntlTestSpoof tests for USpoofDetector
     */
    public void TestSpoofAPI() {

        setup();
        String s = "xyz";  // Many latin ranges are whole-script confusable with other scripts.
                           // If this test starts failing, consult confusablesWholeScript.txt
        SpoofChecker.CheckResult result = new SpoofChecker.CheckResult();
        result.position = 666;
        boolean checkResults = sc.check(s, result);
        TEST_ASSERT(false == checkResults);
        TEST_ASSERT_EQ(666, result.position); // not changed
        teardown();

        setup();
        String s1 = "cxs";
        String s2 = Utility.unescape("\\u0441\\u0445\\u0455"); // Cyrillic "cxs"
        int checkResult = sc.areConfusable(s1, s2);
        TEST_ASSERT_EQ(SpoofChecker.MIXED_SCRIPT_CONFUSABLE | SpoofChecker.WHOLE_SCRIPT_CONFUSABLE, checkResult);
        teardown();

        setup();
        s = "I1l0O";
        String dest = sc.getSkeleton(SpoofChecker.ANY_CASE, s);
        TEST_ASSERT(dest.equals("lllOO"));
        teardown();
    }

    // testSkeleton. Spot check a number of confusable skeleton substitutions from the
    // Unicode data file confusables.txt
    // Test cases chosen for substitutions of various lengths, and
    // membership in different mapping tables.
    public void TestSkeleton() {
        int ML = 0;
        int SL = SpoofChecker.SINGLE_SCRIPT_CONFUSABLE;
        int MA = SpoofChecker.ANY_CASE;
        int SA = SpoofChecker.SINGLE_SCRIPT_CONFUSABLE | SpoofChecker.ANY_CASE;

        setup();
        // A long "identifier" that will overflow implementation stack buffers, forcing heap allocations.
        //    (in the C implementation)
        checkSkeleton(
                sc,
                SL,
                " A 1ong \\u02b9identifier' that will overflow implementation stack buffers, forcing heap allocations."
                        + " A 1ong 'identifier' that will overflow implementation stack buffers, forcing heap allocations."
                        + " A 1ong 'identifier' that will overflow implementation stack buffers, forcing heap allocations."
                        + " A 1ong 'identifier' that will overflow implementation stack buffers, forcing heap allocations.",
                " A long 'identifier' that vvill overflovv irnplernentation stack buffers, forcing heap allocations."
                        + " A long 'identifier' that vvill overflovv irnplernentation stack buffers, forcing heap allocations."
                        + " A long 'identifier' that vvill overflovv irnplernentation stack buffers, forcing heap allocations."
                        + " A long 'identifier' that vvill overflovv irnplernentation stack buffers, forcing heap allocations.");

        checkSkeleton(sc, SL, "nochange", "nochange");
        checkSkeleton(sc, MA, "love", "love");
        checkSkeleton(sc, MA, "1ove", "love");   // Digit 1 to letter l
        checkSkeleton(sc, ML, "OOPS", "OOPS");
        checkSkeleton(sc, ML, "00PS", "00PS");   // Digit 0 unchanged in lower case mode.
        checkSkeleton(sc, MA, "OOPS", "OOPS");
        checkSkeleton(sc, MA, "00PS", "OOPS");   // Digit 0 to letter O in any case mode only
        checkSkeleton(sc, SL, "\\u059c", "\\u0301");
        checkSkeleton(sc, SL, "\\u2A74", "\\u003A\\u003A\\u003D");
        checkSkeleton(sc, SL, "\\u247E", "\\u0028\\u006c\\u006c\\u0029");  // "(ll)"
        checkSkeleton(sc, SL, "\\uFDFB", "\\u062C\\u0644\\u0020\\u062C\\u0644\\u0627\\u0644\\u0647");

        // This mapping exists in the ML and MA tables, does not exist in SL, SA
        // 0C83 ; 0983 ; ML #  KANNADA SIGN VISARGA to 
        checkSkeleton(sc, SL, "\\u0C83", "\\u0C83");
        checkSkeleton(sc, SA, "\\u0C83", "\\u0C83");
        checkSkeleton(sc, ML, "\\u0C83", "\\u0983");
        checkSkeleton(sc, MA, "\\u0C83", "\\u0983");

        // 0391 ; 0041 ; MA # GREEK CAPITAL LETTER ALPHA to LATIN CAPITAL LETTER A
        // This mapping exists only in the MA table.
        checkSkeleton(sc, MA, "\\u0391", "A");
        checkSkeleton(sc, SA, "\\u0391", "\\u0391");
        checkSkeleton(sc, ML, "\\u0391", "\\u0391");
        checkSkeleton(sc, SL, "\\u0391", "\\u0391");

        // 13CF ; 0062 ; MA # CHEROKEE LETTER SI to LATIN SMALL LETTER B
        // This mapping exists in the ML and MA tables
        checkSkeleton(sc, ML, "\\u13CF", "b");
        checkSkeleton(sc, MA, "\\u13CF", "b");
        checkSkeleton(sc, SL, "\\u13CF", "\\u13CF");
        checkSkeleton(sc, SA, "\\u13CF", "\\u13CF");

        // 0022 ; 0027 0027 ; 
        // all tables
        checkSkeleton(sc, SL, "\"", "\\u0027\\u0027");
        checkSkeleton(sc, SA, "\"", "\\u0027\\u0027");
        checkSkeleton(sc, ML, "\"", "\\u0027\\u0027");
        checkSkeleton(sc, MA, "\"", "\\u0027\\u0027");

        teardown();
    }

    // Internal function to run a single skeleton test case.
    //
    // Run a single confusable skeleton transformation test case.
    //
    void checkSkeleton(SpoofChecker sc, int type, String input, String expected) {
        String uInput = Utility.unescape(input);
        String uExpected = Utility.unescape(expected);
        String actual;
        actual = sc.getSkeleton(type, uInput);
        if (!uExpected.equals(actual)) {
            errln("Actual and Expected skeletons differ.");
            errln((" Actual   Skeleton: \"") + actual + ("\"\n") + (" Expected Skeleton: \"") + uExpected + ("\""));
        }
    }

    public void TestAreConfusable() {
        setup();
        String s1 = "A long string that will overflow stack buffers.  A long string that will overflow stack buffers. "
                + "A long string that will overflow stack buffers.  A long string that will overflow stack buffers. ";
        String s2 = "A long string that wi11 overflow stack buffers.  A long string that will overflow stack buffers. "
                + "A long string that wi11 overflow stack buffers.  A long string that will overflow stack buffers. ";
        TEST_ASSERT_EQ(SpoofChecker.SINGLE_SCRIPT_CONFUSABLE, sc.areConfusable(s1, s2));
        teardown();
    }

    public void TestInvisible() {
        setup();
        String s = Utility.unescape("abcd\\u0301ef");
        SpoofChecker.CheckResult result = new SpoofChecker.CheckResult();
        result.position = -42;
        TEST_ASSERT(false == sc.check(s, result));
        TEST_ASSERT_EQ(0, result.checks);
        TEST_ASSERT(result.position == -42); // unchanged

        String s2 = Utility.unescape("abcd\\u0301\\u0302\\u0301ef");
        TEST_ASSERT(true == sc.check(s2, result));
        TEST_ASSERT_EQ(SpoofChecker.INVISIBLE, result.checks);
        TEST_ASSERT_EQ(7, result.position);

        // Two acute accents, one from the composed a with acute accent, \u00e1,
        // and one separate.
        result.position = -42;
        String s3 = Utility.unescape("abcd\\u00e1\\u0301xyz");
        TEST_ASSERT(true == sc.check(s3, result));
        TEST_ASSERT_EQ(SpoofChecker.INVISIBLE, result.checks);
        TEST_ASSERT_EQ(7, result.position);
        teardown();
    }

    private String parseHex(String in) {
        StringBuilder sb = new StringBuilder();
        for (String oneCharAsHexString : in.split("\\s+")) {
            if (oneCharAsHexString.length() > 0) {
                sb.appendCodePoint(Integer.parseInt(oneCharAsHexString, 16));
            }
        }
        return sb.toString();
    }

    private String escapeString(String in) {
        StringBuilder out = new StringBuilder();
        for (int i = 0; i < in.length(); i++) {
            int c = in.codePointAt(i);
            if (c <= 0x7f) {
                out.append((char) c);
            } else if (c <= 0xffff) {
                out.append(String.format("\\u%04x", c));
            } else {
                out.append(String.format("\\U%06x", c));
                i++;
            }
        }
        return out.toString();
    }

    // Verify that each item from the Unicode confusables.txt file
    // transforms into the expected skeleton.
    public void testConfData() {
        try {
            // Read in the confusables.txt file. (Distributed by Unicode.org)
            String fileName = "unicode/confusables.txt";
            BufferedReader confusablesRdr = TestUtil.getDataReader(fileName, "UTF-8");

            // Create a default spoof checker to use in this test.
            SpoofChecker sc = new SpoofChecker.Builder().build();

            // Parse lines from the confusables.txt file. Example Line:
            // FF44 ; 0064 ; SL # ( d -> d ) FULLWIDTH ....
            // Lines have three fields. The hex fields can contain more than one character,
            // and each character may be more than 4 digits (for supplemntals)
            // This regular expression matches lines and splits the fields into capture groups.
            // Capture group 1: map from chars
            // 2: map to chars
            // 3: table type, SL, ML, SA or MA
            // 4: Comment Lines Only
            // 5: Error Lines Only
            Matcher parseLine = Pattern.compile(
                    "\\ufeff?" + "(?:([0-9A-F\\s]+);([0-9A-F\\s]+);\\s*(SL|ML|SA|MA)\\s*(?:#.*?)?$)"
                            + "|\\ufeff?(\\s*(?:#.*)?)"). // Comment line
                    matcher("");
            Normalizer2 normalizer = Normalizer2.getInstance(null, "nfc", Normalizer2.Mode.DECOMPOSE);
            int lineNum = 0;
            String inputLine;
            while ((inputLine = confusablesRdr.readLine()) != null) {
                lineNum++;
                parseLine.reset(inputLine);
                if (!parseLine.matches()) {
                    errln("Syntax error in confusable data file at line " + lineNum);
                    errln(inputLine);
                    break;
                }
                if (parseLine.group(4) != null) {
                    continue; // comment line
                }
                String from = parseHex(parseLine.group(1));

                if (!normalizer.isNormalized(from)) {
                    // The source character was not NFD.
                    // Skip this case; the first step in obtaining a skeleton is to NFD the input,
                    // so the mapping in this line of confusables.txt will never be applied.
                    continue;
                }

                String rawExpected = parseHex(parseLine.group(2));
                String expected = normalizer.normalize(rawExpected);

                int skeletonType = 0;
                String tableType = parseLine.group(3);
                if (tableType.equals("SL")) {
                    skeletonType = SpoofChecker.SINGLE_SCRIPT_CONFUSABLE;
                } else if (tableType.indexOf("SA") >= 0) {
                    skeletonType = SpoofChecker.SINGLE_SCRIPT_CONFUSABLE | SpoofChecker.ANY_CASE;
                } else if (tableType.indexOf("ML") >= 0) {
                    skeletonType = 0;
                } else if (tableType.indexOf("MA") >= 0) {
                    skeletonType = SpoofChecker.ANY_CASE;
                }

                String actual;
                actual = sc.getSkeleton(skeletonType, from);

                if (!actual.equals(expected)) {
                    errln("confusables.txt: " + lineNum + ": " + parseLine.group(0));
                    errln("Actual: " + escapeString(actual));
                }
            }
            confusablesRdr.close();
        } catch (IOException e) {
            errln(e.toString());
        }
    }
}
