package com.worksmobile.Assignment.Domain;

import lombok.Getter;
import lombok.Setter;

public class BoardHistoryDTO extends NodePtrDTO{
	@Getter @Setter private String created;
	@Getter @Setter private String status;
	@Getter @Setter private String history_subject;
	@Getter @Setter private byte[] history_content;

	@Getter @Setter private int parent_board_id;
	@Getter @Setter private int parent_version;
	@Getter @Setter private int parent_branch;
	
	@Setter @Getter private String file_name;
	@Setter @Getter private byte[] file_data;
	@Setter @Getter private long file_size;
	
	public BoardHistoryDTO() {};
	/***
	 * 게시글을 새롭게 만들때 사용. 공통된 내용을 전부 복사합니다.
	 * 게시글 내용 압축도 담당.
	 * @param article 새로운 게시글의 내용.
	 * @param nodePtrDTO 게시글의 포인터
	 */
	public BoardHistoryDTO(BoardDTO article, NodePtrDTO nodePtrDTO) {
		board_id = nodePtrDTO.getBoard_id();
		version = nodePtrDTO.getVersion() ;
		branch = nodePtrDTO.getBranch();
		status = "Created";
		history_subject = article.getSubject();
		//created = null;
		// TODO : article.getContent() conver to history_content. with zipping.
	}
	
	public void setParentNodePtr(NodePtrDTO parentNodePtrDTO) {
		parent_board_id = parentNodePtrDTO.getBoard_id();
		parent_version = parentNodePtrDTO.getVersion();
		parent_branch = parentNodePtrDTO.getBranch();
	}
	
	public NodePtrDTO getParentPtrDTO() {
		return new NodePtrDTO(parent_board_id, parent_version, parent_branch);
	}
}