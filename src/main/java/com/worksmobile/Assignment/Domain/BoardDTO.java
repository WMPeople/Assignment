package com.worksmobile.Assignment.Domain;

import java.util.HashMap;

import lombok.Getter;
import lombok.Setter;

public class BoardDTO extends NodePtrDTO{
	@Setter @Getter private String subject;
	@Setter @Getter private String content;
	@Setter @Getter private String created;
	@Setter @Getter private int file_id;
	@Setter @Getter private int cookie_id = NodePtrDTO.LEAF_NODE_COOKIE_ID;
	
	public BoardDTO() { }
	/***
	 * BoardHistoryDTO의 내용제외하고 공통된 내용을 전부 가져옵니다.
	 * 중요! : 내용을 압축 해제 역할은 담당하지 않습니다.
	 * @param boardHistoryDTO
	 */
	public BoardDTO(BoardHistoryDTO boardHistoryDTO) {
		setNodePtrDTO(boardHistoryDTO);

		subject = boardHistoryDTO.getHistory_subject();
		content = null;
		created = boardHistoryDTO.getCreated();

		file_id = boardHistoryDTO.getFile_id();
	}

	public void setNodePtrDTO(NodePtrDTO nodePtrDTO) {
		board_id = nodePtrDTO.getBoard_id();
		version = nodePtrDTO.getVersion();
		root_board_id = nodePtrDTO.getRoot_board_id();
	}
	@Override
	public HashMap<String, Integer> toMap() {
		HashMap<String, Integer> map = new HashMap<>();
		map.put("board_id", board_id);
		map.put("version", version);
		map.put("cookie_id",cookie_id);
		return map;
	}
	
}
