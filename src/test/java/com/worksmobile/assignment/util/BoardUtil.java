package com.worksmobile.assignment.util;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.util.HashMap;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.worksmobile.assignment.bo.AsyncAwaitHelper;
import com.worksmobile.assignment.bo.VersionManagementService;
import com.worksmobile.assignment.mapper.BoardAdapter;
import com.worksmobile.assignment.mapper.BoardMapper;
import com.worksmobile.assignment.model.Board;
import com.worksmobile.assignment.model.BoardHistory;
import com.worksmobile.assignment.model.NodePtr;

@Component
public class BoardUtil {
	@Autowired
	BoardMapper boardMapper;
	
	@Autowired
	VersionManagementService versionManagementService;
	
	@Autowired
	AsyncAwaitHelper asyncAwaitHelper;
	
	public static Board makeArticle(String subject, String content) {
		Board article = new Board();
		article.setSubject(subject);
		article.setContent(content);
		
		return article;
	}
	
	public List<Board> selectAllArticles() {
		HashMap<String, Integer> articleParams = new HashMap<>();
		articleParams.put("offset", 0);
		articleParams.put("noOfRecords", Integer.MAX_VALUE);
		return boardMapper.articleList(articleParams);	
	}
	
	public NodePtr makeChild(NodePtr parentPtr) throws JsonProcessingException {
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
}
