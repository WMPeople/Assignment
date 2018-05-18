package com.worksmobile.assignment.bo;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.servlet.ServletContext;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.worksmobile.assignment.model.BoardHistory;
import com.worksmobile.assignment.model.NodePtr;
import com.worksmobile.assignment.util.JsonUtils;

@Service
public class TreeViewerService {
	@Autowired
	private BoardHistoryService boardHistoryService;
	
	@Autowired
	private ServletContext servletContext;

	// @return container, type, nodeStructure : <Node>
	public ObjectNode getTreeJson(int rootBoardId) throws NotExistNodePtrException{
		ObjectMapper mapper = new ObjectMapper();
		
		NodePtr invisibleRootPtr = new NodePtr(rootBoardId, NodePtr.INVISIBLE_ROOT_VERSION, rootBoardId);
		BoardHistory invisibleRootHistory = boardHistoryService.selectHistory(invisibleRootPtr);
		Map<Entry<Integer, Integer>, BoardHistory> map = boardHistoryService.getHistoryMap(rootBoardId);

		map.put(invisibleRootHistory.toBoardIdAndVersionEntry(), invisibleRootHistory);
		
		ObjectNode rootNode = mapper.createObjectNode();
		
		ObjectNode chart = mapper.createObjectNode();
		chart.put("container", "#basic-example");
		rootNode.set("chart", chart);

		ObjectNode connectors = mapper.createObjectNode();
		connectors.put("type", "step");
		rootNode.set("connectors", connectors);	
		
		ObjectNode nodeStructure = recursivelyCreateNode(map, mapper, invisibleRootHistory);
		rootNode.set("nodeStructure", nodeStructure);
		return rootNode;
	}
	
	private List<BoardHistory> getChildren(Map<Entry<Integer, Integer>, BoardHistory> map, NodePtr nodePtr) {
		List<BoardHistory> children = new LinkedList<>();
		for(BoardHistory ele : map.values()) {
			NodePtr parentPtr = ele.getParentPtrAndRoot();
			if( nodePtr.getBoard_id().equals(parentPtr.getBoard_id()) &&
				nodePtr.getVersion().equals(parentPtr.getVersion())) {
				children.add(ele);
			}
		}
		return children;
	}
	
	/*
	 * Node : text, link, stackChildren:true, children(array<Node>)
	 */
	private ObjectNode recursivelyCreateNode(Map<Entry<Integer, Integer>, BoardHistory> map, ObjectMapper mapper, NodePtr nodePtr) {
		BoardHistory history = map.get(nodePtr.toBoardIdAndVersionEntry());
		if(history == null) {
			String errorStr = "nodePtr : " + JsonUtils.jsonStringIfExceptionToString(nodePtr);
			errorStr += "map : ";
			errorStr += JsonUtils.jsonStringIfExceptionToString(map);
			throw new RuntimeException(errorStr);
		}
		
		ArrayNode childrenArrayNode = mapper.createArrayNode();
		List<BoardHistory> children = getChildren(map, nodePtr);
		for(BoardHistory child : children) {
			ObjectNode childNode = recursivelyCreateNode(map, mapper, child);
			childrenArrayNode.add(childNode);
		}
		
		ObjectNode node = mapper.createObjectNode();
		
		ObjectNode text = mapper.createObjectNode();
		String name = nodePtr.getNodePtrStr();
		if(children.size() == 0) {
			name += "(leaf)";
		}
		text.put("name", name);
		text.put("title", history.getHistory_subject());
		String desc = String.format("%s (%d)", history.getCreated_time(), history.getFile_id());
		text.put("desc", desc);
		node.set("text", text);

		ObjectNode link = mapper.createObjectNode();
		String nodeLinkStr;
		if(children.size() == 0) {
			nodeLinkStr = String.format("%s/boards/%d/%d", servletContext.getContextPath(), nodePtr.getBoard_id(), nodePtr.getVersion());
		} else {
			nodeLinkStr = String.format("%s/history/%d/%d", servletContext.getContextPath(), nodePtr.getBoard_id(), nodePtr.getVersion());
		}
		link.put("href", nodeLinkStr);
		node.set("link", link);
		
		if(children.size() != 0) {
			node.set("children", childrenArrayNode);
		}
		
		return node;
	}
}
