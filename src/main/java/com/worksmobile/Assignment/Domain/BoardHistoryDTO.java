package com.worksmobile.Assignment.Domain;

import java.io.IOException;

import lombok.Getter;
import lombok.Setter;

public class BoardHistoryDTO extends NodePtrDTO{
	@Getter @Setter private String created;
	@Getter @Setter private String status;
	@Getter @Setter private String history_subject;
	@Getter @Setter private byte[] history_content;

	@Getter @Setter private Integer parent_board_id = null;
	@Getter @Setter private Integer parent_version = null;

	@Setter@Getter private int file_id;
	
	public static final String STATUS_CREATED = "Created";
	public static final String STATUS_MODIFIED = "Modified";
	public static final String STATUS_RECOVERED= "Recovered";
	public static final String STATUS_TEMP = "Temp";
	public static final String STATUS_ROOT = "Root";
	
	public BoardHistoryDTO() {};
	/***
	 * 게시글을 새롭게 만들때 사용. (내용 제외)공통된 내용을 전부 복사합니다.
	 * 중요! : 게시글 내용 압축은 담당하지 않습니다.
	 * @param article 새로운 게시글의 내용.
	 * @param nodePtrDTO 게시글의 포인터
	 * @throws IOException 
	 */
	public BoardHistoryDTO(BoardDTO article, NodePtrDTO nodePtrDTO, String status) {
		board_id = nodePtrDTO.getBoard_id();
		version = nodePtrDTO.getVersion() ;
		this.status = status;
		history_subject = article.getSubject();
		file_id = article.getFile_id();
	}
	
	public void setParentNodePtrAndRoot(NodePtrDTO parentNodePtrDTO) {
		parent_board_id = parentNodePtrDTO.getBoard_id();
		parent_version = parentNodePtrDTO.getVersion();
		root_board_id = parentNodePtrDTO.getRoot_board_id();
	}
	
	public NodePtrDTO getParentPtrAndRoot() {
		return new NodePtrDTO(parent_board_id, parent_version, root_board_id);
	}
	
	@Override
	public BoardHistoryDTO clone() {
		BoardHistoryDTO rtn = new BoardHistoryDTO();
		rtn.board_id = board_id;
		rtn.version = version;
		rtn.created = created;
		rtn.status = status;
		rtn.history_subject = history_subject;
		if(history_content != null) {
			rtn.history_content = history_content.clone();
		}
		rtn.parent_board_id = parent_board_id;
		rtn.parent_version = parent_version;
		rtn.root_board_id = root_board_id;
		rtn.file_id = file_id;
		return rtn;
	}

	public boolean isRoot() {
		return root_board_id == 0 &&
				parent_board_id == null &&
				parent_version == null;
	}
}