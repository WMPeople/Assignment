package com.worksmobile.assignment.Service;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import java.io.IOException;
import java.util.ArrayList;
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
import com.worksmobile.Assignment.Service.NotLeafNodeException;
import com.worksmobile.Assignment.Service.VersionManagementService;
import com.worksmobile.Assignment.util.Utils;

@RunWith(SpringRunner.class)
@SpringBootTest
public class VersionManagementServiceAutoSaveTest {

	@Autowired
	VersionManagementService versionManagementService;
	
	@Autowired
	BoardMapper boardMapper;
	
	@Autowired
	BoardHistoryMapper boardHistoryMapper;
	
	private BoardDTO defaultBoardDTO;
	private BoardHistoryDTO defaultCreatedDTO;
	private static final int DEFAULT_COOKIE_ID = 99999;
	private BoardDTO autoSaveArticle = new BoardDTO();
	
	@Before
	public void createDefault() throws InterruptedException, ExecutionException {
		defaultBoardDTO = new BoardDTO();
		defaultBoardDTO.setSubject("versionTestSub");
		defaultBoardDTO.setContent("versionTestCont");;

		defaultCreatedDTO = versionManagementService.createArticle(defaultBoardDTO);
		
		autoSaveArticle.setSubject("자동 저장중...");
		autoSaveArticle.setContent("temp article content");
		autoSaveArticle.setNodePtrDTO(defaultCreatedDTO);
		autoSaveArticle.setCookie_id(DEFAULT_COOKIE_ID);
		
		versionManagementService.createTempArticleOverwrite(autoSaveArticle);
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
	
	private BoardDTO makeAutoSaveDTO(NodePtrDTO nodePtrDTO) {
		BoardDTO autoSaveDTO = new BoardDTO();
		autoSaveDTO.setNodePtrDTO(nodePtrDTO);
		autoSaveDTO.setCookie_id(DEFAULT_COOKIE_ID);
		versionManagementService.createTempArticleOverwrite(autoSaveDTO);
		return autoSaveDTO;
	}
	
	
	@Test
	public void testCreateAutoSaveArticle() throws IOException {
		BoardDTO dbTempArticle = boardMapper.viewDetail(autoSaveArticle.toMap());
		Utils.assertConvertToJsonObject(autoSaveArticle.toMap(), dbTempArticle.toMap());
		Utils.assertConvertToJsonObject(autoSaveArticle, dbTempArticle);
	}
	
	@Test
	public void testDeleteArticleHasAutoSave() throws JsonProcessingException, NotLeafNodeException {
		NodePtrDTO rootPtrDTO = defaultCreatedDTO;
		
		NodePtrDTO hasChildrenPtrDTO = makeChild(rootPtrDTO);
		
		int childrenCnt = 2;
		List<NodePtrDTO> childrenList = new ArrayList<>(childrenCnt);
		for(int i = 0; i < childrenCnt; i++) {
			childrenList.add(makeChild(hasChildrenPtrDTO));
		}
		
		NodePtrDTO hasChildPtrDTO = childrenList.get(0);
		NodePtrDTO leapPtrDTO = makeChild(hasChildPtrDTO);
		
		BoardDTO autoSaveDTO = makeAutoSaveDTO(leapPtrDTO);
		
		versionManagementService.deleteArticle(leapPtrDTO);
		
		BoardDTO dbAutoSavedArticle = boardMapper.viewDetail(autoSaveDTO.toMap());
		assertNull(dbAutoSavedArticle);
	}
	
	@Test
	public void testDeleteWhenHasChildNodeAndAutoSave() throws JsonProcessingException {
		NodePtrDTO rootPtrDTO = defaultCreatedDTO;
		
		NodePtrDTO middlePtrDTO = makeChild(rootPtrDTO);
		NodePtrDTO childPtrDTO = makeChild(middlePtrDTO);
		
		BoardDTO autoSaveDTO = makeAutoSaveDTO(middlePtrDTO);
		
		versionManagementService.deleteVersion(middlePtrDTO);
		
		BoardHistoryDTO childHistoryDTO = boardHistoryMapper.getHistory(childPtrDTO);
		NodePtrDTO childParentPtrDTO = childHistoryDTO.getParentPtrAndRoot();
		
		assertEquals(rootPtrDTO, childParentPtrDTO);
		
		BoardDTO dbAutoSaveArticle = boardMapper.viewDetail(autoSaveArticle.toMap());
		assertNotNull(dbAutoSaveArticle);
		BoardDTO dbAtuoSaveDTO = boardMapper.viewDetail(autoSaveDTO.toMap());
		assertNull(dbAtuoSaveDTO);
	}
	
	@Test
	public void testDeleteHasChildrenNodeAndAutoSave() throws JsonProcessingException {
		NodePtrDTO rootPtrDTO = defaultCreatedDTO;
		
		NodePtrDTO middlePtrDTO = makeChild(rootPtrDTO);

		int childrenCnt = 10;
		List<NodePtrDTO> childrenList = new ArrayList<>(childrenCnt);
		List<NodePtrDTO> autoSavedList = new ArrayList<>(childrenCnt);
		for(int i = 0; i < childrenCnt; i++) {
			childrenList.add(makeChild(middlePtrDTO));
			autoSavedList.add(makeAutoSaveDTO(middlePtrDTO));
		}
		
		versionManagementService.deleteVersion(middlePtrDTO);
		
		for(NodePtrDTO child : childrenList) {
			BoardHistoryDTO historyDTO = boardHistoryMapper.getHistory(child);
			NodePtrDTO parentPtrDTO = historyDTO.getParentPtrAndRoot();
			assertEquals(rootPtrDTO, parentPtrDTO);
		}
	}
}
