package com.worksmobile.Assignment.Service;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.ExecutionException;

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
public class VersionManagementTest {

	@Autowired
	VersionManagementService versionManagementService;
	
	@Autowired
	BoardMapper boardMapper;
	
	@Autowired
	BoardHistoryMapper boardHistoryMapper;

	private BoardDTO defaultBoardDTO;
	private BoardHistoryDTO defaultCreatedDTO;
	private Comparator<NodePtrDTO> compareNodePtrDTO = Comparator
					.comparing(NodePtrDTO::getRoot_board_id)
					.thenComparing(NodePtrDTO::getBoard_id)
					.thenComparing(NodePtrDTO::getVersion);
	
	@Before
	public void createDefault() throws InterruptedException, ExecutionException {
		defaultBoardDTO = new BoardDTO();
		defaultBoardDTO.setSubject("versionTestSub");
		defaultBoardDTO.setContent("versionTestCont");;

		defaultCreatedDTO = versionManagementService.createArticle(defaultBoardDTO);
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
	
	@Test
	public void testCreateSevenHundredThousandTextContentArticle() throws JsonProcessingException, InterruptedException, ExecutionException {
		StringBuilder sevenHundredContent =  new StringBuilder();
		for(int i = 0; i < 700000; i++) {
			sevenHundredContent.append(i % 10);
		}
		
		defaultBoardDTO = new BoardDTO();
		defaultBoardDTO.setSubject("칠십만자가 들어가있습니다");
		defaultBoardDTO.setContent(sevenHundredContent.toString());

		defaultCreatedDTO = versionManagementService.createArticle(defaultBoardDTO);
		
		NodePtrDTO nodePtr = defaultCreatedDTO;
		BoardHistoryDTO dbHistoryDTO = boardHistoryMapper.getHistory(nodePtr);
		
		Utils.assertConvertToJsonObject(defaultCreatedDTO, dbHistoryDTO);
		
		defaultBoardDTO.setNodePtrDTO(nodePtr);
		defaultBoardDTO.setCreated(dbHistoryDTO.getCreated());
		
		BoardDTO dbBoardDTO = boardMapper.viewDetail(nodePtr.toMap());
		Utils.assertConvertToJsonObject(defaultBoardDTO, dbBoardDTO);	
	}
	
	@Test
	public void testRecoverVersion() throws InterruptedException, ExecutionException, JsonProcessingException {
		BoardHistoryDTO createdHistoryDTO= versionManagementService.createArticle(defaultBoardDTO);
		
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
		int defaultVersion = defaultBoardDTO.getVersion() == null ? 0 : defaultBoardDTO.getVersion();
		assertEquals((Integer)(defaultVersion + 1), resultPtrDTO.getVersion());
		
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
		int parentVersion = parentPtrDTO.getVersion() == null ? 0 : parentPtrDTO.getVersion();
		assertEquals((Integer)(parentVersion + 1), childPtrDTO.getVersion());
		
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
		NodePtrDTO childParentPtrDTO = childHistoryDTO.getParentPtrAndRoot();
		
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
			NodePtrDTO parentPtrDTO = historyDTO.getParentPtrAndRoot();
			assertEquals(rootPtrDTO, parentPtrDTO);
		}
	}
	
	@Test
	public void testDeleteArticle() throws JsonProcessingException, NotLeafNodeException {
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
	public void testDeleteArticleHasOneChild() throws JsonProcessingException, NotLeafNodeException {
		int generationCnt = 5;
		List<NodePtrDTO> generationList = new ArrayList<>(generationCnt);
		generationList.add(defaultBoardDTO);
		for(int i = 1; i < generationCnt; i++) {
			generationList.add(makeChild(generationList.get(i - 1)));
		}
		
		versionManagementService.deleteArticle(generationList.get(generationCnt - 1));
	}
	
	@Test(expected=NotLeafNodeException.class)
	public void testDeleteArticleShouldTrhowNotLeafNodeException() throws JsonProcessingException, NotLeafNodeException {
		int generationCnt = 5;
		List<NodePtrDTO> generationList = new ArrayList<>(generationCnt);
		generationList.add(defaultBoardDTO);
		for(int i = 1; i < generationCnt; i++) {
			generationList.add(makeChild(generationList.get(i - 1)));
		}
		
		versionManagementService.deleteArticle(generationList.get(0));
	}
	
	@Test(expected=NotLeafNodeException.class)
	public void testGetRelatedHistoryhouldTrhowNotLeafNodeException() throws JsonProcessingException, NotLeafNodeException {
		int generationCnt = 5;
		List<NodePtrDTO> generationList = new ArrayList<>(generationCnt);
		generationList.add(defaultBoardDTO);
		for(int i = 1; i < generationCnt; i++) {
			generationList.add(makeChild(generationList.get(i - 1)));
		}
		
		@SuppressWarnings("unused")
		List<BoardHistoryDTO> relatedHistoryList = versionManagementService.getRelatedHistory(generationList.get(0));
	}
	
	@Test
	public void testGetRelatedHistory() throws JsonProcessingException, NotLeafNodeException {
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
		NodePtrDTO leapPtrDTOWithoutRootBoardId = new NodePtrDTO(leapPtrDTO.getBoard_id(), leapPtrDTO.getVersion());
		
		List<BoardHistoryDTO> relatedHistoryList = versionManagementService.getRelatedHistory(leapPtrDTOWithoutRootBoardId);
		assertEquals(relatedHistoryList.size(), nodePtrList.size());
		assertNotNull(relatedHistoryList);
		for(BoardHistoryDTO eleHistoryDTO : relatedHistoryList) {
			assertNotNull(eleHistoryDTO);
			NodePtrDTO eleNodePtr = eleHistoryDTO;
			assertTrue(nodePtrList.contains(eleNodePtr));
		}
	}
	
	@Test
	public void testGetRelatedHistoryWhenDifferentBoardId() throws JsonProcessingException, NotLeafNodeException {
		List<NodePtrDTO> leafToRoot = new ArrayList<>();
		NodePtrDTO rootPtrDTO = defaultCreatedDTO;
		leafToRoot.add(rootPtrDTO);
		
		@SuppressWarnings("unused")
		NodePtrDTO middle1 = makeChild(rootPtrDTO);
		NodePtrDTO middle2 = makeChild(rootPtrDTO);
		leafToRoot.add(middle2);
		
		NodePtrDTO child = makeChild(middle2);
		leafToRoot.add(child);
		
		List<BoardHistoryDTO> relatedHistoryList = versionManagementService.getRelatedHistory(child);
		
		assertEquals(relatedHistoryList.size(), leafToRoot.size());
		
		leafToRoot.sort(compareNodePtrDTO);
		relatedHistoryList.sort(compareNodePtrDTO);

		for(int i = 0; i < relatedHistoryList.size(); i++) {
			NodePtrDTO relatedEle = relatedHistoryList.get(i);
			NodePtrDTO addedEle = leafToRoot.get(i);
			assertEquals(relatedEle, addedEle);
		}
	}
}
