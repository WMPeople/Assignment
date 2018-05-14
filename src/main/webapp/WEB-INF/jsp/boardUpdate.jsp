<%@ page language="java" contentType="text/html; charset=utf-8"
	pageEncoding="utf-8"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt"%>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<link rel="stylesheet" href="${pageContext.request.contextPath}/css/jquery-ui-1.11.0.css">
<link rel="stylesheet" href="https://maxcdn.bootstrapcdn.com/bootstrap/3.3.2/css/bootstrap.min.css">
<link rel="stylesheet" href="https://maxcdn.bootstrapcdn.com/bootstrap/3.3.2/css/bootstrap-theme.min.css">
<link rel="stylesheet" type="text/css" href="${pageContext.request.contextPath}/css/home.css">
<link rel="stylesheet" type="text/css" href="${pageContext.request.contextPath}/css/common_ncs.css">
<link rel="stylesheet" type="text/css" href="${pageContext.request.contextPath}/css/home_editor.min.css">
<script src="${pageContext.request.contextPath}/js/jquery-1.10.2.js"></script>
<script src="${pageContext.request.contextPath}/js/jquery-ui-1.11.0.js"></script>
<script src="${pageContext.request.contextPath}/js/board.js"></script>
<script src="${pageContext.request.contextPath}/js/auto.js"></script>
<script src="${pageContext.request.contextPath}/js/auto_save.js"></script>
<script src="${pageContext.request.contextPath}/js/diff_match_patch.js"></script>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=utf-8">
<title>게시글 수정</title>
</head>
<body>
    
    <button class="btn btn-primary" style="float:right;"  onclick="location.href='${path}/assignment'">홈으로</button>
	<span class="date" id="currentBoard" style="float:left; margin-left : 10px; font-size : 15px;">board_id :   <%=request.getParameter("board_id")%>  version :  <%=request.getParameter("version")%></span>
	<span class="date" id="notice" style="float:right; margin-right : 10px; font-size : 20px;"></span>
	<form id="fileForm" action="fileUpload" method='post'
		enctype="multipart/form-data">
		<div id="articleEditor">
			<div class="board_write">
				<h2 class="tit">글수정</h2>
				<ul class="option _info">
					<li class="opt_sbj">
						<h3 class="tx">제목</h3> <span class="wrap_sbj"> <input
							type="text" name="subject" id="subject"
							class="itxt subject _title"
							value="<%=request.getParameter("subject")%>">
					   </span>
					</li>
					<li class="opt_file">
						<h3 class="tx">파일첨부</h3> <c:if test="${param.file_name != ''}">
							<span class="date" id="test">첨부 파일 : <a
								href="${path}/assignment/boards/download/${param.file_id}/"
								name="file">${param.file_name} </a> (${param.file_size})
								<button type="button" id="fileUpdate" class="btn tx_point _save">파일
									수정</button>
							</span>
						</c:if> <c:if test="${param.file_name == '' }">
							<input type="file" id="fileUp" name="fileUp" />
						</c:if>
						
						
					</li>
					<li>
						<h3 class="tx">자동 저장 옵션</h3> <span class="wrap_sbj"> 
	                        <input type="checkbox" id="autoSaveChkBox" />
	                       </span>
                    </li>
				</ul>
				    
<!-- 				좀 더 고민..  -->
                    <%--
 					response.addHeader("X-XSS-Protection","0");
 					pageContext.setAttribute("tab", "	"); 
 					pageContext.setAttribute("nbsp", "&nbsp;"); 
 					pageContext.setAttribute("crcn", "\r\n"); 
 		 			pageContext.setAttribute("br", "<br/>"); 
                    --%>
				<textarea name="content" id="content"
					style="margin: 8px;min-height: 500px; min-width: 700px; padding: 0px;">${param.content}</textarea>
				<div class="btn_area _btn_area">
					<input type="text" name="board_id" id="board_id"
						style="display: none;"
						value="<%=request.getParameter("board_id")%>"> <input
						type="text" name="version" id="version" style="display: none;"
						value="<%=request.getParameter("version")%>">  
						<input
						type="text" name="cookie_id" id="cookie_id" style="display: none;"
						value="<%=request.getParameter("cookie_id")%>">
					<button type="button" id="btnUpdate" class="btn tx_point _save">
						<strong>수정</strong>
					</button>
				</div>
			</div>
		</div>
	</form>
</body>
</html>