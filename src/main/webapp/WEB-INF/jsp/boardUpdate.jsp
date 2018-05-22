<%@ page language="java" contentType="text/html; charset=utf-8"
    pageEncoding="utf-8"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt"%>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>

<link rel="stylesheet" href="${pageContext.request.contextPath}/css/jquery-ui-1.11.0.css">
<link rel="stylesheet" type="text/css" href="${pageContext.request.contextPath}/css/home.css">
<link rel="stylesheet" type="text/css" href="${pageContext.request.contextPath}/css/common_ncs.css">
<link rel="stylesheet" type="text/css" href="${pageContext.request.contextPath}/css/home_editor.min.css">
<link rel="stylesheet" href="${pageContext.request.contextPath}/css/billboard.css">
<link rel="stylesheet" href="${pageContext.request.contextPath}/css/semantic.css">
<link rel="stylesheet" href="${pageContext.request.contextPath}/css/transition.css">
<script src="https://d3js.org/d3.v4.min.js"></script>
<script src="${pageContext.request.contextPath}/js/jquery-1.10.2.js"></script>
<script src="${pageContext.request.contextPath}/js/jquery-ui-1.11.0.js"></script>
<script src="${pageContext.request.contextPath}/js/board.js"></script>
<script src="${pageContext.request.contextPath}/js/auto.js"></script>
<script src="${pageContext.request.contextPath}/js/auto_save.js"></script>
<script src="${pageContext.request.contextPath}/js/diff_match_patch.js"></script>
<script src = "${pageContext.request.contextPath}/js/billboard.js"></script>
<script src="${pageContext.request.contextPath}/js/semantic.js"></script>
<script src="${pageContext.request.contextPath}/js/transition.js"></script>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=utf-8">
<title>게시글 수정</title>
</head>
<body>

    <button class="btn btn-primary" style="float: right;"
        onclick="location.href='${path}/assignment'">홈으로</button>
    <span class="date" id="currentBoard"
        style="float: left; margin-left: 10px; font-size: 15px;">board_id
        : <%=request.getParameter("board_id")%> version : <%=request.getParameter("version")%></span>
    <span class="date" id="notice"
        style="float: right; margin-right: 10px; font-size: 20px;"></span>
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
                        <h3 class="tx">파일첨부</h3> 
                        <c:if test="${param.file_name != ''}">
                            <span class="date" id="op_file">첨부 파일 : <a
                                href="${path}/assignment/boards/download/${param.file_id}/"
                                name="file">${param.file_name} </a> <span id="span_fileSize">${param.file_size}</span> <button type="button" id="fileUpdate" class="btn tx_point _save">파일 수정</button></span> 
                        </c:if> 
                        <c:if test="${param.file_name == '' }">
                            <input type="file" id="fileUp" name="fileUp" />
                        </c:if>

                    </li>
                    <li>
                        <h3 class="tx">자동 저장 옵션</h3> <span class="wrap_sbj"> <input
                            type="checkbox" id="autoSaveChkBox" />
                    </span>
                        <div id=conditionList style="display: inline;"></div>
                    </li>
                </ul>
                <textarea name="content" id="content"
                    style="margin: 8px; min-height: 100px; min-width: 99%; padding: 0px;">${param.content}</textarea>
                    <style>
                    .chart_container {
                        width: 25%;
                        float: left;
                    }
                    </style>
             <div>
                <div class="chart_container">
                <span id="chart1"></span>
                </div>
                <div class="chart_container">
                <span id="chart2"></span>
                </div>
                <div class="chart_container">
                <span id="chart3"></span>
                </div>
                <div class="chart_container">
                <span id="chart4"></span>
                </div>
             </div>
                <div class="btn_area _btn_area">
                    <input type="text" name="board_id" id="board_id"
                        style="display: none;"
                        value="<%=request.getParameter("board_id")%>"> <input
                        type="text" name="version" id="version" style="display: none;"
                        value="<%=request.getParameter("version")%>"> <input
                        type="text" name="cookie_id" id="cookie_id" style="display: none;"
                        value="<%=request.getParameter("cookie_id")%>">
                    <button type="button" id="btnUpdate" class="btn tx_point _save">
                        <strong>수정</strong>
                    </button>
                </div>
                <div id="pre_set" style="display: none">
                    <label title = "사소한 변경, 즉  띄어쓰기나 3글자 이내에 변경은  카운트 하지 않고 변경된 부분을 카운트 합니다. 값범위 : 1 ~ 100" for="addCountCondition">수정된 부분 개수 : </label><input type="number" min="1" max="100" id="addCountCondition" name="addCountCondition" value="0" style="width: 50px">
                    <label title = "개행 수를 카운트합니다. 값범위 : 1 ~ 50" for="newLineCountCondition">추가된 개행개수 : </label><input type="number" min="1" max="50" id="newLineCountCondition" name="newLineCountCondition" value="0" style="width: 50px">
                    <label title = "원본 내용의 크기에서 현재 내용의 크기를 뺀 값을 다시 원본 내용킈 크기로 나눕니다. 값이 낮을 수록 둔감하게 버전업이 됩니다. 값범위 : -2 ~ -0.2" for="stringSizeDifferenceCondition">(원본 내용 크기 - 현재 내용 크기) / 원본 내용 크기 : </label><input type="number" min="-2" max="-0.2" id="stringSizeDifferenceCondition" name="stringSizeDifferenceCondition" value="0" style="width: 50px">
                    <label title = "전체 문자열의 증가가 아닌 추가된 부분의 문자열 길이를 카운트 합니다. 값범위 : 100 ~ 1000" for="addLengthCondition">추가된 문자열길이 : </label><input type="number" min="100" max="1000" id="addLengthCondition" name="addLengthCondition" value="0" style="width: 50px">
                    <input type="button" id="btnCondition" name="btnCondition" value="적용" style="width: 30px" onClick="applyChange()" >
                </div>
            </div>
        </div>
    </form>
</body>
</html>