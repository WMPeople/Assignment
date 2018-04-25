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
public class CreateTree {
	@Autowired
	private BoardHistoryService boardHistoryService;
	
	@Autowired
	private ServletContext servletContext;

	// @return container, type, nodeStructure : <Node>
	public ObjectNode createTree(int rootBoardId) {
		ObjectMapper mapper = new ObjectMapper();
		
		Map<Entry<Integer, Integer>, BoardHistory> map = boardHistoryService.getHistoryMap(rootBoardId);
		if(map.size() == 0) {
			throw new RuntimeException("이력이 존재하지 않는 게시글 id입니다. rootBoardId : " + rootBoardId);
		}

		BoardHistory invisibleRootHistory = new BoardHistory();
		invisibleRootHistory.setBoard_id(rootBoardId);
		invisibleRootHistory.setVersion(NodePtr.INVISIBLE_ROOT_VERSION);
		invisibleRootHistory.setRoot_board_id(NodePtr.INVISIALBE_ROOT_BOARD_ID);
		invisibleRootHistory.setHistory_subject("invisible root");
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
		String name = nodePtr.toString();
		if(children.size() == 0) {
			name += "(leaf)";
		}
		text.put("name", name);
		text.put("title", history.getHistory_subject());
		String desc = String.format("%s (%d)", history.getCreated_time(), history.getFile_id());
		text.put("desc", desc);
		node.set("text", text);

		ObjectNode link = mapper.createObjectNode();
		// TODO : 리프 노드는 게시글 링크로 하면 성능 향상이 기대됨
		String nodelinkStr = String.format("%s/history/%d/%d", servletContext.getContextPath(), nodePtr.getBoard_id(), nodePtr.getVersion());
		link.put("href", nodelinkStr);
		node.set("link", link);
		node.put("stackChildren", true);
		
		if(children.size() != 0) {
			node.set("children", childrenArrayNode);
		}
		
		return node;
	}
}
