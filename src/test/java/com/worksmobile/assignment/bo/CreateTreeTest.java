package com.worksmobile.assignment.bo;

import static org.junit.Assert.assertEquals;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

@RunWith(SpringRunner.class)
@SpringBootTest
public class CreateTreeTest {
	
	@Autowired
	private CreateTree createTree;
	
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
		
		@SuppressWarnings("unused") ArrayNode children1 = mapper.createArrayNode();
		mapper.createArrayNode();
		
		System.out.println(mapper.writerWithDefaultPrettyPrinter().writeValueAsString(rootNode));
	}
	
	@Test
	public void testGen() throws JsonProcessingException {
		ObjectMapper mapper = new ObjectMapper();

		ObjectNode node = createTree.main(17128);
		
		String out = mapper.writerWithDefaultPrettyPrinter().writeValueAsString(node);
		assertEquals("", out);
	}
}
