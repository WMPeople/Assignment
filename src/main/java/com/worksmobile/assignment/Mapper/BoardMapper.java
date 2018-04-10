package com.worksmobile.assignment.Mapper;

import java.util.HashMap;
import java.util.List;

import org.apache.ibatis.annotations.Mapper;

import com.worksmobile.assignment.Model.Board;
import com.worksmobile.assignment.Model.File;
import com.worksmobile.assignment.Model.NodePtr;

/***
 * @author KHH
 * @author RWS
 */
@Mapper
public interface BoardMapper {

	public int boardCreate(Board board) ;

	/**
	 * 관련된 자동 저장 게시글도 같이 삭제됩니다.
	 * @param parmas HashMap(board_id, version)
	 * @return
	 */
	public int boardDelete(HashMap<String, Integer> parmas) ;

	
	public int boardDeleteWithCookieId(HashMap<String, Integer> params);

	public int boardUpdate(Board board) ;
	
	public Board viewDetail(HashMap<String, Integer> parmas) ;
	
	public File boardFileDownload(HashMap<String,Integer> params) ;

	public int getMaxBoardId();
	
	public int getMaxCookieId();
	
	public List<Board> articleList(HashMap<String, Integer> params);
	
	/***
	 * root_board_id 는 무시됩니다. 
	 * @param nodePtr board_id 와 version만 사용하여 가져옵니다.
	 * @return
	 */
	public List<Board> getBoardList(NodePtr nodePtr);

	public List<Board> autoList(HashMap<String, Integer> params);
	
	public int articleGetCount();
	
	public int getFileCount(int file_id);
	
}
