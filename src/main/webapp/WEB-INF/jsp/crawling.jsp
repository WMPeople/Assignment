<%@ page language="java" contentType="text/html; charset=UTF-8"
	pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt"%>
<span style="display:none" id="total">${total}</span>
<c:if test="${type eq 'book'}">
	<c:forEach var="jsonArray" items="${jsonArray}">
		<li class="container card">
			<div class="img">
				<a href="${jsonArray.link}" target="_blank">
				<img src="${jsonArray.image}" onerror="this.src='http://static.naver.net/book/image/noimg3.gif';"
					alt="${jsonArray.title}" width="67" height="auto">
				</a>
			</div>
			<dl class="context">
				<dt class="link">
					<a href="${jsonArray.link}" target="_blank">${jsonArray.title} </a>
				</dt>
				<dd>
					<a href="http://book.naver.com/search/search.nhn?query=${jsonArray.author}&amp;frameFilterType=1&amp;frameFilterValue=6000002015">${jsonArray.author}</a>
					<span>|</span> <a href="http://book.naver.com/search/search.nhn?filterType=7&amp;query=${jsonArray.publisher}">${jsonArray.publisher}</a>
				</dd>
				<dd class="price">${jsonArray.price}</dd>
				<dd>
					출판일 : <span class="date">${jsonArray.pubdate}</span>
				</dd>
			</dl>
		</li>
	</c:forEach>
</c:if>
<c:if test="${type eq 'movie'}">
	<c:forEach var="jsonArray" items="${jsonArray}">
		<li class="container card">
			<div class="img">
				<a href="${jsonArray.link}">
				<img src="${jsonArray.image}"
					onerror="this.src='http://static.naver.net/book/image/noimg3.gif';"
					width="67" height="auto" alt="" ></a>
			</div>
			<dl class="context">
				<dt>
					<a href="${jsonArray.link}">${jsonArray.title}</a>
				</dt>
				<dd>
					<div>
					<span>평점 :</span>
					<span class="st_off">
						<span class="st_on" style="width:${jsonArray.userRating * 10}%"></span>
					</span>
					<span>${jsonArray.userRating}</span>
					</div>
				</dd>
				<dd class="director">감독 : ${jsonArray.director}</dd>
				<dd class="actor">배우 : ${jsonArray.actor}</dd>
			</dl>
		</li>
	</c:forEach>
</c:if>
<c:if test="${type eq 'news'}">
	<c:forEach var="jsonArray" items="${jsonArray}">
	<li class="container">
		<dl class="full_context card">
			<dt class="text_overflow">
				<a href="${jsonArray.link}" style="color: blue;">${jsonArray.title}</a>
			</dt>
			<dd>날짜: ${jsonArray.pubDate}
			</dd>
			<dd class="clamp3line" style="margin-top: 10px">${jsonArray.description}</dd>
		</dl>
	   </li>
	</c:forEach>
</c:if>
<c:if test="${type eq 'shop'}">
	<c:forEach var="ele" items="${jsonArray}" varStatus="status" step="2">
		<li class="column card">
			<div class="image">
				<a href="${ele.link}"><img src="${ele.image}"
					onerror="this.src='http://static.naver.net/book/image/noimg3.gif';"
					width="auto" height="100" alt="" ></a>
			</div>
			<dl class="container">
				<dt>
					<a href="${ele.link}"><span class="clamp3line">${ele.title}</span></a>
				</dt>
				<dd style="color: gray; ">${ele.mallName} </dd>
				<dd>${ele.lprice}원 <c:if test="${ele.hprice ne '0'}"> - ${ele.hprice}원 </c:if> </dd>
			</dl>
		</li>
		<c:if test="${!status.last}">
		<c:set var="ele" value="${jsonArray[status.index + 1]}"/>
		<li class="column card">
			<div class="image">
				<a href="${ele.link}"><img src="${ele.image}"
					onerror="this.src='http://static.naver.net/book/image/noimg3.gif';"
					width="auto" height="100" alt="" ></a>
			</div>
			<dl class="container">
				<dt>
					<a href="${ele.link}"><span class="clamp3line">${ele.title}</span></a>
				</dt>
				<dd style="color: gray; ">${ele.mallName} </dd>
				<dd>${ele.lprice}원 <c:if test="${ele.hprice ne '0'}"> - ${ele.hprice}원 </c:if> </dd>
			</dl>
		</li>
		</c:if>
	</c:forEach>
</c:if>
 <c:if test="${type eq 'geocode'}">
	<c:forEach var="jsonArray" items="${jsonArray}">
	<li class="container card">
		<dl class="full_context" style="float: left">
			<dt>
				<a href="${jsonArray.place_url}">${jsonArray.place_name}</a>
			</dt>
			<dd style="color:gray">${jsonArray.road_address_name}
			</dd>
		</dl>
		<span class="right_float_btn" style="float: right">
			<a href="${jsonArray.place_url}">
				<img src='${pageContext.request.contextPath}/img/placeholder.png'/>
			</a>
		</span>
	</li>
	</c:forEach>
</c:if>
 <c:if test="${type eq 'dictionary'}">
	<c:forEach var="jsonArray" items="${jsonArray}">
	   <li class="container">
		   <dl class="full_context card">
			   <dt>
				   <a href="${jsonArray.link}" style="color:blue">${jsonArray.title}</a> 
			   </dt>
			<dd>${jsonArray.expression}</dd>
			<dd>${jsonArray.meaning}</dd>
		</dl>
	</li>
	</c:forEach>
</c:if>

 <c:if test="${type eq 'local'}">
	<c:forEach var="jsonArray" items="${jsonArray}">
	<li class="container">
		<dl class="full_context card">
			<dt>
				<a href="${jsonArray.link}" style="color:blue">${jsonArray.title}</a>
			</dt>
			<dd>${jsonArray.address}</dd>
			<dd>${jsonArray.description}</dd>
		</dl>
	</li>
	</c:forEach>
</c:if>
<img src="${pageContext.request.contextPath}/img/ajax-loader.gif" id="loading"/>
<!-- 
<div>Icons made by <a href="https://www.flaticon.com/authors/smashicons" title="Smashicons">Smashicons</a> from <a href="https://www.flaticon.com/" title="Flaticon">www.flaticon.com</a> is licensed by <a href="http://creativecommons.org/licenses/by/3.0/" title="Creative Commons BY 3.0" target="_blank">CC 3.0 BY</a></div>
 -->