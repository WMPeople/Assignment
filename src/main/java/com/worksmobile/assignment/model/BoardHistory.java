package com.worksmobile.assignment.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
@AllArgsConstructor
public class BoardHistory extends NodePtr{
	private String created_time;
	private String status;
	private String history_subject;
	private byte[] history_content;
	private boolean is_content_compressed;

	private Integer parent_board_id = null;
	private Integer parent_version = null;

	private int file_id;
	
	public static final String STATUS_CREATED = "Created";
	public static final String STATUS_MODIFIED = "Modified";
	public static final String STATUS_RECOVERED = "Recovered";
	public static final String STATUS_INVISIBLE_ROOT = "Root";
	
	public BoardHistory() {};
	
	public void setNodePtr(NodePtr nodePtr) {
		board_id = nodePtr.getBoard_id();
		version = nodePtr.getVersion();
		root_board_id = nodePtr.getRoot_board_id();
	}
	
	public void setParentNodePtrAndRoot(NodePtr parentNodePtr) {
		parent_board_id = parentNodePtr.getBoard_id();
		parent_version = parentNodePtr.getVersion();
		root_board_id = parentNodePtr.getRoot_board_id();
	}

	public NodePtr getParentPtrAndRoot() {
		return new NodePtr(parent_board_id, parent_version, root_board_id);
	}

	@Override
	public BoardHistory clone() {
		BoardHistory rtn = new BoardHistory();
		rtn.board_id = board_id;
		rtn.version = version;
		rtn.created_time = created_time;
		rtn.status = status;
		rtn.history_subject = history_subject;
		if (history_content != null) {
			rtn.history_content = history_content.clone();
		}
		rtn.is_content_compressed = is_content_compressed;
		rtn.parent_board_id = parent_board_id;
		rtn.parent_version = parent_version;
		rtn.root_board_id = root_board_id;
		rtn.file_id = file_id;
		return rtn;
	}

	public boolean isInvisibleRoot() {
		return version == NodePtr.INVISIBLE_ROOT_VERSION &&
			root_board_id == NodePtr.INVISIALBE_ROOT_BOARD_ID &&
			parent_board_id == null &&
			parent_version == null;
	}
}