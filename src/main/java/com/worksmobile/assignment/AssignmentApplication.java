package com.worksmobile.assignment;

import javax.sql.DataSource;

import org.apache.catalina.connector.Connector;
import org.apache.ibatis.session.SqlSessionFactory;
import org.mybatis.spring.SqlSessionFactoryBean;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.web.embedded.tomcat.TomcatServletWebServerFactory;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.boot.web.servlet.server.ServletWebServerFactory;
import org.springframework.boot.web.servlet.support.ErrorPageFilter;
import org.springframework.boot.web.servlet.support.SpringBootServletInitializer;
import org.springframework.context.annotation.Bean;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.scheduling.annotation.EnableAsync;

@SpringBootApplication
@EnableAsync
public class AssignmentApplication extends SpringBootServletInitializer {

	public static void main(String[] args) {
		SpringApplication.run(AssignmentApplication.class, args);
	}

	// 외장 톰캣을 사용할때
	@Override
	protected SpringApplicationBuilder configure(SpringApplicationBuilder application) {
		return application.sources(AssignmentApplication.class);
	}

	// 외장 톰캣 사용시 에러 패이지 출력안되는 것을 방지.
	@Bean
	public ErrorPageFilter errorPageFilter() {
		return new ErrorPageFilter();
	}

	// 외장 톰캣 사용시 에러 패이지 출력안되는 것을 방지.
	@Bean
	public FilterRegistrationBean<ErrorPageFilter> disableSpringBootErrorFilter(ErrorPageFilter filter) {
		FilterRegistrationBean<ErrorPageFilter> filterRegistrationBean = new FilterRegistrationBean<ErrorPageFilter>();
		filterRegistrationBean.setFilter(filter);
		filterRegistrationBean.setEnabled(false);
		return filterRegistrationBean;
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
		Resource[] res = new PathMatchingResourcePatternResolver().getResources("mapper/*Mapper.xml");
		sessionFactory.setMapperLocations(res);
		return sessionFactory.getObject();
	}

	@Bean
	public ServletWebServerFactory servletContainer() {
		TomcatServletWebServerFactory tomcat = new TomcatServletWebServerFactory();
		if (tomcatAjpEnabled) {
			Connector ajpConnector = new Connector(ajpProtocol);
			ajpConnector.setPort(ajpPort);
			ajpConnector.setSecure(false);
			ajpConnector.setAllowTrace(false);
			ajpConnector.setScheme("http");
			tomcat.addAdditionalTomcatConnectors(ajpConnector);
		}
		return tomcat;
	}
}
