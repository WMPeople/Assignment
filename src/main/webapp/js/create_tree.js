function displayTree() {
	var rootBoardId = document.getElementById("root_board_id");
	var rootBoardIdVal = rootBoardId.value;
	var xhttp = new XMLHttpRequest();
	  xhttp.onreadystatechange = function() {
	    if (this.readyState == 4) {
	    	if(this.status == 200) {
		    	var jsonObj = JSON.parse(this.responseText);
		    	new Treant(jsonObj);	
	    	} else {
	    		alert("status : " + this.status);
	    	}
	    }
	  };
	  xhttp.open("GET", "api/" + rootBoardIdVal, true);
	  xhttp.send();
    //new Treant( chart_config );
}