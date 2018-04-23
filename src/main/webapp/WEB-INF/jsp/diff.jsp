<%@ page language="java" contentType="text/html; charset=utf-8"
	pageEncoding="utf-8"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
<script src="//code.jquery.com/jquery-1.10.2.js"></script>
<script src="//code.jquery.com/ui/1.11.0/jquery-ui.js"></script>
<script src="${pageContext.request.contextPath}/js/diff_match_patch.js"></script>
<script src="${pageContext.request.contextPath}/js/diff_match_custom.js"></script>
<link rel="stylesheet"
	href="https://maxcdn.bootstrapcdn.com/bootstrap/3.3.2/css/bootstrap.min.css">
<link rel="stylesheet"
	href="https://maxcdn.bootstrapcdn.com/bootstrap/3.3.2/css/bootstrap-theme.min.css">
<meta http-equiv="Content-Type" content="text/html; charset=utf-8">
<title>버전 비교</title>
</head>

<body>

	<button class="btn btn-primary" style="float: right;"
		onclick="location.href='${path}/assignment'">홈으로</button>
	<h2>버전 비교</h2>
	<br> 아래의 정규식에 대항되는 것들은 비교에서 같은 것으로 취급합니다.
	<br> 적용 순위는 개행, 공백 -> 정규식 규칙 -> 대소문자 구분
	<br> 정규식 안에서의 대소문자 구분은 옵션에 i를 줌으로써 가능합니다.
	<br> 
	정규식 규칙 :
	<input type="checkbox" id="regularExpChkBox" />
	<input type="text" id="regularExp" value="&lt;br&gt;|&lt;/br&gt;" />
	옵션:
	<input type="text" id="regularExpOpt" value="g" /> 
	대소문자 구분
	<input type="checkbox" id="caseSensitive" checked="checked" /> 
	공백 및 개행 무시
	<input type="number" id="ignoreWhiteCharCnt" value="1" />
	<br>
        우선순위 옵션 <input type="radio" id="whiteCharPriorityOpt" name="priOpt" value="whitePri" checked="checked"/> <label for="whiteCharPriorityOpt">공백 및 개행 무시 우선</label>
            <input type="radio" id="regularExpPriorityOpt" name="priOpt" value="regularPri" /> <label for="regularExpPriorityOpt">정규식 무시 우선</label>
	<form action="#" onsubmit="return false">
		<table width="100%">
			<tr>
				<td width="50%">
					<h3>게시글 : ${left.board_id} - ${left.version}</h3> <textarea
						id="text1" style="width: 100%" rows=10>${leftContent}</textarea>
				</td>
				<td width="50%">
					<h3>게시글 : ${right.board_id} - ${right.version}</h3> <textarea
						id="text2" style="width: 100%" rows=10>${rightContent}</textarea>
				</td>
			</tr>
		</table>
	</form>
	<p>
		<button class="btn btn-primary" type="button" onclick="launch();">Compute
			Diff</button>
	</p>

	<div id="outputdivLeft" style="float: left; width: 50%;"></div>
	<div id="outputdivRight" style="float: right; width: 50%;"></div>
</body>
</html>
