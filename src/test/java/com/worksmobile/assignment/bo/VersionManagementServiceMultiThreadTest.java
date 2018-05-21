package com.worksmobile.assignment.bo;

import static org.awaitility.Awaitility.await;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;

import org.junit.After;
import org.junit.Before;
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
import com.worksmobile.assignment.model.Board;
import com.worksmobile.assignment.model.BoardHistory;
import com.worksmobile.assignment.model.NodePtr;
import com.worksmobile.assignment.util.BoardUtil;
import com.worksmobile.assignment.util.JsonUtils;

/**
 * 게시글 버전관리의 충돌 영역 테스트를 진행합니다
 * @author khh
 *
 */
@RunWith(SpringRunner.class)
@SpringBootTest
public class VersionManagementServiceMultiThreadTest {

	@Autowired
	private VersionManagementService versionManagementService;
	
	@Autowired
	private BoardMapper boardMapper;
	
	@Autowired
	private BoardHistoryMapper boardHistoryMapper;
	
	@Autowired
	private BoardUtil boardUtil;
	
	@Rule
	public ErrorCollector collector = new ErrorCollector();
	
	private final static int THREAD_COUNT = 20000;
	
	private Board defaultCreated;
	private List<Thread> threadList = new ArrayList<>(THREAD_COUNT);
	
	@Before
	public void createDefault() throws InterruptedException, ExecutionException {
		Board defaultBoard = BoardUtil.makeArticle("versionTestSub", "versionTestCont");

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
					Board copyedBoard = BoardUtil.makeArticle(defaultCreated.getSubject(), defaultCreated.getContent());
					
					Board createdHistory = versionManagementService.createArticle(copyedBoard);
					
					NodePtr nodePtr = createdHistory;
					BoardHistory dbHistory = boardHistoryMapper.selectHistory(nodePtr);
					
					JsonUtils.assertConvertToJsonObject(createdHistory, dbHistory);
					
					copyedBoard.setNodePtr(nodePtr);
					copyedBoard.setCreated_time(dbHistory.getCreated_time());
					
					Board dbBoard = boardMapper.viewDetail(nodePtr.toMap());
					assertEquals(copyedBoard, dbBoard);
				}
				catch(NotExistNodePtrException | org.apache.ibatis.exceptions.PersistenceException e) {
					
				}
				catch(Exception e) {
					collector.addError(e);
				}
			});
			threadList.add(thread);
		}
	}
	
	@Test
	public void testMakeChildWithMultiThread() {
		for(int i = 0; i < THREAD_COUNT; i++) {
			Thread thread = new Thread(()-> {
				try {
					for(int j = 0; j < 10; j++) {
						@SuppressWarnings("unused")
						NodePtr child = boardUtil.makeChild(defaultCreated);
					}
				} catch(Exception e) {
					collector.addError(e);
				}
			});
			threadList.add(thread);
		}
	}
	
	@Test
	public void testModifyAndDelete() throws JsonProcessingException {
		int generationCnt = THREAD_COUNT;
		List<NodePtr> generation = new ArrayList<>(generationCnt);
		generation.add(defaultCreated);
		for(int i = 1; i < generationCnt; i++) {
			NodePtr parentPtr = generation.get(i - 1);
			await().untilAsserted(() -> assertThat(boardHistoryMapper.selectHistory(parentPtr), is(notNullValue())));
			NodePtr child = boardUtil.makeChild(parentPtr);
			generation.add(child);
		}
		
		int i = 0;
		for(; i < THREAD_COUNT / 3; i++) {
			Thread modifyThread = new Thread(()-> {
				try {
					List<BoardHistory> list = boardHistoryMapper.selectHistoryByRootBoardId(defaultCreated.getRoot_board_id());
					int randIdx = (int) (Math.random() * list.size());
					NodePtr nodePtr = list.get(randIdx);
					
					Board modifiedBoard = BoardUtil.makeArticle("modifiedSub", "modifiedCont");
					versionManagementService.modifyVersion(modifiedBoard, nodePtr, null);
				}
				catch(NotExistNodePtrException | org.apache.ibatis.exceptions.PersistenceException e) {
						
				} catch(Exception e) {
					e.printStackTrace();
					System.err.println(e.getStackTrace());
					collector.addError(e);
				}
			});
			threadList.add(modifyThread);
		}
		
		int root_board_id = defaultCreated.getRoot_board_id();
		
		for(; i < THREAD_COUNT / 3 * 2; i++) {
			Thread deleteVersionThread = new Thread(()-> {
				try {
					List<BoardHistory> sameRoot = boardHistoryMapper.selectHistoryByRootBoardId(defaultCreated.getRoot_board_id());
					int maxIdx = sameRoot.size() - 1;
					int randIdx = (int) (Math.random() * maxIdx);
					
					NodePtr deletePtr = sameRoot.get(randIdx);
					
					versionManagementService.deleteVersion(deletePtr);
				}catch(NotExistNodePtrException | org.apache.ibatis.exceptions.PersistenceException e) {
					
				} catch(Exception e) {
					e.printStackTrace();
					System.err.println(e.getStackTrace());
					collector.addError(e);
				}
			});
			threadList.add(deleteVersionThread);
		}
		
		for(; i < THREAD_COUNT; i++) {
			Thread deleteArticleThread = new Thread(()-> {
				try {
					Thread.sleep(1000);	// 수정이 어느정도 진행되었을 때 삭제를 진행하기 위함
					List<Board> sameRoot = boardUtil.selectAllArticles();
					sameRoot.removeIf(item -> {return item.getRoot_board_id() != root_board_id; } );
					int maxIdx = sameRoot.size() - 1;
					int randIdx = (int) (Math.random() * maxIdx);
					
					NodePtr deletePtr = sameRoot.get(randIdx);
					
					versionManagementService.deleteArticle(deletePtr);
				}
				catch(NotExistNodePtrException | org.apache.ibatis.exceptions.PersistenceException e) {
					
				} catch(Exception e) {
					collector.addError(e);
				}
			});
			threadList.add(deleteArticleThread);
		}
	}
	
	@Test(expected=NotExistNodePtrException.class)
	public void testSelectLock() {
		Board modifiedBoard = BoardUtil.makeArticle("modifiedSub", "modifiedContent");
		
		versionManagementService.modifyVersion(modifiedBoard, defaultCreated, null);
		versionManagementService.deleteVersion(defaultCreated);
	}
}
