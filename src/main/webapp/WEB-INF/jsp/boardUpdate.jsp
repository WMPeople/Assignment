<%@ page language="java" contentType="text/html; charset=utf-8"
	pageEncoding="utf-8"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<link rel="stylesheet"
	href="//code.jquery.com/ui/1.11.0/themes/smoothness/jquery-ui.css">
<link rel="stylesheet"
	href="https://maxcdn.bootstrapcdn.com/bootstrap/3.3.2/css/bootstrap.min.css">
<link rel="stylesheet"
	href="https://maxcdn.bootstrapcdn.com/bootstrap/3.3.2/css/bootstrap-theme.min.css">
<link rel="stylesheet" type="text/css" href="../css/home.css">
<link rel="stylesheet" type="text/css" href="../css/common_ncs.css">
<link rel="stylesheet" type="text/css" href="../css/home_editor.min.css">
<script src="//code.jquery.com/jquery-1.10.2.js"></script>
<script src="//code.jquery.com/ui/1.11.0/jquery-ui.js"></script>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=utf-8">
<title>Insert title here</title>
</head>
<body>


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
								href="${path}/Assignment/boards/download/${param.file_id}/"
								name="file">${param.file_name} </a> (${param.file_size})
								<button type="button" id="fileUpdate" class="btn tx_point _save">파일
									수정</button>
							</span>
						</c:if> <c:if test="${param.file_name == '' }">
							<input type="file" id="fileUp" name="fileUp" />
						</c:if>
					</li>
				</ul>

				<textarea name="content" id="content"
					style="min-height: 500px; min-weight: 500px;">${param.content} </textarea>
				<!--      	<input type="text" name="content" id="content" class="itxt content _title"> -->
				<div class="btn_area _btn_area">
					<!-- 			<p class="next"> -->
					<!-- 				<button type="button" class="btn _cancel">작성취소</button> -->
					<!-- 				<button type="button" class="btn _atc_delete">삭제</button> -->
					<!-- 			</p> -->
					<!-- 			<button type="button" class="btn _temp">임시저장</button> -->
					<!-- 			<button type="button" class="btn _preview">미리보기</button> -->
					<input type="text" name="board_id" id="board_id"
						style="display: none;"
						value="<%=request.getParameter("board_id")%>"> <input
						type="text" name="version" id="version" style="display: none;"
						value="<%=request.getParameter("version")%>">
					<button type="button" id="btnUpdate" class="btn tx_point _save">
						<strong>수정</strong>
					</button>
				</div>
			</div>
		</div>
	</form>
</body>
<script>
$(document).ready(function(){
	  $("#fileUpdate").click(function(){
	    	 document.getElementById('test').innerHTML='<input type="file" id="fileUp" name="fileUp" />'
	     });
	  var replaceText = $('#content').val().replace(/<br>/g, '\n');
 	 $('#content').val(replaceText);

});
$("#btnUpdate").click(function(){
   	var file_name = '<%=request.getParameter("file_name")%>';
   	if(file_name != ''){
   		file_name = '<%=request.getParameter("file_name")%>';
		}
		var formData = new FormData($("#fileForm")[0]);

		//원본 게시물에 파일이 있지만 수정하지 않았을 때
		if (file_name != null && $("#fileUp").val() == null) {
			$.ajax({
				type : "POST",
				contentType : "application/json; charset=UTF-8",
				data : formData,
				processData : false,
				contentType : false,
				url : "/Assignment/boards/update3",
				success : function(result) {
					if (result == 1) {
						alert("보드 수정 완료");
						location.href = "/Assignment/";
					} else {
						alert("수정 실패");
					}
				},
				error : function(xhr, status, error) {
					console.log(error);
					console.log(formData)
				}
			});
		} else {

			$.ajax({
				type : "POST",
				contentType : "application/json; charset=UTF-8",
				data : formData,
				processData : false,
				contentType : false,
				url : "/Assignment/boards/update2",
				success : function(result) {
					if (result == 1) {
						alert("보드 수정 완료");
						location.href = "/Assignment/";
					} else {
						alert("수정 실패");
					}
				},
				error : function(xhr, status, error) {
					console.log(error);
					console.log(formData)

				}
			});
		}
	});
</script>
</html>