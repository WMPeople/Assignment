package com.worksmobile.Assignment.Service;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.stereotype.Service;
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
	
	private BoardHistoryDTO defaultCreateDTO;
	
	@Before
	public void createDefault() throws InterruptedException, ExecutionException {
		defaultBoardDTO = new BoardDTO();
		defaultBoardDTO.setSubject("versionTestSub");
		defaultBoardDTO.setContent("versionTestCont");;

		Future<BoardHistoryDTO> asyncExpectHistoryDTO = versionManagementService.createArticle(defaultBoardDTO);
		defaultCreateDTO = asyncExpectHistoryDTO.get();
	}
	
	@Test
	public void testCanCreate() {
		versionManagementService = new VersionManagementService();
	}

	@Test
	public void testCreateArticle() throws InterruptedException, ExecutionException, JsonProcessingException {
		NodePtrDTO nodePtr = new NodePtrDTO(defaultCreateDTO);
		BoardHistoryDTO dbHistoryDTO = boardHistoryMapper.getHistory(nodePtr);
		
		Utils.assertConvertToJsonObject(defaultCreateDTO, dbHistoryDTO);
		
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
		
		NodePtrDTO prevPtrDTO = new NodePtrDTO(createdHistoryDTO);
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
		
		NodePtrDTO parentPtrDTO = new NodePtrDTO(defaultCreateDTO);
		NodePtrDTO resultPtrDTO = versionManagementService.modifyVersion(child, parentPtrDTO);
		
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
		
		BoardDTO leapBoardDTO = boardMapper.viewDetail(childPtrDTO.toMap());
		assertNotNull(leapBoardDTO);
		assertEquals(defaultBoardDTO.getVersion() + 1, childPtrDTO.getVersion());
		
		child.setNodePtrDTO(childPtrDTO);
		Utils.assertConvertToJsonObject(child, leapBoardDTO);
		
		return childPtrDTO;
	}
	
	@Test
	public void testMakeModifyHasChild() throws JsonProcessingException {
		NodePtrDTO parentPtrDTO = new NodePtrDTO(defaultCreateDTO);

		makeChild(parentPtrDTO);
		makeChild(parentPtrDTO);
	}
	
	@Test
	public void testDeleteLeapNode() throws JsonProcessingException {
		NodePtrDTO rootPtrDTO = new NodePtrDTO(defaultCreateDTO);

		NodePtrDTO deletePtrDTO = makeChild(rootPtrDTO);
		versionManagementService.deleteVersion(deletePtrDTO);

		List<BoardHistoryDTO> children = boardHistoryMapper.getChildren(rootPtrDTO);
		assertEquals(0, children.size());
	}
	
	@Test
	public void testDeleteHasChildNode() throws JsonProcessingException {
		NodePtrDTO rootPtrDTO = new NodePtrDTO(defaultCreateDTO);
		
		NodePtrDTO middlePtrDTO = makeChild(rootPtrDTO);
		NodePtrDTO childPtrDTO = makeChild(middlePtrDTO);
		
		versionManagementService.deleteVersion(middlePtrDTO);
		
		BoardHistoryDTO childHistoryDTO = boardHistoryMapper.getHistory(childPtrDTO);
		NodePtrDTO childParentPtrDTO = childHistoryDTO.getParentPtrDTO();
		
		Utils.assertConvertToJsonObject(childParentPtrDTO, rootPtrDTO);
	}
	
	@Test
	public void testDeleteHasChildrenNode() throws JsonProcessingException {
		NodePtrDTO rootPtrDTO = new NodePtrDTO(defaultCreateDTO);
		
		NodePtrDTO middlePtrDTO = makeChild(rootPtrDTO);

		for(int i = 0; i < 10; i++) {
			makeChild(middlePtrDTO);
		}
		// TODO : not completed!
	}
}
