//##header
/*
 *******************************************************************************
 * Copyright (C) 1996-2007, International Business Machines Corporation and    *
 * others. All Rights Reserved.                                                *
 *******************************************************************************
 *
 */

package com.ibm.icu.dev.test.serializable;

import java.util.Date;
import java.util.HashMap;
import java.util.Locale;

import com.ibm.icu.impl.DateNumberFormat;
import com.ibm.icu.text.ChineseDateFormat;
import com.ibm.icu.text.ChineseDateFormatSymbols;
import com.ibm.icu.text.DateFormat;
import com.ibm.icu.text.DateFormatSymbols;
import com.ibm.icu.text.DecimalFormat;
import com.ibm.icu.text.DecimalFormatSymbols;
import com.ibm.icu.text.MessageFormat;
import com.ibm.icu.text.NumberFormat;
import com.ibm.icu.text.RuleBasedNumberFormat;
import com.ibm.icu.text.SimpleDateFormat;
import com.ibm.icu.util.GregorianCalendar;
import com.ibm.icu.util.ULocale;

/**
 * @author emader
 *
 * TODO To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
public class FormatTests
{

    public static class NumberFormatHandler implements SerializableTest.Handler
    {
        public Object[] getTestObjects()
        {
            NumberFormat formats[] = {
                NumberFormat.getInstance(Locale.US),
                NumberFormat.getCurrencyInstance(Locale.US),
                NumberFormat.getPercentInstance(Locale.US),
                NumberFormat.getScientificInstance(Locale.US)
               
            };
            
            return formats;
        }

        public boolean hasSameBehavior(Object a, Object b)
        {
            NumberFormat format_a = (NumberFormat) a;
            NumberFormat format_b = (NumberFormat) b;
            double number = 1234.56;
            
            return format_a.format(number).equals(format_b.format(number));
        }
    }
    
    public static class DecimalFormatHandler extends NumberFormatHandler
    {
        public Object[] getTestObjects()
        {
            Locale locales[] = SerializableTest.getLocales();
            DecimalFormat formats[] = new DecimalFormat[locales.length];
            
            for (int i = 0; i < locales.length; i += 1) {
                formats[i] = new DecimalFormat("#,##0.###", new DecimalFormatSymbols(locales[i]));
            }
            
            return formats;
        }
    }
    
    public static class RuleBasedNumberFormatHandler extends NumberFormatHandler
    {
        // default rules, from root.txt
        String xx_SpelloutRules = "=#,##0.######=;\n";
        String xx_OrdinalRules = "=#,##0=;\n";
        String xx_DurationRules = "=#,##0=;\n";
        
        String ja_spelloutRules = 
            "%financial:\n" +
                "\u96f6; \u58f1; \u5f10; \u53c2; \u56db; \u4f0d; \u516d; \u4e03; \u516b; \u4e5d;\n" +
                "\u62fe[>>];\n" +
                "20: <<\u62fe[>>];\n" +
                "100: <<\u767e[>>];\n" +
                "1000: <<\u5343[>>];\n" +
                "10,000: <<\u4e07[>>];\n" +
                "100,000,000: <<\u5104[>>];\n" +
                "1,000,000,000,000: <<\u5146[>>];\n" +
                "10,000,000,000,000,000: =#,##0=;\n" +
                
            "%traditional:\n" +
                "\u96f6; \u4e00; \u4e8c; \u4e09; \u56db; \u4e94; \u516d; \u4e03; \u516b; \u4e5d;\n" +
                "\u5341[>>];\n" +
                "20: <<\u5341[>>];\n" +
                "100: <<\u767e[>>];\n" +
                "1000: <<\u5343[>>];\n" +
                "10,000: <<\u4e07[>>];\n" +
                "100,000,000: <<\u5104[>>];\n" +
                "1,000,000,000,000: <<\u5146[>>];\n" +
                "10,000,000,000,000,000: =#,##0=;";
        
        String en_SpelloutRules = 
            // This rule set shows the normal simple formatting rules for English
            "%simplified:\n" +
                   // negative number rule.  This rule is used to format negative
                   // numbers.  The result of formatting the number's absolute
                   // value is placed where the >> is.
                "-x: minus >>;\n" +
                   // faction rule.  This rule is used for formatting numbers
                   // with fractional parts.  The result of formatting the
                   // number's integral part is substituted for the <<, and
                   // the result of formatting the number's fractional part
                   // (one digit at a time, e.g., 0.123 is "zero point one two
                   // three") replaces the >>.
                "x.x: << point >>;\n" +
                   // the rules for the values from 0 to 19 are simply the
                   // words for those numbers
                "zero; one; two; three; four; five; six; seven; eight; nine;\n" +
                "ten; eleven; twelve; thirteen; fourteen; fifteen; sixteen;\n" +
                    "seventeen; eighteen; nineteen;\n" +
                   // beginning at 20, we use the >> to mark the position where
                   // the result of formatting the number's ones digit.  Thus,
                   // we only need a new rule at every multiple of 10.  Text in
                   // backets is omitted if the value being formatted is an
                   // even multiple of 10.
                "20: twenty[->>];\n" +
                "30: thirty[->>];\n" +
                "40: forty[->>];\n" +
                "50: fifty[->>];\n" +
                "60: sixty[->>];\n" +
                "70: seventy[->>];\n" +
                "80: eighty[->>];\n" +
                "90: ninety[->>];\n" +
                   // beginning at 100, we can use << to mark the position where
                   // the result of formatting the multiple of 100 is to be
                   // inserted.  Notice also that the meaning of >> has shifted:
                   // here, it refers to both the ones place and the tens place.
                   // The meanings of the << and >> tokens depend on the base value
                   // of the rule.  A rule's divisor is (usually) the highest
                   // power of 10 that is less than or equal to the rule's base
                   // value.  The value being formatted is divided by the rule's
                   // divisor, and the integral quotient is used to get the text
                   // for <<, while the remainder is used to produce the text
                   // for >>.  Again, text in brackets is omitted if the value
                   // being formatted is an even multiple of the rule's divisor
                   // (in this case, an even multiple of 100)
                "100: << hundred[ >>];\n" +
                   // The rules for the higher numbers work the same way as the
                   // rule for 100: Again, the << and >> tokens depend on the
                   // rule's divisor, which for all these rules is also the rule's
                   // base value.  To group by thousand, we simply don't have any
                   // rules between 1,000 and 1,000,000.
                "1000: << thousand[ >>];\n" +
                "1,000,000: << million[ >>];\n" +
                "1,000,000,000: << billion[ >>];\n" +
                "1,000,000,000,000: << trillion[ >>];\n" +
                   // overflow rule.  This rule specifies that values of a
                   // quadrillion or more are shown in numerals rather than words.
                   // The == token means to format (with new rules) the value
                   // being formatted by this rule and place the result where
                   // the == is.  The #,##0 inside the == signs is a
                   // DecimalFormat pattern.  It specifies that the value should
                   // be formatted with a DecimalFormat object, and that it
                   // should be formatted with no decimal places, at least one
                   // digit, and a thousands separator.
                "1,000,000,000,000,000: =#,##0=;\n" +

            // %default is a more elaborate form of %simplified;  It is basically
            // the same, except that it introduces "and" before the ones digit
            // when appropriate (basically, between the tens and ones digits) and
            // separates the thousands groups with commas in values over 100,000.
            "%default:\n" +
                   // negative-number and fraction rules.  These are the same
                   // as those for %simplified, but have to be stated here too
                   // because this is an entry point
                "-x: minus >>;\n" +
                "x.x: << point >>;\n" +
                   // just use %simplified for values below 100
                "=%simplified=;\n" +
                   // for values from 100 to 9,999 use %%and to decide whether or
                   // not to interpose the "and"
                "100: << hundred[ >%%and>];\n" +
                "1000: << thousand[ >%%and>];\n" +
                   // for values of 100,000 and up, use %%commas to interpose the
                   // commas in the right places (and also to interpose the "and")
                "100,000>>: << thousand[>%%commas>];\n" +
                "1,000,000: << million[>%%commas>];\n" +
                "1,000,000,000: << billion[>%%commas>];\n" +
                "1,000,000,000,000: << trillion[>%%commas>];\n" +
                "1,000,000,000,000,000: =#,##0=;\n" +
            // if the value passed to this rule set is greater than 100, don't
            // add the "and"; if it's less than 100, add "and" before the last
            // digits
            "%%and:\n" +
                "and =%default=;\n" +
                "100: =%default=;\n" +
            // this rule set is used to place the commas
            "%%commas:\n" +
                   // for values below 100, add "and" (the apostrophe at the
                   // beginning is ignored, but causes the space that follows it
                   // to be significant: this is necessary because the rules
                   // calling %%commas don't put a space before it)
                "' and =%default=;\n" +
                   // put a comma after the thousands (or whatever preceded the
                   // hundreds)
                "100: , =%default=;\n" +
                   // put a comma after the millions (or whatever precedes the
                   // thousands)
                "1000: , <%default< thousand, >%default>;\n" +
                   // and so on...
                "1,000,000: , =%default=;" +
            // %%lenient-parse isn't really a set of number formatting rules;
            // it's a set of collation rules.  Lenient-parse mode uses a Collator
            // object to compare fragments of the text being parsed to the text
            // in the rules, allowing more leeway in the matching text.  This set
            // of rules tells the formatter to ignore commas when parsing (it
            // already ignores spaces, which is why we refer to the space; it also
            // ignores hyphens, making "twenty one" and "twenty-one" parse
            // identically)
            "%%lenient-parse:\n" +
            //                "& ' ' , ',' ;\n" +
            "   &\u0000 << ' ' << ',' << '-'; \n";

        String en_GB_SpelloutRules =
            "%simplified:\n" +
            "-x: minus >>;\n" +
            "x.x: << point >>;\n" +
            "zero; one; two; three; four; five; six; seven; eight; nine;\n" +
            "ten; eleven; twelve; thirteen; fourteen; fifteen; sixteen;\n" +
            "    seventeen; eighteen; nineteen;\n" +
            "20: twenty[->>];\n" +
            "30: thirty[->>];\n" +
            "40: forty[->>];\n" +
            "50: fifty[->>];\n" +
            "60: sixty[->>];\n" +
            "70: seventy[->>];\n" +
            "80: eighty[->>];\n" +
            "90: ninety[->>];\n" +
            "100: << hundred[ >>];\n" +
            "1000: << thousand[ >>];\n" +
            "1,000,000: << million[ >>];\n" +
            "1,000,000,000,000: << billion[ >>];\n" +
            "1,000,000,000,000,000: =#,##0=;\n" +
        "%default:\n" +
            "-x: minus >>;\n" +
            "x.x: << point >>;\n" +
            "=%simplified=;\n" +
            "100: << hundred[ >%%and>];\n" +
            "1000: << thousand[ >%%and>];\n" +
            "100,000>>: << thousand[>%%commas>];\n" +
            "1,000,000: << million[>%%commas>];\n" +
            "1,000,000,000,000: << billion[>%%commas>];\n" +
            "1,000,000,000,000,000: =#,##0=;\n" +
        "%%and:\n" +
            "and =%default=;\n" +
            "100: =%default=;\n" +
        "%%commas:\n" +
            "' and =%default=;\n" +
            "100: , =%default=;\n" +
            "1000: , <%default< thousand, >%default>;\n" +
            "1,000,000: , =%default=;" +
        "%%lenient-parse:\n" +
            "& ' ' , ',' ;\n";
        
        String fr_SpelloutRules =
            // the main rule set
            "%main:\n" +
                "-x: moins >>;\n" +
                "x.x: << virgule >>;\n" +
                   // words for numbers from 0 to 10
                "z\u00e9ro; un; deux; trois; quatre; cinq; six; sept; huit; neuf;\n" +
                "dix; onze; douze; treize; quatorze; quinze; seize;\n" +
                "    dix-sept; dix-huit; dix-neuf;\n" +
                   // ords for the multiples of 10: %%alt-ones inserts "et"
                   // when needed
                "20: vingt[->%%alt-ones>];\n" +
                "30: trente[->%%alt-ones>];\n" +
                "40: quarante[->%%alt-ones>];\n" +
                "50: cinquante[->%%alt-ones>];\n" +
                   // rule for 60.  The /20 causes this rule's multiplier to be
                   // 20 rather than 10, allowinhg us to recurse for all values
                   // from 60 to 79...
                "60/20: soixante[->%%alt-ones>];\n" +
                   // ...except for 71, which must be special-cased
                "71: soixante et onze;\n" +
                   // at 72, we have to repeat the rule for 60 to get us to 79
                "72/20: soixante->%%alt-ones>;\n" +
                   // at 80, we state a new rule with the phrase for 80.  Since
                   // it changes form when there's a ones digit, we need a second
                   // rule at 81.  This rule also includes "/20," allowing it to
                   // be used correctly for all values up to 99
                "80: quatre-vingts; 81/20: quatre-vingt->>;\n" +
                   // "cent" becomes plural when preceded by a multiplier, and
                   // the multiplier is omitted from the singular form
                "100: cent[ >>];\n" +
                "200: << cents[ >>];\n" +
                "1000: mille[ >>];\n" +
                   // values from 1,100 to 1,199 are rendered as "onze cents..."
                   // instead of "mille cent..."  The > after "1000" decreases
                   // the rule's exponent, causing its multiplier to be 100 instead
                   // of 1,000.  This prevents us from getting "onze cents cent
                   // vingt-deux" ("eleven hundred one hundred twenty-two").
                "1100>: onze cents[ >>];\n" +
                   // at 1,200, we go back to formating in thousands, so we
                   // repeat the rule for 1,000
                "1200: mille >>;\n" +
                   // at 2,000, the multiplier is added
                "2000: << mille[ >>];\n" +
                "1,000,000: << million[ >>];\n" +
                "1,000,000,000: << milliard[ >>];\n" +
                "1,000,000,000,000: << billion[ >>];\n" +
                "1,000,000,000,000,000: =#,##0=;\n" +
            // %%alt-ones is used to insert "et" when the ones digit is 1
            "%%alt-ones:\n" +
                "; et-un; =%main=;\n" + 
            "%%lenient-parse:\n" +
                "&\u0000 << ' ' << ',' << '-';\n";
        
        String de_SpelloutRules =
            // 1 is "eins" when by itself, but turns into "ein" in most
            // combinations
            "%alt-ones:\n" +
                "-x: minus >>;\n" +
                "x.x: << komma >>;\n" +
                "null; eins; =%%main=;\n" +
            "%%main:\n" +
                   // words for numbers from 0 to 12.  Notice that the values
                   // from 13 to 19 can derived algorithmically, unlike in most
                   // other languages
                "null; ein; zwei; drei; vier; f\u00fcnf; sechs; sieben; acht; neun;\n" +
                "zehn; elf; zw\u00f6lf; >>zehn;\n" +
                   // rules for the multiples of 10.  Notice that the ones digit
                   // goes on the front
                "20: [>>und]zwanzig;\n" +
                "30: [>>und]drei\u00dfig;\n" +
                "40: [>>und]vierzig;\n" +
                "50: [>>und]f\u00fcnfzig;\n" +
                "60: [>>und]sechzig;\n" +
                "70: [>>und]siebzig;\n" +
                "80: [>>und]achtzig;\n" +
                "90: [>>und]neunzig;\n" +
                "100: hundert[>%alt-ones>];\n" +
                "200: <<hundert[>%alt-ones>];\n" +
                "1000: tausend[>%alt-ones>];\n" +
                "2000: <<tausend[>%alt-ones>];\n" +
                "1,000,000: eine Million[ >%alt-ones>];\n" +
                "2,000,000: << Millionen[ >%alt-ones>];\n" +
                "1,000,000,000: eine Milliarde[ >%alt-ones>];\n" +
                "2,000,000,000: << Milliarden[ >%alt-ones>];\n" +
                "1,000,000,000,000: eine Billion[ >%alt-ones>];\n" +
                "2,000,000,000,000: << Billionen[ >%alt-ones>];\n" +
                "1,000,000,000,000,000: =#,##0=;" +
            "%%lenient-parse:\n" +
                "&\u0000 << ' ' << '-'\n" +
                "& ae , \u00e4 & ae , \u00c4\n" +
                "& oe , \u00f6 & oe , \u00d6\n" +
                "& ue , \u00fc & ue , \u00dc\n";
        
        String it_SpelloutRules =
            // main rule set.  Follows the patterns of the preceding rule sets,
            // except that the final vowel is omitted from words ending in
            // vowels when they are followed by another word; instead, we have
            // separate rule sets that are identical to this one, except that
            // all the words that don't begin with a vowel have a vowel tacked
            // onto them at the front.  A word ending in a vowel calls a
            // substitution that will supply that vowel, unless that vowel is to
            // be elided.
            "%main:\n" +
                "-x: meno >>;\n" +
                "x.x: << virgola >>;\n" +
                "zero; uno; due; tre; quattro; cinque; sei; sette; otto; nove;\n" +
                "dieci; undici; dodici; tredici; quattordici; quindici; sedici;\n" +
                "    diciasette; diciotto; diciannove;\n" +
                "20: venti; vent>%%with-i>;\n" +
                "30: trenta; trent>%%with-i>;\n" +
                "40: quaranta; quarant>%%with-a>;\n" +
                "50: cinquanta; cinquant>%%with-a>;\n" +
                "60: sessanta; sessant>%%with-a>;\n" +
                "70: settanta; settant>%%with-a>;\n" +
                "80: ottanta; ottant>%%with-a>;\n" +
                "90: novanta; novant>%%with-a>;\n" +
                "100: cento; cent[>%%with-o>];\n" +
                "200: <<cento; <<cent[>%%with-o>];\n" +
                "1000: mille; mill[>%%with-i>];\n" +
                "2000: <<mila; <<mil[>%%with-a>];\n" +
                "100,000>>: <<mila[ >>];\n" +
                "1,000,000: =#,##0= (incomplete data);\n" +
            "%%with-a:\n" +
                "azero; uno; adue; atre; aquattro; acinque; asei; asette; otto; anove;\n" +
                "adieci; undici; adodici; atredici; aquattordici; aquindici; asedici;\n" +
                "    adiciasette; adiciotto; adiciannove;\n" +
                "20: aventi; avent>%%with-i>;\n" +
                "30: atrenta; atrent>%%with-i>;\n" +
                "40: aquaranta; aquarant>%%with-a>;\n" +
                "50: acinquanta; acinquant>%%with-a>;\n" +
                "60: asessanta; asessant>%%with-a>;\n" +
                "70: asettanta; asettant>%%with-a>;\n" +
                "80: ottanta; ottant>%%with-a>;\n" +
                "90: anovanta; anovant>%%with-a>;\n" +
                "100: acento; acent[>%%with-o>];\n" +
                "200: <%%with-a<cento; <%%with-a<cent[>%%with-o>];\n" +
                "1000: amille; amill[>%%with-i>];\n" +
                "2000: <%%with-a<mila; <%%with-a<mil[>%%with-a>];\n" +
                "100,000: =%main=;\n" +
            "%%with-i:\n" +
                "izero; uno; idue; itre; iquattro; icinque; isei; isette; otto; inove;\n" +
                "idieci; undici; idodici; itredici; iquattordici; iquindici; isedici;\n" +
                "    idiciasette; idiciotto; idiciannove;\n" +
                "20: iventi; ivent>%%with-i>;\n" +
                "30: itrenta; itrent>%%with-i>;\n" +
                "40: iquaranta; iquarant>%%with-a>;\n" +
                "50: icinquanta; icinquant>%%with-a>;\n" +
                "60: isessanta; isessant>%%with-a>;\n" +
                "70: isettanta; isettant>%%with-a>;\n" +
                "80: ottanta; ottant>%%with-a>;\n" +
                "90: inovanta; inovant>%%with-a>;\n" +
                "100: icento; icent[>%%with-o>];\n" +
                "200: <%%with-i<cento; <%%with-i<cent[>%%with-o>];\n" +
                "1000: imille; imill[>%%with-i>];\n" +
                "2000: <%%with-i<mila; <%%with-i<mil[>%%with-a>];\n" +
                "100,000: =%main=;\n" +
            "%%with-o:\n" +
                "ozero; uno; odue; otre; oquattro; ocinque; osei; osette; otto; onove;\n" +
                "odieci; undici; ododici; otredici; oquattordici; oquindici; osedici;\n" +
                "    odiciasette; odiciotto; odiciannove;\n" +
                "20: oventi; ovent>%%with-i>;\n" +
                "30: otrenta; otrent>%%with-i>;\n" +
                "40: oquaranta; oquarant>%%with-a>;\n" +
                "50: ocinquanta; ocinquant>%%with-a>;\n" +
                "60: osessanta; osessant>%%with-a>;\n" +
                "70: osettanta; osettant>%%with-a>;\n" +
                "80: ottanta; ottant>%%with-a>;\n" +
                "90: onovanta; onovant>%%with-a>;\n" +
                "100: ocento; ocent[>%%with-o>];\n" +
                "200: <%%with-o<cento; <%%with-o<cent[>%%with-o>];\n" +
                "1000: omille; omill[>%%with-i>];\n" +
                "2000: <%%with-o<mila; <%%with-o<mil[>%%with-a>];\n" +
                "100,000: =%main=;\n" ;
        
        String en_OrdinalRules =
            // this rule set formats the numeral and calls %%abbrev to
            // supply the abbreviation
            "%main:\n" +
                "=#,##0==%%abbrev=;\n" +
            // this rule set supplies the abbreviation
            "%%abbrev:\n" +
                   // the abbreviations.  Everything from 4 to 19 ends in "th"
                "th; st; nd; rd; th;\n" +
                   // at 20, we begin repeating the cycle every 10 (13 is "13th",
                   // but 23 and 33 are "23rd" and "33rd")  We do this by
                   // ignoring all bug the ones digit in selecting the abbreviation
                "20: >>;\n" +
                   // at 100, we repeat the whole cycle by considering only the
                   // tens and ones digits in picking an abbreviation
                "100: >>;\n";
        
        String en_DurationRules =
            // main rule set for formatting with words
            "%with-words:\n" +
                   // take care of singular and plural forms of "second"
                "0 seconds; 1 second; =0= seconds;\n" +
                   // use %%min to format values greater than 60 seconds
                "60/60: <%%min<[, >>];\n" +
                   // use %%hr to format values greater than 3,600 seconds
                   // (the ">>>" below causes us to see the number of minutes
                   // when when there are zero minutes)
                "3600/60: <%%hr<[, >>>];\n" +
            // this rule set takes care of the singular and plural forms
            // of "minute"
            "%%min:\n" +
                "0 minutes; 1 minute; =0= minutes;\n" +
            // this rule set takes care of the singular and plural forms
            // of "hour"
            "%%hr:\n" +
                "0 hours; 1 hour; =0= hours;\n" +

            // main rule set for formatting in numerals
            "%in-numerals:\n" +
                   // values below 60 seconds are shown with "sec."
                "=0= sec.;\n" +
                   // higher values are shown with colons: %%min-sec is used for
                   // values below 3,600 seconds...
                "60: =%%min-sec=;\n" +
                   // ...and %%hr-min-sec is used for values of 3,600 seconds
                   // and above
                "3600: =%%hr-min-sec=;\n" +
            // this rule causes values of less than 10 minutes to show without
            // a leading zero
            "%%min-sec:\n" +
                "0: :=00=;\n" +
                "60/60: <0<>>;\n" +
            // this rule set is used for values of 3,600 or more.  Minutes are always
            // shown, and always shown with two digits
            "%%hr-min-sec:\n" +
                "0: :=00=;\n" +
                "60/60: <00<>>;\n" +
                "3600/60: <#,##0<:>>>;\n" +
            // the lenient-parse rules allow several different characters to be used
            // as delimiters between hours, minutes, and seconds
            "%%lenient-parse:\n" +
                "& ':' = '.' = ' ' = '-';\n";

            HashMap cannedData = new HashMap();
        
        {
            cannedData.put("en_CA/SpelloutRules",      en_SpelloutRules);
            cannedData.put("en_CA/OrdinalRules",       en_OrdinalRules);
            cannedData.put("en_CA/DurationRules",      en_DurationRules);
            
            cannedData.put("fr_CA/SpelloutRules",      fr_SpelloutRules);
            cannedData.put("fr_CA/OrdinalRules",       xx_OrdinalRules);
            cannedData.put("fr_CA/DurationRules",      xx_DurationRules);
            
            cannedData.put("zh_CN/SpelloutRules",      en_SpelloutRules);
            cannedData.put("zh_CN/OrdinalRules",       en_OrdinalRules);
            cannedData.put("zh_CH/DurationRules",      xx_DurationRules);
            
            cannedData.put("zh/SpelloutRules",         en_SpelloutRules);
            cannedData.put("zh/OrdinalRules",          en_OrdinalRules);
            cannedData.put("zh_DurationRules",         xx_DurationRules);
            
            cannedData.put("en/SpelloutRules",         en_SpelloutRules);
            cannedData.put("en/OrdinalRules",          en_OrdinalRules);
            cannedData.put("en/DurationRules",         en_DurationRules);
            
            cannedData.put("fr_FR/SpelloutRules",      fr_SpelloutRules);
            cannedData.put("fr_FR/OrdinalRules",       xx_OrdinalRules);
            cannedData.put("fr_FR/DurationRules",      xx_DurationRules);
            
            cannedData.put("fr/SpelloutRules",         fr_SpelloutRules);
            cannedData.put("fr/OrdinalRules",          xx_OrdinalRules);
            cannedData.put("fr/DurationRules",         xx_DurationRules);
            
            cannedData.put("de/SpelloutRules",         de_SpelloutRules);
            cannedData.put("de/OrdinalRules",          xx_OrdinalRules);
            cannedData.put("de/DurationRules",         xx_DurationRules);
            
            cannedData.put("de_DE/SpelloutRules",      de_SpelloutRules);
            cannedData.put("de_DE/OrdinalRules",       xx_OrdinalRules);
            cannedData.put("de_DE/DurationRules",      xx_DurationRules);
            
            cannedData.put("it/SpelloutRules",         it_SpelloutRules);
            cannedData.put("it/OrdinalRules",          xx_OrdinalRules);
            cannedData.put("it/DurationRules",         xx_DurationRules);
            
            cannedData.put("it_IT/SpelloutRules",      it_SpelloutRules);
            cannedData.put("it_IT/OrdinalRules",       xx_OrdinalRules);
            cannedData.put("it_IT/DuratonRules",       xx_DurationRules);
            
            cannedData.put("ko_KR/SpelloutRules",      en_SpelloutRules);
            cannedData.put("ko_KR/OrdinalRules",       en_OrdinalRules);
            cannedData.put("ko_KR/DurationRules",      en_DurationRules);
            
            cannedData.put("ko/SpelloutRules",         en_SpelloutRules);
            cannedData.put("ko/OrdinalRules",          en_OrdinalRules);
            cannedData.put("ko/DurationRules",         en_DurationRules);
            
            cannedData.put("zh_Hans_CN/SpelloutRules", en_SpelloutRules);
            cannedData.put("zh_Hans_CN/OrdinalRules",  en_OrdinalRules);
            cannedData.put("zh_Hans_CH/DurationRules", xx_DurationRules);
            
            cannedData.put("zh_Hant_TW/SpelloutRules", en_SpelloutRules);
            cannedData.put("zh_Hant_TW/OrdinalRules",  en_OrdinalRules);
            cannedData.put("zh_Hant_TW/DurationRules", en_DurationRules);
            
            cannedData.put("zh_TW/SpelloutRules",      en_SpelloutRules);
            cannedData.put("zh_TW/OrdinalRules",       en_OrdinalRules);
            cannedData.put("zh_TW/DurationRules",      en_DurationRules);
            
            cannedData.put("en_GB/SpelloutRules",      en_GB_SpelloutRules);
            cannedData.put("en_GB/OrdinalRules",       en_OrdinalRules);
            cannedData.put("en_GB/DurationRules",      en_DurationRules);
            
            cannedData.put("en_US/SpelloutRules",      en_SpelloutRules);
            cannedData.put("en_US/OrdinalRules",       en_OrdinalRules);
            cannedData.put("en_US/DurationRules",      en_DurationRules);

            cannedData.put("ja/SpelloutRules",         ja_spelloutRules);
            cannedData.put("ja/OrdinalRules",          xx_OrdinalRules);
            cannedData.put("ja/DurationRules",         xx_DurationRules);
            
            cannedData.put("ja_JP/SpelloutRules",      ja_spelloutRules);
            cannedData.put("ja_JP/OrdinalRules",       xx_OrdinalRules);
            cannedData.put("ja_JP/DurationRules",      xx_DurationRules);
        }
        
        int types[]        = {RuleBasedNumberFormat.SPELLOUT, RuleBasedNumberFormat.ORDINAL, RuleBasedNumberFormat.DURATION};
        String typeNames[] = {"SpelloutRules", "OrdinalRules", "DurationRules"};
        
        public Object[] getTestObjects()
        {
            Locale locales[] = SerializableTest.getLocales();
            RuleBasedNumberFormat formats[] = new RuleBasedNumberFormat[types.length * locales.length];
            int i = 0;
            
            for (int t = 0; t < types.length; t += 1) {
                for (int l = 0; l < locales.length; l += 1) {
                    String cannedRules = (String) cannedData.get(locales[l].toString() + "/" + typeNames[t]);
                    
                    if (cannedRules != null) {
                        formats[i++] = new RuleBasedNumberFormat(cannedRules, locales[l]);
                    } else {
                        formats[i++] = new RuleBasedNumberFormat(locales[l], types[t]);
                    }
                }
            }
            
            return formats;
        }
    }
    
    public static class DecimalFormatSymbolsHandler implements SerializableTest.Handler
    {
        /*
         * The serialized form of a normally created DecimalFormatSymbols object
         * will have locale-specific data in it that might change from one version
         * of ICU4J to another. To guard against this, we store the following canned
         * data into the test objects we create.
         */
        static HashMap cannedData = new HashMap();
        
        static String en_CA_StringSymbols[] = {
            "$", 
            "E", 
            "\u221E", 
            "CAD", 
            "\uFFFD", 
        };

        static String fr_CA_StringSymbols[] = {
            "$", 
            "E", 
            "\u221E", 
            "CAD", 
            "\uFFFD", 
        };

        static String zh_CN_StringSymbols[] = {
            "\uFFE5", 
            "E", 
            "\u221E", 
            "CNY", 
            "\uFFFD", 
        };

        static String zh_StringSymbols[] = {
            "\u00A4", 
            "E", 
            "\u221E", 
            "XXX", 
            "\uFFFD", 
        };

        static String en_StringSymbols[] = {
            "\u00A4", 
            "E", 
            "\u221E", 
            "XXX", 
            "\uFFFD", 
        };

        static String fr_FR_StringSymbols[] = {
            "\u20AC", 
            "E", 
            "\u221E", 
            "EUR", 
            "\uFFFD", 
        };

        static String fr_StringSymbols[] = {
            "\u00A4", 
            "E", 
            "\u221E", 
            "XXX", 
            "\uFFFD", 
        };

        static String de_StringSymbols[] = {
            "\u00A4", 
            "E", 
            "\u221E", 
            "XXX", 
            "\uFFFD", 
        };

        static String de_DE_StringSymbols[] = {
            "\u20AC", 
            "E", 
            "\u221E", 
            "EUR", 
            "\uFFFD", 
        };

        static String it_StringSymbols[] = {
            "\u00A4", 
            "E", 
            "\u221E", 
            "XXX", 
            "\uFFFD", 
        };

        static String it_IT_StringSymbols[] = {
            "\u20AC", 
            "E", 
            "\u221E", 
            "EUR", 
            "\uFFFD", 
        };

        static String ja_JP_StringSymbols[] = {
            "\uFFE5", 
            "E", 
            "\u221E", 
            "JPY", 
            "\uFFFD", 
        };

        static String ja_StringSymbols[] = {
            "\u00A4", 
            "E", 
            "\u221E", 
            "XXX", 
            "\uFFFD", 
        };

        static String ko_KR_StringSymbols[] = {
            "\uFFE6", 
            "E", 
            "\u221E", 
            "KRW", 
            "\uFFFD", 
        };

        static String ko_StringSymbols[] = {
            "\u00A4", 
            "E", 
            "\u221E", 
            "XXX", 
            "\uFFFD", 
        };

        static String zh_Hans_CN_StringSymbols[] = {
            "\uFFE5", 
            "E", 
            "\u221E", 
            "CNY", 
            "\uFFFD", 
        };

        static String zh_Hant_TW_StringSymbols[] = {
            "NT$", 
            "E", 
            "\u221E", 
            "TWD", 
            "\uFFFD", 
        };

        static String zh_TW_StringSymbols[] = {
            "NT$", 
            "E", 
            "\u221E", 
            "TWD", 
            "\uFFFD", 
        };

        static String en_GB_StringSymbols[] = {
            "\u00A3", 
            "E", 
            "\u221E", 
            "GBP", 
            "\uFFFD", 
        };

        static String en_US_StringSymbols[] = {
            "$", 
            "E", 
            "\u221E", 
            "USD", 
            "\uFFFD", 
        };
        
        {
            cannedData.put("en_CA",      en_CA_StringSymbols);
            cannedData.put("fr_CA",      fr_CA_StringSymbols);
            cannedData.put("zh_CN",      zh_CN_StringSymbols);
            cannedData.put("zh",         zh_StringSymbols);
            cannedData.put("en",         en_StringSymbols);
            cannedData.put("fr_FR",      fr_FR_StringSymbols);
            cannedData.put("fr",         fr_StringSymbols);
            cannedData.put("de",         de_StringSymbols);
            cannedData.put("de_DE",      de_DE_StringSymbols);
            cannedData.put("it",         it_StringSymbols);
            cannedData.put("it_IT",      it_IT_StringSymbols);
            cannedData.put("ja_JP",      ja_JP_StringSymbols);
            cannedData.put("ja",         ja_StringSymbols);
            cannedData.put("ko_KR",      ko_KR_StringSymbols);
            cannedData.put("ko",         ko_StringSymbols);
            cannedData.put("zh_Hans_CN", zh_Hans_CN_StringSymbols);
            cannedData.put("zh_Hant_TW", zh_Hant_TW_StringSymbols);
            cannedData.put("zh_TW",      zh_TW_StringSymbols);
            cannedData.put("en_GB",      en_GB_StringSymbols);
            cannedData.put("en_US",      en_US_StringSymbols);
        }
        
        private char[] getCharSymbols(DecimalFormatSymbols dfs)
        {
            char symbols[] = {
                dfs.getDecimalSeparator(),
                dfs.getDigit(),
                dfs.getGroupingSeparator(),
                dfs.getMinusSign(),
                dfs.getMonetaryDecimalSeparator(),
                dfs.getPadEscape(),
                dfs.getPatternSeparator(),
                dfs.getPercent(),
                dfs.getPerMill(),
                dfs.getPlusSign(),
                dfs.getSignificantDigit(),
                dfs.getZeroDigit()
            };
            
            return symbols;
        }
        
        private String[] getStringSymbols(DecimalFormatSymbols dfs)
        {
            String symbols[] = {
                dfs.getCurrencySymbol(),
                dfs.getExponentSeparator(),
                dfs.getInfinity(),
                dfs.getInternationalCurrencySymbol(),
                dfs.getNaN()
            };
            
            return symbols;
        }
        
        private void setStringSymbols(DecimalFormatSymbols dfs, String symbols[])
        {
            dfs.setCurrencySymbol(symbols[0]);
            dfs.setExponentSeparator(symbols[1]);
            dfs.setInfinity(symbols[2]);
            dfs.setInternationalCurrencySymbol(symbols[3]);
            dfs.setNaN(symbols[4]);
        }
        
        public Object[] getTestObjects()
        {
            Locale locales[] = SerializableTest.getLocales();
            DecimalFormatSymbols dfs[] = new DecimalFormatSymbols[locales.length];
            
            for (int i = 0; i < locales.length; i += 1) {
                ULocale uloc = ULocale.forLocale(locales[i]);

                dfs[i] = new DecimalFormatSymbols(uloc);
                setStringSymbols(dfs[i], (String[]) cannedData.get(uloc.toString()));
            }
            
            return dfs;
        }
        
        public boolean hasSameBehavior(Object a, Object b)
        {
            DecimalFormatSymbols dfs_a = (DecimalFormatSymbols) a;
            DecimalFormatSymbols dfs_b = (DecimalFormatSymbols) b;
            String strings_a[] = getStringSymbols(dfs_a);
            String strings_b[] = getStringSymbols(dfs_b);
            char chars_a[] = getCharSymbols(dfs_a);
            char chars_b[] = getCharSymbols(dfs_b);

            return SerializableTest.compareStrings(strings_a, strings_b) && SerializableTest.compareChars(chars_a, chars_b);
        }
    }
    
    public static class MessageFormatHandler implements SerializableTest.Handler
    {
        public Object[] getTestObjects()
        {
            MessageFormat formats[] = {new MessageFormat("pattern{0}")};
            
            return formats;
        }

        public boolean hasSameBehavior(Object a, Object b)
        {
            MessageFormat mfa = (MessageFormat) a;
            MessageFormat mfb = (MessageFormat) b;
            Object arguments[] = {new Integer(123456)};
            
            return mfa.format(arguments) != mfb.format(arguments);
        }
    }
    
    public static class DateFormatHandler implements SerializableTest.Handler
    {
        public Object[] getTestObjects()
        {
            Locale locales[] = SerializableTest.getLocales();
            DateFormat formats[] = new DateFormat[locales.length];
            
            for (int i = 0; i < locales.length; i += 1) {
                formats[i] = DateFormat.getDateTimeInstance(DateFormat.FULL, DateFormat.FULL, locales[i]);
            }
            
            return formats;
        }
        
        public boolean hasSameBehavior(Object a, Object b)
        {
            DateFormat dfa = (DateFormat) a;
            DateFormat dfb = (DateFormat) b;
            Date date = new Date(System.currentTimeMillis());
            String sfa = dfa.format(date);
            String sfb = dfa.format(date);
            
           return sfa.equals(sfb);
        }
        
    }
    
    public static class DateFormatSymbolsHandler implements SerializableTest.Handler
    {
        /*
         * The serialized form of a normally created DateFormatSymbols object
         * will have locale-specific data in it that might change from one version
         * of ICU4J to another. To guard against this, we store the following canned
         * data into the test objects we create.
         */
        static HashMap cannedData = new HashMap();

        static String en_CA_MonthNames[] = {
            "January", 
            "February", 
            "March", 
            "April", 
            "May", 
            "June", 
            "July", 
            "August", 
            "September", 
            "October", 
            "November", 
            "December", 
        };

        static String fr_CA_MonthNames[] = {
            "janvier", 
            "f\u00E9vrier", 
            "mars", 
            "avril", 
            "mai", 
            "juin", 
            "juillet", 
            "ao\u00FBt", 
            "septembre", 
            "octobre", 
            "novembre", 
            "d\u00E9cembre", 
        };

        static String zh_Hans_CN_MonthNames[] = {
            "\u4E00\u6708", 
            "\u4E8C\u6708", 
            "\u4E09\u6708", 
            "\u56DB\u6708", 
            "\u4E94\u6708", 
            "\u516D\u6708", 
            "\u4E03\u6708", 
            "\u516B\u6708", 
            "\u4E5D\u6708", 
            "\u5341\u6708", 
            "\u5341\u4E00\u6708", 
            "\u5341\u4E8C\u6708", 
        };

        static String zh_CN_MonthNames[] = {
            "\u4E00\u6708", 
            "\u4E8C\u6708", 
            "\u4E09\u6708", 
            "\u56DB\u6708", 
            "\u4E94\u6708", 
            "\u516D\u6708", 
            "\u4E03\u6708", 
            "\u516B\u6708", 
            "\u4E5D\u6708", 
            "\u5341\u6708", 
            "\u5341\u4E00\u6708", 
            "\u5341\u4E8C\u6708", 
        };

        static String zh_MonthNames[] = {
            "\u4E00\u6708", 
            "\u4E8C\u6708", 
            "\u4E09\u6708", 
            "\u56DB\u6708", 
            "\u4E94\u6708", 
            "\u516D\u6708", 
            "\u4E03\u6708", 
            "\u516B\u6708", 
            "\u4E5D\u6708", 
            "\u5341\u6708", 
            "\u5341\u4E00\u6708", 
            "\u5341\u4E8C\u6708", 
        };

        static String en_MonthNames[] = {
            "January", 
            "February", 
            "March", 
            "April", 
            "May", 
            "June", 
            "July", 
            "August", 
            "September", 
            "October", 
            "November", 
            "December", 
        };

        static String fr_FR_MonthNames[] = {
            "janvier", 
            "f\u00E9vrier", 
            "mars", 
            "avril", 
            "mai", 
            "juin", 
            "juillet", 
            "ao\u00FBt", 
            "septembre", 
            "octobre", 
            "novembre", 
            "d\u00E9cembre", 
        };

        static String fr_MonthNames[] = {
            "janvier", 
            "f\u00E9vrier", 
            "mars", 
            "avril", 
            "mai", 
            "juin", 
            "juillet", 
            "ao\u00FBt", 
            "septembre", 
            "octobre", 
            "novembre", 
            "d\u00E9cembre", 
        };

        static String de_MonthNames[] = {
            "Januar", 
            "Februar", 
            "M\u00E4rz", 
            "April", 
            "Mai", 
            "Juni", 
            "Juli", 
            "August", 
            "September", 
            "Oktober", 
            "November", 
            "Dezember", 
        };

        static String de_DE_MonthNames[] = {
            "Januar", 
            "Februar", 
            "M\u00E4rz", 
            "April", 
            "Mai", 
            "Juni", 
            "Juli", 
            "August", 
            "September", 
            "Oktober", 
            "November", 
            "Dezember", 
        };

        static String it_MonthNames[] = {
            "gennaio", 
            "febbraio", 
            "marzo", 
            "aprile", 
            "maggio", 
            "giugno", 
            "luglio", 
            "agosto", 
            "settembre", 
            "ottobre", 
            "novembre", 
            "dicembre", 
        };

        static String it_IT_MonthNames[] = {
            "gennaio", 
            "febbraio", 
            "marzo", 
            "aprile", 
            "maggio", 
            "giugno", 
            "luglio", 
            "agosto", 
            "settembre", 
            "ottobre", 
            "novembre", 
            "dicembre", 
        };

        static String ja_JP_MonthNames[] = {
            "1\u6708", 
            "2\u6708", 
            "3\u6708", 
            "4\u6708", 
            "5\u6708", 
            "6\u6708", 
            "7\u6708", 
            "8\u6708", 
            "9\u6708", 
            "10\u6708", 
            "11\u6708", 
            "12\u6708", 
        };

        static String ja_MonthNames[] = {
            "1\u6708", 
            "2\u6708", 
            "3\u6708", 
            "4\u6708", 
            "5\u6708", 
            "6\u6708", 
            "7\u6708", 
            "8\u6708", 
            "9\u6708", 
            "10\u6708", 
            "11\u6708", 
            "12\u6708", 
        };

        static String ko_KR_MonthNames[] = {
            "1\uC6D4", 
            "2\uC6D4", 
            "3\uC6D4", 
            "4\uC6D4", 
            "5\uC6D4", 
            "6\uC6D4", 
            "7\uC6D4", 
            "8\uC6D4", 
            "9\uC6D4", 
            "10\uC6D4", 
            "11\uC6D4", 
            "12\uC6D4", 
        };

        static String ko_MonthNames[] = {
            "1\uC6D4", 
            "2\uC6D4", 
            "3\uC6D4", 
            "4\uC6D4", 
            "5\uC6D4", 
            "6\uC6D4", 
            "7\uC6D4", 
            "8\uC6D4", 
            "9\uC6D4", 
            "10\uC6D4", 
            "11\uC6D4", 
            "12\uC6D4", 
        };

        static String zh_Hant_TW_MonthNames[] = {
            "\u4E00\u6708", 
            "\u4E8C\u6708", 
            "\u4E09\u6708", 
            "\u56DB\u6708", 
            "\u4E94\u6708", 
            "\u516D\u6708", 
            "\u4E03\u6708", 
            "\u516B\u6708", 
            "\u4E5D\u6708", 
            "\u5341\u6708", 
            "\u5341\u4E00\u6708", 
            "\u5341\u4E8C\u6708", 
        };

        static String zh_TW_MonthNames[] = {
            "\u4E00\u6708", 
            "\u4E8C\u6708", 
            "\u4E09\u6708", 
            "\u56DB\u6708", 
            "\u4E94\u6708", 
            "\u516D\u6708", 
            "\u4E03\u6708", 
            "\u516B\u6708", 
            "\u4E5D\u6708", 
            "\u5341\u6708", 
            "\u5341\u4E00\u6708", 
            "\u5341\u4E8C\u6708", 
            };

        static String en_GB_MonthNames[] = {
            "January", 
            "February", 
            "March", 
            "April", 
            "May", 
            "June", 
            "July", 
            "August", 
            "September", 
            "October", 
            "November", 
            "December", 
        };

        static String en_US_MonthNames[] = {
            "January", 
            "February", 
            "March", 
            "April", 
            "May", 
            "June", 
            "July", 
            "August", 
            "September", 
            "October", 
            "November", 
            "December", 
        };

        {
            cannedData.put("en_CA",      en_CA_MonthNames);
            cannedData.put("fr_CA",      fr_CA_MonthNames);
            cannedData.put("zh_Hans_CN", zh_Hans_CN_MonthNames);
            cannedData.put("zh_CN",      zh_CN_MonthNames);
            cannedData.put("zh",         zh_MonthNames);
            cannedData.put("en",         en_MonthNames);
            cannedData.put("fr_FR",      fr_FR_MonthNames);
            cannedData.put("fr",         fr_MonthNames);
            cannedData.put("de",         de_MonthNames);
            cannedData.put("de_DE",      de_DE_MonthNames);
            cannedData.put("it",         it_MonthNames);
            cannedData.put("it_IT",      it_IT_MonthNames);
            cannedData.put("ja_JP",      ja_JP_MonthNames);
            cannedData.put("ja",         ja_MonthNames);
            cannedData.put("ko_KR",      ko_KR_MonthNames);
            cannedData.put("ko",         ko_MonthNames);
            cannedData.put("zh_Hant_TW", zh_Hant_TW_MonthNames);
            cannedData.put("zh_TW",      zh_TW_MonthNames);
            cannedData.put("en_GB",      en_GB_MonthNames);
            cannedData.put("en_US",      en_US_MonthNames);
        }
        
        public Object[] getTestObjects()
        {
            Locale locales[] = SerializableTest.getLocales();
            DateFormatSymbols dfs[] = new DateFormatSymbols[locales.length];
            
            for (int i = 0; i < locales.length; i += 1) {
                ULocale uloc = ULocale.forLocale(locales[i]);
                
                dfs[i] = new DateFormatSymbols(GregorianCalendar.class, uloc);
                dfs[i].setMonths((String[]) cannedData.get(uloc.toString()));
            }
            
            return dfs;
        }
        
        public boolean hasSameBehavior(Object a, Object b)
        {
            DateFormatSymbols dfs_a = (DateFormatSymbols) a;
            DateFormatSymbols dfs_b = (DateFormatSymbols) b;
            String months_a[] = dfs_a.getMonths();
            String months_b[] = dfs_b.getMonths();
            
            return SerializableTest.compareStrings(months_a, months_b);
        }
    }
    
    public static class SimpleDateFormatHandler extends DateFormatHandler
    {
        String patterns[] = {
            "EEEE, yyyy MMMM dd",
            "yyyy MMMM d",
            "yyyy MMM d",
            "yy/MM/dd"
        };
        
        public Object[] getTestObjects()
        {
            Locale locales[] = SerializableTest.getLocales();
            SimpleDateFormat dateFormats[] = new SimpleDateFormat[patterns.length * locales.length];
            int i = 0;
            
            for (int p = 0; p < patterns.length; p += 1) {
                for (int l = 0; l < locales.length; l += 1) {
                    dateFormats[i++] = new SimpleDateFormat(patterns[p], ULocale.forLocale(locales[l]));
                }
            }
            
            return dateFormats;
        }
    }
    
    public static class ChineseDateFormatHandler extends DateFormatHandler
    {
        String patterns[] = {
            "EEEE y'x'G-Ml-d",
            "y'x'G-Ml-d",
            "y'x'G-Ml-d",
            "y'x'G-Ml-d"
        };
        
        public Object[] getTestObjects()
        {
            Locale locales[] = SerializableTest.getLocales();
            ChineseDateFormat dateFormats[] = new ChineseDateFormat[patterns.length * locales.length];
            int i = 0;
            
            for (int p = 0; p < patterns.length; p += 1) {
                for (int l = 0; l < locales.length; l += 1) {
                    ULocale locale = new ULocale(locales[l].toString() + "@calendar=chinese");
                    
                    dateFormats[i++] = new ChineseDateFormat(patterns[p], locale);
                }
            }
            
            return dateFormats;
        }
    }
    
    public static class ChineseDateFormatSymbolsHandler extends DateFormatSymbolsHandler
    {
        public Object[] getTestObjects()
        {
            Locale locales[] = SerializableTest.getLocales();
            ChineseDateFormatSymbols cdfs[] = new ChineseDateFormatSymbols[locales.length];
            
            for (int i = 0; i < locales.length; i += 1) {
                ULocale uloc = ULocale.forLocale(locales[i]);
                
                cdfs[i] = new ChineseDateFormatSymbols(uloc);
                cdfs[i].setMonths((String[]) cannedData.get(uloc.toString()));
            }
            
            return cdfs;
        }
        
        public boolean hasSameBehavior(Object a, Object b)
        {
            if (! super.hasSameBehavior(a, b)) {
                return false;
            }
            
            ChineseDateFormatSymbols cdfs_a = (ChineseDateFormatSymbols) a;
            ChineseDateFormatSymbols cdfs_b = (ChineseDateFormatSymbols) b;
            
            return cdfs_a.getLeapMonth(0).equals(cdfs_b.getLeapMonth(0)) &&
                   cdfs_a.getLeapMonth(1).equals(cdfs_b.getLeapMonth(1));
        }
    }

//#ifndef FOUNDATION
    public static class NumberFormatFieldHandler implements SerializableTest.Handler
    {
        public Object[] getTestObjects()
        {
            NumberFormat.Field fields[] = {
                NumberFormat.Field.CURRENCY, NumberFormat.Field.DECIMAL_SEPARATOR, NumberFormat.Field.EXPONENT,
                NumberFormat.Field.EXPONENT_SIGN, NumberFormat.Field.EXPONENT_SYMBOL, NumberFormat.Field.FRACTION,
                NumberFormat.Field.GROUPING_SEPARATOR, NumberFormat.Field.INTEGER, NumberFormat.Field.PERCENT,
                NumberFormat.Field.PERMILLE, NumberFormat.Field.SIGN
            };
            
            return fields;
        }
        
        public boolean hasSameBehavior(Object a, Object b)
        {
            NumberFormat.Field field_a = (NumberFormat.Field) a;
            NumberFormat.Field field_b = (NumberFormat.Field) b;
            
            return field_a.toString().equals(field_b.toString());
        }
    }
//#endif

    public static class DateNumberFormatHandler implements SerializableTest.Handler
    {
        public Object[] getTestObjects()
        {
            Locale locales[] = SerializableTest.getLocales();
            DateNumberFormat[] dnfmts = new DateNumberFormat[locales.length];
            for (int i = 0; i < locales.length; i++) {
                ULocale uloc = ULocale.forLocale(locales[i]);
                dnfmts[i] = new DateNumberFormat(uloc);
            }
            return dnfmts;
        }

        public boolean hasSameBehavior(Object a, Object b) {
            return a.equals(b);
        }
    }
    
    public static void main(String[] args)
    {
        // nothing needed...
    }
}
//eof