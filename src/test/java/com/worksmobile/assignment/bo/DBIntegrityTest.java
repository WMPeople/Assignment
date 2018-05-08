package com.worksmobile.assignment.bo;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.not;
import static org.junit.Assert.assertEquals;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ErrorCollector;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.worksmobile.assignment.mapper.BoardHistoryMapper;
import com.worksmobile.assignment.mapper.BoardMapper;
import com.worksmobile.assignment.mapper.FileMapper;
import com.worksmobile.assignment.model.Board;
import com.worksmobile.assignment.model.BoardHistory;
import com.worksmobile.assignment.model.File;
import com.worksmobile.assignment.model.NodePtr;
import com.worksmobile.assignment.util.JsonUtils;

/**
 * DB 의 무결성을 검사하는 테스트입니다.
 * @author khh
 *
 */
@RunWith(SpringRunner.class)
@SpringBootTest
public class DBIntegrityTest {

	@Autowired
	private BoardMapper boardMapper;
	
	@Autowired
	private BoardHistoryMapper boardHistoryMapper;
	
	@Autowired
	private FileMapper fileMapper;
	
	@Rule
	public ErrorCollector collector = new ErrorCollector();
	
	private boolean isLeafByBoardHistory(NodePtr nodePtr) {
		List<BoardHistory> children = boardHistoryMapper.selectChildren(nodePtr);
		if(children.size() == 0) {
			return true;
		} else {
			return false;
		}
	}
	
	private List<Board> selectAllBoard() {
		HashMap<String, Integer> articleParams = new HashMap<>();
		articleParams.put("offset", 0);
		articleParams.put("noOfRecords", Integer.MAX_VALUE);
		return boardMapper.articleList(articleParams);	
	}
	
	/*
	 * leaf 잎 노드는 항상 board에도 존재하여야 합니다.
	 */
	@Test
	public void testLeafIntegrity() throws JsonProcessingException {
		List<BoardHistory> notFoundList = new LinkedList<>();
		List<BoardHistory> historyList = boardHistoryMapper.selectAllHistory();

		for(BoardHistory ele : historyList) {
			if(isLeafByBoardHistory(ele)) {
				Board dbBoard = boardMapper.viewDetail(ele.toMap());
				collector.checkThat(null, not(dbBoard));
			}
		}

		System.err.println(JsonUtils.jsonStringFromObject(notFoundList));
		assertEquals(0, notFoundList.size());
	}
	
	/*
	 * board와 boardHisoty의 내용은 압축 풀어도 같아야 합니다.
	 */
	@Test
	public void testLeafContentIntegrity() throws IOException {
		List<Board> allBoadList = selectAllBoard();
		
		for(Board ele : allBoadList) {
			BoardHistory history = boardHistoryMapper.selectHistory(ele);
			String content = BoardHistoryCompression.getDeCompressedContent(history);
			if(ele.getContent() == null) {
				ele.setContent("");
			}
			collector.checkThat(JsonUtils.jsonStringFromObject(ele), content, is(ele.getContent()));
		}
	}
	
	/*
	 * board와 boardHistory의 생성시간
	 */
	@Test
	public void testLeafCreatedTimeIntegrity() throws JsonProcessingException {
		List<Board> allBoadList = selectAllBoard();
		
		for(Board ele : allBoadList) {
			BoardHistory history = boardHistoryMapper.selectHistory(ele);
			collector.checkThat(JsonUtils.jsonStringFromObject(ele), ele.getCreated_time(), is(history.getCreated_time()));
		}
	}
	
	/*
	 * board와 boardHistory와 파일 id는 같아야 합니다. 
	 */
	@Test
	public void testLeafFileIdIntegrity() throws JsonProcessingException {
		List<Board> allBoardList = selectAllBoard();
		
		for(Board ele : allBoardList) {
			BoardHistory history = boardHistoryMapper.selectHistory(ele);
			collector.checkThat(JsonUtils.jsonStringFromObject(ele), ele.getFile_id(), is(history.getFile_id()));
		}	
	}
	
	/* 
	 * file 테이블에 있는 file_id는 history에 존재하여야 합니다.
	 * 연결이 끊어진 파일 id는 존재하면 안됩니다.
	 */
	@Test
	public void testFileIdIntegrity() throws JsonProcessingException {
		List<File> allFileList = fileMapper.getAllFile();

		List<Board> allBoardList = selectAllBoard();
		List<Integer> allFileIdInBoardList = new ArrayList<>(allBoardList.size());
		for(Board ele : allBoardList) {
			allFileIdInBoardList.add(ele.getBoard_id());
		}
		
		for(File ele : allFileList) {
			int fileId = ele.getFile_id();
			if(fileId == 0) {
				continue;
			}
			collector.checkThat(JsonUtils.jsonStringFromObject(ele), true, is(allFileIdInBoardList.contains(fileId)));
		}
	}
	
	/*
	 * 부모와 자식간의 관계는 끊어져 있지 않아야 합니다.
	 * 또한 하나만 존재하는 노드는 존재하지 않습니다. (안보이는 노드가 존재하기 때문에)
	 */
	@Test
	public void testHistoryParentIntegrity() throws JsonProcessingException {
		List<BoardHistory> historyList = boardHistoryMapper.selectAllHistory();
		for(BoardHistory ele : historyList) {
			if(ele.isInvisibleRoot()) {
				continue;
			}
			NodePtr parent = ele.getParentPtrAndRoot();
			BoardHistory dbParent = boardHistoryMapper.selectHistory(parent);

			collector.checkThat(JsonUtils.jsonStringFromObject(ele), null, not(dbParent));
		}
	}
	
	/*
	 * 보이지 않는 루트는 버전이 0, 부모NodePtr이 null, 루트 게시판id 가 0 이어야 합니다.
	 */
	@Test
	public void testInvisibleRootVersionIsCorrect() {
		List<BoardHistory> historyList = boardHistoryMapper.selectAllHistory();
		for(BoardHistory ele : historyList) {
			if(ele.getVersion() == BoardHistory.INVISIBLE_ROOT_VERSION ||
				ele.getParent_board_id() == null ||
				ele.getParent_version() == null ||
				ele.getRoot_board_id() == BoardHistory.INVISIALBE_ROOT_BOARD_ID) {
				assertEquals((Integer)BoardHistory.INVISIBLE_ROOT_VERSION, ele.getVersion());
				assertEquals(null, ele.getParent_board_id());
				assertEquals(null, ele.getParent_version());
				assertEquals(BoardHistory.INVISIALBE_ROOT_BOARD_ID, ele.getRoot_board_id());
			}
		}
	}
}
