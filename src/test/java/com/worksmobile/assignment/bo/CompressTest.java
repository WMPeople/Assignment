package com.worksmobile.assignment.bo;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Scanner;

import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import com.worksmobile.assignment.mapper.BoardHistoryMapper;
import com.worksmobile.assignment.mapper.BoardMapper;
import com.worksmobile.assignment.model.Board;
import com.worksmobile.assignment.model.BoardHistory;

@RunWith(SpringRunner.class)
@SpringBootTest
public class CompressTest {
	
	@Autowired
	private BoardMapper boardMapper;
	
	@Autowired
	private BoardHistoryMapper boardHistoryMapper;
	
	private final String plainText = "CDDCACBCBCCCBBCDA";
	private final String hangulText = "가나다라마바사abcefegjwegioj";
	
	@Test
	public void testZipping() throws IOException {
		byte[] stringToBytes = plainText.getBytes(StandardCharsets.UTF_8);
		byte[] zippedBytes = Compress.zipBytes(Compress.ZIP_FILE_NAME, stringToBytes);
		byte[] unzippedBytes = Compress.unzip(zippedBytes);
		assertFalse(Arrays.equals(zippedBytes, unzippedBytes));
		assertTrue(Arrays.equals(stringToBytes, unzippedBytes));
		String unzippedStr = new String(unzippedBytes, StandardCharsets.UTF_8);
		assertEquals(plainText, unzippedStr);
		System.out.println(stringToBytes.length);
		System.out.println(zippedBytes.length);
	}
	
	@Test
	public void testHangulZipping() throws IOException {
		byte[] stringToBytes = hangulText.getBytes(StandardCharsets.UTF_8);
		byte[] zippedBytes = Compress.zipBytes(Compress.ZIP_FILE_NAME, stringToBytes);
		byte[] unzippedBytes = Compress.unzip(zippedBytes);
		assertFalse(Arrays.equals(zippedBytes, unzippedBytes));
		assertTrue(Arrays.equals(stringToBytes, unzippedBytes));
		String unzippedStr = new String(unzippedBytes, StandardCharsets.UTF_8);
		assertEquals(hangulText, unzippedStr);
	}
	
	@Test
	public void testZippingSizeCompare() throws IOException {
		StringBuilder sevenHundredThousandBuilder = new StringBuilder(); 
		for(int i = 0; i < 70000; i++) {
			sevenHundredThousandBuilder.append("가나다라마바사아자차");	// 10자
		}
		byte[] stringToBytes = sevenHundredThousandBuilder.toString().getBytes(StandardCharsets.UTF_8);
		System.out.println("압축하지 않은 문자열 크기 : " + stringToBytes.length + "bytes");
		
		byte[] zippedBytes = Compress.zipBytes(Compress.ZIP_FILE_NAME, stringToBytes);
		System.out.println("압축된 문자열 바이트 크기 : " + zippedBytes.length + "bytes");
		
		byte[] unzippedBytes = Compress.unzip(zippedBytes);
		assertFalse(Arrays.equals(zippedBytes, unzippedBytes));
		assertTrue(Arrays.equals(stringToBytes, unzippedBytes));
		String unZippedStr = new String(unzippedBytes, StandardCharsets.UTF_8);
		assertEquals(sevenHundredThousandBuilder.toString(), unZippedStr);
	}
	
	@Test
	public void testZippingFromFile() throws IOException {
		File file = new File("C:\\txt\\인터넷기사.txt");
		Scanner scan = new Scanner(file);
		StringBuilder textBuilder = new StringBuilder();
		while (scan.hasNextLine()) {
			textBuilder.append(scan.nextLine());
		}
		scan.close();

		byte[] stringToBytes = textBuilder.toString().getBytes(StandardCharsets.UTF_8);
		System.out.println("압축하지 않은 문자열 크기 : " + stringToBytes.length + "bytes");
		
		byte[] zippedBytes = Compress.zipBytes(Compress.ZIP_FILE_NAME, stringToBytes);
		System.out.println("압축된 문자열 바이트 크기 : " + zippedBytes.length + "bytes");
		
		byte[] unzippedBytes = Compress.unzip(zippedBytes);
		assertFalse(Arrays.equals(zippedBytes, unzippedBytes));
		assertTrue(Arrays.equals(stringToBytes, unzippedBytes));
		String unZippedStr = new String(unzippedBytes, StandardCharsets.UTF_8);
		assertEquals(textBuilder.toString(), unZippedStr);
	}
	
	@Test
	public void testTimeZippingFromFile() throws IOException {

		File file = new File("C:\\txt\\7.txt");
		Scanner scan = new Scanner(file);
		StringBuilder textBuilder = new StringBuilder();
		while (scan.hasNextLine()) {
			textBuilder.append(scan.nextLine());
		}
		scan.close();

		long start = System.currentTimeMillis();

		for (int i = 0; i < 10000; i++) {
			byte[] stringToBytes = textBuilder.toString().getBytes(StandardCharsets.UTF_8);

			@SuppressWarnings("unused")
			byte[] zippedBytes = Compress.zipBytes(Compress.ZIP_FILE_NAME, stringToBytes);
		}

		long end = System.currentTimeMillis();
		System.out.println(end - start);

		byte[] zippedBytes = Compress.zipBytes(Compress.ZIP_FILE_NAME,
			textBuilder.toString().getBytes(StandardCharsets.UTF_8));

		long start2 = System.currentTimeMillis();

		for (int i = 0; i < 10000; i++) {
			@SuppressWarnings("unused")
			String unzipped = Compress.deCompress(zippedBytes);
		}

		long end2 = System.currentTimeMillis();

		System.out.println(end2 - start2);
	}

	@Test
	@Ignore
	public void testDBEquals() throws IOException {
		HashMap<String, Integer> map = new HashMap<>();
		map.put("offset", 0);
		map.put("noOfRecords", Integer.MAX_VALUE);
		List<Board> boardList = boardMapper.articleList(map);
		List<String> contentStrList = new ArrayList<>(boardList.size());
		List<byte[]> historyContentList = new ArrayList<>(boardList.size());
		List<Boolean> compressResult = new ArrayList<>(boardList.size());
		List<Boolean> decompressResult = new ArrayList<>(boardList.size());
		
		for (Board board : boardList) {
			contentStrList.add(board.getContent());
			BoardHistory history = boardHistoryMapper.selectHistory(board);
			assertNotNull(history);
			historyContentList.add(history.getHistory_content());
		}
		
		for(int i = 0; i < boardList.size(); i++) {
			byte[] compressedBytes = Compress.compress(contentStrList.get(i));
			String deCompressed = Compress.deCompress(historyContentList.get(i));
			compressResult.add(Arrays.equals(compressedBytes, historyContentList.get(i)));
			decompressResult.add(deCompressed == null ? 
					(contentStrList.get(i) == null ? true : false)
										: deCompressed.equals(contentStrList.get(i))
								);
		}
		
		boolean allResult = false;
		for(int i = 0; i < boardList.size(); i++) {
			if(!compressResult.get(i)) {
				System.out.println(i + "에러 인가??");
				//allResult = true;
			}
			if(!decompressResult.get(i)) {
				System.out.println(i + "에러");
				allResult = true;
			}
		}
		assertFalse(allResult);
	}
}
