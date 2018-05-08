package com.worksmobile.assignment.bo;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThat;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

import org.hamcrest.core.IsNot;
import org.junit.Before;
import org.junit.Test;

import com.worksmobile.assignment.model.BoardHistory;

public class BoardHistoryCompressTest {
	private BoardHistory smallContentHistory;
	private BoardHistory bigContentHistory;
	
	private String smallContent = "1234567890";
	private String bigContent;
	
	@Before
	public void before() {
		smallContentHistory = new BoardHistory("2018CREATED", BoardHistory.STATUS_CREATED, "smallSub", new byte[0], false, 1, 2, 3);
		bigContentHistory = new BoardHistory("20182CREATED", BoardHistory.STATUS_MODIFIED, "bigSub", new byte[1], true, 4, 5, 6);
		
		StringBuilder builder = new StringBuilder(smallContent);
		for(int i = 0; i < 1000; i++) {
			builder.append(smallContent);
		}
		bigContent = builder.toString();
	}
	
	@Test
	public void testSetHistoryContentWhenCompressSizeIsBigger() throws IOException {
		smallContentHistory = BoardHistoryCompress.setContent(smallContentHistory, smallContent);
		assertEquals(smallContentHistory.is_content_compressed(), false);
		assertNotNull(smallContentHistory.getHistory_content());
		assertArrayEquals(smallContentHistory.getHistory_content(), smallContent.getBytes(StandardCharsets.UTF_8));
	}
	
	@Test
	public void testSetHistoryContentWhenCompressSizeIsSmaller() throws IOException {
		bigContentHistory = BoardHistoryCompress.setContent(bigContentHistory, bigContent);
		assertEquals(bigContentHistory.is_content_compressed(), true);
		assertNotNull(bigContentHistory.getHistory_content());
		assertThat(bigContentHistory.getHistory_content(), IsNot.not(equalTo(bigContent.getBytes(StandardCharsets.UTF_8))));
	}
	
	@Test
	public void testGetHistoryContentWhenNotcompressed() throws IOException {
		testSetHistoryContentWhenCompressSizeIsBigger();
		String deCompressedStr = BoardHistoryCompress.getDeCompressedContent(smallContentHistory);
		assertEquals(smallContent, deCompressedStr);
	}
	
	@Test
	public void testGetHistoryContentWhenCompreesed() throws IOException {
		testSetHistoryContentWhenCompressSizeIsSmaller();
		String deCompressedStr = BoardHistoryCompress.getDeCompressedContent(bigContentHistory);
		assertEquals(bigContent, deCompressedStr);
	}
}
