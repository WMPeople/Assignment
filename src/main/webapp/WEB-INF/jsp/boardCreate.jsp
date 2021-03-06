<%@ page language="java" contentType="text/html; charset=utf-8"
    pageEncoding="utf-8"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<link rel="stylesheet" href="${pageContext.request.contextPath}/css/jquery-ui-1.11.0.css">
<link rel="stylesheet" href="https://maxcdn.bootstrapcdn.com/bootstrap/3.3.2/css/bootstrap.min.css">
<link rel="stylesheet" href="https://maxcdn.bootstrapcdn.com/bootstrap/3.3.2/css/bootstrap-theme.min.css">
<link rel="stylesheet" href="${pageContext.request.contextPath}/css/semantic.css">
<link rel="stylesheet" href="${pageContext.request.contextPath}/css/transition.css">
<link rel="stylesheet" type="text/css" href="${pageContext.request.contextPath}/css/home.css">
<link rel="stylesheet" type="text/css" href="${pageContext.request.contextPath}/css/common_ncs.css">
<link rel="stylesheet" type="text/css" href="${pageContext.request.contextPath}/css/home_editor.min.css">
<script src="${pageContext.request.contextPath}/js/jquery-1.10.2.js"></script>
<script src="${pageContext.request.contextPath}/js/jquery-ui-1.11.0.js"></script>
<script src="${pageContext.request.contextPath}/js/board.js" type="text/javascript"> </script>
<script src="${pageContext.request.contextPath}/js/semantic.js"></script>
<script src="${pageContext.request.contextPath}/js/transition.js"></script>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=utf-8">
<title>Insert title here</title>
</head>
<body>

<form id="fileForm" action="fileUpload" method="post" enctype="multipart/form-data">
	<div id="articleEditor" >
		<div class="board_write">
			<h2 class="tit">글쓰기</h2>
			<ul class="option _info">
				<li class="opt_sbj">
					<h3 class="tx">제목</h3>
					<span class="wrap_sbj">
						<input type="text" name="subject" id="subject" class="itxt subject _title"  value="">
					</span>
				</li>
				<li class="opt_file">
					<h3 class="tx">파일첨부</h3>
					<div class="_file_btn" style="width: 500; overflow: hidden; position: relative;">
						<div class="browse-box _article_attach_btn_back" >
							 <input type="file" id="fileUp" name="fileUp"/>
						</div>
					</div>
				</li>
			</ul>
			<textarea  name="content" id="content" style="margin:8px; min-height: 500px; min-weight: 500px; padding:0px;" ></textarea>
			<div class="btn_area _btn_area">
				 <button type="button" id= "btnCreate" class="btn tx_point _save"><strong>확인</strong></button>
			</div>
		</div>
	</div>
</form>

</body>
</html>