<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<link rel="stylesheet" type="text/css" href="${pageContext.request.contextPath}/css/context_info.css">
<head>
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
<title>Insert title here</title>
</head>
<body>
<span id="ui_modal"></span>
<div id="my_div"></div>
<script>
var fakeJson =  window.localStorage.getItem('log');
if (fakeJson == null) {
	throw "no data"
}
var prettyJson = fakeJsonnToprettyJson(fakeJson);
createHTML(prettyJson);
function fakeJsonnToprettyJson(fakeJson) {
    var list = fakeJson.split('marker');
    var prettyJson ='[';
    for (var i = 0 ; i < list.length -1 ; i ++) {
        prettyJson += list[i];
        if( i != (list.length - 2)) {
            prettyJson += ',';
        }
    }
    prettyJson += ']';
    return prettyJson;
}

function createHTML(prettyJson) {
	var obj = JSON.parse(prettyJson);
	var logNameList = window.localStorage.getItem("logNameList").split(',');
	for (var i = obj.length - 1 ; i >= 0 ; i --) {
		newDiv = document.createElement("div");
		leftHTML = '<li class="container"><dl class="full_context card">';
		var midHTML = '';
		for (var j = 0 ; j <  logNameList.length  ; j++) {
			var str = logNameList[j];
			midHTML += '<dt>'+logNameList[j] +':' + obj[i][str] + '</dt>';
		}
		var rightHTML = '</dl></li>';
		newDiv.innerHTML = leftHTML + midHTML + rightHTML;
		my_div = document.getElementById("my_div");
		my_div.parentNode.insertBefore(newDiv, my_div);
	}
}
</script>
</body>
</html>