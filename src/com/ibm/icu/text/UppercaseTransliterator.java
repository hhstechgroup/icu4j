/*
 *******************************************************************************
 * Copyright (C) 1996-2000, International Business Machines Corporation and    *
 * others. All Rights Reserved.                                                *
 *******************************************************************************
 *
 * $Source: /xsrl/Nsvn/icu/icu4j/src/com/ibm/icu/text/UppercaseTransliterator.java,v $ 
 * $Date: 2002/02/16 03:06:23 $ 
 * $Revision: 1.6 $
 *
 *****************************************************************************************
 */
package com.ibm.icu.text;
import java.util.*;
import com.ibm.icu.lang.*;

/**
 * A transliterator that performs locale-sensitive toUpper()
 * case mapping.
 */
class UppercaseTransliterator extends Transliterator {

    /**
     * Package accessible ID.
     */
    static final String _ID = "Any-Upper";
    // TODO: Add variants for tr, az, lt, default = default locale

    /**
     * System registration hook.
     */
    static void register() {
        Transliterator.registerFactory(_ID, new Transliterator.Factory() {
            public Transliterator getInstance(String ID) {
                return new UppercaseTransliterator(Locale.US);
            }
        });
    }

    private Locale loc;

    /**
     * Constructs a transliterator.
     */
    public UppercaseTransliterator(Locale loc) {
        super(_ID, null);
        this.loc = loc;
    }

    /**
     * Implements {@link Transliterator#handleTransliterate}.
     */
    protected void handleTransliterate(Replaceable text,
                                       Position offsets, boolean isIncremental) {
        int textPos = offsets.start;
        if (textPos >= offsets.limit) return;

        // get string for context
        // TODO: add convenience method to do this, since we do it all over
        
        char[] strBuffer = new char[offsets.contextLimit - offsets.contextStart]; // get whole context
        text.getChars(offsets.contextStart, offsets.contextLimit, strBuffer, 0);
        String original = new String(strBuffer);
        
        // Walk through original string
        // If there is a case change, modify corresponding position in replaceable
        
        int i = textPos - offsets.contextStart;
        int limit = offsets.limit - offsets.contextStart;
        int cp;
        int oldLen;
        
        for (; i < limit; i += oldLen) {
            cp = UTF16.charAt(original, i);
            oldLen = UTF16.getCharCount(cp);
            int newLen = UCharacter.toUpperCase(loc, original, i, buffer);
            if (newLen >= 0) {
                text.replace(textPos, textPos + oldLen, buffer, 0, newLen);
                if (newLen != oldLen) {
                    textPos += newLen;
                    offsets.limit += newLen - oldLen;
                    offsets.contextLimit += newLen - oldLen;
                    continue;
                }
            }
            textPos += oldLen;
        }
        offsets.start = offsets.limit;
    }
    
    private char buffer[] = new char[UCharacter.getMaxCaseExpansion()];

}
