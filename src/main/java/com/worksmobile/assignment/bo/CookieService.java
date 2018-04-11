package com.worksmobile.assignment.bo;

import java.util.UUID;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.stereotype.Service;

@Service
public class CookieService {
	
	 public final static String COOKIE_NAME = "cookieName";
	  
	    public Cookie getCookie(HttpServletRequest req) {
	    	Cookie[] cookies =req.getCookies();
	    	if(cookies == null) {
	    		return null;
	    	}
	    	Cookie curCookie= null ;
			for(int i=0; i<cookies.length; i++){
				Cookie c = cookies[i];
				if(c.getName().equals(COOKIE_NAME)) {
					curCookie = c;
					break;
				}
			}
			return curCookie;
	    }
  
	     public Cookie creteCookie(HttpServletResponse res) {
	    	String cookieId = UUID.randomUUID().toString().replace("-", "");
	    	System.out.println(cookieId);
	    	Cookie cookie = new Cookie(COOKIE_NAME,cookieId);
	    	res.addCookie(cookie);
	    	return cookie;
	    }

}
