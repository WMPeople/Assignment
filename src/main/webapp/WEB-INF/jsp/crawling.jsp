<%@ page language="java" contentType="text/html; charset=UTF-8"
	pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt"%>
<!DOCTYPE html>
<html lang="ko">

<script src="${pageContext.request.contextPath}/js/jquery-1.10.2.js"></script>
<script src="${pageContext.request.contextPath}/js/jquery-ui-1.11.0.js"></script>
<script src="${pageContext.request.contextPath}/js/board.js"></script>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
<!-- BootStrap CDN -->
<link rel="stylesheet" href="css/jquery-ui-1.11.0.css">
<link rel="stylesheet"
	href="https://maxcdn.bootstrapcdn.com/bootstrap/3.3.2/css/bootstrap.min.css">
<link rel="stylesheet"
	href="https://maxcdn.bootstrapcdn.com/bootstrap/3.3.2/css/bootstrap-theme.min.css">
<title>크롤링</title>
</head>
<style>
.result_thumb {
	display: block;
	float: left;
	width: 67px;
	height: 95px;
	margin-right: 13px;
	background: #9f9f9f;
	vertical-align: middle;
}

.book {
	display: block;
	float: left;
	margin-right: 13px;
	background: #9f9f9f;
	vertical-align: middle;
}
</style>
<body>
	네이버 정보
	<c:if test="${type eq 'book'}">
		<c:forEach var="jsonArray" items="${jsonArray}">
			<li style="margin-bottom: 30px;">

				<p class="book">
					<a href="${jsonArray.link}" target="_blank"> <img
						src="${jsonArray.image}"
						onerror="this.src='http://static.naver.net/book/image/noimg3.gif';"
						alt="${jsonArray.title}" width="67" height="95">
					</a>
				</p>
				<dl>
					<dt>
						<a href="${jsonArray.link}" target="_blank">${jsonArray.title}
						</a>
					</dt>
					<dd class="point">
						<a
							href="http://book.naver.com/search/search.nhn?query=${jsonArray.author}&amp;frameFilterType=1&amp;frameFilterValue=6000002015">${jsonArray.author}</a>
						<span>|</span> &nbsp;<a
							href="http://book.naver.com/search/search.nhn?filterType=7&amp;query=${jsonArray.publisher}">${jsonArray.publisher}</a>&nbsp;
					</dd>
					<dd>가격 : ${jsonArray.price} 원</dd>
					<dd>출판일 : ${jsonArray.pubdate}</dd>
				</dl>
			</li>
		</c:forEach>
	</c:if>
	<c:if test="${type eq 'movie'}">
		<c:forEach var="jsonArray" items="${jsonArray}">
			<li style="margin-bottom: 40px;">
				<p class="result_thumb">
					<a href="${jsonArray.link}"><img src="${jsonArray.image}"
						width="67" height="95" alt=""></a>
				</p>
				<dl>
					<dt>
						<a href="${jsonArray.link}">${jsonArray.title}</a>
					</dt>
					<dd class="point">평점 : 8.21</dd>

					<dd class="director">감독 : ${jsonArray.director}</dd>
					<dd class="actor" style="font-size:10px">배우 : ${jsonArray.actor}</dd>
				</dl>
			</li>
		</c:forEach>
	</c:if>
</body>
</html>


