/*
 *******************************************************************************
 * Copyright (C) 1996-2000, International Business Machines Corporation and    *
 * others. All Rights Reserved.                                                *
 *******************************************************************************
 *
 * $Source: /xsrl/Nsvn/icu/icu4j/src/com/ibm/test/bnf/Attic/BigNumberFormatTest.java,v $ 
 * $Date: 2000/03/10 03:47:44 $ 
 * $Revision: 1.4 $
 *
 *****************************************************************************************
 */
package com.ibm.test.bnf;

import com.ibm.test.*;
import com.ibm.text.*;
import java.text.ParseException;
import java.util.*;
import java.math.BigInteger;

/**
 * @test
 * General test of Big NumberFormat
 */
public class BigNumberFormatTest extends TestFmwk {

    static final int ILLEGAL = -1;

    public static void main(String[] args) throws Exception {
        new BigNumberFormatTest().run(args);
    }
    	
    public void TestExponent() {
        DecimalFormatSymbols US = new DecimalFormatSymbols(Locale.US);
        DecimalFormat fmt1 = new DecimalFormat("0.###E0", US);
        DecimalFormat fmt2 = new DecimalFormat("0.###E+0", US);
        Number n = new Long(1234);
        expect(fmt1, n, "1.234E3");
        expect(fmt2, n, "1.234E+3");
        expect(fmt1, "1.234E3", n);
        expect(fmt1, "1.234E+3", n); // Either format should parse "E+3"
        expect(fmt2, "1.234E+3", n);
    }

    private void expectPad(DecimalFormat fmt, String pat, int pos) {
        expectPad(fmt, pat, pos, 0, (char)0);
    }

    private void expectPad(DecimalFormat fmt, String pat,
                           int pos, int width, char pad) {
        int apos = 0, awidth = 0;
        char apad = 0;
        try {
            fmt.applyPattern(pat);
            apos = fmt.getPadPosition();
            awidth = fmt.getFormatWidth();
            apad = fmt.getPadCharacter();
        } catch (IllegalArgumentException e) {
            apos = -1;
            awidth = width;
            apad = pad;
        }
        if (apos == pos && awidth == width && apad == pad) {
            logln("Ok   \"" + pat + "\" pos=" + apos +
                  ((pos == -1) ? "" : " width=" + awidth + " pad=" + apad));
        } else {
            logln("FAIL \"" + pat + "\" pos=" + apos +
                  " width=" + awidth + " pad=" + apad +
                  ", expected " + pos + " " + width + " " + pad);
        }
    }

    /**
     */
    public void TestPatterns() {
        DecimalFormatSymbols US = new DecimalFormatSymbols(Locale.US);
        DecimalFormat fmt = new DecimalFormat("#", US);

        expectPad(fmt, "*^#", DecimalFormat.PAD_BEFORE_PREFIX, 1, '^');
        expectPad(fmt, "$*^#", DecimalFormat.PAD_AFTER_PREFIX, 2, '^');
        expectPad(fmt, "#*^", DecimalFormat.PAD_BEFORE_SUFFIX, 1, '^');
        expectPad(fmt, "#$*^", DecimalFormat.PAD_AFTER_SUFFIX, 2, '^');
        expectPad(fmt, "$*^$#", ILLEGAL);
        expectPad(fmt, "#$*^$", ILLEGAL);
        expectPad(fmt, "'pre'#,##0*x'post'", DecimalFormat.PAD_BEFORE_SUFFIX,
                  12, 'x');
        expectPad(fmt, "''#0*x", DecimalFormat.PAD_BEFORE_SUFFIX,
                  3, 'x');
        expectPad(fmt, "'I''ll'*a###.##", DecimalFormat.PAD_AFTER_PREFIX,
                  10, 'a');

        fmt.applyPattern("AA#,##0.00ZZ");
        fmt.setPadCharacter('^');

        fmt.setFormatWidth(10);

        fmt.setPadPosition(DecimalFormat.PAD_BEFORE_PREFIX);
        expectPat(fmt, "*^AA#,##0.00ZZ");

        fmt.setPadPosition(DecimalFormat.PAD_BEFORE_SUFFIX);
        expectPat(fmt, "AA#,##0.00*^ZZ");

        fmt.setPadPosition(DecimalFormat.PAD_AFTER_SUFFIX);
        expectPat(fmt, "AA#,##0.00ZZ*^");

        //            12  3456789012
        String exp = "AA*^#,##0.00ZZ";
        fmt.setFormatWidth(12);
        fmt.setPadPosition(DecimalFormat.PAD_AFTER_PREFIX);
        expectPat(fmt, exp);

        fmt.setFormatWidth(13);
        //              12  34567890123
        expectPat(fmt, "AA*^##,##0.00ZZ");

        fmt.setFormatWidth(14);
        //              12  345678901234
        expectPat(fmt, "AA*^###,##0.00ZZ");

        fmt.setFormatWidth(15);
        //              12  3456789012345
        expectPat(fmt, "AA*^####,##0.00ZZ"); // This is the interesting case

        fmt.setFormatWidth(16);
        //              12  34567890123456
        expectPat(fmt, "AA*^#,###,##0.00ZZ");
    }

    private void expectPat(DecimalFormat fmt, String exp) {
        String pat = fmt.toPattern();
        if (pat.equals(exp)) {
            logln("Ok   \"" + pat + '"');
        } else {
            errln("FAIL \"" + pat + "\", expected \"" + exp + '"');
        }
    }

//This has been removed pending addition of com.ibm.math package to ICU4J
    /*
     * Test the handling of the AlphaWorks BigDecimal
     *
    public void TestAlphaBigDecimal() {
        DecimalFormatSymbols US = new DecimalFormatSymbols(Locale.US);
        expect(NumberFormat.getScientificInstance(Locale.US),
               new Number[] { new com.ibm.math.BigDecimal("12345.678901"),
                           },
               "1.234568E4");
        expect(new DecimalFormat("##0.####E0", US),
               new Number[] { new com.ibm.math.BigDecimal("12345.4999"),
                              new com.ibm.math.BigDecimal("12344.5001"),
                            },
               "12.345E3");
        expect(new DecimalFormat("##0.####E0", US),
               new Number[] { new com.ibm.math.BigDecimal("12345.5000"),
                              new com.ibm.math.BigDecimal("12346.5000"),
                            },
               "12.346E3");
    } */

    /**
     */
    public void TestScientific() {
        DecimalFormatSymbols US = new DecimalFormatSymbols(Locale.US);
        expect(NumberFormat.getScientificInstance(Locale.US),
               new Number[] { new Double(12345.678901),
                              new java.math.BigDecimal("12345.678901"),
                            },
               "1.234568E4");
        expect(new DecimalFormat("##0.###E0", US),
               new Double(12345),
               "12.34E3");
        expect(new DecimalFormat("##0.###E0", US),
               new Double(12345.00001),
               "12.35E3");
        expect(new DecimalFormat("##0.####E0", US),
               new Number[] { new Integer(12345),
                              new Long(12345),
                              new java.math.BigDecimal("12345.4999"),
                              new java.math.BigDecimal("12344.5001"),
                            },
               "12.345E3");
        expect(new DecimalFormat("##0.####E0", US),
               new Number[] { new java.math.BigDecimal("12345.5000"),
                              new java.math.BigDecimal("12346.5000"),
                            },
               "12.346E3");
        expect(NumberFormat.getScientificInstance(Locale.FRANCE),
               new Double(12345.678901),
               "1,234568E4");
        expect(new DecimalFormat("##0.####E0", US),
               new Double(789.12345e-9),
               "789.12E-9");
        expect(new DecimalFormat("##0.####E0", US),
               new Double(780.e-9),
               "780E-9");
        expect(new DecimalFormat(".###E0", US),
               new Double(45678),
               ".457E5");
        expect(new DecimalFormat(".###E0", US),
               new Long(0),
               ".0E0");
        expect(new DecimalFormat[] { new DecimalFormat("#E0", US),
                                     new DecimalFormat("##E0", US),
                                     new DecimalFormat("####E0", US),
                                     new DecimalFormat("0E0", US),    
                                     new DecimalFormat("00E0", US),   
                                     new DecimalFormat("000E0", US), 
                                   },
               new Long(45678000),
               new String[] { "4.5678E7",
                              "45.678E6",
                              "4567.8E4",
                              "5E7",
                              "46E6",  
                              "457E5",
                            }
               );
        expect(new DecimalFormat("###E0", US),
               new Object[] { new Double(0.0000123), "12.3E-6",
                              new Double(0.000123), "123E-6",
                              new java.math.BigDecimal("0.00123"), "1.23E-3", // Cafe VM messes up Double(0.00123)
                              new Double(0.0123), "12.3E-3",
                              new Double(0.123), "123E-3",
                              new Double(1.23), "1.23E0",
                              new Double(12.3), "12.3E0",
                              new Double(123), "123E0",
                              new Double(1230), "1.23E3",
                             });
        expect(new DecimalFormat("0.#E+00", US),
               new Object[] { new Double(0.00012), "1.2E-04",
                              new Long(12000),     "1.2E+04",
                             });
    }

    /**
     */
    public void TestPad() {
        DecimalFormatSymbols US = new DecimalFormatSymbols(Locale.US);
        expect(new DecimalFormat("*^##.##", US),
               new Object[] { new Long(0),      "^^^^0",
                              new Double(-1.3), "^-1.3",
                            }
               );
        expect(new DecimalFormat("##0.0####E0*_ g-m/s^2", US),
               new Object[] { new Long(0),       "0.0E0______ g-m/s^2",
                              new Double(1.0/3), "333.333E-3_ g-m/s^2",
                            }
               );
        expect(new DecimalFormat("##0.0####*_ g-m/s^2", US),
               new Object[] { new Long(0),       "0.0______ g-m/s^2",
                              new Double(1.0/3), "0.33333__ g-m/s^2",
                            }
               );
    }
    
    private void expect(NumberFormat fmt, Object[] data) {
        for (int i=0; i<data.length; i+=2) {
            expect(fmt, (Number) data[i], (String) data[i+1]);
        }
    }
    
    private void expect(Object fmto, Object numo, Object expo) {
        NumberFormat fmt = null, fmts[] = null;
        Number num = null, nums[] = null;
        String exp = null, exps[] = null;
        if (fmto instanceof NumberFormat[]) {
            fmts = (NumberFormat[]) fmto;
        } else {
            fmt = (NumberFormat) fmto;
        }
        if (numo instanceof Number[]) {
            nums = (Number[]) numo;
        } else {
            num = (Number) numo;
        }
        if (expo instanceof String[]) {
            exps = (String[]) expo;
        } else {
            exp = (String) expo;
        }
        int n = 1;
        if (fmts != null) {
            n = Math.max(n, fmts.length);
        }
        if (nums != null) {
            n = Math.max(n, nums.length);
        }
        if (exps != null) {
            n = Math.max(n, exps.length);
        }
        for (int i=0; i<n; ++i) {
            expect(fmts == null ? fmt : fmts[i],
                   nums == null ? num : nums[i],
                   exps == null ? exp : exps[i]);
        }
    }

    private static String showNumber(Number n) {
        String cls = n.getClass().getName();
        //This has been removed pending addition of com.ibm.math package to ICU4J
        if (!(/*n instanceof com.ibm.math.BigDecimal
              ||*/ n instanceof java.math.BigDecimal)) {
            int i = cls.lastIndexOf('.');
            cls = cls.substring(i+1);
        }
        return n.toString() + " (" + cls + ')';
    }

    private void expect(NumberFormat fmt, Number n, String exp) {
        String saw = fmt.format(n);
        String pat = ((DecimalFormat) fmt).toPattern();
        if (saw.equals(exp)) {
            logln("Ok   " + showNumber(n) + " x " +
                  pat + " = \"" +
                  saw + '"');
        } else {
            errln("FAIL " + showNumber(n) + " x " +
                  pat + " = \"" +
                  saw + "\", expected \"" + exp + '"');
        }
    }

    private void expect(NumberFormat fmt, String str, Number exp) {
        Number saw = null;
        try {
            saw = fmt.parse(str);
        } catch (ParseException e) {
            saw = null;
        }
        String pat = ((DecimalFormat) fmt).toPattern();
        if (saw.equals(exp)) {
            logln("Ok   \"" + str + "\" x " +
                  pat + " = " +
                  showNumber(saw));
        } else {
            errln("FAIL \"" + str + "\" x " +
                  pat + " = " +
                  showNumber(saw) + ", expected " + showNumber(exp));
        }
    }

    public void Test4161100() {
        NumberFormat f = NumberFormat.getInstance();
        f.setMinimumFractionDigits(1);
        f.setMaximumFractionDigits(1);
        double a = -0.09;
        String s = f.format(a);
        logln(a + " x " +
              ((DecimalFormat) f).toPattern() + " = " +
              s);
        if (!s.equals("-0.1")) {
            errln("FAIL");
        }
    }        
}
