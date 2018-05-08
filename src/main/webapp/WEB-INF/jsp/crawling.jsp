<%@ page language="java" contentType="text/html; charset=UTF-8"
	pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt"%>
<!DOCTYPE html>
<html lang="ko">
<head>
<link rel="stylesheet" href="//code.jquery.com/ui/1.11.0/themes/smoothness/jquery-ui.css">
<script src="${pageContext.request.contextPath}/js/jquery-1.10.2.js"></script>
<script src="${pageContext.request.contextPath}/js/jquery-ui-1.11.0.js"></script>
<script src="${pageContext.request.contextPath}/js/board.js"></script>
<script src="${pageContext.request.contextPath}/js/clamp.js"></script>
<script type="text/javascript" src="https://openapi.map.naver.com/openapi/v3/maps.js?clientId=TFUBwdm3MrMuN3_1TYil&submodules=geocoder"></script>
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
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

.container {
	width: 100%;
	overflow: auto;
	display: block;
}

.img {
	float: left;
	width: 20%;
	height: auto;
	margin: 2px -10px;
}

.context {
	float: right;
	width: 80%;
}

.context * {
	text-overflow: ellipsis;
	overflow: hidden;
	white-space: nowrap;
}

.text_overflow {
	text-overflow: ellipsis;
	overflow: hidden;
	white-space: nowrap;
}

.full_context * {
	text-overflow: ellipsis;
	/*overflow: hidden;
	white-space: nowrap;*/
}

.card {
	padding: 2px 16px;
	box-shadow: 0 4px 8px 0 rgba(0,0,0,0.2);
	transition: 0.3s;
	width: 85%;
	margin: 10px;
}

.card:hover {
	box-shadow: 0 8px 16px 0 rgba(0,0,0,0.2);
}

.container {
}

.st_off {
	float: left;
	overflow: hidden;
	width: 75px;
	height: 12px;
	margin-top: 4px;
	background: url(https://ssl.pstatic.net/static/movie/2012/09/sp_star.png) no-repeat;
}

.st_on {
	display: block;
	overflow: hidden;
	height: 12px;
	background: url(https://ssl.pstatic.net/static/movie/2012/09/sp_star.png) 0 -20px no-repeat;
}

.column {
	float: left;
	width: 50%;
}


/* Clear floats after the columns */
.row:after {
	content: "";
	display: table;
	clear: both;
}

.book {
	display: block;
	float: left;
	margin-right: 13px;
	background: #9f9f9f;
	vertical-align: middle;
}
</style>
<script>
$('.price').each(function (){
	var item = $(this).text();
	var num = Number(item).toLocaleString('kr');
	$(this).text(num);
});
$('.clamp3line').each(function(){
	$clamp($(this), {clamp: 3});
})
</script>
<body>
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
		<div class="row">
			<div class="column">
				<div class="image"><p class="result_thumb">
								<a href="${ele.link}"><img src="${ele.image}"
								onerror="this.src='http://static.naver.net/book/image/noimg3.gif';"
								width="100" height="auto" alt="" ></a>
						</p>
				</div>
				<dl class="container">
					<dt>
						<a href="${ele.link}">${ele.title}</a>
					</dt>
					<dd style="color: gray; ">${ele.mallName} </dd>
					<dd>${ele.lprice}원 - ${ele.hprice}원</dd>
				</dl>
			</div>
			<c:if test="${!status.last}">
			<c:set var="ele" value="${jsonArray[status.index + 1]}"/>
			<div class="column">
				<div class="image"><p class="result_thumb">
								<a href="${ele.link}"><img src="${ele.image}"
								onerror="this.src='http://static.naver.net/book/image/noimg3.gif';"
								width="100" height="auto" alt="" ></a>
						</p>
				</div>
				<dl class="container">
					<dt>
						<a href="${ele.link}">${ele.title}</a>
					</dt>
					<dd style="color: gray; ">${ele.mallName} </dd>
					<dd>${ele.lprice}원 - ${ele.hprice}원</dd>
				</dl>
			</div>
			</c:if>
		</div>
		</c:forEach>
	</c:if>
	 <c:if test="${type eq 'geocode'}">
		<c:forEach var="jsonArray" items="${jsonArray}">
		<li class="container card">
			<dl class="full_context" style="float: left">
				<dt>
					<a href="${jsonArray.hyper}">${jsonArray.title}</a>
				</dt>
				<dd style="color:gray">${jsonArray.address}
				</dd>
			</dl>
			<span class="right_float_btn" style="float: right">
				<img src='${pageContext.request.contextPath}/img/placeholder.png' onclick="location.href='${jsonArray.link}'" style=")"/>
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
	
	 <c:if test="${type eq 'place'}">
		<c:forEach var="jsonArray" items="${jsonArray}">
		<li class="container card">
			 <div class="img">
				<a href="${jsonArray.link}">
					<img src="${jsonArray.image}"
						onerror="this.src='http://static.naver.net/book/image/noimg3.gif';"
						alt="${jsonArray.title}"
						width="67" height="auto">
				</a>
			</div>
			<dl class="context">
				<dt>
					<a href="${jsonArray.link}" style="color:blue">${jsonArray.title}</a>
				</dt>
				<dd>${jsonArray.description}</dd>
			</dl>
		</li>
		</c:forEach>
	</c:if>
	<!-- 
	<div>Icons made by <a href="https://www.flaticon.com/authors/smashicons" title="Smashicons">Smashicons</a> from <a href="https://www.flaticon.com/" title="Flaticon">www.flaticon.com</a> is licensed by <a href="http://creativecommons.org/licenses/by/3.0/" title="Creative Commons BY 3.0" target="_blank">CC 3.0 BY</a></div>
	 -->
</body>
</html>


