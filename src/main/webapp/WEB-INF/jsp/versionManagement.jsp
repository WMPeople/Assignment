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
	

    <h3>버전 관리 페이지 <button TYPE="BUTTON" style="float:right;" VALUE="HOME" ONCLICK="location.href='${path}/Assignment'">홈으로</button></h3> 
    
    <!--     DIFF 임 -->
	<form method="post" name="diffForm">
		<input name="board_id1" type="text" id="board_id1"value="0" style="display:none;">
		<input name="version1" type="text" id="version1" value="0" style="display:none;">
        <input name="board_id2" type="text" id="board_id2" value="2" style="display:none;">
        <input name="version2" type="text" id="version2" value="1" style="display:none;">
        <button class="btn btn-primary" type="button"  onclick="btnDiff();">비교</button>
	</form>

    <table class="table" id="table">
        <tr>
            <th>체크박스</th>
            <th>게시물번호</th>
            <th>버전</th>
            <th>제목</th>
            <th>날짜</th>
            <th>상태</th>
            <th>동작</th>
            
        <c:forEach var="boardHistory" items="${list}" varStatus="index">
        <tr>
            <td><input type="checkbox" name="cbox[]" 
            value="${boardHistory.board_id}-${boardHistory.version}"
            onchange="cbox.remain_two(this);"></td>
            <td>${boardHistory.board_id}</td>
            <td>${boardHistory.version}</td>
            <td><a href="${path}/Assignment/history/${boardHistory.board_id}/${boardHistory.version}">${boardHistory.history_subject}</a></td>
            <td>${boardHistory.created}</td>
            <td>${boardHistory.status}</td>
       		<td>
       		
            <c:choose>
	               <c:when test="${!index.first }"> 
	               		<button class="btn btn-primary" id="btnVersionDelete" 
			       		onclick="btnVersionDelete(${boardHistory.board_id},${boardHistory.version})">삭제</button>
	               		<button class="btn btn-primary" id="btnRecover" 
       					onclick="btnRecover(${boardHistory.board_id},${boardHistory.version})">복원</button>   
	               </c:when>
	               	<c:otherwise>
			       		<button class="btn btn-primary" id="btnDelete" 
			       		onclick="btnVersionDelete(${boardHistory.board_id},${boardHistory.version})">삭제</button>
					</c:otherwise>
			</c:choose>
       
       		 
    
       		</td>
        </tr>
        </c:forEach>
    </table>
<script>
$(document).ready(function(){
	
 });
 

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

function btnVersionDelete(board_id,version){
	  $.ajax({
	        type: "DELETE",
	        url: "${path}/Assignment/boards/version/"+board_id+"/"+version,
	        success: function(result){
	        	if(result == 'success'){
	        		alert("삭제완료");
	        		location.reload();
	        	}
	        	else{
	        		alert("삭제실패");
	        	}
	        }
	    })
}
function btnDelete(board_id,version){
	  $.ajax({
	        type: "DELETE",
	        url: "${path}/Assignment/boards/"+board_id+"/"+version,
	        success: function(result){
	        	if(result == 'success'){
	        		alert("삭제완료");
	        		location.href = "/Assignment/";
	        	}
	        	else{
	        		alert("삭제실패");
	        	}
	        }
	    })
}

function btnRecover(board_id,version){
	var tableSearch = ${"table"};
	
	var leafBoard_id = Number(tableSearch.rows[1].cells[1].innerHTML);
	var leafVersion = Number(tableSearch.rows[1].cells[2].innerHTML);
	  $.ajax({
	        type: "GET",
	        url: "${path}/Assignment/boards/recover/"+board_id+"/"+version+"/"+leafBoard_id+"/"+leafVersion,
	        success: function(result){
	        	if(result.result == 'success'){
	        		alert("복원완료");
	        		location.href='${path}/Assignment/boards/management/'+result.board_id+'/'+result.version;
	        	}
	        	else{
	        		alert("복원실패");
	        	}
	        }
	    })
}

function btnDiff(){

	var num = 0;  
 	var checkArr = [];
 
 	$(":checkbox[name='cbox[]']:checked").each(function (index){  
    	num += 1;  
   		checkArr.push($(this).val());
 	});  

	if (num==2){
		
		 var firstNode = checkArr[0].split('-');
		 var secondNode = checkArr[1].split('-');
	
	     $("#board_id1").val(Number(firstNode[0]));
	     $("#version1").val(Number(firstNode[1]));
	     
	     $("#board_id2").val(Number(secondNode[0]));
	     $("#version2").val(Number(secondNode[1]));
	}
	var fm = document.diffForm;
	fm.method='post';
	fm.action='${path}/Assignment/boards/diff';
	fm.submit();

}


    



</script>
</body>
</html>


