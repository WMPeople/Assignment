<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt"%>
<!DOCTYPE html>
<html lang="ko">

<script src="//code.jquery.com/jquery-1.10.2.js"></script>
<script src="//code.jquery.com/ui/1.11.0/jquery-ui.js"></script>
<head>
<meta http-equiv="Content-Type" content= "text/html; charset=UTF-8">
<!-- BootStrap CDN -->
<link rel="stylesheet" href="//code.jquery.com/ui/1.11.0/themes/smoothness/jquery-ui.css">
<link rel="stylesheet" href="https://maxcdn.bootstrapcdn.com/bootstrap/3.3.2/css/bootstrap.min.css">
<link rel="stylesheet" href="https://maxcdn.bootstrapcdn.com/bootstrap/3.3.2/css/bootstrap-theme.min.css">
<title>버전 관리 페이지</title>
</head>
<body>
    <h3>버전 관리 페이지</h3> 
    <button class="btn btn-primary" style="float : left;" onclick="btnDiff();">선택한 버전 비교</button>
    
    <table class="table">
        <tr>
            <th>체크박스</th>
            <th>게시물번호</th>
            <th>버전</th>
            <th>브랜치</th>
            <th>날짜</th>
            <th>상태</th>
            <th>동작</th>
            
<%--             <c:forEach var="boardHistory" items="${list}"> --%>
<!--         <tr> -->
<%--             <td><input type="checkbox" name="cbox[]" value="${board.board_id}-${board.version}-${board.branch}" onchange="cbox.remain_two(this);"></td> --%>
<%--             <td>${boardHistory.board_id}</td> --%>
<%--             <td>${boardHistory.version}</td> --%>
<%--             <td>${boardHistory.branch}</td> --%>
<%--             <td>${boardHistory.created}</td> --%>
<%--             <td>${boardHistory.status}</td> --%>
<!--        		<td> -->
<%--        		<button class="btn btn-primary" id="btnDelete" onclick="btnDelete('${board.board_id}')">삭제</button> --%>
<%--        		<button class="btn btn-primary" id="btnRestore" onclick="btnRestore('${board.board_id}')">복원</button> --%>
<!--        		</td> -->
          

<!--         </tr> -->
<%--         </c:forEach> --%>
    </table>
<script>
$(document).ready(function(){
    
 });
 
function isTwoSelected() {  
    var num = 0;  
    $("input:checkbox:checked").each(function (index) {  
        num += 1;  
    });  
    
    if (num==2) return false;
    if (num!=2) return true;

//  
}  
 
function remain_two_obj(prefix){
	 
	this.old = new Array();
	this.prefix = prefix;
	
	this.remain_two = function(cur){
	
		items = document.getElementsByName(this.prefix + '[]');
		for( i = 0, count = 0 ; i < items.length; i++ )
			if( items[i].checked )
				count++;
				
		if( count > 0 && cur.checked == false && this.old[0] == cur.value )
			this.old[0] = this.old[1];
			
		if( cur.checked == false )
			return;
			
		if( count < 2 )
			this.old[count] = cur.value;
			
		else {
			this.old[0] = this.old[1];
			this.old[1] = cur.value;
			
			items = document.getElementsByName(this.prefix + '[]');
			for( j = 0 ; j < items.length ; j++ ){
				if( items[j].value != this.old[0] && items[j].value != this.old[1] )
					items[j].checked = false;
			}
		}
	}
}
 
var cbox = new remain_two_obj('cbox');
 
function btnDelete(board_id){
    $.ajax({
        type: "DELETE",
        url: "${path}/Assignment/boards/"+board_id,
        success: function(result){
           alert("삭제완료")
           location.reload();
        }
    })
}


function btnDiff(board_id){
	
	if(isTwoSelected()){
		  $.ajax({
		        type: "GET",
		        url: "${path}/Assignment/boards/diff",
		        data: 
		        success: function(result){
		        	location.href='/Assignment/'+result;
		           
		        }
		    })
		
	}
	else{
		alert("두개 선택하세요.");
	}
  
}

function btnVersion(){
	function_click();
}

</script>
</body>
</html>


