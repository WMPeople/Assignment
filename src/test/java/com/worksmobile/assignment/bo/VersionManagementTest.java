package com.worksmobile.assignment.bo;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
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
import com.worksmobile.assignment.mapper.BoardHistoryMapper;
import com.worksmobile.assignment.mapper.BoardMapper;
import com.worksmobile.assignment.model.Board;
import com.worksmobile.assignment.model.BoardHistory;
import com.worksmobile.assignment.model.NodePtr;
import com.worksmobile.assignment.util.JsonUtils;

@RunWith(SpringRunner.class)
@SpringBootTest
public class VersionManagementTest {

	@Autowired
	VersionManagementService versionManagementService;
	
	@Autowired
	BoardMapper boardMapper;
	
	@Autowired
	BoardHistoryMapper boardHistoryMapper;

	private Board defaultBoard;
	private BoardHistory defaultCreated;
	private Comparator<NodePtr> compareNodePtr = Comparator.comparing(NodePtr::getRoot_board_id)
			.thenComparing(NodePtr::getBoard_id).thenComparing(NodePtr::getVersion);
	
	@Before
	public void createDefault() throws InterruptedException, ExecutionException {
		defaultBoard = new Board();
		defaultBoard.setSubject("versionTestSub");
		defaultBoard.setContent("versionTestCont");

		defaultCreated = versionManagementService.createArticle(defaultBoard);
	}
	
	@Test
	public void testCanCreate() {
		versionManagementService = new VersionManagementService();
	}

	private NodePtr makeChild(NodePtr parentPtr) throws JsonProcessingException {
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
	public void testCreateChild() throws JsonProcessingException {
		NodePtr child = makeChild(defaultCreated);
		assertNotNull(child.getBoard_id());
		assertNotEquals(0, (int)child.getBoard_id());
		assertEquals(defaultCreated.getBoard_id(), child.getBoard_id());
		assertNotNull(defaultCreated.getVersion());
		assertEquals(defaultCreated.getVersion() + 1, (int) child.getVersion());
		assertEquals(defaultCreated.getRoot_board_id(), child.getRoot_board_id());
	}
	
	@Test
	public void testCreateTwoArticleAndModify() throws JsonProcessingException {
		Board defaultBoard2 = new Board();
		defaultBoard2.setSubject("versionTestSub2");
		defaultBoard2.setContent("versionTestCont2");

		defaultCreated = versionManagementService.createArticle(defaultBoard2);
		
		assertNotNull(defaultCreated);
		
		NodePtr newChildPtr = makeChild(defaultCreated);

		assertEquals(defaultCreated.getBoard_id(), newChildPtr.getBoard_id());
		assertNotNull(defaultCreated.getVersion());
		assertEquals(defaultCreated.getVersion() + 1, (int) newChildPtr.getVersion());
		assertEquals(defaultCreated.getRoot_board_id(), newChildPtr.getRoot_board_id());
	}
	
	@Test
	public void testCreateArticle() throws InterruptedException, ExecutionException, JsonProcessingException {
		BoardHistory dbHistory = defaultCreated;
		
		JsonUtils.assertConvertToJsonObject(defaultCreated, dbHistory);
		
		defaultBoard.setNodePtr(dbHistory);
		
		Board dbBoard = boardMapper.viewDetail(dbHistory.toMap());
		JsonUtils.assertConvertToJsonObject(defaultBoard, dbBoard);
	}
	
	@Test
	public void testCreateSevenHundredThousandTextContentArticle() throws JsonProcessingException, InterruptedException, ExecutionException {
		StringBuilder sevenHundredContent =  new StringBuilder();
		for(int i = 0; i < 700000; i++) {
			sevenHundredContent.append(i % 10);
		}
		
		defaultBoard = new Board();
		defaultBoard.setSubject("칠십만자가 들어가있습니다");
		defaultBoard.setContent(sevenHundredContent.toString());

		defaultCreated = versionManagementService.createArticle(defaultBoard);
		
		NodePtr nodePtr = defaultCreated;
		BoardHistory dbHistory = boardHistoryMapper.selectHistory(nodePtr);
		
		JsonUtils.assertConvertToJsonObject(defaultCreated, dbHistory);
		
		defaultBoard.setNodePtr(nodePtr);
		defaultBoard.setCreated_time(dbHistory.getCreated_time());
		
		Board dbBoard = boardMapper.viewDetail(nodePtr.toMap());
		JsonUtils.assertConvertToJsonObject(defaultBoard, dbBoard);
	}
	
	@Test
	public void testRecoverVersion() throws InterruptedException, ExecutionException, JsonProcessingException {
		BoardHistory createdHistory = versionManagementService.createArticle(defaultBoard);
		
		NodePtr prevPtr = createdHistory;
		Board prevleaf = boardMapper.viewDetail(prevPtr.toMap());
		NodePtr newleafPtr = versionManagementService.recoverVersion(prevPtr, prevPtr);
		
		BoardHistory recoveredHistory = boardHistoryMapper.selectHistory(newleafPtr);
		Board newleaf = boardMapper.viewDetail(newleafPtr.toMap());
		
		prevleaf.setNodePtr(newleafPtr);
		prevleaf.setCreated_time(newleaf.getCreated_time()); // 버전 복구시 시간이 달라짐
		
		assertNotNull(recoveredHistory);
		JsonUtils.assertConvertToJsonObject(newleaf, prevleaf);
	}
	
	@Test
	public void testMakeleafVersion() throws InterruptedException, ExecutionException, JsonProcessingException {
		Board child = new Board();
		child.setSubject("childSub");
		child.setContent("childCont");
		
		NodePtr parentPtr = defaultCreated;
		NodePtr resultPtr = versionManagementService.modifyVersion(child, parentPtr, null);
		
		Board parentBoard = boardMapper.viewDetail(parentPtr.toMap());
		assertNull(parentBoard);
		Board leafBoard = boardMapper.viewDetail(resultPtr.toMap());
		assertNotNull(leafBoard);
		int defaultVersion = defaultBoard.getVersion() == null ? 0 : defaultBoard.getVersion();
		assertEquals((Integer) (defaultVersion + 1), resultPtr.getVersion());
		
		child.setNodePtr(resultPtr);
		JsonUtils.assertConvertToJsonObject(child, leafBoard);
	}
	
	
	@Test
	public void testMakeModifyHasChild() throws JsonProcessingException {
		NodePtr parentPtr = defaultCreated;

		makeChild(parentPtr);
		makeChild(parentPtr);
	}
	
	@Test
	public void testDeleteleafNode() throws JsonProcessingException {
		NodePtr rootPtr = defaultCreated;

		NodePtr deletePtr = makeChild(rootPtr);
		versionManagementService.deleteVersion(deletePtr);

		List<BoardHistory> children = boardHistoryMapper.selectChildren(rootPtr);
		assertEquals(0, children.size());
	}
	
	@Test
	public void testDeleteWhenHasChildNode() throws JsonProcessingException {
		NodePtr rootPtr = defaultCreated;
		
		NodePtr middlePtr = makeChild(rootPtr);
		NodePtr childPtr = makeChild(middlePtr);
		
		versionManagementService.deleteVersion(middlePtr);
		
		BoardHistory childHistory = boardHistoryMapper.selectHistory(childPtr);
		NodePtr childParentPtr = childHistory.getParentPtrAndRoot();
		
		assertEquals(rootPtr, childParentPtr);
	}
	
	@Test
	public void testDeleteHasChildrenNode() throws JsonProcessingException {
		NodePtr rootPtr = defaultCreated;
		
		NodePtr middlePtr = makeChild(rootPtr);

		int childrenCnt = 10;
		List<NodePtr> childrenList = new ArrayList<>(childrenCnt);
		for(int i = 0; i < childrenCnt; i++) {
			childrenList.add(makeChild(middlePtr));
		}
		
		versionManagementService.deleteVersion(middlePtr);
		
		for (NodePtr child : childrenList) {
			BoardHistory history = boardHistoryMapper.selectHistory(child);
			NodePtr parentPtr = history.getParentPtrAndRoot();
			assertEquals(rootPtr, parentPtr);
		}
	}
	
	@Test
	public void testDeleteRootNode() throws JsonProcessingException {
		NodePtr child = makeChild(defaultCreated);
		assertNotNull(child);
		
		NodePtr newLeafNodePtr = versionManagementService.deleteVersion(defaultCreated);
		assertNull(newLeafNodePtr);
	}
	
	@Test
	public void testDeleteArticle() throws JsonProcessingException, NotLeafNodeException {
		NodePtr rootPtr = defaultCreated;
		
		NodePtr hasChildrenPtr = makeChild(rootPtr);
		
		int childrenCnt = 2;
		List<NodePtr> childrenList = new ArrayList<>(childrenCnt);
		for(int i = 0; i < childrenCnt; i++) {
			childrenList.add(makeChild(hasChildrenPtr));
		}
		
		NodePtr hasChildPtr = childrenList.get(0);
		NodePtr leafPtr = makeChild(hasChildPtr);
		
		versionManagementService.deleteArticle(leafPtr);
	}
	
	@Test
	public void testDeleteArticleHasOneChild() throws JsonProcessingException, NotLeafNodeException {
		int generationCnt = 5;
		List<NodePtr> generationList = new ArrayList<>(generationCnt);
		generationList.add(defaultBoard);
		for(int i = 1; i < generationCnt; i++) {
			generationList.add(makeChild(generationList.get(i - 1)));
		}
		
		versionManagementService.deleteArticle(generationList.get(generationCnt - 1));
	}
	
	@Test(expected=NotLeafNodeException.class)
	public void testDeleteArticleShouldTrhowNotLeafNodeException() throws JsonProcessingException, NotLeafNodeException {
		int generationCnt = 5;
		List<NodePtr> generationList = new ArrayList<>(generationCnt);
		generationList.add(defaultBoard);
		for(int i = 1; i < generationCnt; i++) {
			generationList.add(makeChild(generationList.get(i - 1)));
		}
		
		versionManagementService.deleteArticle(generationList.get(0));
	}
	
	@Test(expected=NotLeafNodeException.class)
	public void testGetRelatedHistoryhouldTrhowNotLeafNodeException() throws JsonProcessingException, NotLeafNodeException {
		int generationCnt = 5;
		List<NodePtr> generationList = new ArrayList<>(generationCnt);
		generationList.add(defaultBoard);
		for(int i = 1; i < generationCnt; i++) {
			generationList.add(makeChild(generationList.get(i - 1)));
		}
		
		@SuppressWarnings("unused")
		List<BoardHistory> relatedHistoryList = versionManagementService.getRelatedHistory(generationList.get(0));
	}
	
	@Test
	public void testGetRelatedHistory() throws JsonProcessingException, NotLeafNodeException {
		int childrenCnt = 2;
		int allCnt = 1 + 1 + childrenCnt + 1;
		int relatedCnt = allCnt - (childrenCnt - 1);	// childrenList에서 1개만 사용
		List<NodePtr> nodePtrList = new ArrayList<>(allCnt);
		NodePtr rootPtr = defaultCreated;
		nodePtrList.add(rootPtr);
		
		NodePtr hasChildrenPtr = makeChild(rootPtr);
		nodePtrList.add(hasChildrenPtr);
		
		List<NodePtr> childrenList = new ArrayList<>(childrenCnt);
		for(int i = 0; i < childrenCnt; i++) {
			childrenList.add(makeChild(hasChildrenPtr));
		}
		nodePtrList.addAll(childrenList);
		
		NodePtr hasChildPtr = childrenList.get(childrenCnt - 1);
		NodePtr leafPtr = makeChild(hasChildPtr);
		nodePtrList.add(leafPtr);
		NodePtr leafPtrWithoutRootBoardId = new NodePtr(leafPtr.getBoard_id(), leafPtr.getVersion());
		
		List<BoardHistory> relatedHistoryList = versionManagementService.getRelatedHistory(leafPtrWithoutRootBoardId);
		assertEquals(relatedHistoryList.size(), relatedCnt);
		assertNotNull(relatedHistoryList);
		for (BoardHistory eleHistory : relatedHistoryList) {
			assertNotNull(eleHistory);
			NodePtr eleNodePtr = eleHistory;
			assertTrue(nodePtrList.contains(eleNodePtr));
		}
	}
	
	@Test
	public void testGetRelatedHistoryWhenDifferentBoardId() throws JsonProcessingException, NotLeafNodeException {
		List<NodePtr> leafToRoot = new ArrayList<>();
		NodePtr rootPtr = defaultCreated;
		leafToRoot.add(rootPtr);
		
		@SuppressWarnings("unused")
		NodePtr middle1 = makeChild(rootPtr);
		NodePtr middle2 = makeChild(rootPtr);
		leafToRoot.add(middle2);
		
		NodePtr child = makeChild(middle2);
		leafToRoot.add(child);
		
		List<BoardHistory> relatedHistoryList = versionManagementService.getRelatedHistory(child);
		
		assertEquals(relatedHistoryList.size(), leafToRoot.size());
		
		leafToRoot.sort(compareNodePtr);
		relatedHistoryList.sort(compareNodePtr);

		for(int i = 0; i < relatedHistoryList.size(); i++) {
			NodePtr relatedEle = relatedHistoryList.get(i);
			NodePtr addedEle = leafToRoot.get(i);
			assertEquals(relatedEle, addedEle);
		}
	}
	
	@Test
	public void testBoardIdIsSequenceNumber() {
		int boardCnt = 10;
		List<BoardHistory> boardHistoryList = new ArrayList<>(boardCnt);
		for(int i = 0; i < boardCnt; i++) {
			BoardHistory created = versionManagementService.createArticle(defaultBoard);
			boardHistoryList.add(created);
		}
		
		for(int i = 1; i < boardCnt; i++) {
			int beforeBoardId = boardHistoryList.get(i - 1).getBoard_id();
			int currentBoardId = boardHistoryList.get(i).getBoard_id();
			assertEquals(beforeBoardId + 1, currentBoardId);
		}
		
		for(BoardHistory ele : boardHistoryList) {
			Board dbBoard = boardMapper.viewDetail(ele.toMap());
			assertNotNull(dbBoard);
			assertEquals(dbBoard.getBoard_id(), ele.getBoard_id());
		}
	}
}