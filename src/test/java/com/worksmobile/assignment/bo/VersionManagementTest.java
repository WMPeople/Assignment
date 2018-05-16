package com.worksmobile.assignment.bo;

import static org.awaitility.Awaitility.await;
import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.hamcrest.Matchers.greaterThan;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertThat;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ErrorCollector;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.worksmobile.assignment.mapper.BoardAdapter;
import com.worksmobile.assignment.mapper.BoardHistoryMapper;
import com.worksmobile.assignment.mapper.BoardMapper;
import com.worksmobile.assignment.model.Board;
import com.worksmobile.assignment.model.BoardHistory;
import com.worksmobile.assignment.model.NodePtr;
import com.worksmobile.assignment.util.JsonUtils;

/**
 * 이력 관리에 기능들에 대한 테스트입니다.
 * @author khh
 *
 */
@RunWith(SpringRunner.class)
@SpringBootTest
public class VersionManagementTest {

	@Autowired
	private VersionManagementService versionManagementService;
	
	@Autowired
	private BoardMapper boardMapper;
	
	@Autowired
	private BoardHistoryMapper boardHistoryMapper;
	
	@Autowired
	private AsyncAwaitHelper asyncAwaitHelper;
	
	@Rule
	public ErrorCollector collector = new ErrorCollector();

	private Board defaultCreated;
	private Comparator<NodePtr> compareNodePtr = Comparator.comparing(NodePtr::getRoot_board_id)
			.thenComparing(NodePtr::getBoard_id).thenComparing(NodePtr::getVersion);
	
	@Before
	public void createDefault() throws InterruptedException, ExecutionException {
		defaultCreated = new Board();
		defaultCreated.setSubject("초기글");
		defaultCreated.setContent("맨 처음에 작성한 글입니다.");

		versionManagementService.createArticle(defaultCreated);
		
		Board article = boardMapper.viewDetail(defaultCreated.toMap());
		assertNotNull(article);
		BoardHistory articleHistory = asyncAwaitHelper.waitAndSelectBoardHistory(defaultCreated);
		assertNotNull(articleHistory);
		assertEquals(article.getCreated_time(), articleHistory.getCreated_time());
	}
	
	@Test
	public void testCanCreate() {
		assertEquals(defaultCreated.getBoard_id(), (Integer)defaultCreated.getRoot_board_id());
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
		
		BoardHistory childHistory = asyncAwaitHelper.waitAndSelectBoardHistory(childPtr);
		Board convertedChild = BoardAdapter.from(childHistory);
		JsonUtils.assertConvertToJsonObject(child, convertedChild);
		
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
		
		await().untilAsserted(() -> assertThat(boardHistoryMapper.selectHistory(defaultCreated), not(nullValue())));
		NodePtr newChildPtr = makeChild(defaultCreated);
		
		assertEquals(defaultCreated.getBoard_id(), newChildPtr.getBoard_id());
		assertNotNull(defaultCreated.getVersion());
		assertEquals(defaultCreated.getVersion() + 1, (int) newChildPtr.getVersion());
		assertEquals(defaultCreated.getRoot_board_id(), newChildPtr.getRoot_board_id());
	}
	
	@Test
	public void testCreateArticle() throws InterruptedException, ExecutionException, JsonProcessingException {
		Board dbBoard = boardMapper.viewDetail(defaultCreated.toMap());
		JsonUtils.assertConvertToJsonObject(defaultCreated, dbBoard);
	}
	
	@Test
	public void testCreateSevenHundredThousandTextContentArticle() throws JsonProcessingException, InterruptedException, ExecutionException {
		StringBuilder sevenHundredContent =  new StringBuilder();
		for(int i = 0; i < 700000; i++) {
			sevenHundredContent.append(i % 10);
		}
		
		Board article = new Board();
		article.setSubject("칠십만자가 들어가있습니다");
		article.setContent(sevenHundredContent.toString());

		defaultCreated = versionManagementService.createArticle(article);
		
		BoardHistory dbHistory = asyncAwaitHelper.waitAndSelectBoardHistory(defaultCreated);
		
		Board deConvered = BoardAdapter.from(dbHistory);
		JsonUtils.assertConvertToJsonObject(defaultCreated, deConvered);
		
		article.setNodePtr(defaultCreated);
		article.setCreated_time(dbHistory.getCreated_time());
		
		Board dbBoard = boardMapper.viewDetail(defaultCreated.toMap());
		JsonUtils.assertConvertToJsonObject(article, dbBoard);
	}
	
	@Test
	public void testRecoverVersion() throws InterruptedException, ExecutionException, JsonProcessingException {
		NodePtr prevPtr = defaultCreated;
		Board leaf = boardMapper.viewDetail(prevPtr.toMap());
		await().untilAsserted(() -> assertThat(boardHistoryMapper.selectHistory(prevPtr), not(nullValue())));
		NodePtr newLeafPtr = versionManagementService.recoverVersion(prevPtr, prevPtr);
		
		BoardHistory recoveredHistory = asyncAwaitHelper.waitAndSelectBoardHistory(newLeafPtr);
		Board newleaf = boardMapper.viewDetail(newLeafPtr.toMap());
		
		assertEquals(leaf.getBoard_id(), newLeafPtr.getBoard_id());
		assertNotEquals(leaf.getVersion(), newLeafPtr.getVersion());
		assertEquals(leaf.getRoot_board_id(), newLeafPtr.getRoot_board_id());
		leaf.setVersion(newLeafPtr.getVersion());
		leaf.setBoard_id(newLeafPtr.getBoard_id());
		leaf.setCreated_time(newleaf.getCreated_time()); // 버전 복구시 시간이 달라짐
		
		assertNotNull(recoveredHistory);
		JsonUtils.assertConvertToJsonObject(newleaf, leaf);
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
		int defaultVersion = defaultCreated.getVersion() == null ? 0 : defaultCreated.getVersion();
		assertEquals((Integer) (defaultVersion + 1), resultPtr.getVersion());
		
		child.setNodePtr(resultPtr);
		JsonUtils.assertConvertToJsonObject(child, leafBoard);
	}
	
	
	@Test
	public void testMakeModifyHasChild() throws JsonProcessingException {
		NodePtr parentPtr = defaultCreated;

		NodePtr child1 = makeChild(parentPtr);
		NodePtr child2 = makeChild(parentPtr);
		assertEquals(parentPtr.getRoot_board_id(), child1.getRoot_board_id());
		assertEquals(parentPtr.getRoot_board_id(), child2.getRoot_board_id());
	}
	
	/**
	 * 보이지 않는 루트 - 루트 - 자식
	 * 자식을 삭제합니다. 
	 */
	@Test
	public void testDeleteleafNode() throws JsonProcessingException {
		NodePtr rootPtr = defaultCreated;
		
		NodePtr deletePtr = makeChild(rootPtr);
		versionManagementService.deleteVersion(deletePtr);
		
		Board deletedArticle = boardMapper.viewDetail(deletePtr.toMap());
		assertNull(deletedArticle);
		BoardHistory deletedHistory = boardHistoryMapper.selectHistory(deletePtr);
		assertNull(deletedHistory);
		List<BoardHistory> children = boardHistoryMapper.selectChildren(rootPtr);
		assertEquals(0, children.size());
	}
	
	/**
	 * 보이지 않는 루트 - 루트
	 * 루트를 삭제하면, 둘다 삭제되어야 합니다.
	 */
	@Test
	public void testDeleteAloneRoot() {
		Board rootArticle = boardMapper.viewDetail(defaultCreated.toMap());
		assertNotNull(rootArticle);
		
		versionManagementService.deleteVersion(rootArticle);
		rootArticle = boardMapper.viewDetail(defaultCreated.toMap());
		assertNull(rootArticle);
		BoardHistory rootHistory = boardHistoryMapper.selectHistory(defaultCreated);
		assertNull(rootHistory);
		NodePtr invisibleNodePtr = new NodePtr(defaultCreated.getBoard_id(), NodePtr.INVISIBLE_ROOT_VERSION, NodePtr.INVISIALBE_ROOT_BOARD_ID);
		BoardHistory invisibleRoot = boardHistoryMapper.selectHistory(invisibleNodePtr);
		assertNull(invisibleRoot);
	}
	
	@Test
	public void testDeleteWhenHasChildNode() throws JsonProcessingException {
		NodePtr rootPtr = defaultCreated;
		
		NodePtr middlePtr = makeChild(rootPtr);
		NodePtr childPtr = makeChild(middlePtr);
		
		versionManagementService.deleteVersion(middlePtr);
		
		BoardHistory childHistory = asyncAwaitHelper.waitAndSelectBoardHistory(childPtr);
		NodePtr childParentPtr = childHistory.getParentPtrAndRoot();
		
		assertEquals(new NodePtr(rootPtr), childParentPtr);
	}
	
	/*
	 * 중간 노드를 지워서 자식이 지워진 노드의 부모를 가리키는지 확인합니다.
	 */
	@Test
	public void testDeleteHasChildrenNode() throws JsonProcessingException {
		NodePtr rootPtr = new NodePtr(defaultCreated);
		
		NodePtr middlePtr = makeChild(rootPtr);

		int childrenCnt = 10;
		List<NodePtr> childrenList = new ArrayList<>(childrenCnt);
		for(int i = 0; i < childrenCnt; i++) {
			childrenList.add(makeChild(middlePtr));
		}
		
		versionManagementService.deleteVersion(middlePtr);
		
		for (NodePtr child : childrenList) {
			BoardHistory childhistory = boardHistoryMapper.selectHistory(child);
			NodePtr parentPtr = childhistory.getParentPtrAndRoot();
			JsonUtils.assertConvertToJsonObject(rootPtr, parentPtr);
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
		
		await().atMost(30, TimeUnit.SECONDS).untilAsserted(() -> assertThat(boardHistoryMapper.selectHistory(leafPtr), is(nullValue())));
		await().atMost(30, TimeUnit.SECONDS).untilAsserted(() -> assertThat(boardHistoryMapper.selectHistory(hasChildPtr), is(nullValue())));
	}
	
	@Test
	public void testDeleteArticleHasOneChild() throws JsonProcessingException, NotLeafNodeException {
		int generationCnt = 5;
		List<NodePtr> generationList = new ArrayList<>(generationCnt);
		generationList.add(defaultCreated);
		for(int i = 1; i < generationCnt; i++) {
			generationList.add(makeChild(generationList.get(i - 1)));
		}
		
		versionManagementService.deleteArticle(generationList.get(generationCnt - 1));
		
		for(NodePtr ele : generationList) {
			await().atMost(30, TimeUnit.SECONDS).untilAsserted(() -> assertThat(boardHistoryMapper.selectHistory(ele), is(nullValue())));
		}
	}
	
	@Test(expected=NotLeafNodeException.class)
	public void testDeleteArticleShouldTrhowNotLeafNodeException() throws JsonProcessingException, NotLeafNodeException {
		int generationCnt = 5;
		List<NodePtr> generationList = new ArrayList<>(generationCnt);
		generationList.add(defaultCreated);
		for(int i = 1; i < generationCnt; i++) {
			generationList.add(makeChild(generationList.get(i - 1)));
		}
		
		versionManagementService.deleteArticle(generationList.get(0));
	}
	
	@Test(expected=NotLeafNodeException.class)
	public void testGetRelatedHistoryhouldTrhowNotLeafNodeException() throws JsonProcessingException, NotLeafNodeException {
		int generationCnt = 5;
		List<NodePtr> generationList = new ArrayList<>(generationCnt);
		generationList.add(defaultCreated);
		for(int i = 1; i < generationCnt; i++) {
			generationList.add(makeChild(generationList.get(i - 1)));
		}
		
		@SuppressWarnings("unused")
		List<BoardHistory> relatedHistoryList = versionManagementService.getRelatedHistory(generationList.get(0));
	}
	
	private boolean hasNodePtr(List<NodePtr> list, NodePtr target) throws JsonProcessingException {
		NodePtr targetPtr = new NodePtr(target);
		String targetJson = JsonUtils.jsonStringFromObject(targetPtr);
		for(NodePtr ele : list) {
			String eleJson = JsonUtils.jsonStringFromObject(new NodePtr(ele));
			boolean has = targetJson.equals(eleJson);
			if(has) {
				return true;
			}
		}
		return false;
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
		
		await().untilAsserted(() -> assertThat(boardHistoryMapper.selectHistory(leafPtr), not(nullValue())));
		List<BoardHistory> relatedHistoryList = versionManagementService.getRelatedHistory(leafPtrWithoutRootBoardId);
		assertEquals(relatedHistoryList.size(), relatedCnt);
		assertNotNull(relatedHistoryList);
		for (BoardHistory eleHistory : relatedHistoryList) {
			assertNotNull(eleHistory);
			NodePtr eleNodePtr = eleHistory;
			collector.checkThat(JsonUtils.jsonStringFromObject(eleNodePtr), true, is(hasNodePtr(nodePtrList, eleNodePtr)));
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
		
		await().untilAsserted(() -> assertThat(boardHistoryMapper.selectHistory(child), not(nullValue())));
		List<BoardHistory> relatedHistoryList = versionManagementService.getRelatedHistory(child);
		
		assertEquals(relatedHistoryList.size(), leafToRoot.size());
		
		leafToRoot.sort(compareNodePtr);
		relatedHistoryList.sort(compareNodePtr);

		for(int i = 0; i < relatedHistoryList.size(); i++) {
			NodePtr relatedEle = relatedHistoryList.get(i);
			NodePtr addedEle = leafToRoot.get(i);
			JsonUtils.assertConvertToJsonObject(new NodePtr(relatedEle), new NodePtr(addedEle));
		}
	}
	
	@Test
	public void testBoardIdIsSequenceNumber() throws InterruptedException, JsonProcessingException {
		int boardCnt = 10;
		List<Board> boardList = new ArrayList<>(boardCnt);
		for(int i = 0; i < boardCnt; i++) {
			Board article = new Board();
			article.setSubject("초기글");
			article.setContent("맨 처음에 작성한 글입니다.");
			versionManagementService.createArticle(article);
			boardList.add(article);
		}
		
		for(int i = 1; i < boardCnt; i++) {
			int beforeBoardId = boardList.get(i - 1).getBoard_id();
			int currentBoardId = boardList.get(i).getBoard_id();
			
			collector.checkThat(currentBoardId, greaterThan(beforeBoardId));
		}
		
		for(Board ele : boardList) {
			Board dbBoard = boardMapper.viewDetail(ele.toMap());
			collector.checkThat(JsonUtils.jsonStringFromObject(dbBoard), dbBoard, is(notNullValue()));
			collector.checkThat(dbBoard.getBoard_id(), is(ele.getBoard_id()));
		}
	}
	
}
