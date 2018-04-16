<%@ page language="java" contentType="text/html; charset=utf-8"
	pageEncoding="utf-8"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
<script src="//code.jquery.com/jquery-1.10.2.js"></script>
<script src="//code.jquery.com/ui/1.11.0/jquery-ui.js"></script>
<script SRC="${pageContext.request.contextPath}/js/diff_match_patch.js"></script>
<link rel="stylesheet" href="https://maxcdn.bootstrapcdn.com/bootstrap/3.3.2/css/bootstrap.min.css">
<link rel="stylesheet" href="https://maxcdn.bootstrapcdn.com/bootstrap/3.3.2/css/bootstrap-theme.min.css">
<meta http-equiv="Content-Type" content="text/html; charset=utf-8">
<title>Diff, Match and Patch: Demo of Diff</title>
</head>

<body>
	<button class="btn btn-primary" style="float:right;" onclick="location.href='${path}/assignment'">홈으로</button> 
	<h2>Demo of Diff</h2>
	<script>
	$(document).ready(function(){
		launch();
	});
	function launch(){
		var dmp = new diff_match_patch();
        var text1 = document.getElementById('text1').value;
        var text2 = document.getElementById('text2').value;
         
        dmp.Diff_Timeout = parseFloat(2);
        dmp.Diff_EditCost = parseFloat(4);
        dmp.Diff_Sensitive = 1;
            
// TODO 체크 박스 만든 후 수정 예정 
//   if(대소문자구분없을시) dmp.sensitive = 1;
//     else 대소문자구분할시 dmp.sensitive = 0;
            
        var ms_start = (new Date()).getTime();
        var d1 = dmp.diff_main(text1, text2);
        var ms_end = (new Date()).getTime();

        dmp.diff_cleanupSemantic(d1);
        var d2 = dmp.diff_main(text2, text1);
        dmp.diff_cleanupSemantic(d2);
        var ds = dmp.diff_prettyHtml(d1,d2);
        
        document.getElementById('outputdivLeft').innerHTML = ds[0]
        document.getElementById('outputdivRight').innerHTML = ds[1]
	}
		

	</script>
	<form action="#" onsubmit="return false">
		<table width="100%">
			<tr>
				<td width="50%">
					<h3>${left.board_id} - ${left.version} : </h3> <textarea id="text1" style="width: 100%"
						rows=10>${leftContent}</textarea>
				</td>
				<td width="50%">
					<h3>${right.board_id} - ${right.version} : </h3> <textarea id="text2" style="width: 100%"
						rows=10>${rightContent}</textarea>
				</td>
			</tr>
		</table>
	</form>
	<p>
	   <button class="btn btn-primary" type="button"  onclick="launch();">Compute Diff</button>
	</p>

	<div id="outputdivLeft" style="float: left; width: 50%;"></div>
	<div id="outputdivRight"style="float: right; width: 50%;"></div>
</body>
</html>
