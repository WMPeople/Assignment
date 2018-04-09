package com.worksmobile.Assignment.Mapper;

import java.util.HashMap;
import java.util.List;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import com.worksmobile.Assignment.Domain.BoardDTO;
import com.worksmobile.Assignment.Domain.FileDTO;
import com.worksmobile.Assignment.Domain.NodePtrDTO;

@Mapper
public interface BoardMapper {

	public int boardCreate(BoardDTO board) ;

	/**
	 * 관련된 자동 저장 게시글도 같이 삭제됩니다.
	 * @param parmas HashMap(board_id, version)
	 * @return
	 */
	public int boardDelete(HashMap<String, Integer> parmas) ;
	
	public int boardDeleteWithCookieId(HashMap<String, Integer> params);
	
	public int boardUpdate(HashMap<String, Integer> parmas) ;
	
	public BoardDTO viewDetail(HashMap<String, Integer> parmas) ;
	
	public FileDTO boardFileDownload(HashMap<String,Integer> params) ;

	public int getMaxBoardId();
	
	public int getMaxCookieId();
	
	public List<BoardDTO> articleList(HashMap<String, Integer> params);
	
	/***
	 * root_board_id 는 무시됩니다. 
	 * @param nodePtrDTO board_id 와 version만 사용하여 가져옵니다.
	 * @return
	 */
	public List<BoardDTO> getBoardList(NodePtrDTO nodePtrDTO);
	
	public int articleGetCount();
	
}
