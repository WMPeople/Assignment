package com.worksmobile.assignment.bo;

import java.util.HashMap;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.worksmobile.assignment.bo.event.AttachmentChangedEvent;
import com.worksmobile.assignment.mapper.BoardMapper;
import com.worksmobile.assignment.mapper.BoardTempMapper;
import com.worksmobile.assignment.model.Board;
import com.worksmobile.assignment.model.BoardTemp;
import com.worksmobile.assignment.util.JsonUtils;

@Service
public class BoardTempService {
	@Autowired
	private BoardMapper boardMapper;

	@Autowired
	private BoardTempMapper boardTempMapper;

	@Autowired
	private ApplicationEventPublisher publisher;

	// TODO : 함수명이 임시 게시글을 만든다 이지만, 만들고 있지 않습니다.
	// TODO : 매개 변수명 type의 의미를 파악하기 힘듭니다. 또한 type이 "withfile"일 경우만 분기를 나뉘는데, 그러한 목적이면 String일 필요가 없을것 같습니다.
	@Transactional
	public BoardTemp createTempArticleOverwrite(BoardTemp tempArticle, String type) {
		//점검
		//tempArticle.setRoot_board_id(tempArticle.getBoard_id()); // getHistoryByRootId에서 검색이 가능하도록

		BoardTemp dbTempArticle = boardTempMapper.viewDetail(tempArticle.toMap());
		if (dbTempArticle != null) {
			if (("withfile").equals(type)) {
				int articleUpdatedCnt = boardTempMapper.boardTempUpdate(tempArticle);
				if (articleUpdatedCnt != 1) {
					String json = JsonUtils.jsonStringIfExceptionToString(tempArticle);
					throw new RuntimeException("createTempArticleOverwrite메소드에서 임시 게시글 수정 에러 tempArticle : " + json + "\n" + "articleUpdatedCnt : " + articleUpdatedCnt);
				}
				publisher.publishEvent(new AttachmentChangedEvent(dbTempArticle.getFile_id()));
			} else {
				int articleUpdatedCnt = boardTempMapper.boardTempUpdateWithoutFile(tempArticle);
				if (articleUpdatedCnt != 1) {
					String json = JsonUtils.jsonStringIfExceptionToString(tempArticle);
					throw new RuntimeException("createTempArticleOverwrite메소드에서 임시 게시글 수정 에러 tempArticle : " + json + "\n" + "articleUpdatedCnt : " + articleUpdatedCnt);
				}
			}
		} else {
			copyBoardAndCreateTempBoard(tempArticle);
		}
		return tempArticle;
	}

	/***
	 * 자동저장 -> 버전업 되었을 때 버전업 된 LeafNode를 복사하여 새로운 자동저장 용 tempBoard를 만들어줍니다.
	 * @param tempArticle
	 */
	public void copyBoardAndCreateTempBoard(BoardTemp tempArticle) {
		Board board = boardMapper.viewDetail(tempArticle.toBoardKeyMap());
		if (tempArticle.getVersion().equals(board.getVersion()) ) {
			int insertedRowCnt = boardTempMapper.createBoardTemp(tempArticle);
			if (insertedRowCnt != 1) {
				throw new RuntimeException("createTempArticleOverwrite메소드에서 createBoard error");
			}
		} else {
			
			if (board == null || board.getBoard_id() == 0) {
				throw new RuntimeException("board 정보를 가져올 수 없습니다.");
			}
			BoardTemp newBoardTemp = new BoardTemp();
			newBoardTemp = tempArticle;
			newBoardTemp.setBoard_id(board.getBoard_id());
			newBoardTemp.setVersion(board.getVersion());
			newBoardTemp.setFile_id(board.getFile_id());
			int insertedRowCnt = boardTempMapper.createBoardTemp(newBoardTemp);
			if (insertedRowCnt != 1) {
				throw new RuntimeException("createTempArticleOverwrite메소드에서 createBoard error");
			}
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
		BoardTemp boardTemp = new BoardTemp(subject, content, created_time, file_id, cookie_id);
		boardTemp.setBoard_id(board_id);
		boardTemp.setVersion(version);

		if (boardTempMapper.viewDetail(boardTemp.toMap()) == null) {
			boardTempMapper.createBoardTemp(boardTemp);
		}

	}

	/***
	 * 하나의 임시게시글을 삭제.
	 * @param deleteParams
	 * @return 
	 */
	public boolean deleteBoardTemp(HashMap<String, Object> deleteParams) {
		BoardTemp dbBoardTemp = boardTempMapper.viewDetail(deleteParams);
		if (dbBoardTemp == null) {
			return false;
		}

		int deletedCnt = boardTempMapper.deleteBoardTemp(deleteParams);
		if (deletedCnt == 0) {
			return false;
		}
		publisher.publishEvent(new AttachmentChangedEvent(dbBoardTemp.getFile_id()));
		
		return true;

	}
}
