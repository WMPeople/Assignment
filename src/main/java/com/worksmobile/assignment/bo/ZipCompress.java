package com.worksmobile.assignment.bo;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

public class ZipCompress implements Compress{
	
	public static final String ZIP_FILE_NAME = "memory.one";

	// TODO : 인터페이스와 클래스로 분리하자.
	// 전략 패턴, 템플릿 메서드 패턴.
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

	public byte[] compress(String plainText) throws IOException {
		if (plainText == null) {
			return null;
		}
		byte[] stringToBytes = plainText.getBytes(StandardCharsets.UTF_8);
		return zipBytes(ZIP_FILE_NAME, stringToBytes);
	}

	public String deCompress(byte[] compressed) throws IOException {
		if (compressed == null) {
			return null;
		}
		byte[] contentByte = unzip(compressed);
		return new String(contentByte, StandardCharsets.UTF_8);
	}
}
