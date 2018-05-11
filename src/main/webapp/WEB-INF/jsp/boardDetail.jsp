﻿<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt"%>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions"%>
    
<!DOCTYPE html>
<html>
<link rel="stylesheet" type="text/css" href="${pageContext.request.contextPath}/css/home.css">
<script src="${pageContext.request.contextPath}/js/jquery-1.10.2.js"></script>
<script src="${pageContext.request.contextPath}/js/jquery-ui-1.11.0.js"></script>
<script src="${pageContext.request.contextPath}/js/board.js"></script>
<script src="${pageContext.request.contextPath}/js/crawling.js"></script>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=utf-8">
<title>게시물 상세보기</title>
</head>
<div id="dialog3"> </div>
<body>
    <div class="board_view">
        <div class="subject _title_area">
            <span class="txt_noti _attention_txt_noti" style="display: none;"></span>
            <span class="_attention"></span>
            <h3 class="txt _title translateArea">
                <font class="NaverTrans-Parent">
                    <font class="NaverTrans-Child">제목 : ${board.subject}</font>
                </font>
            </h3>
            <button class="btn btn-primary" style="float: right;" onclick="location.href='${path}/assignment'">홈으로</button>
        </div>
        <script>
//         function textToHTML() {
//             sHtml = document.getElementById('content2').value;

//             var sContent = sHtml, aTemp = null;
            
//             // applyConverter에서 추가한 sTmpStr를 잠시 제거해준다. sTmpStr도 하나의 string으로 인식하는 경우가 있기 때문.
//             aTemp = sContent.match('@[0-9]+@');
//             if (aTemp !== null) {
//                 sContent = sContent.replace(aTemp[0], "");
//             }
                    
//             sContent =  sContent.replace(/&/g, '&amp;').replace(/</g, '&lt;').replace(/>/g, '&gt;').replace(/ /g, '&nbsp;');
//             sContent = addLineBreaker(sContent);

//             if (aTemp !== null) {
//                 sContent = aTemp[0] + sContent;
//             }
            
//             document.getElementById('content2').innerHTML = sContent;
//         }

//         function addLineBreaker(sContent){
//             var oContent = '';
//                 aContent = sContent.split('\n'); // \n을 기준으로 블럭을 나눈다.
//                 aContentLng = aContent.length; 
//                 sTemp = "";
            
//             for (var i = 0; i < aContentLng; i++) {
//                 //sTemp = jindo.$S(aContent[i]).trim().$value();
//                 sTemp = aContent[i].trim();
//                 if (i === aContentLng -1 && sTemp === "") {
//                     break;
//                 }
                
//                 if (sTemp !== null && sTemp !== "") {
//                     oContent +='<P>';
//                     oContent += aContent[i];
//                     oContent += '</P>';
//                 } else {
//                     oContent += '<P><BR></P>';
//                 }
//             }
            
//             return oContent.toString();
//         }
        </script>
        <div class="infor _infor">
            <span class="name">게시물 번호 : <span class="_group">${board.board_id} ,</span> <span class="_company"></span></span>
            <span class="name">버전 : <span class="_group">${board.version} ,</span> <span class="_company"></span></span>
            <span class="date">최종 수정시간 :  ${board.created_time} ,</span>
            <span class="date">첨부 파일 :   <a href="${path}/assignment/boards/download/${file.file_id}" name="file">${file.file_name}     </a> (${file.file_size})</span>
            <button class="btn btn-primary" id="btnAutoList" onclick="location.href='${path}/assignment/autos/${board.board_id}/${board.version}'">자동저장리스트</button>
        <div class="cont _content translateArea" id="content3">
                <textarea name="content3" id="content2" style="min-height: 500px; min-width: 700px; display : none;" >${board.content}</textarea> 
        </div>
        
          <button id = "temp" onclick="textToHTML()"></button>
        <div class="btn_box _btn_area _no_print">
            <form action="/assignment/boards/update" method="post" style="display: inline;">
                <input name="board_id" type="text" id="board_id" value="${board.board_id}" style="display:none;">
                <input name="version" type="text" id="version" value="${board.version}" style="display:none;">
                <input name="subject" type="text" id="subject" value="${board.subject}" style="display:none;">
                <input name="created_time" type="text" id="created_time" value="${board.created_time}" style="display:none;">
                <textarea name="content" id="content"  style="display:none;"><c:out value="${board.content}" escapeXml="true" /></textarea>
                <input name="file_id" type="text" id="file_id" value="${file.file_id}" style="display:none;">
                <input name="file_name" type="text" id="file_name" value="${file.file_name}" style="display:none;">
                <input name="file_data" type="file" id="file_data" value="${file.file_data}" style="display:none;">
                <input name="file_size" type="text" id="file_size" value="${file.file_size}" style="display:none;">
                <button class="_edit_atc" type="submit">수정</button> 
                <button class="_delete_atc" type="button" onclick="btnDelete(${board.board_id},${board.version});">삭제</button>
            </form>
        </div>
    </div>
</div>
</body>
<script>
function m_over(target){
    var crawling_api = 'search';
    var crawling_category_kakao = 'web';
    var crawling_category = target.id;
    var crawling_text = target.value;
    var url = "/assignment/api/naver/" + crawling_api + "/"
            + crawling_category + "/" + encodeURI(crawling_text);

    $('html', parent.parent.document).find('#dialog3').dialog({
        open : function() {
            $(this).load(url);
        },
        width : 400,
        height : 600,
        resizable : false,
        draggable : true,
        modal : true
    });
}
function textToHTML() {
    sHtml = document.getElementById('content2').value;

    var sContent = sHtml, aTemp = null;
    
    // applyConverter에서 추가한 sTmpStr를 잠시 제거해준다. sTmpStr도 하나의 string으로 인식하는 경우가 있기 때문.
    aTemp = sContent.match('@[0-9]+@');
    if (aTemp !== null) {
        sContent = sContent.replace(aTemp[0], "");
    }
            
    sContent =  sContent.replace(/&/g, '&amp;').replace(/</g, '&lt;').replace(/>/g, '&gt;').replace(/ /g, '&nbsp;');
    sContent = addLineBreaker(sContent);

    if (aTemp !== null) {
        sContent = aTemp[0] + sContent;
    }
    
    document.getElementById('content3').innerHTML = sContent;
}

function addLineBreaker(sContent){
    var oContent = '';
        aContent = sContent.split('\n'); // \n을 기준으로 블럭을 나눈다.
        aContentLng = aContent.length; 
        sTemp = "";
    
    for (var i = 0; i < aContentLng; i++) {
        //sTemp = jindo.$S(aContent[i]).trim().$value();
        sTemp = aContent[i].trim();
        if (i === aContentLng -1 && sTemp === "") {
            break;
        }
        
        if (sTemp !== null && sTemp !== "") {
            oContent +='<P>';
            oContent += aContent[i];
            oContent += '</P>';
        } else {
            oContent += '<P><BR></P>';
        }
    }
    
    return oContent.toString();
}


</script>
</html>