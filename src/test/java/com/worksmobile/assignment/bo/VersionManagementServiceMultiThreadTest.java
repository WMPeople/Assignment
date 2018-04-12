package com.worksmobile.assignment.bo;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.ExecutionException;

import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.worksmobile.assignment.mapper.BoardHistoryMapper;
import com.worksmobile.assignment.mapper.BoardMapper;
import com.worksmobile.assignment.model.Board;
import com.worksmobile.assignment.model.BoardHistory;
import com.worksmobile.assignment.model.NodePtr;
import com.worksmobile.assignment.util.JsonUtils;

@RunWith(SpringRunner.class)
@SpringBootTest
@Ignore
public class VersionManagementServiceMultiThreadTest {

	@Autowired
	VersionManagementService versionManagementService;
	
	@Autowired
	BoardMapper boardMapper;
	
	@Autowired
	BoardHistoryMapper boardHistoryMapper;

	private final static int THREAD_COUNT = 100;
	
	private Board defaultBoard;
	private BoardHistory defaultCreated;
	private List<Thread> threadList = new ArrayList<>(THREAD_COUNT);
	
	@Before
	public void createDefault() throws InterruptedException, ExecutionException {
		defaultBoard = new Board();
		defaultBoard.setSubject("versionTestSub");
		defaultBoard.setContent("versionTestCont");

		defaultCreated = versionManagementService.createArticle(defaultBoard);
	}
	
	@After
	public void startAndJoinThread() throws InterruptedException {
		for(Thread ele : threadList) {
			ele.start();
		}

		for(Thread ele : threadList) {
			ele.join();
		}
	}
	
	@Test
	public void testCanCreate() {
		versionManagementService = new VersionManagementService();
	}
	
	@Test
	public void testCreateArticle() throws InterruptedException, ExecutionException, JsonProcessingException {
		for(int i = 0; i < THREAD_COUNT; i++) {
			Thread thread = new Thread(()->{
				try {
					Board copyedBoard = new Board();
					copyedBoard.setSubject(defaultBoard.getSubject());
					copyedBoard.setContent(defaultBoard.getContent());

					BoardHistory createdHistory = versionManagementService.createArticle(copyedBoard);
					
					NodePtr nodePtr = createdHistory;
					BoardHistory dbHistory = boardHistoryMapper.selectHistory(nodePtr);
					
					JsonUtils.assertConvertToJsonObject(createdHistory, dbHistory);
					
					copyedBoard.setNodePtr(nodePtr);
					copyedBoard.setCreated_time(dbHistory.getCreated_time());
					
					Board dbBoard = boardMapper.viewDetail(nodePtr.toMap());
					assertEquals(copyedBoard, dbBoard);
				}
				catch(JsonProcessingException e) {
					e.printStackTrace();
				}
			});
			threadList.add(thread);
		}
	}
	
	public static NodePtr makeChild(VersionManagementService versionManagementService, BoardMapper boardMapper,
			NodePtr parentPtr) throws JsonProcessingException {
		Board child = new Board();
		child.setSubject("childSub");
		child.setContent("childCont");
		
		NodePtr childPtr = versionManagementService.modifyVersion(child, parentPtr, null);
		child.setNodePtr(childPtr);
		
		Board leafBoard = boardMapper.viewDetail(childPtr.toMap());
		assertNotNull(leafBoard);
		int parentVersion = parentPtr.getVersion() == null ? 0 : parentPtr.getVersion();
		assertEquals((Integer) (parentVersion + 1), childPtr.getVersion());
		
		JsonUtils.assertConvertToJsonObject(child, leafBoard);
		
		return childPtr;
	}
	
	@Test
	public void testMakeChildWithMultiThread() {
		for(int i = 0; i < THREAD_COUNT; i++) {
			Thread thread = new Thread(()-> {
				try {
					for(int j = 0; j < 10; j++) {
						@SuppressWarnings("unused")
						NodePtr child = VersionManagementServiceMultiThreadTest.makeChild(versionManagementService,
								boardMapper, defaultCreated);
					}
				} catch (JsonProcessingException e) {
					e.printStackTrace();
				}
			});
			threadList.add(thread);
		}
	}
	
	@Test
	public void testModifyAndDeleteVersion() throws JsonProcessingException {
		int generationCnt = THREAD_COUNT;
		List<NodePtr> generation = new ArrayList<>(generationCnt);
		generation.add(defaultCreated);
		for(int i = 1; i < generationCnt; i++) {
			NodePtr parentPtr = generation.get(i - 1);
			NodePtr child = VersionManagementServiceMultiThreadTest.makeChild(versionManagementService, boardMapper,
					parentPtr);
			generation.add(child);
		}
		
		Board modifiedBoard = new Board();
		modifiedBoard.setSubject("modifiedSub");
		modifiedBoard.setSubject("modifiedContent");

		int i = 0;
		for(; i < THREAD_COUNT / 2; i++) {
			Thread thread = new Thread(()-> {
				int randIdx = (int) (Math.random() * THREAD_COUNT);
				NodePtr nodePtr = generation.get(randIdx);

				versionManagementService.modifyVersion(modifiedBoard, nodePtr, null);
			});
			threadList.add(thread);
		}
		
		HashMap<String, Integer> articleListParams = new HashMap<>();
		articleListParams.put("offset", 0);
		articleListParams.put("noOfRecords", Integer.MAX_VALUE);
		int root_board_id = defaultCreated.getRoot_board_id();

		for(; i < THREAD_COUNT; i++) {
			Thread thread = new Thread(()-> {
				List<Board> sameRoot = boardMapper.articleList(articleListParams);
				sameRoot.removeIf(item -> { return item.getRoot_board_id() != root_board_id; } );
				int maxIdx = sameRoot.size() - 1;
				int randIdx = (int) (Math.random() * maxIdx);

				NodePtr deletePtr = sameRoot.get(randIdx);
				
				versionManagementService.deleteVersion(deletePtr);
			});
			threadList.add(thread);
		}
	}
}