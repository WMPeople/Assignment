package com.worksmobile.assignment.bo;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

/**
 * 
 * @author khh
 *
 */
public class ZipCompression implements Compression{
	
	public static final String ZIP_FILE_NAME = "memory.one";

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
		ZipInputStream zis = new ZipInputStream(bis);
		ZipEntry ze = zis.getNextEntry();

		while (ze != null) {
			unzippedBytes = zis.readAllBytes();
			ze = zis.getNextEntry();
		}

		zis.closeEntry();
		zis.close();

		return unzippedBytes;
	}

	public byte[] compress(byte[] byteArr) throws IOException {
		if (byteArr == null) {
			return null;
		}
		return zipBytes(ZIP_FILE_NAME, byteArr);
	}

	public byte[] deCompress(byte[] compressed) throws IOException {
		if (compressed == null) {
			return null;
		}
		byte[] contentByte = unzip(compressed);
		return contentByte;
	}
}
