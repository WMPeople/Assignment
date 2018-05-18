function showLog(){
	$("#logDialog").dialog({
		open : function() {
			$(this).load('api/log');
		},
		width : 400,
		height : 600,
		resizable : false,
		draggable : true
	});
}

function deleteLog(){
	$('.ui.small.basic.modal').modal({
	    onApprove : function() {
	    	window.localStorage.setItem('log','');
	    },
	    closable : false
	  }).modal('show');
}