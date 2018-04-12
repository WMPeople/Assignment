package com.worksmobile.assignment.mapper;

import static org.junit.Assert.assertNotNull;

import java.sql.Connection;
import java.sql.SQLException;

import javax.sql.DataSource;

import org.apache.ibatis.session.SqlSessionFactory;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.web.WebAppConfiguration;

import com.worksmobile.assignment.AssignmentApplication;


@RunWith(SpringJUnit4ClassRunner.class)
@SpringBootTest(classes = AssignmentApplication.class)
@WebAppConfiguration
public class DBConnectionTest {

	@Autowired
	private SqlSessionFactory sqlSession;
	
	@Autowired
    private DataSource ds;
 
    @Test
    public void contextLoads() {
    }
 
    @Test
    public void testConnection() throws SQLException {
        System.out.println("ds : "+ds);
        
        Connection con = ds.getConnection();
        
        System.out.println("con : "+con);
        
        con.close();
    }
    
    @Test
    public void testSqlSession() {
    	System.out.println("sqlSession : " +sqlSession);
    	assertNotNull(sqlSession);
    }
}