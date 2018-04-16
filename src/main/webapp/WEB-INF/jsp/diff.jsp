<%@ page language="java" contentType="text/html; charset=utf-8"
	pageEncoding="utf-8"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=utf-8>
<title>Insert title here</title>
</head>
<body>
	<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<HTML>
<HEAD>
<TITLE>Diff, Match and Patch: Demo of Diff</TITLE>
<script src="//code.jquery.com/jquery-1.10.2.js"></script>
<script src="//code.jquery.com/ui/1.11.0/jquery-ui.js"></script>
<script SRC="${pageContext.request.contextPath}/js/diff_match_patch.js"></script>
<link rel="stylesheet" href="https://maxcdn.bootstrapcdn.com/bootstrap/3.3.2/css/bootstrap.min.css">
<link rel="stylesheet" href="https://maxcdn.bootstrapcdn.com/bootstrap/3.3.2/css/bootstrap-theme.min.css">
</HEAD>

<BODY>
	<button class="btn btn-primary" style="float:right;" onclick="location.href='${path}/assignment'">홈으로</button> 
	<H2>Demo of Diff</H2>
	<SCRIPT>
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
		

	</SCRIPT>
	<FORM action="#" onsubmit="return false">
		<TABLE WIDTH="100%">
			<TR>
				<TD WIDTH="50%">
					<H3>Text Version 1:</H3> <TEXTAREA ID="text1" STYLE="width: 100%"
						ROWS=10>${leftContent}</TEXTAREA>
				</TD>
				<TD WIDTH="50%">
					<H3>Text Version 2:</H3> <TEXTAREA ID="text2" STYLE="width: 100%"
						ROWS=10>${rightContent}</TEXTAREA>
				</TD>
			</TR>
		</TABLE>
	</FORM>
	<p>
	   <button class="btn btn-primary" type="button"  onclick="launch();">Compute Diff</button>
	</p>

	<DIV ID="outputdivLeft" style="float: left; width: 50%;"></DIV>
	<DIV ID="outputdivRight"style="float: right; width: 50%;"></DIV>
</BODY>
</HTML>

</body>
</html>