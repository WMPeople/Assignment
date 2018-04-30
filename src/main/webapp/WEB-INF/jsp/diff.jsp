<%@ page language="java" contentType="text/html; charset=utf-8"
	pageEncoding="utf-8"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
<script src="//code.jquery.com/jquery-1.10.2.js"></script>
<script src="//code.jquery.com/ui/1.11.0/jquery-ui.js"></script>
<script src="${pageContext.request.contextPath}/js/diff_match_patch.js"></script>
<script src="${pageContext.request.contextPath}/js/replace_and_restore.js"></script>
<script src="${pageContext.request.contextPath}/js/diff_match_custom.js"></script>
<link rel="stylesheet"
	href="https://maxcdn.bootstrapcdn.com/bootstrap/3.3.2/css/bootstrap.min.css">
<link rel="stylesheet"
	href="https://maxcdn.bootstrapcdn.com/bootstrap/3.3.2/css/bootstrap-theme.min.css">
<link rel="stylesheet"
	href="${pageContext.request.contextPath}/css/tooltip.css">
<meta http-equiv="Content-Type" content="text/html; charset=utf-8">
<title>버전 비교</title>
</head>

<body>

	<button class="btn btn-primary" style="float: right;"
		onclick="location.href='${path}/assignment'">홈으로</button>
	<h2>버전 비교</h2>
	<span class="toolTip">
	정규식 <input type="button" value="add" onclick="add_item()"/>
		<span class="tooltiptext">정규식에 해당되는 것들은 비교에서 같은 것으로 나옵니다.(순차적으로 적용됩니다)</span>
	</span>
	<span id="pre_set" style="display:none">
		<span id="regularOptSpan">
		<label for="regularExp">정규식</label><input type="text" id="regularExp" value="&lt;br&gt;|&lt;/br&gt;"/>
			<span class="toolTip">
			<label for="regularExpOpt">옵션</label><input type="text" id="regularExpOpt" value="g"/>
				<span class="tooltiptext">옵션에는 g, i 가 올수 있습니다.</span>
			</span>
		</span>
		<input type="button" value="delete" onclick="remove_item(this)" />
		<br>
	</span>
	<br>

	<span id="regularList">
	</span>
	<br>
	<label for="caseSensitive">대소문자 구분하기</label><input type="checkbox" id="caseSensitive" checked="checked" />
	<br>
	<label for="ignoreWhiteCharCnt">무시할 공백 및 개행의 최소 개수</label><input type="number" min="0" id="ignoreWhiteCharCnt" value="1" onchange="onIgnoreWhiteSpaceChagned()"/>
	<br>
	우선순위 옵션 <span class="toolTip">
			<input type="radio" id="whiteCharPriorityOpt" name="priOpt" value="whitePri" checked="checked"/>
			<label for="whiteCharPriorityOpt">공백 및 개행 무시 우선</label>
				<span class="tooltiptext">공백 문자 -> 정규식 -> 비교 알고리즘</span>
			</span>
			<span class="toolTip">
			<input type="radio" id="regularExpPriorityOpt" name="priOpt" value="regularPri" />
			<label for="regularExpPriorityOpt">정규식 무시 우선</label>
				<span class="tooltiptext">정규식 -> 공백 문자 -> 비교 알고리즘</span>
			</span>
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
