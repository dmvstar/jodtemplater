/*
 *
 *  $Id: UUDecode.java,v 0.0.2 2007/10/02 17:20 sdv Exp $
 *
 *  Copyright (C) 2006-2007 Dmitry Starjinsky
 *
 *  File :               UUDecode.java
 *  Description :        uudecode
 *  Author's email :     dvstar@users.sourceforge.net
 *  Author's Website :   http://swirl.sourceforge.net
 *
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2 of the License, or (at your option) any later version.
 *
 * This library is distributed inputStream the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the
 * Free Software Foundation, Inc., 59 Temple Place - Suite 330,
 * Boston, MA 02111-1307, USA.
 *
 * Created on 09 жотня 2007, 18:32
 *
 */

package org.stardust.libreoffice.libretempla.utils;

import java.io.*;

public class UUEncoDeco {
    BufferedReader   bufferedReader;
    DataOutputStream dataOutputStream;
    OutputStream outputStream;
    
    static void debug(String str) {
        System.err.println("UUDecode: " + str);
    }
    
    // To get a string without the first n words inputStream string str.
    public static String skipWords(String str, int n) {
        int i=0;
        
        while (i<str.length() && Character.isSpaceChar(str.charAt(i))) {i++;}
        
        while (n>0) {
            while (i<str.length() && !Character.isSpaceChar(str.charAt(i))) {i++;}
            while (i<str.length() && Character.isSpaceChar(str.charAt(i))) {i++;}
            n--;
        }
       
        return(str.substring(i));
    }
    
    // To get the first word inputStream a string. Returns a string with all characters
    // found before the first space character.
    public static String getFirstWord(String str) {
        int i=0;
        while (i<str.length() && !Character.isSpaceChar(str.charAt(i))) {i++;}
        return(str.substring(0,i));
    }
    
    public static String getWord(String str, int n) {
        return(getFirstWord(skipWords(str,n)));
    }

    protected static final int DEFAULT_MODE = 644;
    private static final int MAX_CHARS_PER_LINE = 45;
    private String name;
    private String storeFileName = null;

    public UUEncoDeco() {
    }

    public UUEncoDeco(String name) {
        this.name = name;
    }
    
    public void processDecode(InputStream in) throws CoderException {
        processDecode(in, (String)null);
    }
    
    public void processDecode(InputStream in, String storeFileName) throws CoderException {
        this.bufferedReader = new BufferedReader(new InputStreamReader(in));
        this.dataOutputStream = null;
        this.storeFileName = storeFileName;
        this.decodeInputStream();
    }

    public void processDecode(InputStream in, OutputStream os) throws CoderException {
        this.bufferedReader = new BufferedReader(new InputStreamReader(in));
        this.dataOutputStream = new DataOutputStream( os );
        this.storeFileName = null;
        this.decodeInputStream();
    }


    protected byte[] decodeString3(String str) throws CoderException {
        ByteArrayOutputStream byteOutputStream = new ByteArrayOutputStream();
        int c0=str.charAt(0)^0x20;
        int c1=str.charAt(1)^0x20;
        int c2=str.charAt(2)^0x20;
        int c3=str.charAt(3)^0x20;
        byteOutputStream.write( ((c0<<2) & 0xfc) | ((c1>>4) & 0x3) );
        byteOutputStream.write( ((c1<<4) & 0xf0) | ((c2>>2) & 0xf) );
        byteOutputStream.write( ((c2<<6) & 0xc0) | ((c3) & 0x3f) );
        return byteOutputStream.toByteArray();
    }
    
    protected byte[] decodeString2(String str) throws CoderException {
        ByteArrayOutputStream byteOutputStream = new ByteArrayOutputStream();
        int c0=str.charAt(0)^0x20;
        int c1=str.charAt(1)^0x20;
        int c2=str.charAt(2)^0x20;
        byteOutputStream.write( ((c0<<2) & 0xfc) | ((c1>>4) & 0x3) );
        byteOutputStream.write( ((c1<<4) & 0xf0) | ((c2>>2) & 0xf) );
        return byteOutputStream.toByteArray();
    }
    
    protected byte[] decodeString1(String str) throws CoderException {
        ByteArrayOutputStream byteOutputStream = new ByteArrayOutputStream();
        int c0=str.charAt(0)^0x20;
        int c1=str.charAt(1)^0x20;
        byteOutputStream.write( ((c0<<2) & 0xfc) | ((c1>>4) & 0x3) );
        return byteOutputStream.toByteArray();
    }
    
    public byte[] decodeLine(String strLine) throws CoderException {
        byte[] ret = null;
        ByteArrayOutputStream byteOutputStream = new ByteArrayOutputStream();
        
        int pos=1;
        int d=0;
        
        int len=((strLine.charAt(0)&0x3f)^0x20);
        
        try {
            
            while ((d+3<=len) && (pos+4<=strLine.length())) {
                byteOutputStream.write( decodeString3(strLine.substring(pos,pos+4)) );
                pos+=4;
                d+=3;
            }
            
            if ((d+2<=len) && (pos+3<=strLine.length())) {
                byteOutputStream.write(decodeString2(strLine.substring(pos,pos+3)) );
                pos+=3;
                d+=2;
            }
            
            if ((d+1<=len) && (pos+2<=strLine.length())) {
                byteOutputStream.write(decodeString1(strLine.substring(pos,pos+2)) );
                pos+=2;
                d+=1;
            }
            
            ret = byteOutputStream.toByteArray();
            
        } catch (IOException ex) {
            ex.printStackTrace();
            throw new CoderException(ex);
        }
        if (d!=len) { throw new CoderException("did not get all"); }
        
        return(ret);
    }
    
    public void decodeInputStream(  ) throws CoderException {
        String str;
        boolean more=true;
        int n=0;
        
        try {
            while(more) {
                // read inputStream a line
                str = bufferedReader.readLine();
                if (str == null) {more=false;break;}
                
                if ( str.startsWith("begin ") ) {
                    debug(str);

                    if(storeFileName==null){
                        String fileName=getWord(str,2);
                        //debug(fileName);
                        if (fileName.length()==0) {
                            break;
                        }
                        storeFileName = fileName;
                    }

                    if(dataOutputStream==null) {
                        dataOutputStream = new DataOutputStream(new FileOutputStream(storeFileName));
                    }
                    
                    for(;;) {
                        str = bufferedReader.readLine();
                        if (str == null) {more=false;break;}
                        if (str.equals("end")) {
                            break;
                        }
                        dataOutputStream.write( decodeLine(str) );
                    }
                    
                    dataOutputStream.close();
                    
                    n++;
                }
            }
        } catch (IOException e) {
            throw new CoderException("run: "+e);
        } finally {
            try {
                if (bufferedReader!=null) {bufferedReader.close();}
                if (dataOutputStream!=null) {dataOutputStream.close();}
            } catch (IOException e) {throw new CoderException("finally: "+e);}
        }
    }

    /**
     * UUEncode bytes from the input stream, and write them as text characters
     * to the output stream. This method will run until it exhausts the
     * input stream.
     * @param is the input stream.
     * @param out the output stream.
     * @throws IOException if there is an error.
     */
    public void processEncode(InputStream is, OutputStream out)
        throws IOException {
        this.outputStream = out;
        encodeBegin();
        byte[] buffer = new byte[MAX_CHARS_PER_LINE * 100];
        int count;
        while ((count = is.read(buffer, 0, buffer.length)) != -1) {
            int pos = 0;
            while (count > 0) {
                int num = count > MAX_CHARS_PER_LINE
                    ? MAX_CHARS_PER_LINE
                    : count;
                encodeLine(buffer, pos, num, out);
                pos += num;
                count -= num;
            }
        }
        out.flush();
        encodeEnd();
    }

    /**
     * Encode a string to the output.
     */
    private void encodeString(String n) throws IOException {
        PrintStream writer = new PrintStream(outputStream);
        writer.print(n);
        writer.flush();
    }

    private void encodeBegin() throws IOException {
        encodeString("begin " + DEFAULT_MODE + " " + name + "\n");
    }

    private void encodeEnd() throws IOException {
        encodeString(" \nend\n");
    }

    /**
     * Encode a single line of data (less than or equal to 45 characters).
     *
     * @param data   The array of byte data.
     * @param off    The starting offset within the data.
     * @param length Length of the data to encode.
     * @param out    The output stream the encoded data is written to.
     *
     * @exception IOException
     */
    private void encodeLine(
        byte[] data, int offset, int length, OutputStream out)
        throws IOException {
        // write out the number of characters encoded in this line.
        out.write((byte) ((length & 0x3F) + ' '));
        byte a;
        byte b;
        byte c;

        for (int i = 0; i < length;) {
            // set the padding defaults
            b = 1;
            c = 1;
            // get the next 3 bytes (if we have them)
            a = data[offset + i++];
            if (i < length) {
                b = data[offset + i++];
                if (i < length) {
                    c = data[offset + i++];
                }
            }

            byte d1 = (byte) (((a >>> 2) & 0x3F) + ' ');
            byte d2 = (byte) ((((a << 4) & 0x30) | ((b >>> 4) & 0x0F)) + ' ');
            byte d3 = (byte) ((((b << 2) & 0x3C) | ((c >>> 6) & 0x3)) + ' ');
            byte d4 = (byte) ((c & 0x3F) + ' ');

            out.write(d1);
            out.write(d2);
            out.write(d3);
            out.write(d4);
        }

        // terminate with a linefeed alone
        out.write('\n');
    }


    public DataOutputStream getOutputStream() {
        return dataOutputStream;
    }
  
    
    public static void main(String args[]) {
        if (args.length < 1 ) {
            System.out.println("Usage: java UUDecode <filename>");
            System.exit(0);
        }
        
        try {
            new UUEncoDeco().processDecode( new FileInputStream(args[0]) );
        } catch (FileNotFoundException ex) {
            ex.printStackTrace();
        } catch (CoderException ex) {
            ex.printStackTrace();
        } 
        //catch (IOException e) {
        //    e.printStackTrace();
        //    System.err.println("UUDecode: " + e);
        //}
        
    }
    
}

