package com.worksmobile.assignment.mapper;

import org.junit.Test;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.worksmobile.assignment.model.Board;
import com.worksmobile.assignment.model.BoardHistory;
import com.worksmobile.assignment.model.NodePtr;
import com.worksmobile.assignment.util.JsonUtils;

/*
 * @author khh
 */
public class BoardAdapterTest {

	/**
	 * 게시판과 이력 모델 간의 변환시에 공통된 부분이 같은지 테스트합니다.
	 * 공통된 부분 : 게시판 번호, 버전, 루트 게시글 번호, 제목, 파일 번호, 게시글 등록 시간,
	 * @throws JsonProcessingException
	 */
	@Test
	public void testCommonEqaulity() throws JsonProcessingException {
		Board article = new Board("sub", "cont", "20180102", 999);
		BoardHistory boardHistory = BoardAdapter.from(article);
		Board recoveredArticle = BoardAdapter.from(boardHistory);
		
		JsonUtils.assertConvertToJsonObject(article, recoveredArticle);
		
		BoardHistory recoveredHistory = BoardAdapter.from(recoveredArticle);
		
		boardHistory.setNodePtr(new NodePtr(0, 0, 0));
		recoveredHistory.setNodePtr(new NodePtr(0, 0, 0));
		JsonUtils.assertConvertToJsonObject(boardHistory, recoveredHistory);
	}
}
