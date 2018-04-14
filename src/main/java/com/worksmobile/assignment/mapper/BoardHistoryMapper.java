package com.worksmobile.assignment.mapper;

import java.util.List;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import com.worksmobile.assignment.model.BoardHistory;
import com.worksmobile.assignment.model.NodePtr;

@Mapper
public interface BoardHistoryMapper {

	public List<BoardHistory> selectHistoryByRootBoardId(@Param("root_board_id")int root_board_id);
	
	public BoardHistory selectHistory(NodePtr nodePtr);
	
	/**
	 * board_history의 모든 것을 반환합니다. 디버깅 용도입니다.
	 * @return
	 */
	public List<BoardHistory> selectAllHistory();
			
	public int deleteHistory(NodePtr nodePtr);
	
	public int createHistory(BoardHistory boardHistory);

	public int updateHistoryParentAndRoot(BoardHistory boardHistory);

	public List<BoardHistory> selectChildren(NodePtr nodePtr);
	
	public int selectFileCount(int file_id);
}
