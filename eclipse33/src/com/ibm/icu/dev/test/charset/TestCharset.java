/**
*******************************************************************************
* Copyright (C) 2006, International Business Machines Corporation and    *
* others. All Rights Reserved.                                                *
*******************************************************************************
*
*******************************************************************************
*/

package com.ibm.icu.dev.test.charset;

import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.charset.CharacterCodingException;
import java.nio.charset.Charset;
import java.nio.charset.CharsetDecoder;
import java.nio.charset.CharsetEncoder;
import java.nio.charset.CoderResult;
import java.nio.charset.CodingErrorAction;
import java.nio.charset.UnsupportedCharsetException;
import java.nio.charset.spi.CharsetProvider;
import java.util.Iterator;
import java.util.MissingResourceException;
import java.util.Set;
import java.util.SortedMap;

import com.ibm.icu.charset.*;
import com.ibm.icu.dev.test.TestFmwk;
import com.ibm.icu.text.UTF16;

public class TestCharset extends TestFmwk {
    private String encoding = "UTF-16";
    CharsetDecoder decoder = null;
    CharsetEncoder encoder = null;
    Charset charset =null;
    static final String unistr = "abcd\ud800\udc00\u1234\u00a5\u3000\r\n";
    static final byte[] byteStr ={   
            (byte) 0x00,(byte) 'a',
            (byte) 0x00,(byte) 'b',
            (byte) 0x00,(byte) 'c',
            (byte) 0x00,(byte) 'd',
            (byte) 0xd8,(byte) 0x00,
            (byte) 0xdc,(byte) 0x00,
            (byte) 0x12,(byte) 0x34,
            (byte) 0x00,(byte) 0xa5,
            (byte) 0x30,(byte) 0x00,
            (byte) 0x00,(byte) 0x0d,
            (byte) 0x00,(byte) 0x0a };
    static final byte[] expectedByteStr ={
        (byte) 0xFE,(byte) 0xFF,    
        (byte) 0x00,(byte) 'a',
        (byte) 0x00,(byte) 'b',
        (byte) 0x00,(byte) 'c',
        (byte) 0x00,(byte) 'd',
        (byte) 0xd8,(byte) 0x00,
        (byte) 0xdc,(byte) 0x00,
        (byte) 0x12,(byte) 0x34,
        (byte) 0x00,(byte) 0xa5,
        (byte) 0x30,(byte) 0x00,
        (byte) 0x00,(byte) 0x0d,
        (byte) 0x00,(byte) 0x0a };
    
    protected void init(){
        try{
            CharsetProviderICU provider = new CharsetProviderICU();
            //Charset charset = CharsetICU.forName(encoding);
            charset = provider.charsetForName(encoding);
            decoder = (CharsetDecoder) charset.newDecoder();
            encoder = (CharsetEncoder) charset.newEncoder();   
        }catch(MissingResourceException ex){
            warnln("Could not load charset data");
        }
    }
    
    public static void main(String[] args) throws Exception {
        new TestCharset().run(args);
    }
    public void TestUTF16Converter(){
        CharsetProvider icu = new CharsetProviderICU();
        Charset cs1 = icu.charsetForName("UTF-16");
        CharsetEncoder e1 = cs1.newEncoder();
        CharsetDecoder d1 = cs1.newDecoder();
        
        Charset cs2 = icu.charsetForName("UTF-16LE");
        CharsetEncoder e2 = cs2.newEncoder();
        CharsetDecoder d2 = cs2.newDecoder();
        
        for(int i=0x0000; i<0x10FFFF; i+=0xFF){
            CharBuffer us = CharBuffer.allocate(0xFF*2);
            ByteBuffer bs1 = ByteBuffer.allocate(0xFF*8);
            ByteBuffer bs2 = ByteBuffer.allocate(0xFF*8);
            for(int j=0;j<0xFF; j++){
                int c = i+j;
              
                if((c>=0xd800&&c<=0xdFFF)||c>0x10FFFF){
                    continue;
                }

                if(c>0xFFFF){
                    char lead = UTF16.getLeadSurrogate(c);
                    char trail = UTF16.getTrailSurrogate(c);
                    if(!UTF16.isLeadSurrogate(lead)){
                        errln("lead is not lead!"+lead+" for cp: \\U"+Integer.toHexString(c));
                        continue;
                    }
                    if(!UTF16.isTrailSurrogate(trail)){
                        errln("trail is not trail!"+trail);
                        continue;
                    }
                    us.put(lead);
                    us.put(trail);
                    bs1.put((byte)(lead>>8));
                    bs1.put((byte)(lead&0xFF));
                    bs1.put((byte)(trail>>8));
                    bs1.put((byte)(trail&0xFF));
                    
                    bs2.put((byte)(lead&0xFF));
                    bs2.put((byte)(lead>>8));
                    bs2.put((byte)(trail&0xFF));
                    bs2.put((byte)(trail>>8));
                }else{

                    if(c<0xFF){
                        bs1.put((byte)0x00);
                        bs1.put((byte)(c));
                        bs2.put((byte)(c));
                        bs2.put((byte)0x00);
                    }else{
                        bs1.put((byte)(c>>8));
                        bs1.put((byte)(c&0xFF));
                        
                        bs2.put((byte)(c&0xFF));
                        bs2.put((byte)(c>>8));
                    }
                    us.put((char)c);
                }
            }
            
            
            us.limit(us.position());
            us.position(0);
            if(us.length()==0){
                continue;
            }
            

            bs1.limit(bs1.position());
            bs1.position(0);
            ByteBuffer newBS = ByteBuffer.allocate(bs1.capacity());
            newBS.put((byte)0xFE);
            newBS.put((byte)0xFF);
            newBS.put(bs1);    
            bs1.position(0);
            smBufDecode(d1, "UTF-16", bs1, us);
            smBufEncode(e1, "UTF-16", us, newBS);
            
            bs2.limit(bs2.position());
            bs2.position(0);
            newBS.clear();
            newBS.put((byte)0xFF);
            newBS.put((byte)0xFE);
            newBS.put(bs2);     
            bs2.position(0);
            smBufDecode(d2, "UTF16-LE", bs2, us);
            smBufEncode(e2, "UTF-16LE", us, newBS);
            
        }
        
    }
    public void TestUTF32Converter(){
        CharsetProvider icu = new CharsetProviderICU();
        Charset cs1 = icu.charsetForName("UTF-32");
        CharsetEncoder e1 = cs1.newEncoder();
        CharsetDecoder d1 = cs1.newDecoder();
        
        Charset cs2 = icu.charsetForName("UTF-32LE");
        CharsetEncoder e2 = cs2.newEncoder();
        CharsetDecoder d2 = cs2.newDecoder();
        
        for(int i=0x1d827; i<0x10FFFF; i+=0xFF){
            CharBuffer us = CharBuffer.allocate(0xFF*2);
            ByteBuffer bs1 = ByteBuffer.allocate(0xFF*8);
            ByteBuffer bs2 = ByteBuffer.allocate(0xFF*8);
            for(int j=0;j<0xFF; j++){
                int c = i+j;
              
                if((c>=0xd800&&c<=0xdFFF)||c>0x10FFFF){
                    continue;
                }

                if(c>0xFFFF){
                    char lead = UTF16.getLeadSurrogate(c);
                    char trail = UTF16.getTrailSurrogate(c);

                    us.put(lead);
                    us.put(trail);
                }else{
                    us.put((char)c);
                }
                bs1.put((byte) (c >>> 24));
                bs1.put((byte) (c >>> 16)); 
                bs1.put((byte) (c >>> 8)); 
                bs1.put((byte) (c & 0xFF));       
                                
                bs2.put((byte) (c & 0xFF));  
                bs2.put((byte) (c >>> 8));
                bs2.put((byte) (c >>> 16)); 
                bs2.put((byte) (c >>> 24));
            }
            bs1.limit(bs1.position());
            bs1.position(0);
            bs2.limit(bs2.position());
            bs2.position(0);
            us.limit(us.position());
            us.position(0);
            if(us.length()==0){
                continue;
            }
            

            ByteBuffer newBS = ByteBuffer.allocate(bs1.capacity());
            
            newBS.put((byte)0x00);
            newBS.put((byte)0x00);
            newBS.put((byte)0xFE);
            newBS.put((byte)0xFF);
            newBS.put(bs1);    
            bs1.position(0);
            smBufDecode(d1, "UTF-32", bs1, us);
            smBufEncode(e1, "UTF-32", us, newBS);
            
            
            newBS.clear();
            newBS.put((byte)0xFF);
            newBS.put((byte)0xFE);
            newBS.put((byte)0x00);
            newBS.put((byte)0x00);
            newBS.put(bs2);    
            bs2.position(0);
            smBufDecode(d2, "UTF-32LE", bs2, us);
            smBufEncode(e2, "UTF-32LE", us, newBS);
        }
        
    }
    public void TestASCIIConverter(){
        CharsetProvider icu = new CharsetProviderICU();
        Charset icuChar = icu.charsetForName("ASCII");
        CharsetEncoder encoder = icuChar.newEncoder();
        CharsetDecoder decoder = icuChar.newDecoder();

        CharBuffer us = CharBuffer.allocate(0x90);
        ByteBuffer bs = ByteBuffer.allocate(0x90);
        for(int j=0;j<=0x7f; j++){
           us.put((char)j);
           bs.put((byte)j);
        }
        bs.limit(bs.position());
        bs.position(0);
        us.limit(us.position());
        us.position(0);
        smBufDecode(decoder, "ASCII", bs, us);
        smBufEncode(encoder, "ASCII", us, bs);
        
    }
    public void Test88591Converter(){
        CharsetProvider icu = new CharsetProviderICU();
        Charset icuChar = icu.charsetForName("iso-8859-1");
        CharsetEncoder encoder = icuChar.newEncoder();
        CharsetDecoder decoder = icuChar.newDecoder();

        CharBuffer us = CharBuffer.allocate(0x100);
        ByteBuffer bs = ByteBuffer.allocate(0x100);
        for(int j=0;j<=0xFf; j++){
           us.put((char)j);
           bs.put((byte)j);
        }
        bs.limit(bs.position());
        bs.position(0);
        us.limit(us.position());
        us.position(0);
        smBufDecode(decoder, "iso-8859-1", bs, us);
        smBufEncode(encoder, "iso-8859-1", us, bs);
        
    }

    public void TestAPISemantics(/*String encoding*/) 
                throws Exception {
        int rc;
        ByteBuffer byes = ByteBuffer.wrap(byteStr);
        CharBuffer uniVal = CharBuffer.wrap(unistr);
        ByteBuffer expected = ByteBuffer.wrap(expectedByteStr);
        
        rc = 0;
        if(decoder==null){
            warnln("Could not load decoder.");
            return;
        }
        decoder.reset();
        /* Convert the whole buffer to Unicode */
        try {
            CharBuffer chars = CharBuffer.allocate(unistr.length());
            CoderResult result = decoder.decode(byes, chars, false);

            if (result.isError()) {
                errln("ToChars encountered Error");
                rc = 1;
            }
            if (result.isOverflow()) {
                errln("ToChars encountered overflow exception");
                rc = 1;
            }
            if (!equals(chars, unistr)) {
                errln("ToChars does not match");
                printchars(chars);
                errln("Expected : ");
                printchars(unistr);
                rc = 2;
            }

        } catch (Exception e) {
            errln("ToChars - exception in buffer");
            rc = 5;
        }

        /* Convert single bytes to Unicode */
        try {
            CharBuffer chars = CharBuffer.allocate(unistr.length());
            ByteBuffer b = ByteBuffer.wrap(byteStr);
            decoder.reset();
            CoderResult result=null;
            for (int i = 1; i <= byteStr.length; i++) {
                b.limit(i);
                result = decoder.decode(b, chars, false);
                if(result.isOverflow()){
                    errln("ToChars single threw an overflow exception");
                }
                if (result.isError()) {
                    errln("ToChars single the result is an error "+result.toString());
                } 
            }
            if (unistr.length() != (chars.limit())) {
                errln("ToChars single len does not match");
                rc = 3;
            }
            if (!equals(chars, unistr)) {
                errln("ToChars single does not match");
                printchars(chars);
                rc = 4;
            }
        } catch (Exception e) {
            errln("ToChars - exception in single");
            //e.printStackTrace();
            rc = 6;
        }

        /* Convert the buffer one at a time to Unicode */
        try {
            CharBuffer chars = CharBuffer.allocate(unistr.length());
            decoder.reset();
            byes.rewind();
            for (int i = 1; i <= byteStr.length; i++) {
                byes.limit(i);
                CoderResult result = decoder.decode(byes, chars, false);
                if (result.isError()) {
                    errln("Error while decoding: "+result.toString());
                }
                if(result.isOverflow()){
                    errln("ToChars Simple threw an overflow exception");
                }
            }
            if (chars.limit() != unistr.length()) {
                errln("ToChars Simple buffer len does not match");
                rc = 7;
            }
            if (!equals(chars, unistr)) {
                errln("ToChars Simple buffer does not match");
                printchars(chars);
                err(" Expected : ");
                printchars(unistr);
                rc = 8;
            }
        } catch (Exception e) {
            errln("ToChars - exception in single buffer");
            //e.printStackTrace(System.err);
            rc = 9;
        }
        if (rc != 0) {
            errln("Test Simple ToChars for encoding : FAILED");
        }

        rc = 0;
        /* Convert the whole buffer from unicode */
        try {
            ByteBuffer bytes = ByteBuffer.allocate(expectedByteStr.length);
            encoder.reset();
            CoderResult result = encoder.encode(uniVal, bytes, false);
            if (result.isError()) {
                errln("FromChars reported error: " + result.toString());
                rc = 1;
            }
            if(result.isOverflow()){
                errln("FromChars threw an overflow exception");
            }
            bytes.position(0);
            if (!bytes.equals(expected)) {
                errln("FromChars does not match");
                printbytes(bytes);
                rc = 2;
            }
        } catch (Exception e) {
            errln("FromChars - exception in buffer");
            //e.printStackTrace(System.err);
            rc = 5;
        }

        /* Convert the buffer one char at a time to unicode */
        try {
            ByteBuffer bytes = ByteBuffer.allocate(expectedByteStr.length);
            CharBuffer c = CharBuffer.wrap(unistr);
            encoder.reset();
            CoderResult result= null;
            for (int i = 1; i <= unistr.length(); i++) {
                c.limit(i);
                result = encoder.encode(c, bytes, false);
                if(result.isOverflow()){
                    errln("FromChars single threw an overflow exception");
                }
                if(result.isError()){
                    errln("FromChars single threw an error: "+ result.toString());
                }
            }
            if (expectedByteStr.length != bytes.limit()) {
                errln("FromChars single len does not match");
                rc = 3;
            }

            bytes.position(0);
            if (!bytes.equals(expected)) {
                errln("FromChars single does not match");
                printbytes(bytes);
                rc = 4;
            }

        } catch (Exception e) {
            errln("FromChars - exception in single");
            //e.printStackTrace(System.err);
            rc = 6;
        }

        /* Convert one char at a time to unicode */
        try {
            ByteBuffer bytes = ByteBuffer.allocate(expectedByteStr.length);
            encoder.reset();
            char[] temp = unistr.toCharArray();
            CoderResult result=null;
            for (int i = 0; i <= temp.length; i++) {
                uniVal.limit(i);
                result = encoder.encode(uniVal, bytes, false);
                if(result.isOverflow()){
                    errln("FromChars simple threw an overflow exception");
                }
                if(result.isError()){
                    errln("FromChars simple threw an error: "+ result.toString());
                }
            }
            if (bytes.limit() != expectedByteStr.length) {
                errln("FromChars Simple len does not match");
                rc = 7;
            }
            if (!bytes.equals(byes)) {
                errln("FromChars Simple does not match");
                printbytes(bytes);
                rc = 8;
            }
        } catch (Exception e) {
            errln("FromChars - exception in single buffer");
            //e.printStackTrace(System.err);
            rc = 9;
        }
        if (rc != 0) {
            errln("Test Simple FromChars " + encoding + " --FAILED");
        }
    }

    void printchars(CharBuffer buf) {
        int i;
        char[] chars = new char[buf.limit()];
        //save the current position
        int pos = buf.position();
        buf.position(0);
        buf.get(chars);
        //reset to old position
        buf.position(pos);
        for (i = 0; i < chars.length; i++) {
            err(hex(chars[i]) + " ");
        }
        errln("");
    }
    void printchars(String str) {
        char[] chars = str.toCharArray();
        for (int i = 0; i < chars.length; i++) {
            err(hex(chars[i]) + " ");
        }
        errln("");
    }
    void printbytes(ByteBuffer buf) {
        int i;
        byte[] bytes = new byte[buf.limit()];
        //save the current position
        int pos = buf.position();
        buf.position(0);
        buf.get(bytes);
        //reset to old position
        buf.position(pos);
        for (i = 0; i < bytes.length; i++) {
            System.out.print(hex(bytes[i]) + " ");
        }
        errln("");
    }

    public boolean equals(CharBuffer buf, String str) {
        return equals(buf, str.toCharArray());
    }
    public boolean equals(CharBuffer buf, CharBuffer str) {
        return equals(buf.array(), str.array());
    }
    public boolean equals(CharBuffer buf, char[] compareTo) {
        char[] chars = new char[buf.limit()];
        //save the current position
        int pos = buf.position();
        buf.position(0);
        buf.get(chars);
        //reset to old position
        buf.position(pos);
        return equals(chars, compareTo);
    }

    public boolean equals(char[] chars, char[] compareTo) {
        if (chars.length != compareTo.length) {
            errln(
                "Length does not match chars: "
                    + chars.length
                    + " compareTo: "
                    + compareTo.length);
            return false;
        } else {
            boolean result = true;
            for (int i = 0; i < chars.length; i++) {
                if (chars[i] != compareTo[i]) {
                    logln(
                        "Got: "
                            + hex(chars[i])
                            + " Expected: "
                            + hex(compareTo[i])
                            + " At: "
                            + i);
                    result = false;
                }
            }
            return result;
        }
    }

    public boolean equals(ByteBuffer buf, byte[] compareTo) {
        byte[] chars = new byte[buf.limit()];
        //save the current position
        int pos = buf.position();
        buf.position(0);
        buf.get(chars);
        //reset to old position
        buf.position(pos);
        return equals(chars, compareTo);
    }
    public boolean equals(ByteBuffer buf, ByteBuffer compareTo) {
        return equals(buf.array(), compareTo.array());
    }
    public boolean equals(byte[] chars, byte[] compareTo) {
        if (chars.length != compareTo.length) {
            errln(
                "Length does not match chars: "
                    + chars.length
                    + " compareTo: "
                    + compareTo.length);
            return false;
        } else {
            boolean result = true;
            for (int i = 0; i < chars.length; i++) {
                if (chars[i] != compareTo[i]) {
                    logln(
                        "Got: "
                            + hex(chars[i])
                            + " Expected: "
                            + hex(compareTo[i])
                            + " At: "
                            + i);
                    result = false;
                }
            }
            return result;
        }
    }

//  TODO
  /*
    public void TestCallback(String encoding) throws Exception {
        
        byte[] gbSource =
            {
                (byte) 0x81,
                (byte) 0x36,
                (byte) 0xDE,
                (byte) 0x36,
                (byte) 0x81,
                (byte) 0x36,
                (byte) 0xDE,
                (byte) 0x37,
                (byte) 0x81,
                (byte) 0x36,
                (byte) 0xDE,
                (byte) 0x38,
                (byte) 0xe3,
                (byte) 0x32,
                (byte) 0x9a,
                (byte) 0x36 };

        char[] subChars = { 'P', 'I' };

        decoder.reset();

        decoder.replaceWith(new String(subChars));
        ByteBuffer mySource = ByteBuffer.wrap(gbSource);
        CharBuffer myTarget = CharBuffer.allocate(5);

        decoder.decode(mySource, myTarget, true);
        char[] expectedResult =
            { '\u22A6', '\u22A7', '\u22A8', '\u0050', '\u0049', };

        if (!equals(myTarget, new String(expectedResult))) {
            errln("Test callback GB18030 to Unicode : FAILED");
        }
        
    }
*/
    public void TestCanConvert(/*String encoding*/)throws Exception {
        char[] mySource = { 
            '\ud800', '\udc00',/*surrogate pair */
            '\u22A6','\u22A7','\u22A8','\u22A9','\u22AA',
            '\u22AB','\u22AC','\u22AD','\u22AE','\u22AF',
            '\u22B0','\u22B1','\u22B2','\u22B3','\u22B4',
            '\ud800','\udc00',/*surrogate pair */
            '\u22B5','\u22B6','\u22B7','\u22B8','\u22B9',
            '\u22BA','\u22BB','\u22BC','\u22BD','\u22BE' 
            };
        if(encoder==null){
            warnln("Could not load encoder.");
            return;
        }
        encoder.reset();
        if (!encoder.canEncode(new String(mySource))) {
            errln("Test canConvert() " + encoding + " failed. "+encoder);
        }

    }
    public void TestAvailableCharsets() {
        SortedMap map = Charset.availableCharsets();
        Set keySet = map.keySet();
        Iterator iter = keySet.iterator();
        while(iter.hasNext()){
            logln("Charset name: "+iter.next().toString());
        }
        Object[] charsets = CharsetProviderICU.getAvailableNames();
        int mapSize = map.size();
        if(mapSize < charsets.length){
            errln("Charset.availableCharsets() returned a number less than the number returned by icu. ICU: " + charsets.length
                    + " JDK: " + mapSize);
        }
        logln("Total Number of chasets = " + map.size());
	}
    
    public void TestWindows936(){
        CharsetProviderICU icu = new CharsetProviderICU();
        Charset cs = icu.charsetForName("windows-936-2000");
        String canonicalName = cs.name();
        if(!canonicalName.equals("GBK")){
            errln("Did not get the expected canonical name. Got: "+canonicalName); //get the canonical name
        }
    }
    
    public void TestICUAvailableCharsets() {
        CharsetProviderICU icu = new CharsetProviderICU();
        Object[] charsets = CharsetProviderICU.getAvailableNames();
        for(int i=0;i<charsets.length;i++){
            Charset cs = icu.charsetForName((String)charsets[i]);
            try{
                CharsetEncoder encoder = cs.newEncoder();
                if(encoder!=null){
                    logln("Creation of encoder succeeded. "+cs.toString());
                }
            }catch(Exception ex){
                errln("Could not instantiate encoder for "+charsets[i]+". Error: "+ex.toString());
            }
            try{
                CharsetDecoder decoder = cs.newDecoder();
                if(decoder!=null){
                    logln("Creation of decoder succeeded. "+cs.toString());
                }
            }catch(Exception ex){
                errln("Could not instantiate decoder for "+charsets[i]+". Error: "+ex.toString());
            }
        }
    }
    /* jitterbug 4312 */
    public void TestUnsupportedCharset(){
        CharsetProvider icu = new CharsetProviderICU();
        Charset icuChar = icu.charsetForName("impossible");
        if(icuChar != null){
            errln("ICU does not conform to the spec");
        }
    }


    public void TestEncoderCreation(){
        try{
            Charset cs = Charset.forName("GB_2312-80");
            CharsetEncoder enc = cs.newEncoder();
            if(enc!=null && (enc instanceof CharsetEncoderICU) ){
                logln("Successfully created the encoder: "+ enc);
            }else{
                errln("Error creating charset encoder.");
            }
        }catch(Exception e){
            warnln("Error creating charset encoder."+ e.toString());
           // e.printStackTrace();
        }
        try{
            Charset cs = Charset.forName("x-ibm-971_P100-1995");
            CharsetEncoder enc = cs.newEncoder();
            if(enc!=null && (enc instanceof CharsetEncoderICU) ){
                logln("Successfully created the encoder: "+ enc);
            }else{
                errln("Error creating charset encoder.");
            }
        }catch(Exception e){
            warnln("Error creating charset encoder."+ e.toString());
        }
    }
    public void TestSubBytes(){
        try{
            //create utf-8 decoder
            CharsetDecoder decoder = new CharsetProviderICU().charsetForName("utf-8").newDecoder();
    
            //create a valid byte array, which can be decoded to " buffer"
            byte[] unibytes = new byte[] { 0x0020, 0x0062, 0x0075, 0x0066, 0x0066, 0x0065, 0x0072 };
    
            ByteBuffer buffer = ByteBuffer.allocate(20);
    
            //add a evil byte to make the byte buffer be malformed input
            buffer.put((byte)0xd8);
    
            //put the valid byte array
            buffer.put(unibytes);
    
            //reset postion
            buffer.flip();  
            
            decoder.onMalformedInput(CodingErrorAction.REPLACE);
            CharBuffer out = decoder.decode(buffer);
            String expected = "\ufffd buffer";
            if(!expected.equals(new String(out.array()))){
                errln("Did not get the expected result for substitution chars. Got: "+
                       new String(out.array()) + "("+ hex(out.array())+")");
            }
            logln("Output: "+  new String(out.array()) + "("+ hex(out.array())+")");
        }catch (CharacterCodingException ex){
            errln("Unexpected exception: "+ex.toString());
        }
    }
    /*
    public void TestImplFlushFailure(){
   
       try{
           CharBuffer in = CharBuffer.wrap("\u3005\u3006\u3007\u30FC\u2015\u2010\uFF0F");
           CharsetEncoder encoder = new CharsetProviderICU().charsetForName("iso-2022-jp").newEncoder();
           ByteBuffer out = ByteBuffer.allocate(30);
           encoder.encode(in, out, true);
           encoder.flush(out);
           if(out.position()!= 20){
               errln("Did not get the expected position from flush");
           }
           
       }catch (Exception ex){
           errln("Could not create encoder for  iso-2022-jp exception: "+ex.toString());
       } 
    }
   */
    public void TestISO88591() {
       
        Charset cs = new CharsetProviderICU().charsetForName("iso-8859-1");
        if(cs!=null){
            CharsetEncoder encoder = cs.newEncoder();
            if(encoder!=null){
                encoder.canEncode("\uc2a3");
            }else{
                errln("Could not create encoder for iso-8859-1");
            }
        }else{
            errln("Could not create Charset for iso-8859-1");
        }
        
    }
    public  void TestUTF8Encode() {
        CharsetEncoder encoderICU = new CharsetProviderICU().charsetForName("utf-8").newEncoder();
        ByteBuffer out = ByteBuffer.allocate(30);
        CoderResult result = encoderICU.encode(CharBuffer.wrap("\ud800"), out, true);
       
        if (result.isMalformed()) {
            logln("\\ud800 is malformed for ICU4JNI utf-8 encoder");
        } else if (result.isUnderflow()) {
            errln("\\ud800 is OK for ICU4JNI utf-8 encoder");
        }

        CharsetEncoder encoderJDK = Charset.forName("utf-8").newEncoder();
        result = encoderJDK.encode(CharBuffer.wrap("\ud800"), ByteBuffer
                .allocate(10), true);
        if (result.isUnderflow()) {
            errln("\\ud800 is OK for JDK utf-8 encoder");
        } else if (result.isMalformed()) {
            logln("\\ud800 is malformed for JDK utf-8 encoder");
        }
    }

    private void printCB(CharBuffer buf){
        buf.rewind();
        while(buf.hasRemaining()){
            System.out.println(hex(buf.get()));
        }
        buf.rewind();
    }
    /*
    public void TestUTF8() throws CharacterCodingException{
           try{
               CharsetEncoder encoderICU = new CharsetProviderICU().charsetForName("utf-8").newEncoder();
               encoderICU.encode(CharBuffer.wrap("\ud800"));
               errln("\\ud800 is OK for ICU4JNI utf-8 encoder");
           }catch (MalformedInputException e) {
               logln("\\ud800 is malformed for JDK utf-8 encoder");
              //e.printStackTrace();
           }
           
           CharsetEncoder encoderJDK = Charset.forName("utf-8").newEncoder();
           try {
               encoderJDK.encode(CharBuffer.wrap("\ud800"));
               errln("\\ud800 is OK for JDK utf-8 encoder");
           } catch (MalformedInputException e) {
               logln("\\ud800 is malformed for JDK utf-8 encoder");
               //e.printStackTrace();
           }         
    }
    */
    public void TestUTF16Bom(){

        Charset cs = (new CharsetProviderICU()).charsetForName("UTF-16");
        char[] in = new char[] { 0x1122, 0x2211, 0x3344, 0x4433,
                                0x5566, 0x6655, 0x7788, 0x8877, 0x9900 };
        CharBuffer inBuf = CharBuffer.allocate(in.length);
        inBuf.put(in);
        CharsetEncoder encoder = cs.newEncoder();
        ByteBuffer outBuf = ByteBuffer.allocate(in.length*2);
        inBuf.rewind();
        encoder.encode(inBuf, outBuf, true);
        outBuf.rewind();
        if(outBuf.remaining()> in.length*2){
            errln("The UTF16 encoder appended bom. Length returned: " + outBuf.remaining());
        }
        while(outBuf.hasRemaining()){
            logln("0x"+hex(outBuf.get()));
        }
        CharsetDecoder decoder = cs.newDecoder();
        outBuf.rewind();
        CharBuffer rt = CharBuffer.allocate(in.length);
        decoder.decode(outBuf, rt, true);
    }
     
    private void smBufDecode(CharsetDecoder decoder, String encoding, ByteBuffer source, CharBuffer target) {

        ByteBuffer mySource = source.duplicate();
        CharBuffer myTarget = CharBuffer.allocate(target.capacity());
        {            
            decoder.reset();
            myTarget.limit(target.limit());
            mySource.limit(source.limit());
            mySource.position(source.position());
            CoderResult result = CoderResult.UNDERFLOW;
            result = decoder.decode(mySource, myTarget, true);
            if (result.isError()) {
                errln("Test complete buffers while decoding failed. "+result.toString());
                return;
            }
            if (result.isOverflow()) {
                errln("Test complete buffers while decoding threw overflow exception");
                return;
            }
            myTarget.limit(myTarget.position());
            myTarget.position(0);
            target.position(0);
            if (result.isUnderflow()&&!equals(myTarget,target)) {
                errln(
                    " Test complete buffers while decoding  "
                        + encoding
                        + " TO Unicode--failed");
            }
        }
        if(isQuick()){
            return;
        }
        {
            decoder.reset();
            myTarget.limit(target.limit());
            mySource.limit(source.limit());
            mySource.position(source.position());
            myTarget.clear();
            myTarget.position(0);
            
            int inputLen = mySource.remaining();

            CoderResult result = CoderResult.UNDERFLOW;
            for(int i=1; i<=inputLen; i++) {
                mySource.limit(i);
                if(i==inputLen){
                    result = decoder.decode(mySource, myTarget, true);
                }else{
                    result = decoder.decode(mySource, myTarget, false);
                }
                if (result.isError()) {
                    errln("Test small input buffers while decoding failed. "+result.toString());
                    break;
                }
                if (result.isOverflow()) {
                    errln("Test small input buffers while decoding threw overflow exception");
                    break;
                }

            }
            myTarget.limit(myTarget.position());
            myTarget.position(0);
            target.position(0);
            if (result.isUnderflow()&&!equals(myTarget,target)) {
                errln(
                    "Test small input buffers while decoding "
                        + encoding
                        + " TO Unicode--failed");
            }
        }
        {
            decoder.reset();
            myTarget.limit(target.limit());
            mySource.limit(source.limit());
            mySource.position(source.position());
            myTarget.clear();
            while (true) {
                int pos = myTarget.position();
                myTarget.limit(++pos);
                CoderResult result = decoder.decode(mySource, myTarget, false);
                if (result.isError()) {
                    errln("Test small output buffers while decoding "+ result.toString());
                }
                if (mySource.position()== mySource.limit()) {
                    result = decoder.decode(mySource, myTarget, true);
                    if (result.isError()) {
                        errln("Test small output buffers while decoding "+result.toString());
                    }
                    result = decoder.flush(myTarget);
                    if (result.isError()) {
                        errln("Test small output buffers while decoding "+ result.toString());
                    }
                    break;
                }
            }

            if (!equals(myTarget,target)) {
                errln(
                    "Test small output buffers "
                        + encoding
                        + " TO Unicode failed");
            }
        }
    }

    private void smBufEncode(CharsetEncoder encoder, String encoding, CharBuffer source, ByteBuffer target) {
        logln("Running smBufEncode for "+ encoding + " with class " + encoder);
        CharBuffer mySource = source.duplicate();
        ByteBuffer myTarget = ByteBuffer.allocate(target.capacity());
        {
            logln("Running tests on small input buffers for "+ encoding);
            encoder.reset();
            myTarget.limit(target.limit());
            mySource.limit(source.limit());
            mySource.position(source.position());
            CoderResult result=null;
            
            result = encoder.encode(mySource, myTarget, true);

            if (result.isError()) {
                errln("Test complete while encoding failed. "+result.toString());
            }
            if (result.isOverflow()) {
                errln("Test complete while encoding threw overflow exception");
            }
            if (!equals(myTarget,target)) {

                errln("Test complete buffers while encoding for "+ encoding+ " failed");

            }else{
                logln("Tests complete buffers for "+ encoding +" passed");
            }
        }
        if(isQuick()){
            return;
        }
        {
            logln("Running tests on small input buffers for "+ encoding);
            encoder.reset();
            myTarget.clear();
            myTarget.limit(target.limit());
            mySource.limit(source.limit());
            mySource.position(source.position());
            int inputLen = mySource.limit();
            CoderResult result=null;
            for(int i=1; i<=inputLen; i++) {
                mySource.limit(i);
                result = encoder.encode(mySource, myTarget, false);
                if (result.isError()) {
                    errln("Test small input buffers while encoding failed. "+result.toString());
                }
                if (result.isOverflow()) {
                    errln("Test small input buffers while encoding threw overflow exception");
                }
            }
            if (!equals(myTarget,target)) {
                errln("Test small input buffers "+ encoding+ " From Unicode failed");
            }else{
                logln("Tests on small input buffers for "+ encoding +" passed");
            }
        }
        {
            logln("Running tests on small output buffers for "+ encoding);
            encoder.reset();
            myTarget.clear();
            myTarget.limit(target.limit());
            mySource.limit(source.limit());
            mySource.position(source.position());
            mySource.position(0);
            myTarget.position(0);
            logln("myTarget.limit: " + myTarget.limit() + " myTarget.capcity: " + myTarget.capacity());
            
            while (true) {
                int pos = myTarget.position();

                CoderResult result = encoder.encode(mySource, myTarget, false);
                logln("myTarget.Position: "+ pos + " myTarget.limit: " + myTarget.limit());
                logln("mySource.position: " + mySource.position() + " mySource.limit: " + mySource.limit());
                
                if (result.isError()) {
                    errln("Test small output buffers while encoding "+result.toString());
                }
                if (mySource.position() == mySource.limit()) {
                    result = encoder.encode(mySource, myTarget, true);
                    if (result.isError()) {
                        errln("Test small output buffers while encoding "+result.toString());
                    }
                    
                    myTarget.limit(myTarget.capacity());
                    result = encoder.flush(myTarget);
                    if (result.isError()) {
                        errln("Test small output buffers while encoding "+result.toString());
                    }
                    break;
                }
            }
            if (!equals(target,myTarget)) {
                errln("Test small output buffers "+ encoding+ " From Unicode failed.");
            }
            logln("Tests on small output buffers for "+ encoding +" passed");

        }
    }
    public void convertAllTest(ByteBuffer bSource, CharBuffer uSource) throws Exception {
        {
            try {
                decoder.reset();
                ByteBuffer mySource = bSource.duplicate();
                CharBuffer myTarget = decoder.decode(mySource);
                if (!equals(myTarget, uSource)) {
                    errln(
                        "--Test convertAll() "
                            + encoding
                            + " to Unicode  --FAILED");
                }
            } catch (Exception e) {
                //e.printStackTrace();
                errln(e.getMessage());
            }
        }
        {
            try {
                encoder.reset();
                CharBuffer mySource = CharBuffer.wrap(uSource);
                ByteBuffer myTarget = encoder.encode(mySource);
                if (!equals(myTarget, bSource)) {
                    errln(
                        "--Test convertAll() "
                            + encoding
                            + " to Unicode  --FAILED");
                }
            } catch (Exception e) {
                //e.printStackTrace();
                errln("encoder.encode() failed "+ e.getMessage()+" "+e.toString());
            }
        }

    }
    //TODO
    /*
    public void testString(ByteBuffer bSource, CharBuffer uSource) {
        try {
            {
                String source = new String(uSource);
                byte[] target = source.getBytes(encoding);
                if (!equals(target, bSource)) {
                    errln("encode using string API failed");
                }
            }
            {

                String target = new String(getByteArray(gbSource), encoding);
                if (!equals(uSource, target.toCharArray())) {
                    errln("decode using string API failed");
                }
            }
        } catch (Exception e) {
            //e.printStackTrace();
            errln(e.getMessage());
        }
    }

    private void fromUnicodeTest() throws Exception {
        
        logln("Loaded Charset: " + charset.getClass().toString());
        logln("Loaded CharsetEncoder: " + encoder.getClass().toString());
        logln("Loaded CharsetDecoder: " + decoder.getClass().toString());
        
        ByteBuffer myTarget = ByteBuffer.allocate(gbSource.length);
        logln("Created ByteBuffer of length: " + uSource.length);
        CharBuffer mySource = CharBuffer.wrap(uSource);
        logln("Wrapped ByteBuffer with CharBuffer  ");
        encoder.reset();
        logln("Test Unicode to " + encoding );
        encoder.encode(mySource, myTarget, true);
        if (!equals(myTarget, gbSource)) {
            errln("--Test Unicode to " + encoding + ": FAILED");
        } 
        logln("Test Unicode to " + encoding +" passed");
    }

    public void TestToUnicode( ) throws Exception {
        
        logln("Loaded Charset: " + charset.getClass().toString());
        logln("Loaded CharsetEncoder: " + encoder.getClass().toString());
        logln("Loaded CharsetDecoder: " + decoder.getClass().toString());
        
        CharBuffer myTarget = CharBuffer.allocate(uSource.length);
        ByteBuffer mySource = ByteBuffer.wrap(getByteArray(gbSource));
        decoder.reset();
        CoderResult result = decoder.decode(mySource, myTarget, true);
        if (result.isError()) {
            errln("Test ToUnicode -- FAILED");
        }
        if (!equals(myTarget, uSource)) {
            errln("--Test " + encoding + " to Unicode :FAILED");
        }
    }

    public static byte[] getByteArray(char[] source) {
        byte[] target = new byte[source.length];
        int i = source.length;
        for (; --i >= 0;) {
            target[i] = (byte) source[i];
        }
        return target;
    }
    /*
    private void smBufCharset(Charset charset) {
        try {
            ByteBuffer bTarget = charset.encode(CharBuffer.wrap(uSource));
            CharBuffer uTarget =
                charset.decode(ByteBuffer.wrap(getByteArray(gbSource)));

            if (!equals(uTarget, uSource)) {
                errln("Test " + charset.toString() + " to Unicode :FAILED");
            }
            if (!equals(bTarget, gbSource)) {
                errln("Test " + charset.toString() + " from Unicode :FAILED");
            }
        } catch (Exception ex) {
            errln("Encountered exception in smBufCharset");
        }
    }
    
    public void TestMultithreaded() throws Exception {
        final Charset cs = Charset.forName(encoding);
        if (cs == charset) {
            errln("The objects are equal");
        }
        smBufCharset(cs);
        try {
            final Thread t1 = new Thread() {
                public void run() {
                    // commented out since the mehtods on
                    // Charset API are supposed to be thread
                    // safe ... to test it we dont sync
            
                    // synchronized(charset){
                   while (!interrupted()) {
                        try {
                            smBufCharset(cs);
                        } catch (UnsupportedCharsetException ueEx) {
                            errln(ueEx.toString());
                        }
                    }

                    // }
                }
            };
            final Thread t2 = new Thread() {
                public void run() {
                        // synchronized(charset){
                    while (!interrupted()) {
                        try {
                            smBufCharset(cs);
                        } catch (UnsupportedCharsetException ueEx) {
                            errln(ueEx.toString());
                        }
                    }

                    //}
                }
            };
            t1.start();
            t2.start();
            int i = 0;
            for (;;) {
                if (i > 1000000000) {
                    try {
                        t1.interrupt();
                    } catch (Exception e) {
                    }
                    try {
                        t2.interrupt();
                    } catch (Exception e) {
                    }
                    break;
                }
                i++;
            }
        } catch (Exception e) {
            throw e;
        }
    }

    public void TestSynchronizedMultithreaded() throws Exception {
        // Methods on CharsetDecoder and CharsetEncoder classes
        // are inherently unsafe if accessed by multiple concurrent
        // thread so we synchronize them
        final Charset charset = Charset.forName(encoding);
        final CharsetDecoder decoder = charset.newDecoder();
        final CharsetEncoder encoder = charset.newEncoder();
        try {
            final Thread t1 = new Thread() {
                public void run() {
                    while (!interrupted()) {
                        try {
                            synchronized (encoder) {
                                smBufEncode(encoder, encoding);
                            }
                            synchronized (decoder) {
                                smBufDecode(decoder, encoding);
                            }
                        } catch (UnsupportedCharsetException ueEx) {
                            errln(ueEx.toString());
                        }
                    }

                }
            };
            final Thread t2 = new Thread() {
                public void run() {
                    while (!interrupted()) {
                        try {
                            synchronized (encoder) {
                                smBufEncode(encoder, encoding);
                            }
                            synchronized (decoder) {
                                smBufDecode(decoder, encoding);
                            }
                        } catch (UnsupportedCharsetException ueEx) {
                            errln(ueEx.toString());
                        }
                    }
                }
            };
            t1.start();
            t2.start();
            int i = 0;
            for (;;) {
                if (i > 1000000000) {
                    try {
                        t1.interrupt();
                    } catch (Exception e) {
                    }
                    try {
                        t2.interrupt();
                    } catch (Exception e) {
                    }
                    break;
                }
                i++;
            }
        } catch (Exception e) {
            throw e;
        }
    }
    */
    
    public void TestMBCS(){      
        {
            // Encoder: from Unicode conversion
            CharsetEncoder encoderICU = new CharsetProviderICU().charsetForName("ibm-971").newEncoder();
            ByteBuffer out = ByteBuffer.allocate(6);
            encoderICU.onUnmappableCharacter(CodingErrorAction.REPLACE);
            CoderResult result = encoderICU.encode(CharBuffer.wrap("\u0131\u0061\u00a1"), out, true);
            if(!result.isError()){
                byte[] expected = {(byte)0xA9, (byte)0xA5, (byte)0xAF, (byte)0xFE, (byte)0xA2, (byte)0xAE};
                if(!equals(expected, out.array())){
                    errln("Did not get the expected result for substitution bytes. Got: "+
                           hex(out.array()));
                }
                logln("Output: "+  hex(out.array()));
            }else{
                errln("Encode operation failed for encoder: "+encoderICU.toString());
            }
        }
        {
            // Decoder: to Unicode conversion
            CharsetDecoder decoderICU = new CharsetProviderICU().charsetForName("ibm-971").newDecoder();
            CharBuffer out = CharBuffer.allocate(3);
            decoderICU.onMalformedInput(CodingErrorAction.REPLACE);
            CoderResult result = decoderICU.decode(ByteBuffer.wrap(new byte[] { (byte)0xA2, (byte)0xAE, (byte)0x12, (byte)0x34, (byte)0xEF, (byte)0xDC }), out, true);
            if(!result.isError()){
                char[] expected = {'\u00a1', '\ufffd', '\u6676'};
                if(!equals(expected, out.array())){
                    errln("Did not get the expected result for substitution chars. Got: "+
                           hex(out.array()));
                }
                logln("Output: "+  hex(out.array()));
            }else{
                errln("Decode operation failed for encoder: "+decoderICU.toString());
            }
        }
    }
    
    public void TestJB4897(){
        CharsetProviderICU provider = new CharsetProviderICU();
        Charset charset = provider.charsetForName("x-abracadabra");  
        if(charset!=null && charset.canEncode()== true){
            errln("provider.charsetForName() does not validate the charset names" );
        }
    }

    public void TestJB5027() {
        CharsetProviderICU provider= new CharsetProviderICU();

        Charset fake = provider.charsetForName("doesNotExist");
        if(fake != null){
            errln("\"doesNotExist\" returned " + fake);
        }
        Charset xfake = provider.charsetForName("x-doesNotExist");
        if(xfake!=null){
            errln("\"x-doesNotExist\" returned " + xfake);
        }
    }
    //test to make sure that number of aliases and canonical names are in the charsets that are in
    public void TestAllNames() {
        
        CharsetProviderICU provider= new CharsetProviderICU();
        Object[] available = CharsetProviderICU.getAvailableNames();
        for(int i=0; i<available.length;i++){
            try{
                String canon  = CharsetProviderICU.getICUCanonicalName((String)available[i]);

                // ',' is not allowed by Java's charset name checker
                if(canon.indexOf(',')>=0){
                    continue;
                }
                Charset cs = provider.charsetForName((String)available[i]);
              
                Object[] javaAliases =  cs.aliases().toArray();
                //seach for ICU canonical name in javaAliases
                boolean inAliasList = false;
                for(int j=0; j<javaAliases.length; j++){
                    String java = (String) javaAliases[j];
                    if(java.equals(canon)){
                        logln("javaAlias: " + java + " canon: " + canon);
                        inAliasList = true;
                    }
                }
                if(inAliasList == false){
                    errln("Could not find ICU canonical name: "+canon+ " for java canonical name: "+ available[i]+ " "+ i);
                }
            }catch(UnsupportedCharsetException ex){
                errln("could no load charset "+ available[i]+" "+ex.getMessage());
                continue;
            }
        }
    }
    public void TestDecoderImplFlush() {
        CharsetProviderICU provider = new CharsetProviderICU();
        Charset ics = provider.charsetForName("UTF-16");
        Charset jcs = Charset.forName("UTF-16"); // Java's UTF-16 charset
        execDecoder(jcs);
        execDecoder(ics);
    }
    public void TestEncoderImplFlush() {
        CharsetProviderICU provider = new CharsetProviderICU();
        Charset ics = provider.charsetForName("UTF-16");
        Charset jcs = Charset.forName("UTF-16"); // Java's UTF-16 charset
        execEncoder(jcs);
        execEncoder(ics);
    }
    private void execDecoder(Charset cs){
        CharsetDecoder decoder = cs.newDecoder();
        decoder.onMalformedInput(CodingErrorAction.REPORT);
        decoder.onUnmappableCharacter(CodingErrorAction.REPORT);
        CharBuffer out = CharBuffer.allocate(10);
        CoderResult result = decoder.decode(ByteBuffer.wrap(new byte[] { -1,
                -2, 32, 0, 98 }), out, false);
        result = decoder.decode(ByteBuffer.wrap(new byte[] { 98 }), out, true);

        logln(cs.getClass().toString()+ ":" +result.toString());
        try {
            result = decoder.flush(out);
            logln(cs.getClass().toString()+ ":" +result.toString());
        } catch (Exception e) {
            errln(e.getMessage()+" "+cs.getClass().toString());
        }
    }
    private void execEncoder(Charset cs){
        CharsetEncoder encoder = cs.newEncoder();
        encoder.onMalformedInput(CodingErrorAction.REPORT);
        encoder.onUnmappableCharacter(CodingErrorAction.REPORT);
        ByteBuffer out = ByteBuffer.allocate(10);
        CoderResult result = encoder.encode(CharBuffer.wrap(new char[] { '\uFFFF',
                '\u2345', 32, 98 }), out, false);
        logln(cs.getClass().toString()+ ":" +result.toString());
        result = encoder.encode(CharBuffer.wrap(new char[] { 98 }), out, true);

        logln(cs.getClass().toString()+ ":" +result.toString());
        try {
            result = encoder.flush(out);
            logln(cs.getClass().toString()+ ":" +result.toString());
        } catch (Exception e) {
            errln(e.getMessage()+" "+cs.getClass().toString());
        }
    }
    public void TestDecodeMalformed() {
        CharsetProviderICU provider = new CharsetProviderICU();
        Charset ics = provider.charsetForName("UTF-16BE");
        //Use SUN's charset
        Charset jcs = Charset.forName("UTF-16");
        CoderResult ir = execMalformed(ics);
        CoderResult jr = execMalformed(jcs);
        if(ir!=jr){
            errln("ICU's decoder did not return the same result as Sun. ICU: "+ir.toString()+" Sun: "+jr.toString());
        }
    }
    private CoderResult execMalformed(Charset cs){
        CharsetDecoder decoder = cs.newDecoder();
        decoder.onMalformedInput(CodingErrorAction.IGNORE);
        decoder.onUnmappableCharacter(CodingErrorAction.REPORT);
        ByteBuffer in = ByteBuffer.wrap(new byte[] { 0x00, 0x41, 0x00, 0x42, 0x01 });
        CharBuffer out = CharBuffer.allocate(3);
        return decoder.decode(in, out, true);
    }
    
    public void TestJavaUTF16Decoder(){
        CharsetProviderICU provider = new CharsetProviderICU();
        Charset ics = provider.charsetForName("UTF-16BE");
        //Use SUN's charset
        Charset jcs = Charset.forName("UTF-16");
        Exception ie = execConvertAll(ics);
        Exception je = execConvertAll(jcs);
        if(ie!=je){
            errln("ICU's decoder did not return the same result as Sun. ICU: "+ie.toString()+" Sun: "+je.toString());
        }
    }
    private Exception execConvertAll(Charset cs){
        ByteBuffer in = ByteBuffer.allocate(400);
        int i=0;
        while(in.position()!=in.capacity()){
            in.put((byte)0xD8);
            in.put((byte)i);
            in.put((byte)0xDC);
            in.put((byte)i);
            i++;
        }
        in.limit(in.position());
        in.position(0);
        CharsetDecoder decoder = cs.newDecoder();
        decoder.onMalformedInput(CodingErrorAction.IGNORE);
        decoder.onUnmappableCharacter(CodingErrorAction.REPORT);
        try{
            CharBuffer out = decoder.decode(in);
            if(out!=null){
                logln(cs.toString()+" encoing succeeded as expected!");
            }
        }catch ( Exception ex){
            errln("Did not get expected exception for encoding: "+cs.toString());
            return ex;
        }
        return null;
    }
}