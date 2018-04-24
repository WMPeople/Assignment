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

	public ObjectNode main(int rootBoardId) {
		ObjectMapper mapper = new ObjectMapper();
		BoardHistory invisibleHistory = new BoardHistory();
		invisibleHistory.setBoard_id(rootBoardId);
		invisibleHistory.setVersion(NodePtr.INVISIBLE_ROOT_VERSION);
		invisibleHistory.setRoot_board_id(NodePtr.INVISIALBE_ROOT_BOARD_ID);
		invisibleHistory.setHistory_subject("invisible root");
		
		Map<Entry<Integer, Integer>, BoardHistory> map = boardHistoryService.getHistoryMap(rootBoardId);
		if(map.size() == 0) {
			throw new RuntimeException("history가 없습니다. rootBoardId : " + rootBoardId);
		}
		map.put(invisibleHistory.toBoardIdAndVersionEntry(), invisibleHistory);
		
		ObjectNode rootNode = mapper.createObjectNode();
		
		ObjectNode chart = mapper.createObjectNode();
		chart.put("container", "#basic-example");
		rootNode.set("chart", chart);

		ObjectNode connectors = mapper.createObjectNode();
		connectors.put("type", "step");
		rootNode.set("connectors", connectors);	
		
		ObjectNode nodeStructure = createNode(map, mapper, invisibleHistory);
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
	 * Node : text, link, stackChildren:true, children(array)
	 */
	public ObjectNode createNode(Map<Entry<Integer, Integer>, BoardHistory> map, ObjectMapper mapper, NodePtr nodePtr) {
		BoardHistory history = map.get(nodePtr.toBoardIdAndVersionEntry());
		if(history == null) {
			String errorStr = "nodePtr : " + JsonUtils.jsonStringIfExceptionToString(nodePtr);
			errorStr += "map : ";
			errorStr += JsonUtils.jsonStringIfExceptionToString(map);
			throw new RuntimeException(errorStr);
		}
		ObjectNode node = mapper.createObjectNode();
		
		ObjectNode text = mapper.createObjectNode();
		text.put("name", nodePtr.toString());
		text.put("title", history.getHistory_subject());
		String desc = String.format("createdTime : %s \tfileId : %d", history.getCreated_time(), history.getFile_id());
		text.put("desc", desc);
		
		node.set("text", text);
		ObjectNode link = mapper.createObjectNode();
		// TODO : 리프 노드는 게시글 링크로 하면 성능 향상이 기대됨
		String nodelinkStr = String.format("%s/history/%d/%d", servletContext.getContextPath(), nodePtr.getBoard_id(), nodePtr.getVersion());
		link.put("href", nodelinkStr);
		node.set("link", link);
		node.put("image", "//:0");
		node.put("stackChildren", true);
		
		ArrayNode childrenArrayNode = mapper.createArrayNode();
		List<BoardHistory> children = getChildren(map, nodePtr);
		for(BoardHistory child : children) {
			ObjectNode childNode = createNode(map, mapper, child);
			childrenArrayNode.add(childNode);
		}
		
		if(children.size() != 0) {
			node.set("children", childrenArrayNode);
		}
		
		return node;
	}
}
