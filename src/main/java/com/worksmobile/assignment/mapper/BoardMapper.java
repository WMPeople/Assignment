package com.worksmobile.assignment.mapper;

import java.util.HashMap;
import java.util.List;

import org.apache.ibatis.annotations.Mapper;

import com.worksmobile.assignment.model.Board;
import com.worksmobile.assignment.model.File;
import com.worksmobile.assignment.model.NodePtr;

/***
 * @author KHH
 * @author RWS
 */
@Mapper
public interface BoardMapper {

	public int boardCreate(Board board);

	/**
	 * 관련된 자동 저장 게시글도 같이 삭제됩니다.
	 * @param parmas HashMap(board_id, version)
	 * @return
	 */
	public int deleteBoardAndAutoSave(HashMap<String, Object> parmas);

	public int deleteBoard(HashMap<String, Object> params);

	public int boardUpdateWithoutFile(Board board);

	public int boardUpdate(Board board);

	public Board viewDetail(HashMap<String, Object> parmas);

	public File boardFileDownload(HashMap<String, Integer> params);

	public int getMaxCookieId();

	/**
	 * 
	 * @param params #{offset}, #{noOfRecords} 을 매개변수로 받습니다.
	 * @return
	 */
	public List<Board> articleList(HashMap<String, Integer> params);

	/***
	 * root_board_id 는 무시됩니다. 
	 * @param nodePtr board_id 와 version만 사용하여 가져옵니다.
	 * @return
	 */
	public List<Board> getBoardList(NodePtr nodePtr);

	public List<Board> autoList(HashMap<String, Integer> params);

	public int articleGetCount();

	public int autoGetCount(HashMap<String, Object> params);

	public int getFileCount(int file_id);

}
