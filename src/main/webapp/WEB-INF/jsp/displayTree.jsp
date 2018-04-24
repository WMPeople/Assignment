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
    
</head>
<body>
    <div class="chart" id="basic-example"></div>
    <script src="${pageContext.request.contextPath}/js/vendor/raphael.js"></script>
    <script src="${pageContext.request.contextPath}/js/Treant.js"></script>
    <script src="${pageContext.request.contextPath}/js/create_tree.js"></script>
    <input type="number" min="0" id="root_board_id"/>
    <input type="button" onClick="displayTree()" value="draw" />
</body>
</html>

