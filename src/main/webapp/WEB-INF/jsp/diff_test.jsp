<%@ page language="java" contentType="text/html; charset=utf-8"
	pageEncoding="utf-8"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html><head><meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
<title>Diff, Match and Patch: Demo of Diff</title>
<script src="js/jquery-1.10.2.js"></script>
<script src="js/jquery-ui-1.11.0.js"></script>
<script src="js/diff_match_patch.js"></script>
<script src="js/replace_and_restore.js"></script>
<script src="js/diff_match_custom.js"></script>
<link rel="stylesheet" type="text/css" href="css/tooltip.css">
<body>
	<h2>Demo of Diff</h2>
	<span class="tooltip">
	정규식 <input type="button" value="add" onclick="add_item()"/>
		<span class="tooltiptext">정규식에 해당되는 것들은 비교에서 같은 것으로 나옵니다.(순차적으로 적용됩니다)</span>
	</span>
	<span id="pre_set" style="display:none">
		<span id="regularOptSpan">
		<label for="regularExp">정규식</label><input type="text" id="regularExp" value="&lt;br&gt;|&lt;/br&gt;"/>
			<span class="tooltip">
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
	우선순위 옵션 <span class="tooltip">
			<input type="radio" id="whiteCharPriorityOpt" name="priOpt" value="whitePri" checked="checked"/>
			<label for="whiteCharPriorityOpt">공백 및 개행 무시 우선</label>
				<span class="tooltiptext">공백 문자 -> 정규식 -> 비교 알고리즘</span>
			</span>
			<span class="tooltip">
			<input type="radio" id="regularExpPriorityOpt" name="priOpt" value="regularPri" />
			<label for="regularExpPriorityOpt">정규식 무시 우선</label>
				<span class="tooltiptext">정규식 -> 공백 문자 -> 비교 알고리즘</span>
			</span>
	<form action="http://localhost/assignment/boards/diff#" onsubmit="return false">
		<table width="100%">
			<tbody><tr>
				<td width="50%">
					<h3>Text Version 1:</h3> <textarea id="text1" style="width: 100%" rows="10">
ABCDEFG

&lt;br&gt;

&lt;br&gt;

&lt;br&gt;

&lt;/div&gt;

피감기관 돈 외유·후원금으로 퇴직금 지급·보좌직원과 해외출장·출장시 관광

"객관적이고 공정한 법적기준 필요…金이 다른 의원보다 도덕성 낮은가"

19·20대 국회의원 출장사례 공개…"16곳 조사하니 민주 65·한국 94차례"

"피감기관 수천 곳 조사하면 얼마나 될지 몰라…金 사례 특정인만의 문제 아냐"

이상헌 박경준 기자 = 청와대는 12일 김기식 금융감독원장의 외유성 출장 의혹을 둘러싼 각종 논란의 적법성 여부를 따지기 위해 중앙선거관리위원회에 질의 사항을 보내 공식적인 판단을 받아보기로 했다고 밝혔다. 

김의겸 청와대 대변인은 이날 춘추관 브리핑에서 "청와대는 오늘 임종석 대통령 비서실장 명의로 중앙선관위에 질의 사항을 보냈다"며 "김 원장을 둘러싼 몇 가지 법률적 쟁점에 대한 선관위의 공식적인 판단을 받아보려는 것"이라고 말했다.

중앙선관위에 보낸 질의 내용은 국회의원이 임기 말에 후원금으로 기부하거나 보좌직원들에게 퇴직금을 주는 게 적법한지 피감기관의 비용부담으로 해외출장을 가는 게 적법한지 보좌직원 또는 인턴과 함께 해외출장 가는 게 적법한지 해외출장 중 관광하는 경우가 적법한지 등 김 원장에게 제기된 4가지 사안이다.

김 대변인은 "이런 질의서를 보낸 것은 김 원장의 과거 해외출장을 평가하면서 좀 더 객관적이고 공정한 법적 기준이 필요하다고 생각해서다"라고 말했다.

그는 "물론 공직자의 자격을 따질 때 법률 잣대로만 들이댈 수는 없으며, 도덕적 기준도 적용돼야 한다는 점에서 김 원장이 티끌 하나 묻지 않았다면 좋았을 것"이라며 "그렇더라도 그의 해외출장이 일반 국회의원과 비교할 때 도덕성이 더 낮았는지 엄밀히 따질 필요가 있다"고 말했다.

그러면서 "김 원장이 문제 되는 이유는 피감기관 지원을 받아 해외출장을 다녀왔다는 것인데, 청와대는 김 원장의 경우가 어느 정도나 심각한 문제인지 알기 위해 민주당 도움을 받아 19∼20대 국회의원들의 해외출장 사례를 조사했다"고 밝혔다.
.
</textarea>
				</td>
				<td width="50%">
					<h3>Text Version 2:</h3> <textarea id="text2" style="width: 100%" rows="10">
abcdefg

&lt;div&gt;

&lt;/br&gt;

&lt;div&gt;

피감기관 돈 외유·후원금으로 퇴직금 지급·보좌직원과 해외출장·출장시 관광

"객관적이고 공정한 법적기준 필요…金이 다른 의원보다 도덕성 낮은가"

19·20대 국회의원 출장사례 공개…"16곳 조사하니 민주 65·한국 94차례"

"피감기관 수천 곳 조사하면 얼마나 될지 몰라…金 사례 특정인만의 문제 아냐"

(서울=연합뉴스) 이상헌 박경준 기자 = 청와대는 12일 김기식 금융감독원장의 외유성 출장 의혹을 둘러싼 각종 논란의 적법성 여부를 따지기 위해 중앙선거관리위원회에 질의 사항을 보내 공식적인 판단을 받아보기로 했다고 밝혔다. 

김의겸 청와대 대변인은 이날 춘추관 브리핑에서 "청와대는 오늘 임종석 대통령 비서실장 명의로 중앙선관위에 질의 사항을 보냈다"며 "김 원장을 둘러싼 몇 가지 법률적 쟁점에 대한 선관위의 공식적인 판단을 받아보려는 것"이라고 말했다.

중앙선관위에 보낸 질의 내용은 ▲ 국회의원이 임기 말에 후원금으로 기부하거나 보좌직원들에게 퇴직금을 주는 게 적법한지 ▲ 피감기관의 비용부담으로 해외출장을 가는 게 적법한지 ▲ 보좌직원 또는 인턴과 함께 해외출장 가는 게 적법한지 ▲ 해외출장 중 관광하는 경우가 적법한지 등 김 원장에게 제기된 4가지 사안이다.

김 대변인은 "이런 질의서를 보낸 것은 김 원장의 과거 해외출장을 평가하면서 좀 더 객관적이고 공정한 법적 기준이 필요하다고 생각해서다"라고 말했다.

그는 "물론 공직자의 자격을 따질 때 법률 잣대로만 들이댈 수는 없으며, 도덕적 기준도 적용돼야 한다는 점에서 김 원장이 티끌 하나 묻지 않았다면 좋았을 것"이라며 "그렇더라도 그의 해외출장이 일반 국회의원과 비교할 때 도덕성이 더 낮았는지 엄밀히 따질 필요가 있다"고 말했다.

그러면서 "김 원장이 문제 되는 이유는 피감기관 지원을 받아 해외출장을 다녀왔다는 것인데, 청와대는 김 원장의 경우가 어느 정도나 심각한 문제인지 알기 위해 민주당 도움을 받아 19∼20대 국회의원들의 해외출장 사례를 조사했다"고 밝혔다.
</textarea>
				</td>
			</tr>
		</tbody></table>
		<p>
			<input type="button" onclick="launch()" value="Compute Diff">
		</p>
	</form>

	<div id="outputdivLeft" style="float: left; width: 50%;"></div>
	<div id="outputdivRight" style="float: right; width: 50%;"></div>



</body>
</html>