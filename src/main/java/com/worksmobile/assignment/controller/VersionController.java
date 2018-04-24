package com.worksmobile.assignment.controller;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.ModelAndView;

import com.worksmobile.assignment.bo.Compress;
import com.worksmobile.assignment.bo.VersionManagementService;
import com.worksmobile.assignment.mapper.BoardHistoryMapper;
import com.worksmobile.assignment.mapper.FileMapper;
import com.worksmobile.assignment.model.Board;
import com.worksmobile.assignment.model.BoardHistory;
import com.worksmobile.assignment.model.File;
import com.worksmobile.assignment.model.NodePtr;

/***
 * 버전관리를 담당하는 컨트롤러입니다.
 * @author khh, rws
 *
 */
@RestController
public class VersionController {

	@Autowired
	private BoardHistoryMapper boardHistoryMapper;

	@Autowired
	private FileMapper fileMapper;

	@Autowired
	private VersionManagementService versionManagementService;
	
	/***
	 * 버전 삭제시 호출 되는 메쏘드 입니다.
	 * @param board_id 버전 삭제를 원하는 이력의 board_id
	 * @param version 버전 삭제를 원하는 이력의 version
	 * @return 성공 했는지 실패 했는지를 알려주는 Map을 리턴합니다.
	 */
	@RequestMapping(value = "/boards/version/{board_id}/{version}", method = RequestMethod.DELETE)
	public Map<String, Object> versionDestory(@PathVariable(value = "board_id") int board_id,
		@PathVariable(value = "version") int version) {

		Map<String, Object> resultMap = new HashMap<String, Object>();
		try {
			NodePtr deletePtr = new NodePtr(board_id, version);

			versionManagementService.deleteVersion(deletePtr);

			resultMap.put("result", "success");
		} catch (Exception e) {
			e.printStackTrace();
			resultMap.put("result", e.getMessage());
			return resultMap;
		}

		return resultMap;
	}

	/***
	 * 버전관리 페이지 이동시 호출되는 메쏘드입니다.
	 * @param board_id 버전관리를 원하는 LeafNode의 board_id
	 * @param version 버전관리를 원하는 LeafNode의 version
	 * @return modelAndView LeafNode의 이력 List를 프론트에 전송합니다.
	 * @throws Exception
	 */
	@RequestMapping(value = "/boards/management/{board_id}/{version}", method = RequestMethod.GET)
	public ModelAndView versionManagement(@PathVariable(value = "board_id") int board_id,
		@PathVariable(value = "version") int version) throws Exception {

		NodePtr leapPtr = new NodePtr(board_id, version);
		List<BoardHistory> boardHistory = versionManagementService.getRelatedHistory(leapPtr);

		ModelAndView modelAndView = new ModelAndView();
		modelAndView.addObject("list", boardHistory);
		modelAndView.setViewName("versionManagement");
		return modelAndView;
	}

	/***
	 * 버전관리 페이지에서 버전 복구 버튼을 눌렀을 때 호출되는 메쏘드 입니다.
	 * @param board_id 복원을 원하는 버전 board_id
	 * @param version 복원을 원하는 버전 version
	 * @param leafBoard_id 복원을 원하는 버전의 LeafNode의 board_id
	 * @param leafVersion  복원을 원하는 버전의 LeafNode의 version
	 * @return resultMap 버전 복구 후 url 주소와 메쏘드 실행 성공 유무를 알려주는 Map을 리턴합니다.
	 * @throws Exception
	 */
	@RequestMapping(value = "/boards/recover/{board_id}/{version}/{leafBoard_id}/{leafVersion}", method = RequestMethod.GET)
	public Map<String, Object> versionRecover(@PathVariable(value = "board_id") int board_id,
		@PathVariable(value = "version") int version,
		@PathVariable(value = "leafBoard_id") int leafBoard_id,
		@PathVariable(value = "leafVersion") int leafVersion) throws Exception {

		Map<String, Object> resultMap = new HashMap<String, Object>();
		NodePtr newLeapNode = null;
		try {
			NodePtr recoverPtr = new NodePtr(board_id, version);
			NodePtr leapNodePtr = new NodePtr();

			leapNodePtr.setBoard_id(leafBoard_id);
			leapNodePtr.setVersion(leafVersion);

			newLeapNode = versionManagementService.recoverVersion(recoverPtr, leapNodePtr);
			resultMap.put("result", "success");
		} catch (Exception e) {
			resultMap.put("result", e.getMessage());
			return resultMap;
		}
		resultMap.put("board_id", newLeapNode.getBoard_id());
		resultMap.put("version", newLeapNode.getVersion());
		return resultMap;
	}

	/***
	 * 이력 상세보기 입니다.
	 * @param board_id 상세 조회 할 게시물의 board_id
	 * @param version 상세 조회 할 게시물의 version
	 * @return modelAndView 이력의 상세 내용, board-boardHistory 구분자 , file 데이터 , viewName을 리턴합니다.
	 */
	@RequestMapping(value = "/history/{board_id}/{version}", method = RequestMethod.GET)
	public ModelAndView history(@PathVariable(value = "board_id") int board_id,
		@PathVariable(value = "version") int version) {

		NodePtr node = new NodePtr(board_id, version);

		BoardHistory boardHistory = boardHistoryMapper.selectHistory(node);
		Board board = new Board(boardHistory);
		File file = fileMapper.getFile(board.getFile_id());

		String deCompreesedContent = "";
		try {
			deCompreesedContent = Compress.deCompress(boardHistory.getHistory_content());
		} catch (IOException e) {
			e.printStackTrace();
			deCompreesedContent = "압축 해제 실패";
			throw new RuntimeException(e);
		}
		board.setContent(deCompreesedContent);

		ModelAndView modelAndView = new ModelAndView();
		modelAndView.addObject("board", board);
		modelAndView.addObject("isHistory", 1);
		modelAndView.addObject("file", file);
		modelAndView.setViewName("boardDetail");

		return modelAndView;

	}

	/***
	 * 두 버전의 content를 비교할 때 호출되는 메쏘드 입니다.
	 * @param board_id1 첫번째 버전의 board_id
	 * @param version1 첫번째 버전의 version
	 * @param board_id2 두번째 버전의 board_id
	 * @param version2 두번째 버전의 version
	 * @return modelAndView 객체로 viewName과, content를 프론트에 전송합니다.
	 * @throws Exception
	 */
	@RequestMapping(value = "/boards/diff", method = RequestMethod.GET)
	public ModelAndView diff(@RequestParam int board_id1,
		@RequestParam int version1,
		@RequestParam int board_id2,
		@RequestParam int version2) throws Exception {
		
		if (board_id1 == 0 || board_id2 == 0 ||
			version1 == 0 || version2 == 0) {
			throw new RuntimeException("잘못된 게시물 id입니다.");
		}

		NodePtr left = new NodePtr(board_id2, version2);
		NodePtr right = new NodePtr(board_id1, version1);

		String leftContent = Compress.deCompress(boardHistoryMapper.selectHistory(left).getHistory_content());
		String rightContent = Compress.deCompress(boardHistoryMapper.selectHistory(right).getHistory_content());

		//압출 해결 후 리턴 , 맵으로 리턴
		ModelAndView modelAndView = new ModelAndView();
		modelAndView.addObject("left", left);
		modelAndView.addObject("right", right);
		modelAndView.addObject("leftContent", leftContent);
		modelAndView.addObject("rightContent", rightContent);
		modelAndView.setViewName("diff");

		return modelAndView;

	}

}
