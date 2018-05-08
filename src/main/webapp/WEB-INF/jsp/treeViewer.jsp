<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
    <head>
    <meta charset="utf-8">
    <meta http-equiv="X-UA-Compatible" content="IE=edge,chrome=1">
    <meta name="viewport" content="width=device-width">
    <title> Basic example </title>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/Treant.css">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/basic-example.css">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/loading.css">
</head>
<body>
	<img src="${pageContext.request.contextPath}/img/ajax-loader.gif" id="loading" />
    <div class="chart" id="basic-example"></div>
    <script src="${pageContext.request.contextPath}/js/vendor/raphael.js"></script>
    <script src="${pageContext.request.contextPath}/js/Treant.js"></script>
    <script src="${pageContext.request.contextPath}/js/tree_viewer.js"></script>
    <form action="javascript:treeViewer()">
		<label for="root_board_id">루트 게시글 번호 : </label><input type="number" min="0" id="root_board_id" value="${rootBoardId}"/>
		<input type="submit" value="draw" />
    </form>
</body>
</html>

