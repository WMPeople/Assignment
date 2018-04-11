package com.worksmobile.assignment.BO;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.worksmobile.assignment.Mapper.BoardHistoryMapper;
import com.worksmobile.assignment.Mapper.BoardMapper;
import com.worksmobile.assignment.Model.Board;
import com.worksmobile.assignment.Model.BoardHistory;
import com.worksmobile.assignment.Model.NodePtr;


@Service
class BoardService{
	@Autowired
	BoardMapper boardMapper;
	
	@Autowired
	BoardHistoryMapper boardHistoryMapper;
	
	@Autowired
	FileService fileService;
	
	public Set<Integer> deleteBoardAndReturnfileIdSet(NodePtr leafPtr) {
		List<Board> boardList = boardMapper.getBoardList(leafPtr);
		Set<Integer> fileIdSet = new HashSet<>();
		for(int i=0;i<boardList.size();i++) {
			fileIdSet.add(boardList.get(i).getFile_id());
		}
		boardMapper.boardDelete(leafPtr.toMap());
		return fileIdSet;
	}
	
	public int deleteBoardHistoryAndReturnfileId(NodePtr leafPtr) {
		BoardHistory boardHistory = boardHistoryMapper.getHistory(leafPtr);

		boardHistoryMapper.deleteHistory(leafPtr);
		
		return boardHistory.getFile_id();
	}
	
	
}