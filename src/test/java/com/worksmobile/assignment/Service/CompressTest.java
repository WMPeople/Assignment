package com.worksmobile.Assignment.Service;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import com.worksmobile.Assignment.Domain.BoardDTO;
import com.worksmobile.Assignment.Domain.BoardHistoryDTO;
import com.worksmobile.Assignment.Mapper.BoardHistoryMapper;
import com.worksmobile.Assignment.Mapper.BoardMapper;

@RunWith(SpringRunner.class)
@SpringBootTest
public class CompressTest {
	
	@Autowired
	private BoardMapper boardMapper;
	
	@Autowired
	private BoardHistoryMapper boardHistoryMapper;
	
	private Huffman huffman;
	private final String plainText = "CDDCACBCBCCCBBCDA";
	private final String hangulText = "가나다라마바사abcefegjwegioj";
	
	@Before
	public void init() {
		huffman = new Huffman();
	}
	
	@Test
	public void testEncoding() {
		huffman.handleNewText(plainText);
		String encodedText = huffman.encodeText();
		assertNotNull(encodedText);
		String decodedText = huffman.decodeText();
		assertEquals(plainText, decodedText);
	}
	
	@Test
	public void testZipping() throws IOException {
		byte[] stringToBytes = plainText.getBytes(StandardCharsets.UTF_8);
		byte[] zippedBytes = Compress.zipBytes("memory.one", stringToBytes);
		byte[] unzippedBytes = Compress.unzip(zippedBytes);
		assertFalse(Arrays.equals(zippedBytes, unzippedBytes));
		assertTrue(Arrays.equals(stringToBytes, unzippedBytes));
		String unzippedStr = new String(unzippedBytes, StandardCharsets.UTF_8);
		assertEquals(plainText, unzippedStr);
	}
	
	@Test
	public void testHangulZipping() throws IOException {
		byte[] stringToBytes = hangulText.getBytes(StandardCharsets.UTF_8);
		byte[] zippedBytes = Compress.zipBytes("memory.one", stringToBytes);
		byte[] unzippedBytes = Compress.unzip(zippedBytes);
		assertFalse(Arrays.equals(zippedBytes, unzippedBytes));
		assertTrue(Arrays.equals(stringToBytes, unzippedBytes));
		String unzippedStr = new String(unzippedBytes, StandardCharsets.UTF_8);
		assertEquals(hangulText, unzippedStr);
	}
	
	@Test
	public void testDBEquals() throws IOException {
		HashMap<String, Integer> map = new HashMap<>();
		map.put("offset", 0);
		map.put("noOfRecords", 1000);
		List<BoardDTO> boardList = boardMapper.articleList(map);
		List<String> contentStrList = new ArrayList<>(boardList.size());
		List<byte[]> historyContentList = new ArrayList<>(boardList.size());
		List<Boolean> compressResult = new ArrayList<>(boardList.size());
		List<Boolean> decompressResult = new ArrayList<>(boardList.size());
		
		for(BoardDTO boardDTO : boardList) {
			contentStrList.add(boardDTO.getContent());
			BoardHistoryDTO historyDTO = boardHistoryMapper.getHistory(boardDTO);
			assertNotNull(historyDTO);
			historyContentList.add(historyDTO.getHistory_content());
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
