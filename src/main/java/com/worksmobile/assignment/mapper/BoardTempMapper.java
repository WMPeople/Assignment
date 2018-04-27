package com.worksmobile.assignment.mapper;

import java.util.HashMap;
import java.util.List;

import org.apache.ibatis.annotations.Mapper;


import com.worksmobile.assignment.model.BoardTemp;
import com.worksmobile.assignment.model.File;
import com.worksmobile.assignment.model.NodePtr;

/***
 * @author KHH
 * @author RWS
 */
@Mapper
public interface BoardTempMapper {
	// TODO 내용수정
	public int createBoardTemp(BoardTemp boardTemp);

	/**
	 * 관련된 자동 저장 게시글도 같이 삭제됩니다.
	 * @param parmas HashMap(board_id, version)
	 * @return
	 */
	public int deleteBoardTemp(HashMap<String, Object> parmas);
	
	public int deleteBoardTempWithoutCookieId(HashMap<String, Object> parmas);
	
	public int boardTempUpdateWithoutFile(BoardTemp boardTemp);

	public int boardTempUpdate(BoardTemp boardTemp);

	public BoardTemp viewDetail(HashMap<String, Object> parmas);

	public File boardTempFileDownload(HashMap<String, Integer> params);


	/**
	 * @param nodePtr board_id 와 version만 사용하여 가져옵니다.
	 * @return
	 */
	public List<BoardTemp> getBoardTempList(NodePtr nodePtr);

	public List<BoardTemp> autoList(HashMap<String, Integer> params);


	public int autoGetCount(HashMap<String, Object> params);


}
