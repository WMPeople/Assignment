package com.worksmobile.assignment.bo;

import static org.awaitility.Awaitility.await;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertThat;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.worksmobile.assignment.mapper.BoardHistoryMapper;
import com.worksmobile.assignment.model.Board;
import com.worksmobile.assignment.model.NodePtr;
import com.worksmobile.assignment.util.BoardUtil;

/**
 * 
 * @author khh
 *
 */
@RunWith(SpringRunner.class)
@SpringBootTest
public class TreeViewerServiceTest {
	
	@Autowired
	private TreeViewerService treeViewer;
	
	@Autowired
	private VersionManagementService versionManagementService;
	
	@Autowired
	private BoardHistoryMapper boardHistoryMapper;
	
	private Board article;
	
	@Before
	public void createTestTree() {
		article = BoardUtil.makeArticle("testCreateTree", "test");
		NodePtr nodePtr = versionManagementService.createArticle(article);
		article.setNodePtr(nodePtr);
		
		await().untilAsserted(() -> assertThat(boardHistoryMapper.selectHistory(article), is(notNullValue())));
	}
	
	/*
	 * Node : text, link, stackChildren:true, children(array)
	 */
	@Test
	public void testCreateTree() throws JsonProcessingException {
		ObjectMapper mapper = new ObjectMapper();
		ObjectNode rootNode = mapper.createObjectNode();
		
		ObjectNode chart = mapper.createObjectNode();
		chart.put("container", "#basic-example");
		rootNode.set("chart", chart);
		
		ObjectNode connectors = mapper.createObjectNode();
		connectors.put("type", "step");
		rootNode.set("connectors", connectors);
		
		ObjectNode nodeStructure = mapper.createObjectNode();
		ObjectNode text1 = mapper.createObjectNode();
		text1.put("id", "1-1");
		text1.put("title", "title1");
		text1.put("link", "link");
		nodeStructure.set("text", text1);
		
		@SuppressWarnings("unused")
		ArrayNode children1 = mapper.createArrayNode();
		mapper.createArrayNode();
		
		System.out.println(mapper.writerWithDefaultPrettyPrinter().writeValueAsString(rootNode));
	}
	
	@Test
	public void testGenerate() throws JsonProcessingException {
		ObjectMapper mapper = new ObjectMapper();
		
		ObjectNode node = treeViewer.getTreeJson(article.getRoot_board_id());
		
		String out = mapper.writerWithDefaultPrettyPrinter().writeValueAsString(node);
		assertNotEquals("", out);
	}
	
	@Test(expected=NotExistNodePtrException.class)
	public void testNotExistNodePtr() {
		treeViewer.getTreeJson(Integer.MIN_VALUE);
	}
}
