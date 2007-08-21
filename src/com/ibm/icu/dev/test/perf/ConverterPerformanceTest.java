/*
 *******************************************************************************
 * Copyright (C) 2002-2007, International Business Machines Corporation and    *
 * others. All Rights Reserved.                                                *
 *******************************************************************************
 */
package com.ibm.icu.dev.test.perf;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.charset.Charset;
import java.nio.charset.CharsetDecoder;
import java.nio.charset.CharsetEncoder;
import java.nio.charset.CodingErrorAction;

import sun.io.CharToByteConverter;

import com.ibm.icu.charset.CharsetProviderICU;

/**
 * @author ram
 */
public class ConverterPerformanceTest extends PerfTest {
   public static void main(String[] args) throws Exception {
       new ConverterPerformanceTest().run(args);
   }
   char[] unicodeBuffer = null;
   byte[] encBuffer = null;

   protected void setup(String[] args) {
        try{
            // read in the input file, being careful with a possible BOM
            FileInputStream in = new FileInputStream(fileName);
            BOMFreeReader reader = new BOMFreeReader(in, encoding);
            unicodeBuffer = readToEOS(reader);
            
            // use java.nio to convert unicodeBuffer from char[] to byte[] 
            CharBuffer source = CharBuffer.wrap(unicodeBuffer, 0, unicodeBuffer.length);
            CharsetEncoder encoder = Charset.forName(encoding).newEncoder();
            encoder.onMalformedInput(CodingErrorAction.REPORT);
            encoder.onUnmappableCharacter(CodingErrorAction.REPORT);
            ByteBuffer target = encoder.encode(source);
            
            CharToByteConverter conv = CharToByteConverter.getConverter("UTF-8");
            conv.setSubstitutionMode(false);
            byte[] encBuffer2 = conv.convertAll(unicodeBuffer);
            
            boolean b = true;
            // target.array() will probably return what we want, but lets take no chances
            encBuffer = new byte[target.limit()];
            for (int i=0; i<encBuffer.length; i++) {
                encBuffer[i] = target.get(i);
                if (encBuffer[i] != encBuffer2[i]) {
                    System.out.println("difference: " + Integer.toHexString(0xff & encBuffer[i]) + " " + Integer.toHexString(0xff & encBuffer2[i]));
                    b = false;
                }
            }
            System.out.println(b);
            System.out.println(encBuffer.length);
            System.out.println(encBuffer2.length);
            
            
            
            // we created some heavy objects, so lets try to clean up a little
            gc();
            
        } catch(Exception e){
            e.printStackTrace();
            throw new RuntimeException(e.getMessage());
        }

   }

   PerfTest.Function TestFromUnicodeStream() {
        return new PerfTest.Function() {
            public void call() {
                try{
                    ByteArrayOutputStream out = new ByteArrayOutputStream(unicodeBuffer.length * 10);
                    OutputStreamWriter writer = new OutputStreamWriter(out, testName);
                    writer.write(unicodeBuffer, 0, unicodeBuffer.length);
                    writer.flush();
                }catch(Exception e){
                    e.printStackTrace();
                    throw new RuntimeException(e.getMessage());
                }
            }
            public long getOperationsPerIteration() {
                return unicodeBuffer.length;
            }
        };
    }
    PerfTest.Function TestToUnicodeStream() {
        return new PerfTest.Function() {
            char[] dst = new char[encBuffer.length];
            public void call() {
                try{
                    ByteArrayInputStream is = new ByteArrayInputStream(encBuffer, 0, encBuffer.length);
                    InputStreamReader reader = new InputStreamReader(is, testName);
                    reader.read(dst, 0, dst.length);
                    reader.close();
                }catch(Exception e){
                    e.printStackTrace();
                    throw new RuntimeException(e.getMessage());
                }
            }
            public long getOperationsPerIteration() {
                return encBuffer.length;
            }
        };
    }
/*
    PerfTest.Function TestByteToCharConverter() { // decoder  charset.forname().newencoder().decode
        try{
            return new PerfTest.Function() {
                char[] dst = new char[encBuffer.length];
                int numOut =0;
                ByteToCharConverter conv = ByteToCharConverter.getConverter(testEncoderName);
                int num =0;
                public void call() {
                    try{
                        numOut= conv.convert(encBuffer, 0, encBuffer.length, dst, 0,dst.length);
                        conv.reset();
                    }catch(Exception e){
                        e.printStackTrace();
                        throw new RuntimeException(e.getMessage());
                    }
                }
                public long getOperationsPerIteration() {
                    return encBuffer.length;
                }
            };
        }catch(Exception e){
            e.printStackTrace();
            throw new RuntimeException(e.getMessage());
        }
    }

    PerfTest.Function TestCharToByteConverter() { // encoder charset.forname().newencoder().encode
        try{
            return new PerfTest.Function() {
                byte[] dst = new byte[encBuffer.length];
                int numOut =0;
                CharToByteConverter conv = CharToByteConverter.getConverter(testEncoderName);
                int num =0;
                public void call() {
                    try{
                        numOut= conv.convert(unicodeBuffer, 0,unicodeBuffer.length,dst,0, dst.length);
                        conv.reset();
                    }catch(Exception e){
                        e.printStackTrace();
                        throw new RuntimeException(e.getMessage());
                    }
                }
                public long getOperationsPerIteration() {
                    return unicodeBuffer.length;
                }
            };
        }catch(Exception e){
            e.printStackTrace();
            throw new RuntimeException(e.getMessage());
        }
    }

    PerfTest.Function TestByteToCharConverterICU() { // decoder  charsetprovidericu.getdecoder
        try{
            return new PerfTest.Function() {
                char[] dst = new char[encBuffer.length];
                int numOut =0;
                ByteToCharConverter conv = ByteToCharConverterICU.createConverter(testEncoderName);
                int num =0;
                public void call() {
                    try{
                        numOut= conv.convert(encBuffer, 0, encBuffer.length, dst, 0,dst.length);
                        conv.reset();
                    }catch(Exception e){
                        e.printStackTrace();
                        throw new RuntimeException(e.getMessage());
                    }
                }
                public long getOperationsPerIteration() {
                    return encBuffer.length;
                }
            };
        }catch(Exception e){
            e.printStackTrace();
            throw new RuntimeException(e.getMessage());
        }
    }

    PerfTest.Function TestCharToByteConverterICU() {
        try{
            return new PerfTest.Function() {
                byte[] dst = new byte[encBuffer.length*2];
                int numOut =0;
                CharToByteConverter conv = CharToByteConverterICU.createConverter(testEncoderName);
                int num =0;
                public void call() {
                    try{
                        numOut= conv.convert(unicodeBuffer, 0,unicodeBuffer.length,dst,0, dst.length);
                        conv.reset();
                    }catch(Exception e){
                        e.printStackTrace();
                        throw new RuntimeException(e.getMessage());
                    }
                }
                public long getOperationsPerIteration() {
                    return unicodeBuffer.length;
                }
            };
        }catch(Exception e){
            e.printStackTrace();
            throw new RuntimeException(e.getMessage());
        }
    }
*/
    PerfTest.Function TestCharsetDecoder() {
        try{
            return new PerfTest.Function() {
                CharBuffer outBuf = CharBuffer.allocate(unicodeBuffer.length);
                Charset myCharset = Charset.forName(testName);
                ByteBuffer srcBuf = ByteBuffer.wrap(encBuffer,0,encBuffer.length);
                CharsetDecoder decoder = myCharset.newDecoder();

                public void call() {
                    try{
                        decoder.decode(srcBuf,outBuf,false);
                        decoder.reset();
                        srcBuf.rewind();
                        outBuf.rewind();
                    }catch(Exception e){
                        e.printStackTrace();
                        throw new RuntimeException(e.getMessage());
                    }
                }
                public long getOperationsPerIteration() {
                    return encBuffer.length;
                }
            };
        }catch(Exception e){
            e.printStackTrace();
            throw new RuntimeException(e.getMessage());
        }
    }

    PerfTest.Function TestCharsetEncoder() {
        try{
            return new PerfTest.Function() {
                ByteBuffer outBuf = ByteBuffer.allocate(encBuffer.length);
                Charset myCharset = Charset.forName(testName);
                CharBuffer srcBuf = CharBuffer.wrap(unicodeBuffer,0,unicodeBuffer.length);
                CharsetEncoder encoder = myCharset.newEncoder();

                public void call() {
                    try{
                        encoder.encode(srcBuf,outBuf,false);
                        encoder.reset();
                        srcBuf.rewind();
                        outBuf.rewind();
                    }catch(Exception e){
                        e.printStackTrace();
                        throw new RuntimeException(e.getMessage());
                    }
                }
                public long getOperationsPerIteration() {
                    return unicodeBuffer.length;
                }
            };
        }catch(Exception e){
            e.printStackTrace();
            throw new RuntimeException(e.getMessage());
        }
    }

    PerfTest.Function TestCharsetDecoderICU() {
        try{
            return new PerfTest.Function() {
                CharBuffer outBuf = CharBuffer.allocate(unicodeBuffer.length);
                Charset myCharset = new CharsetProviderICU().charsetForName(testName);
                ByteBuffer srcBuf = ByteBuffer.wrap(encBuffer,0,encBuffer.length);
                CharsetDecoder decoder = myCharset.newDecoder();

                public void call() {
                    try{
                        decoder.decode(srcBuf,outBuf,false);
                        decoder.reset();
                        srcBuf.rewind();
                        outBuf.rewind();
                    }catch(Exception e){
                        e.printStackTrace();
                        throw new RuntimeException(e.getMessage());
                    }
                }
                public long getOperationsPerIteration() {
                    return encBuffer.length;
                }
            };
        }catch(Exception e){
            e.printStackTrace();
            throw new RuntimeException(e.getMessage());
        }
    }

    PerfTest.Function TestCharsetEncoderICU() {
        try{
            return new PerfTest.Function() {
                ByteBuffer outBuf = ByteBuffer.allocate(encBuffer.length);
                Charset myCharset = new CharsetProviderICU().charsetForName(testName);
                CharBuffer srcBuf = CharBuffer.wrap(unicodeBuffer,0,unicodeBuffer.length);
                CharsetEncoder encoder = myCharset.newEncoder();

                public void call() {
                    try{
                        encoder.encode(srcBuf,outBuf,false);
                        encoder.reset();
                        srcBuf.rewind();
                        outBuf.rewind();
                    }catch(Exception e){
                        e.printStackTrace();
                        throw new RuntimeException(e.getMessage());
                    }
                }
                public long getOperationsPerIteration() {
                    return unicodeBuffer.length;
                }
            };
        }catch(Exception e){
            e.printStackTrace();
            throw new RuntimeException(e.getMessage());
        }
    }
}
