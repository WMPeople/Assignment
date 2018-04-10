//package com.worksmobile.assignment.Controller;
//
//import static org.junit.Assert.assertEquals;
//import static org.junit.Assert.assertNotEquals;
//import static org.junit.Assert.assertNotNull;
//import static org.junit.Assert.assertNull;
//
//import java.io.IOException;
//import java.net.URLEncoder;
//import java.util.ArrayList;
//import java.util.HashMap;
//import java.util.Iterator;
//import java.util.List;
//import java.util.Map;
//
//import javax.servlet.http.HttpServletRequest;
//import javax.servlet.http.HttpServletResponse;
//
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.boot.test.context.SpringBootTest;
//import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
//import org.springframework.test.context.web.WebAppConfiguration;
//import org.springframework.transaction.annotation.Transactional;
//import org.springframework.web.bind.annotation.PathVariable;
//import org.springframework.web.bind.annotation.RequestBody;
//import org.springframework.web.bind.annotation.RequestMapping;
//import org.springframework.web.bind.annotation.RequestMethod;
//import org.springframework.web.bind.annotation.ResponseBody;
//import org.springframework.web.multipart.MultipartFile;
//import org.springframework.web.multipart.MultipartHttpServletRequest;
//import org.springframework.web.servlet.ModelAndView;
//
//import com.worksmobile.assignment.assignmentApplication;
//import com.worksmobile.assignment.Domain.Board;
//import com.worksmobile.assignment.Domain.BoardHistory;
//import com.worksmobile.assignment.Domain.File;
//import com.worksmobile.assignment.Domain.NodePtr;
//import com.worksmobile.assignment.Mapper.BoardHistoryMapper;
//import com.worksmobile.assignment.Mapper.BoardMapper;
//import com.worksmobile.assignment.Mapper.FileMapper;
//import com.worksmobile.assignment.Service.Compress;
//import com.worksmobile.assignment.Service.Paging;
//import com.worksmobile.assignment.Service.VersionManagementService;
//import com.worksmobile.assignment.util.Utils;
//
//import org.junit.Test;
//import org.junit.runner.RunWith;
//
//@RunWith(SpringJUnit4ClassRunner.class)
//@SpringBootTest(classes = assignmentApplication.class)
//@WebAppConfiguration
//@Transactional
//public class RestControllerTest {
//	
//	@Autowired
//    private BoardMapper boardMapper;
//    
//    @Autowired
//	private VersionManagementService versionManagementService;
//    
//    @Autowired
//    private BoardHistoryMapper boardHistoryMapper;
//    
//    @Autowired
//    private FileMapper fileMapper;
//    
//    public static final int defaultBoardId = 1;
//	private static BoardHistory defaultHistory;
//	private static BoardHistory defaultHistory2;
//	private static File defaultFile;
//	private static NodePtr defaultNodePtr;
//	private static NodePtr defaultNodePtr2;
//
//	public RestControllerTest() {
//		defaultNodePtr = new NodePtr(1, 6);
//		defaultHistory = new BoardHistory();
//		defaultHistory.setBoard_id(1);
//		defaultHistory.setVersion(6);
//		defaultHistory.setFile_id(1000);
//		defaultHistory.setStatus("Created");
//		defaultHistory.setHistory_subject("sub");
//		
//		defaultNodePtr2 = new NodePtr(1, 7);
//		defaultHistory2 = new BoardHistory();
//		defaultHistory2.setBoard_id(1);
//		defaultHistory2.setVersion(7);
//		defaultHistory2.setFile_id(1000);
//		defaultHistory2.setStatus("Created");
//		defaultHistory2.setHistory_subject("sub");
//		
//		defaultFile = new File();
//		defaultFile.setFile_id(1000);
//		defaultFile.setFile_name("test");
//		defaultFile.setFile_data(null);
//		defaultFile.setFile_size(0);
//	}
//	
//	@Test
//	public void testVersionDestory() {
//
//		boardHistoryMapper.createHistory(defaultHistory);
//		boardHistoryMapper.createHistory(defaultHistory2);
//		fileMapper.createFile(defaultFile);
//		
//		int file_id = defaultHistory.getFile_id();
//		int fileCount=0;
//		
//		if(file_id !=0) {
//			fileCount = boardHistoryMapper.getFileCount(file_id);
//			assertEquals(2, fileCount);
//		}
//		
//		if(fileCount==1) {
//			int deletedCnt = fileMapper.deleteFile(file_id);
//			if(deletedCnt != 1) {
//				throw new RuntimeException("파일 삭제 에러");
//			}
//			
//		}
//		versionManagementService.deleteVersion(defaultNodePtr);
//		
//		assertEquals(1,boardHistoryMapper.getFileCount(file_id));
//		assertNotNull(fileMapper.getFile(file_id));
//		
//		int file_id2 = defaultHistory2.getFile_id();
//		int fileCount2=0;
//		
//		if(file_id2 !=0) {
//			fileCount2 = boardHistoryMapper.getFileCount(file_id2);
//			assertEquals(1, fileCount2);
//		}
//		
//		if(fileCount==1) {
//			int deletedCnt = fileMapper.deleteFile(file_id2);
//			if(deletedCnt != 1) {
//				throw new RuntimeException("파일 삭제 에러");
//			}
//			
//		}
//		versionManagementService.deleteVersion(defaultNodePtr2);
//		
//		assertEquals(0,boardHistoryMapper.getFileCount(file_id));
//		assertEquals(null, fileMapper.getFile(file_id));
//		
//		
//		
//		
//	}
//    
//
//}
