package com.worksmobile.Assignment.Controller;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.List;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.worksmobile.Assignment.AssignmentApplication;
import com.worksmobile.Assignment.Domain.BoardDTO;
import com.worksmobile.Assignment.Mapper.BoardMapper;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringBootTest(classes = AssignmentApplication.class)
@AutoConfigureMockMvc
public class RestControllerTest {
	private final Logger logger = LoggerFactory.getLogger(RestControllerTest.class);

	@Autowired
	private MockMvc mockMvc;
	
	@Autowired
	private BoardMapper articleMapper;
	
	private static final String DEFAULT_TEST_ID = "test";
    private static BoardDTO DEFUALT_article_VO;
    
    public RestControllerTest() throws JsonProcessingException {
    	DEFUALT_article_VO = new BoardDTO();
    	DEFUALT_article_VO.setSubject("testing create subject");
    	DEFUALT_article_VO.setContent("testing content");
    }

    private String jsonStringFromObject(Object object) throws JsonProcessingException {
        ObjectMapper mapper = new ObjectMapper();
        return mapper.writeValueAsString(object);
    }
	
	@Test
	public void testGetBoardList() throws Exception {
		List<BoardDTO> article = articleMapper.boardList();
		String jsonString = this.jsonStringFromObject(article);
		
		MvcResult result = mockMvc.perform(
								MockMvcRequestBuilders.get("/api/articles").accept(MediaType.APPLICATION_JSON))
				.andExpect(status().isOk())
				.andExpect(content().string(equalTo(jsonString)))
				.andReturn();
		
		logger.info(result.getResponse().getContentAsString());
	}
	
	@Test
	public void testGetArticle() throws Exception{
		BoardDTO article = articleMapper.getArticle(0);
		String jsonString = this.jsonStringFromObject(article);
		
		MvcResult result = mockMvc.perform(
								MockMvcRequestBuilders.get("/api/articles/{id}", DEFAULT_TEST_ID)
								.accept(MediaType.APPLICATION_JSON))
				.andExpect(status().isOk())
				.andExpect(content().string(equalTo(jsonString)))
				.andReturn();
		
		logger.info(result.getResponse().getContentAsString());
	}
	
	@Test
	public void testCreate() throws Exception{
		BoardDTO newArticle = new BoardDTO();
		newArticle.setSubject("testing subject");
		newArticle.setContent("testing content");
		
		String jsonString = this.jsonStringFromObject(newArticle);
		
		MvcResult result = mockMvc.perform(
									MockMvcRequestBuilders.post("/api/articles")
									.contentType(MediaType.APPLICATION_JSON)
									.content(jsonString))
				.andExpect(status().isOk())
				.andExpect(content().string(equalTo(jsonString)))
				.andReturn();
		
		logger.info(result.getResponse().getContentAsString());
	}
	
	// 부분 업데이트
	@Test
	public void testPatch() throws Exception{
		BoardDTO article = articleMapper.getArticle(0);
		if(article == null)
		{
			article = new BoardDTO();
			article.setId(0);
			article.setSubject("origin subject");
			article.setContent("origin contnet");
			articleMapper.boardCreate(article);
		}
		
		BoardDTO newArticle = new BoardDTO();
		newArticle.setId(0);
		newArticle.setSubject("updated subject");
		newArticle.setSubject("updated content");
		
		articleMapper.boardUpdate(article);
		String jsonString = this.jsonStringFromObject(newArticle);
		
		MvcResult result = mockMvc.perform(
									MockMvcRequestBuilders.patch("/api/articles/{id}", DEFAULT_TEST_ID)
									.contentType(MediaType.APPLICATION_JSON)
									.content(jsonString))
				.andExpect(status().isOk())
				.andExpect(content().string(equalTo(jsonString)))
				.andReturn();
		
		logger.info(result.getResponse().getContentAsString());
	}
	
	// TODO : 전체 리소스 변경때 사용 한다는데 patch와 차이점을 잘 모르겠음.
	@Test
	public void testUpdate() throws Exception{
		BoardDTO article = articleMapper.getArticle(0);
		if(article == null)
		{
			article = new BoardDTO();
			article.setId(0);
			article.setSubject("origin subject");
			article.setContent("origin contnet");
			articleMapper.boardCreate(article);
		}
		
		BoardDTO newArticle = new BoardDTO();
		newArticle.setId(0);
		newArticle.setSubject("updated subject");
		newArticle.setSubject("updated content");
		String jsonString = this.jsonStringFromObject(newArticle);
		
		MvcResult result = mockMvc.perform(
									MockMvcRequestBuilders.put("/api/articles/{id}", DEFAULT_TEST_ID)
									.contentType(MediaType.APPLICATION_JSON)
									.content(jsonString))
				.andExpect(status().isOk())
				.andExpect(content().string(equalTo(jsonString)))
				.andReturn();
		
		logger.info(result.getRequest().getContentAsString());
	}
}
