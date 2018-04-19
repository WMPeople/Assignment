package com.worksmobile.assignment.bo;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

import com.worksmobile.assignment.model.Board;
import com.worksmobile.assignment.model.BoardHistory;
import com.worksmobile.assignment.util.JsonUtils;

public class Compress {

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

	public static byte[] compress(String plainText) throws IOException {
		if (plainText == null) {
			return null;
		}
		byte[] stringToBytes = plainText.getBytes(StandardCharsets.UTF_8);
		return Compress.zipBytes("memory.one", stringToBytes);
	}

	public static String deCompress(byte[] compressed) throws IOException {
		if (compressed == null) {
			return null;
		}
		byte[] contentByte = Compress.unzip(compressed);
		return new String(contentByte, StandardCharsets.UTF_8);
	}

	public static byte[] compressArticleContent(Board article) throws RuntimeException {
		byte[] compressedContent = null;
		try {
			compressedContent = Compress.compress(article.getContent());
		} catch (IOException e) {
			e.printStackTrace();
			String json = JsonUtils.jsonStringIfExceptionToString(article);
			throw new RuntimeException("게시글 내용을 압축에 실패하였습니다. \n게시글 : " + json);
		}
		return compressedContent;
	}

	public static String deCompressHistoryContent(BoardHistory boardHistory) throws RuntimeException {
		String content = null;
		try {
			content = Compress.deCompress(boardHistory.getHistory_content());
		} catch (IOException e) {
			e.printStackTrace();
			String history = JsonUtils.jsonStringIfExceptionToString(boardHistory);
			throw new RuntimeException("게시글 내용 압축 해제 실패 \nhistory : " + history);
		}
		return content;
	}
}
