package com.worksmobile.Assignment;

import javax.sql.DataSource;

import org.apache.catalina.connector.Connector;
import org.apache.ibatis.session.SqlSessionFactory;
import org.mybatis.spring.SqlSessionFactoryBean;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.web.embedded.tomcat.TomcatServletWebServerFactory;
import org.springframework.boot.web.servlet.server.ServletWebServerFactory;
import org.springframework.boot.web.servlet.support.SpringBootServletInitializer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;

import com.worksmobile.Assignment.Controller.BoardController;

@SpringBootApplication // @SpringBootApplication 애노테이션은 @Configuration, @EnableAutoConfiguration 과@ComponentScan의 기본속성을 사용한 것과 동일하다:
public class AssignmentApplication extends SpringBootServletInitializer {

	public static void main(String[] args) {
		SpringApplication.run(AssignmentApplication.class, args);
	}

	@Value("${tomcat.ajp.protocol}")
	String ajpProtocol;

	@Value("${tomcat.ajp.port}")
	int ajpPort;

	@Value("${tomcat.ajp.enabled}")
	boolean tomcatAjpEnabled;

	@Bean
	public SqlSessionFactory sqlSessionFactory(DataSource dataSource) throws Exception {
		SqlSessionFactoryBean sessionFactory = new SqlSessionFactoryBean();
		sessionFactory.setDataSource(dataSource);
		Resource[] res = new PathMatchingResourcePatternResolver().getResources("classpath:*/Mapper/*Mapper.xml");
		sessionFactory.setMapperLocations(res);
		return sessionFactory.getObject();
	}

	@Bean
	public ServletWebServerFactory servletContainer() {
		TomcatServletWebServerFactory tomcat = new TomcatServletWebServerFactory();
		if (tomcatAjpEnabled) {
			Connector ajpConnector = new Connector(ajpProtocol);
			ajpConnector.setProtocol(ajpProtocol);
			ajpConnector.setPort(ajpPort);
			ajpConnector.setSecure(false);
			ajpConnector.setAllowTrace(false);
			ajpConnector.setScheme("http");
			tomcat.addAdditionalTomcatConnectors(ajpConnector);
		}
		return tomcat;
	}
}
