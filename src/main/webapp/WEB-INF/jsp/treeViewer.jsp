<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
    <head>
    <meta charset="utf-8">
    <meta http-equiv="X-UA-Compatible" content="IE=edge,chrome=1">
    <meta name="viewport" content="width=device-width">
    <title> Basic example </title>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/Treant.css">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/basic-example.css">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/loading.css">
	<link rel="stylesheet" type="text/css" href="${pageContext.request.contextPath}/css/context_info.css">
    <style>
    body {
    font-family: "Lato", sans-serif;
}

.sidenav {
    height: 100%;
    width: 0;
    position: fixed;
    z-index: 1;
    top: 0;
    right: 0;
    overflow-x: hidden;
    transition: 0.5s;
    padding-top: 60px;
    border: 1px solid;
}

.sidenav a {
    padding: 8px 8px 8px 32px;
    text-decoration: none;
    font-size: 25px;
    color: #818181;
    display: block;
    transition: 0.3s;
}

.sidenav a:hover {
    color: #f1f1f1;
}

.sidenav .closebtn {
    position: absolute;
    top: 0;
    right: 25px;
    font-size: 36px;
    margin-left: 50px;
}

#main {
    transition: margin-right .5s;
    padding: 16px;
}

@media screen and (max-height: 450px) {
  .sidenav {padding-top: 15px;}
  .sidenav a {font-size: 18px;}
}

    </style>
</head>
<script>
function openNav() {
    document.getElementById("mySidenav").style.width = "250px";
    document.getElementById("main").style.marginRight = "250px";
}

function closeNav() {
    document.getElementById("mySidenav").style.width = "0";
    document.getElementById("main").style.marginRight= "0";
}

function makeHTML(className, innerHTML) {
	var rtnHTML = "<div class = " + className;
	rtnHTML += ">";
	rtnHTML += innerHTML;
	rtnHTML += "</div>";
	
	return rtnHTML;
}

function makeCardElement(jsonObj) {
	var rtnHTML = "<div class='card'>";
	var nameArr = ["nodePtr", "subject", "created", "cookieId", "fileId"];
	for(var i = 0; i < nameArr.length; i++) {
		var context = jsonObj[nameArr[i]].toString();
		var contextHTML = makeHTML(nameArr[i], context);
		rtnHTML += contextHTML;
		rtnHTML += "\n";
	}
	
	rtnHTML += "</div>";
	return rtnHTML;
}

function makeCardView(jsonObj) {
	var elements = jsonObj["elements"];
	var rtnHTML = "<div class='cardContainer'>";
	
	for(var i = 0; i < elements.length; i++) {
		var jsonEle = elements[i];
		rtnHTML += makeCardElement(jsonEle);
	}
	rtnHTML += "</div>";
	
	return rtnHTML;
}
var isLoading = false;
var lastMouseOver;

function onNodeMouseOver(event) {
	if(lastMouseOver && (Date.now() - lastMouseOver) / 1000 < 0.2) {
		return;
	}
	var nodePtrStr = this.children[0].innerText;
	var navPtrStr = document.getElementsByClassName("nodePtr");
	if(navPtrStr && navPtrStr[0] && nodePtrStr == navPtrStr[0].innerText) {
		return;
	}
	
	var nodePtr = nodePtrStr.split('-');
	var boardId = nodePtr[0];
	var version = nodePtr[1];
	if(version.indexOf('(leaf)') != -1) {
		version = version.substring(0, version.indexOf('('));
	}
	
	var xhttp = new XMLHttpRequest();
	xhttp.onreadystatechange = function() {
	  if (this.readyState == 4) {
	  	if(this.status == 200) {
		    var jsonObj = JSON.parse(this.responseText);
		    document.getElementById("navContent").innerHTML = makeCardView(jsonObj);
		    openNav();
	  	} else {
	  		alert("status : " + this.status);
	  	}
	  }
	  isLoading = false;
	};
	xhttp.open("GET", "../api/autoSave/" + boardId + "/" + version, true);
	xhttp.send();
	isLoading = true;
	lastMouseOver = Date.now();
}

function treeViewerFinished() {
	var nodes = document.getElementsByClassName("node");
	var delay = function (elem, callback) {
		var timeout = null;
		var setTimeoutFunc = function() {
			timeout = setTimeout(callback.bind(this), 500);
		};
		var clearTimeoutFunc = function() {
			clearTimeout(timeout);
		}
		elem.addEventListener("mouseover", setTimeoutFunc);
		elem.addEventListener("mouseleave", clearTimeoutFunc);
		
		return [setTimeoutFunc, clearTimeoutFunc];
	};
	for(var i = 0; i < nodes.length; i++) {
		var event = delay(nodes[i], onNodeMouseOver);
}
}
</script>
<body>
	<div id="mySidenav" class="sidenav">
	  <a href="javascript:void(0)" class="closebtn" onclick="closeNav()">&times;</a>
	  <div id="navContent">
	  </div>
	</div>
	<div id="main">
		<img src="${pageContext.request.contextPath}/img/ajax-loader.gif" id="loading" />
		<div class="chart" id="basic-example"></div>
		<script src="${pageContext.request.contextPath}/js/vendor/raphael.js"></script>
		<script src="${pageContext.request.contextPath}/js/Treant.js"></script>
		<script src="${pageContext.request.contextPath}/js/tree_viewer.js"></script>
		<form action="javascript:treeViewer()">
			<label for="root_board_id">루트 게시글 번호 : </label><input type="number" min="0" id="root_board_id" value="${rootBoardId}"/>
			<input type="submit" value="draw" />
		</form>
	</div>
</body>
</html>

