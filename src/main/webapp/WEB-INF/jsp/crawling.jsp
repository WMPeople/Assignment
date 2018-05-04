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

.book {
	display: block;
	float: left;
	margin-right: 13px;
	background: #9f9f9f;
	vertical-align: middle;
}
</style>
<body>
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
					    onerror="this.src='http://static.naver.net/book/image/noimg3.gif';"
						width="67" height="auto" alt="" ></a>
				</p>
				<dl>
					<dt>
						<a href="${jsonArray.link}">${jsonArray.title}</a>
					</dt>
					<dd class="point">평점 : ${jsonArray.userRating}</dd>
					<dd class="director">감독 : ${jsonArray.director}</dd>
					<dd class="actor" style="font-size:10px">배우 : ${jsonArray.actor}</dd>
				</dl>
			</li>
		</c:forEach>
	</c:if>
	<c:if test="${type eq 'news'}">
        <c:forEach var="jsonArray" items="${jsonArray}">
        <table style="margin-bottom:10px;  border: 1px solid #AAAAAA;
  padding: 3px 2px; text-align: middle;">
    <tbody>
        <tr>
            <td style="white-space: nowrap; ">날짜: </td>
            <td style="border-left: 10px;">${jsonArray.pubDate}</td>
        </tr>
        <tr>
            <td>제목:</td>
            <td style="border-left: 10px; "><a href="${jsonArray.link}">${jsonArray.title}</a></td>
        </tr>
        <tr>
            <td>설명:</td>
            <td style="border-left: 10px;">${jsonArray.description}</td>
        </tr>
    </tbody>
    </table>
           
        </c:forEach>
    </c:if>
    <c:if test="${type eq 'shop'}">
        <c:forEach var="jsonArray" items="${jsonArray}">
    <table>
    <tbody>
        <tr>
            <td style="white-space: nowrap; "><p class="result_thumb">
                    <a href="${jsonArray.link}"><img src="${jsonArray.image}"
                        onerror="this.src='http://static.naver.net/book/image/noimg3.gif';"
                        width="100" height="auto" alt="" ></a>
                </p></td>
        </tr>
        <tr>
            <td colspan="4"><a href="${jsonArray.link}">${jsonArray.title}</a></td>
        </tr>
        <tr>
            <td>${jsonArray.mallName}</td>
        </tr>
        <tr>
            <td>${jsonArray.lprice}원 - ${jsonArray.hprice}원</td>
        </tr>
    </tbody>
    </table>
           
        </c:forEach>
    </c:if>
    
     <c:if test="${type eq 'geocode'}">
        <c:forEach var="jsonArray" items="${jsonArray}">
    <table>
    <tbody>
       
        <tr>
            <td colspan="4"><a href="${jsonArray.hyper}">${jsonArray.title}</a></td>
        </tr>
        <tr>
            <td>주소 : </td>
            <td>${jsonArray.address}</td>
        </tr>
    </tbody>
    
    </table>
            <button onclick="location.href='${jsonArray.link}'" style="margin-bottom:10px;">거리뷰</button>
        </c:forEach>
    </c:if>
    
     <c:if test="${type eq 'dictionary'}">
        <c:forEach var="jsonArray" items="${jsonArray}">
    <table>
    <tbody>
       
        <tr>
            <td colspan="4"><a href="${jsonArray.link}">${jsonArray.title}</a></td>
        </tr>
        <tr>
            <td>${jsonArray.expression}</td>
            <td>${jsonArray.meaning}</td>
        </tr>
   
    </tbody>
    </table>
        </c:forEach>
    </c:if>
    
     <c:if test="${type eq 'place'}">
        <c:forEach var="jsonArray" items="${jsonArray}">
    <table>
    <tbody>
       
        <tr>
             <td style="white-space: nowrap; "><p class="result_thumb">
                    <a href="${jsonArray.link}"><img src="${jsonArray.image}"
                        onerror="this.src='http://static.naver.net/book/image/noimg3.gif';"
                        width="100" height="auto" alt="" ></a>
                </p></td>
        </tr>
        <tr>
            <td><a href="${jsonArray.link}">${jsonArray.title}</a></td>
            <td>${jsonArray.description}</td>
            
        </tr>
   
    </tbody>
    </table>
        </c:forEach>
    </c:if>
</body>
</html>


