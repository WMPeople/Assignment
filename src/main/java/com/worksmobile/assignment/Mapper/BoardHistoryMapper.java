package com.worksmobile.assignment.Mapper;

import java.util.List;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import com.worksmobile.assignment.Model.BoardHistory;
import com.worksmobile.assignment.Model.NodePtr;

@Mapper
public interface BoardHistoryMapper {

	public List<BoardHistory> getHistoryByRootBoardId(@Param("root_board_id")int root_board_id);
	
	public BoardHistory getHistory(NodePtr nodePtr);
			
	public int deleteHistory(NodePtr nodePtr);
	
	public int createHistory(BoardHistory boardHistory);

	public int updateHistoryParentAndRoot(BoardHistory boardHistory);

	public List<BoardHistory> getChildren(NodePtr nodePtr);
	
	public int getFileCount(int file_id);
}
