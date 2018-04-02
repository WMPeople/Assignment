package com.worksmobile.Assignment.Service;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;

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
	private List<Thread> threadList = new ArrayList<>();
	
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
						NodePtrDTO child = this.makeChild(versionManagementService, boardMapper, defaultCreatedDTO);
					}
				} catch (JsonProcessingException e) {
					e.printStackTrace();
				}
			});
			threadList.add(thread);
		}
	}
}