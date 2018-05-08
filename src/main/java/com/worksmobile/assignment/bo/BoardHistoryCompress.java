package com.worksmobile.assignment.bo;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

import com.worksmobile.assignment.model.BoardHistory;

public class BoardHistoryCompress {
	static Compress compress = CompressMaker.getCompress();
	
	public static BoardHistory setContent(BoardHistory boardHistory, String content) throws IOException {
		BoardHistory rtn = boardHistory;
		
		byte[] unCompressed = content.getBytes(StandardCharsets.UTF_8);
		byte[] compressed = compress.compress(unCompressed);
		
		if(compressed.length > unCompressed.length) {
			rtn.set_content_compressed(false);
			rtn.setHistory_content(unCompressed);
		} else {
			rtn.set_content_compressed(true);
			rtn.setHistory_content(compressed);
		}
		
		return rtn;
	}
	
	public static String getDeCompressedContent(BoardHistory boardHistory) throws IOException {
		byte[] decompressed;
		if(boardHistory.is_content_compressed()) {
			byte[] compressed = boardHistory.getHistory_content();
			decompressed = compress.deCompress(compressed);
			
		} else {
			decompressed = boardHistory.getHistory_content();
		}
		String decompressedStr = new String(decompressed, StandardCharsets.UTF_8);
		return decompressedStr;
	}
}
