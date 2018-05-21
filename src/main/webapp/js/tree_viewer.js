window.onload = function () {
	treeViewer();
}

function treeViewer() {
	var rootBoardId = document.getElementById("root_board_id");
	var rootBoardIdVal = rootBoardId.value;
	if(rootBoardId == 'undefined' || rootBoardIdVal== 0) {
		return;
	}
	var loading = document.getElementById("loading");
	loading.style.display = "block";
	
	var xhttp = new XMLHttpRequest();
	  xhttp.onreadystatechange = function() {
		loading.style.display = "none";
	    if (this.readyState == 4) {
	    	if(this.status == 200) {
		    	var jsonObj = JSON.parse(this.responseText);
		    	new Treant(jsonObj);
		    	treeViewerFinished();
	    	} else {
	    		alert("status : " + this.status);
	    	}
	    }
	  };
	  xhttp.open("GET", "api/" + rootBoardIdVal, true);
	  xhttp.send();
}