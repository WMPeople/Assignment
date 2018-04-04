package com.worksmobile.Assignment.Service;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.worksmobile.Assignment.Domain.BoardDTO;
import com.worksmobile.Assignment.Domain.BoardHistoryDTO;
import com.worksmobile.Assignment.Domain.NodePtrDTO;
import com.worksmobile.Assignment.Mapper.BoardHistoryMapper;
import com.worksmobile.Assignment.Mapper.BoardMapper;
import com.worksmobile.Assignment.util.Utils;

@RunWith(SpringRunner.class)
@SpringBootTest
public class VersionManagementServiceMultiThreadTest {

	@Autowired
	VersionManagementService versionManagementService;
	
	@Autowired
	BoardMapper boardMapper;
	
	@Autowired
	BoardHistoryMapper boardHistoryMapper;

	private final static int THREAD_COUNT = 100;
	
	private BoardDTO defaultBoardDTO;
	private BoardHistoryDTO defaultCreatedDTO;
	private List<Thread> threadList = new ArrayList<>(THREAD_COUNT);
	
	@Before
	public void createDefault() throws InterruptedException, ExecutionException {
		defaultBoardDTO = new BoardDTO();
		defaultBoardDTO.setSubject("versionTestSub");
		defaultBoardDTO.setContent("versionTestCont");

		defaultCreatedDTO = versionManagementService.createArticle(defaultBoardDTO);
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
					BoardDTO copyedBoardDTO = new BoardDTO();
					copyedBoardDTO.setSubject(defaultBoardDTO.getSubject());
					copyedBoardDTO.setContent(defaultBoardDTO.getContent());

					BoardHistoryDTO createdHistoryDTO = versionManagementService.createArticle(copyedBoardDTO);
					
					NodePtrDTO nodePtr = createdHistoryDTO;
					BoardHistoryDTO dbHistoryDTO = boardHistoryMapper.getHistory(nodePtr);
					
					Utils.assertConvertToJsonObject(createdHistoryDTO, dbHistoryDTO);
					
					copyedBoardDTO.setNodePtrDTO(nodePtr);
					copyedBoardDTO.setCreated(dbHistoryDTO.getCreated());
					
					BoardDTO dbBoardDTO = boardMapper.viewDetail(nodePtr.toMap());
					assertEquals(copyedBoardDTO, dbBoardDTO);
				}
				catch(JsonProcessingException e) {
					e.printStackTrace();
				}
			});
			threadList.add(thread);
		}
	}
	
	public static NodePtrDTO makeChild(VersionManagementService versionManagementService, 
			BoardMapper boardMapper, NodePtrDTO parentPtrDTO) throws JsonProcessingException {
		BoardDTO child = new BoardDTO(); 
		child.setSubject("childSub");
		child.setContent("childCont");
		
		NodePtrDTO childPtrDTO = versionManagementService.modifyVersion(child, parentPtrDTO);
		child.setNodePtrDTO(childPtrDTO);
		
		BoardDTO leapBoardDTO = boardMapper.viewDetail(childPtrDTO.toMap());
		assertNotNull(leapBoardDTO);
		int parentVersion = parentPtrDTO.getVersion() == null ? 0 : parentPtrDTO.getVersion();
		assertEquals((Integer)(parentVersion + 1), childPtrDTO.getVersion());
		
		Utils.assertConvertToJsonObject(child, leapBoardDTO);
		
		return childPtrDTO;
	}
	
	@Test
	public void testMakeChildWithMultiThread() {
		for(int i = 0; i < THREAD_COUNT; i++) {
			Thread thread = new Thread(()-> {
				try {
					for(int j = 0; j < 10; j++) {
						NodePtrDTO child = VersionManagementServiceMultiThreadTest.makeChild(versionManagementService, boardMapper, defaultCreatedDTO);
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
		List<NodePtrDTO> generation = new ArrayList<>(generationCnt);
		generation.add(defaultCreatedDTO);
		for(int i = 1; i < generationCnt; i++) {
			NodePtrDTO parentPtr = generation.get(i - 1);
			NodePtrDTO child = VersionManagementServiceMultiThreadTest.makeChild(versionManagementService, boardMapper, parentPtr);
			generation.add(child);
		}
		
		BoardDTO modifiedBoardDTO = new BoardDTO();
		modifiedBoardDTO.setSubject("modifiedSub");
		modifiedBoardDTO.setSubject("modifiedContent");

		int i = 0;
		for(; i < THREAD_COUNT / 2; i++) {
			Thread thread = new Thread(()-> {
				int randIdx = (int) (Math.random() * THREAD_COUNT);
				NodePtrDTO nodePtrDTO = generation.get(randIdx);

				versionManagementService.modifyVersion(modifiedBoardDTO, nodePtrDTO);
			});
			threadList.add(thread);
		}
		
		HashMap<String, Integer> articleListParams = new HashMap<>();
		articleListParams.put("offset", 0);
		articleListParams.put("noOfRecords", Integer.MAX_VALUE);
		int root_board_id = defaultCreatedDTO.getRoot_board_id();

		for(; i < THREAD_COUNT; i++) {
			Thread thread = new Thread(()-> {
				List<BoardDTO> sameRoot = boardMapper.articleList(articleListParams);
				sameRoot.removeIf(item -> { return item.getRoot_board_id() != root_board_id; } );
				int maxIdx = sameRoot.size() - 1;
				int randIdx = (int) (Math.random() * maxIdx);

				NodePtrDTO deletePtrDTO = sameRoot.get(randIdx);
				
				versionManagementService.deleteVersion(deletePtrDTO);
			});
			threadList.add(thread);
		}
	}
}
