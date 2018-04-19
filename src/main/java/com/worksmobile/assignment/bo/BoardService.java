package com.worksmobile.assignment.bo;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.worksmobile.assignment.mapper.BoardHistoryMapper;
import com.worksmobile.assignment.mapper.BoardMapper;
import com.worksmobile.assignment.model.Board;
import com.worksmobile.assignment.model.BoardHistory;
import com.worksmobile.assignment.model.NodePtr;
import com.worksmobile.assignment.util.JsonUtils;

/***
 * Board, BoardHistory 삭제할 경우 이 서비스를 통해 삭제 됩니다.
 * 현재는 DAO 같은 역할을 하고 있습니다.
 * @author khh, rws
 *
 */
@Service
public class BoardService {
	@Autowired
	BoardMapper boardMapper;

	@Autowired
	BoardHistoryMapper boardHistoryMapper;

	@Autowired
	FileService fileService;

	/***
	 * 리프 게시글과 관련된 자동 저장 게시글을 함께 삭제합니다.
	 * @param deleteNodePtr 삭제할 노드의 포인터
	 * @return 삭제된 개수
	 */
	public int deleteBoardAndAutoSave(NodePtr deleteNodePtr) {
		List<Board> boardList = boardMapper.getBoardList(deleteNodePtr);
		if (boardList.size() == 0) {
			return 0;
		}
		Set<Integer> fileIdSet = new HashSet<>();
		for (Board ele : boardList) {
			fileIdSet.add(ele.getFile_id());
		}
		int deletedCnt = boardMapper.deleteBoardAndAutoSave(deleteNodePtr.toMap());

		fileService.deleteNoMoreUsingFile(fileIdSet);

		return deletedCnt;
	}

	// TODO : deleteNoMoreUsingFile메소드가 Set을 매개변수로 취하고 있으나, 한개 만을 넘기는 경우가 있어
	// 이를 개선할 방안이 있으면 좋을것 같습니다.
	/***
	 * 만약, 존재하지 않는 게시글이라면 삭제 되지 않습니다.
	 * @param deleteParams 삭제할 board의 board_id, version, cookie_id를 사용합니다.
	 * @return 삭제 되었는지 여부
	 */
	public boolean deleteBoardWithCookieId(HashMap<String, Object> deleteParams) {
		Board dbBoard = boardMapper.viewDetail(deleteParams);
		if (dbBoard == null) {
			return false;
		}
		Set<Integer> fileIdSet = new HashSet<>();
		fileIdSet.add(dbBoard.getFile_id());

		int deletedCnt = boardMapper.deleteBoardWithCookieId(deleteParams);
		if (deletedCnt != 1) {
			String json = JsonUtils.jsonStringIfExceptionToString(deleteParams);
			throw new RuntimeException("Board 테이블의 게시글 삭제 중 오류가 발생했습니다. (특정 쿠키 값을 가진 Board삭제) \n" +
				"삭제된 개수 : " + deletedCnt + " deleteParams : " + json);
		}
		fileService.deleteNoMoreUsingFile(fileIdSet);

		return true;
	}

	public void deleteBoardHistory(NodePtr deleteNodePtr) {
		BoardHistory history = boardHistoryMapper.selectHistory(deleteNodePtr);
		Set<Integer> fileIdSet = new HashSet<>(1);
		fileIdSet.add(history.getFile_id());

		int deletedCnt = boardHistoryMapper.deleteHistory(deleteNodePtr);
		if (deletedCnt != 1) {
			String json = JsonUtils.jsonStringIfExceptionToString(deleteNodePtr);
			throw new RuntimeException("이력 삭제 실패  삭제 개수 : " + deletedCnt + " deletePtr : " + json);
		}

		fileService.deleteNoMoreUsingFile(fileIdSet);
	}

	public void deleteBoardHistoryAndAttachment(NodePtr nodePtr) {
		List<Board> boardList = boardMapper.getBoardList(nodePtr);
		Set<Integer> fileIdSet = new HashSet<>();
		for (int i = 0; i < boardList.size(); i++) {
			fileIdSet.add(boardList.get(i).getFile_id());
		}
		boardMapper.deleteBoardAndAutoSave(nodePtr.toMap());

		fileService.deleteNoMoreUsingFile(fileIdSet);
	}

	public Set<Integer> deleteBoardAndReturnfileIdSet(NodePtr leafPtr) {
		List<Board> boardList = boardMapper.getBoardList(leafPtr);
		Set<Integer> fileIdSet = new HashSet<>();
		for (int i = 0; i < boardList.size(); i++) {
			fileIdSet.add(boardList.get(i).getFile_id());
		}
		boardMapper.deleteBoardAndAutoSave(leafPtr.toMap());
		return fileIdSet;
	}

	public int deleteBoardHistoryAndReturnfileId(NodePtr leafPtr) {
		BoardHistory boardHistory = boardHistoryMapper.selectHistory(leafPtr);

		boardHistoryMapper.deleteHistory(leafPtr);

		return boardHistory.getFile_id();
	}

	Map<Map.Entry<Integer, Integer>, BoardHistory> getHistoryMap(int root_board_id) {
		List<BoardHistory> historyList = boardHistoryMapper.selectHistoryByRootBoardId(root_board_id);
		Map<Map.Entry<Integer, Integer>, BoardHistory> historyMap = new HashMap<>();
		for (BoardHistory ele : historyList) {
			historyMap.put(ele.toBoardIdAndVersionEntry(), ele);
		}
		return historyMap;
	}

	public boolean isLeaf(final NodePtr nodePtr) {
		Board board = boardMapper.viewDetail(nodePtr.toMap());

		if (board != null) {
			return true;
		} else {
			return false;
		}
	}

	public void createTempBoard(Board tempArticle) {
		int createdCnt = boardMapper.boardCreate(tempArticle);
		if (createdCnt != 1) {
			String json = JsonUtils.jsonStringIfExceptionToString(tempArticle);
			throw new RuntimeException("createTempArticle에서 게시글 생성 실패 : " + json);
		}
	}

	/***
	 * LeafNode 게시글을 복제하여 자신의 쿠키 ID 값을 넣어 자동 저장 게시글을 생성합니다.
	 * @param board_id
	 * @param version
	 * @param cookie_id
	 * @param created_time
	 * @param content
	 * @param file_id
	 * @param subject
	 */
	public void makeTempBoard(int board_id, int version, String cookie_id, String created_time, String content,
		int file_id, String subject) {
		Board updateBoard = new Board(subject, content, created_time, file_id, cookie_id);
		updateBoard.setBoard_id(board_id);
		updateBoard.setVersion(version);

		HashMap<String, Object> params = new HashMap<String, Object>();
		params.put("board_id", board_id);
		params.put("version", version);
		params.put("cookie_id", cookie_id);
		Board board = boardMapper.viewDetail(params);
		if (board == null) {
			boardMapper.boardCreate(updateBoard);
		}

	}

	/***
	 * 자동저장 -> 버전업 되었을 때 버전업 된 LeafNode를 복사하여 새로운 자동저장 용 tempBoard를 만들어줍니다.
	 * @param tempArticle
	 */
	public void copyBoardAndCreateTempBoard(Board tempArticle) {
		String cookie_id = tempArticle.getCookie_id();
		Board board = tempArticle;
		board.setCookie_id(Board.LEAF_NODE_COOKIE_ID);
		Board updatedBoard = boardMapper.viewDetail(board.toMap());
		updatedBoard.setCookie_id(cookie_id);
		int insertedRowCnt = boardMapper.boardCreate(updatedBoard);
		if (insertedRowCnt != 1) {
			throw new RuntimeException("createTempArticleOverwrite메소드에서 boardCreate error");
		}
	}

}