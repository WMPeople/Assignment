package com.worksmobile.Assignment.Service;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

public class Compress {

	// 출처 : https://stackoverflow.com/questions/357851/in-java-how-to-zip-file-from-byte-array
	public static byte[] zipBytes(String filename, byte[] input) throws IOException {
	    ByteArrayOutputStream baos = new ByteArrayOutputStream();
	    ZipOutputStream zos = new ZipOutputStream(baos);
	    ZipEntry entry = new ZipEntry(filename);
	    entry.setSize(input.length);
	    zos.putNextEntry(entry);
	    zos.write(input);
	    zos.closeEntry();
	    zos.close();
	    return baos.toByteArray();
	}
	
	public static byte[] unzip(byte[] zippedBytes) throws IOException {
		byte[] unzippedBytes = null;
		ByteArrayInputStream bis = new ByteArrayInputStream(zippedBytes);
		//get the zip file content
    	ZipInputStream zis = new ZipInputStream(bis);
    	//get the zipped file list entry
    	ZipEntry ze = zis.getNextEntry();
    	
    	while(ze!=null){
     	     //String fileName = ze.getName();
             unzippedBytes = zis.readAllBytes();
             ze = zis.getNextEntry();
     	}
     	
        zis.closeEntry();
     	zis.close();
     	
     	return unzippedBytes;
	}

	public static byte[] compress(String plainText) throws IOException {
		if(plainText == null) {
			return null;
		}
		byte[] stringToBytes = plainText.getBytes(StandardCharsets.UTF_8);
		return Compress.zipBytes("memory.one", stringToBytes);
	}
	
	public static String deCompress(byte[] compressed) throws IOException {
		if(compressed == null) {
			return null;
		}
		byte[] contentByte = Compress.unzip(compressed);
		return new String(contentByte, StandardCharsets.UTF_8);
	}
}
