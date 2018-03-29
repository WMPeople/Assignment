package com.worksmobile.Assignment.Service;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

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
import com.worksmobile.Assignment.Mapper.TempBoardMapper;
import com.worksmobile.Assignment.util.Utils;

@RunWith(SpringRunner.class)
@SpringBootTest
public class VersionManagementTest {

	@Autowired
	VersionManagementService versionManagementService;
	
	@Autowired
	BoardMapper boardMapper;
	
	@Autowired
	BoardHistoryMapper boardHistoryMapper;

	@Autowired
	TempBoardMapper recnetVersionMapper;
	
	private BoardDTO defaultBoardDTO;
	
	private BoardHistoryDTO defaultCreatedDTO;
	
	@Before
	public void createDefault() throws InterruptedException, ExecutionException {
		defaultBoardDTO = new BoardDTO();
		defaultBoardDTO.setSubject("versionTestSub");
		defaultBoardDTO.setContent("versionTestCont");;

		Future<BoardHistoryDTO> asyncExpectHistoryDTO = versionManagementService.createArticle(defaultBoardDTO);
		defaultCreatedDTO = asyncExpectHistoryDTO.get();
	}
	
	@Test
	public void testCanCreate() {
		versionManagementService = new VersionManagementService();
	}

	@Test
	public void testCreateArticle() throws InterruptedException, ExecutionException, JsonProcessingException {
		NodePtrDTO nodePtr = defaultCreatedDTO;
		BoardHistoryDTO dbHistoryDTO = boardHistoryMapper.getHistory(nodePtr);
		
		Utils.assertConvertToJsonObject(defaultCreatedDTO, dbHistoryDTO);
		
		defaultBoardDTO.setNodePtrDTO(nodePtr);
		defaultBoardDTO.setCreated(dbHistoryDTO.getCreated());
		
		BoardDTO dbBoardDTO = boardMapper.viewDetail(nodePtr.toMap());
		Utils.assertConvertToJsonObject(defaultBoardDTO, dbBoardDTO);
	}
	
	// recoverVersion == make leap Ver.
	@Test
	public void testRecoverVersion() throws InterruptedException, ExecutionException, JsonProcessingException {
		// TODO : Content zipping not completed.
		defaultBoardDTO.setContent(null);
		Future<BoardHistoryDTO> asyncCreatedHistoryDTO = versionManagementService.createArticle(defaultBoardDTO);
		BoardHistoryDTO createdHistoryDTO = asyncCreatedHistoryDTO.get();
		
		NodePtrDTO prevPtrDTO = createdHistoryDTO;
		BoardDTO prevLeapDTO = boardMapper.viewDetail(prevPtrDTO.toMap());
		NodePtrDTO newLeapPtrDTO = versionManagementService.recoverVersion(prevPtrDTO, prevPtrDTO);
		
		BoardHistoryDTO recoveredHistoryDTO = boardHistoryMapper.getHistory(newLeapPtrDTO);
		BoardDTO newLeapDTO = boardMapper.viewDetail(newLeapPtrDTO.toMap());
		
		prevLeapDTO.setCreated(recoveredHistoryDTO.getCreated());
		prevLeapDTO.setNodePtrDTO(newLeapPtrDTO);
		
		assertNotNull(recoveredHistoryDTO);
		Utils.assertConvertToJsonObject(newLeapDTO, prevLeapDTO);
	}
	
	@Test
	public void testMakeLeapVersion() throws InterruptedException, ExecutionException, JsonProcessingException {
		BoardDTO child = new BoardDTO();
		child.setSubject("childSub");
		child.setContent("childCont");
		
		NodePtrDTO parentPtrDTO = defaultCreatedDTO;
		NodePtrDTO resultPtrDTO = versionManagementService.modifyVersion(child, parentPtrDTO);
		
		BoardDTO parentBoardDTO = boardMapper.viewDetail(parentPtrDTO.toMap());
		assertNull(parentBoardDTO);
		BoardDTO leapBoardDTO = boardMapper.viewDetail(resultPtrDTO.toMap());
		assertNotNull(leapBoardDTO);
		assertEquals(defaultBoardDTO.getVersion() + 1, resultPtrDTO.getVersion());
		
		child.setNodePtrDTO(resultPtrDTO);
		Utils.assertConvertToJsonObject(child, leapBoardDTO);
	}
	
	private NodePtrDTO makeChild(NodePtrDTO parentPtrDTO) throws JsonProcessingException {
		BoardDTO child = new BoardDTO();
		child.setSubject("childSub");
		child.setContent("childCont");
		
		NodePtrDTO childPtrDTO = versionManagementService.modifyVersion(child, parentPtrDTO);
		child.setNodePtrDTO(childPtrDTO);
		
		BoardDTO leapBoardDTO = boardMapper.viewDetail(childPtrDTO.toMap());
		assertNotNull(leapBoardDTO);
		assertEquals(parentPtrDTO.getVersion() + 1, childPtrDTO.getVersion());
		
		Utils.assertConvertToJsonObject(child, leapBoardDTO);
		
		return childPtrDTO;
	}
	
	@Test
	public void testMakeModifyHasChild() throws JsonProcessingException {
		NodePtrDTO parentPtrDTO = defaultCreatedDTO;

		makeChild(parentPtrDTO);
		makeChild(parentPtrDTO);
	}
	
	@Test
	public void testDeleteLeapNode() throws JsonProcessingException {
		NodePtrDTO rootPtrDTO = defaultCreatedDTO;

		NodePtrDTO deletePtrDTO = makeChild(rootPtrDTO);
		versionManagementService.deleteVersion(deletePtrDTO);

		List<BoardHistoryDTO> children = boardHistoryMapper.getChildren(rootPtrDTO);
		assertEquals(0, children.size());
	}
	
	@Test
	public void testDeleteWhenHasChildNode() throws JsonProcessingException {
		NodePtrDTO rootPtrDTO = defaultCreatedDTO;
		
		NodePtrDTO middlePtrDTO = makeChild(rootPtrDTO);
		NodePtrDTO childPtrDTO = makeChild(middlePtrDTO);
		
		versionManagementService.deleteVersion(middlePtrDTO);
		
		BoardHistoryDTO childHistoryDTO = boardHistoryMapper.getHistory(childPtrDTO);
		NodePtrDTO childParentPtrDTO = childHistoryDTO.getParentPtrDTO();
		
		assertEquals(rootPtrDTO, childParentPtrDTO);
	}
	
	@Test
	public void testDeleteHasChildrenNode() throws JsonProcessingException {
		NodePtrDTO rootPtrDTO = defaultCreatedDTO;
		
		NodePtrDTO middlePtrDTO = makeChild(rootPtrDTO);

		int childrenCnt = 10;
		List<NodePtrDTO> childrenList = new ArrayList<>(childrenCnt);
		for(int i = 0; i < childrenCnt; i++) {
			childrenList.add(makeChild(middlePtrDTO));
		}
		
		versionManagementService.deleteVersion(middlePtrDTO);
		
		for(NodePtrDTO child : childrenList) {
			BoardHistoryDTO historyDTO = boardHistoryMapper.getHistory(child);
			NodePtrDTO parentPtrDTO = historyDTO.getParentPtrDTO();
			assertEquals(rootPtrDTO, parentPtrDTO);
		}
	}
	
	@Test
	public void testDeleteArticle() throws JsonProcessingException, NotLeapNodeException {
		NodePtrDTO rootPtrDTO = defaultCreatedDTO;
		
		NodePtrDTO hasChildrenPtrDTO = makeChild(rootPtrDTO);
		
		int childrenCnt = 2;
		List<NodePtrDTO> childrenList = new ArrayList<>(childrenCnt);
		for(int i = 0; i < childrenCnt; i++) {
			childrenList.add(makeChild(hasChildrenPtrDTO));
		}
		
		NodePtrDTO hasChildPtrDTO = childrenList.get(0);
		NodePtrDTO leapPtrDTO = makeChild(hasChildPtrDTO);
		
		versionManagementService.deleteArticle(leapPtrDTO);
		
	}
	
	@Test
	public void testGetRelatedHistory() throws JsonProcessingException, NotLeapNodeException {
		int childrenCnt = 2;
		List<NodePtrDTO> nodePtrList = new ArrayList<>(1 + 1 + childrenCnt + 1);
		NodePtrDTO rootPtrDTO = defaultCreatedDTO;
		nodePtrList.add(rootPtrDTO);
		
		NodePtrDTO hasChildrenPtrDTO = makeChild(rootPtrDTO);
		nodePtrList.add(hasChildrenPtrDTO);
		
		List<NodePtrDTO> childrenList = new ArrayList<>(childrenCnt);
		for(int i = 0; i < childrenCnt; i++) {
			childrenList.add(makeChild(hasChildrenPtrDTO));
		}
		nodePtrList.addAll(childrenList);
		
		NodePtrDTO hasChildPtrDTO = childrenList.get(0);
		NodePtrDTO leapPtrDTO = makeChild(hasChildPtrDTO);
		nodePtrList.add(leapPtrDTO);
		
		List<BoardHistoryDTO> relatedHistoryList = versionManagementService.getRelatedHistory(leapPtrDTO);
		assertNotNull(relatedHistoryList);
		for(BoardHistoryDTO eleHistoryDTO : relatedHistoryList) {
			assertNotNull(eleHistoryDTO);
			NodePtrDTO eleNodePtr = eleHistoryDTO;
			assertTrue(nodePtrList.contains(eleNodePtr));
		}
	}
}
