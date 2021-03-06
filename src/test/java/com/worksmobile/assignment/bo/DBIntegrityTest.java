package com.worksmobile.assignment.bo;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.junit.Assert.assertEquals;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

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
import com.worksmobile.assignment.util.BoardUtil;
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
	
	@Autowired
	private BoardUtil boardUtil;
	
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
	
	/*
	 * 이력의 리프 노드는 항상 board에도 존재하여야 합니다.
	 */
	@Test
	public void testLeafIntegrity() throws JsonProcessingException {
		List<BoardHistory> notFoundList = new LinkedList<>();
		List<BoardHistory> historyList = boardHistoryMapper.selectAllHistory();
		
		for(BoardHistory ele : historyList) {
			if(isLeafByBoardHistory(ele)) {
				Board dbBoard = boardMapper.viewDetail(ele.toMap());
				collector.checkThat(JsonUtils.jsonStringFromObject(ele), dbBoard, is(notNullValue()));
			}
		}
		
		assertEquals(0, notFoundList.size());
		System.err.println(JsonUtils.jsonStringFromObject(notFoundList));
	}
	
	/*
	 * 게시판의 게시글들은 이력에서 리프여야 합니다.
	 */
	@Test
	public void testArticleIsHistoryLeaf() throws JsonProcessingException {
		List<Board> articleList = boardUtil.selectAllArticles();
		for(Board articleEle : articleList) {
			BoardHistory boardHistory = boardHistoryMapper.selectHistory(articleEle);
			collector.checkThat(JsonUtils.jsonStringFromObject(articleEle), boardHistory, is(notNullValue()));
			boolean isLeaf = isLeafByBoardHistory(articleEle);
			collector.checkThat(isLeaf, is(true));
		}
	}
	
	/*
	 * board와 boardHisoty의 내용은 압축 풀어도 같아야 합니다.
	 */
	@Test
	public void testLeafContentIntegrity() throws IOException {
		List<Board> allBoadList = boardUtil.selectAllArticles();
		
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
		List<Board> allBoadList = boardUtil.selectAllArticles();
		
		for(Board ele : allBoadList) {
			BoardHistory history = boardHistoryMapper.selectHistory(ele);
			collector.checkThat(history, not(nullValue()));
			collector.checkThat(JsonUtils.jsonStringFromObject(ele), ele.getCreated_time(), is(history.getCreated_time()));
		}
	}
	
	/*
	 * board와 boardHistory와 파일 id는 같아야 합니다. 
	 */
	@Test
	public void testLeafFileIdIntegrity() throws JsonProcessingException {
		List<Board> allBoardList = boardUtil.selectAllArticles();
		
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

		List<Board> allBoardList = boardUtil.selectAllArticles();
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
				assertEquals(true, ele.isInvisibleRoot());
			}
		}
	}
	
	/*
	 * 모든 노드들은 보이지 않는 루트를 가지고 있어야 합니다.
	 */
	@Test
	public void testAllNodeHaveInvisibleRoot() {
		List<BoardHistory> historyList = boardHistoryMapper.selectAllHistory();
		Set<Integer> rootBoardIdSet = new HashSet<>();
		for(BoardHistory eleHistory : historyList) {
			rootBoardIdSet.add(eleHistory.getRoot_board_id());
		}
		
		for(int rootBoardId : rootBoardIdSet) {
			// 보이지 않는 루트의 루트 게시글 번호는 0 입니다.
			if(rootBoardId == 0) {
				continue;
			}
			NodePtr invisibleRootPtr = new NodePtr(rootBoardId, NodePtr.INVISIBLE_ROOT_VERSION, NodePtr.INVISIALBE_ROOT_BOARD_ID);
			BoardHistory invisibleRootHistory = boardHistoryMapper.selectHistory(invisibleRootPtr);
			collector.checkThat(invisibleRootPtr.toString(), invisibleRootHistory, is(notNullValue()));
		}
	}
}
