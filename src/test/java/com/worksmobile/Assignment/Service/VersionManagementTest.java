package com.worksmobile.Assignment.Service;

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import com.worksmobile.Assignment.Domain.BoardDTO;

@RunWith(SpringRunner.class)
@SpringBootTest
public class VersionManagementTest {

	VersionManagementService versionManagementService;
	@Before
	public void canCreate() {
		versionManagementService = new VersionManagementService();
	}
	
	@Test
    @Ignore("미완성.")
	public void createArticleTest() {
		BoardDTO article = new BoardDTO();
		versionManagementService.createArticle(article);
		
	}
	
}
