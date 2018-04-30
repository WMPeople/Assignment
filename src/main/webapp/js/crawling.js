$(function() {
	// 공백 무시 정규식
	// var str = str.replace(/^\s*/,'').replace(/\s*$/, '');
	var textList = $('#content3').val().split(' ');
	if (textList[0] == '책') {
		var crawling_api = 'search';
		var crawling_category = 'book';
		var crawling_text = textList[1].replace(/^\s*/,'').replace(/\s*$/, '');
//		dialogFunction("naver",crawling_api,crawling_category,crawling_text);
		dialogFunction("kakao",crawling_api,crawling_category,crawling_text);
	}

	if (textList[0] == '영화') {
		var crawling_api = 'search';
		var crawling_category = 'movie';
		var crawling_category_kakao = 'web';
		var crawling_text = textList[1].replace(/^\s*/,'').replace(/\s*$/, '');
//		dialogFunction("naver",crawling_api,crawling_category,crawling_text);
		dialogFunction("kakao",crawling_api,crawling_category_kakao,crawling_text);
	}

	if (textList[0] == '뉴스') {
		var crawling_api = 'search';
		var crawling_category = 'news';
		var crawling_text = textList[1].replace(/^\s*/,'').replace(/\s*$/, '');
	}

// TODO 수정요망
//	$("#kakao").dialog({
//		open:function(){
//			$(this).load("/assignment/api/kakao/"+crawling_category+"/"+crawling_text);
//			$(this).parents(".ui-dialog").find(".ui-dialog-titlebar").remove();
//		}
//	
//	})
//	
//	$("#browser").dialog({
//		open:function(){
//			$(this).load("/assignment/api/browser/crawling");
//			$(this).parents(".ui-dialog").find(".ui-dialog-titlebar").remove();
//		}
//	
//	})


});

function dialogFunction(name,crawling_api,crawling_category,crawling_text){
	$("#"+name).dialog({
		open:function(){
			$(this).load("/assignment/api/"+name+"/"+crawling_api+"/"+crawling_category+"/"+crawling_text);
		},
		width : 400,
		height : 600,
		resizable : false,
		draggable : true
	})
	
}
