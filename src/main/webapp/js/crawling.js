$(function() {
	// 공백 무시 정규식
	// var str = str.replace(/^\s*/,'').replace(/\s*$/, '');
	var textList = $('#content3').val().split(' ');
	if (textList[0] == '책' || textList[0] == '도서') {
		var crawling_api = 'search';
		var crawling_category = 'book';
		var crawling_text = $('#content3').val().substring(
				textList[0].length + 1);
		var url = "/assignment/api/" + "naver" + "/" + crawling_api + "/"
				+ crawling_category + "/" + encodeURI(crawling_text);

		dialogFunction("naver", crawling_category, url);
	}

	if (textList[0] == '영화') {
		var crawling_api = 'search';
		var crawling_category = 'movie';
		var crawling_category_kakao = 'web';
		var crawling_text = $('#content3').val().substring(
				textList[0].length + 1);
		var url = "/assignment/api/" + "naver" + "/" + crawling_api + "/"
				+ crawling_category + "/" + encodeURI(crawling_text);

		dialogFunction("naver", crawling_category, url);
	}

	if (textList[0] == '뉴스') {
		var name = "naver";
		var crawling_api = 'search';
		var crawling_category = 'news';
		var crawling_text = $('#content3').val().substring(
				textList[0].length + 1);
		var url = "/assignment/api/" + name + "/" + crawling_api + "/"
				+ crawling_category + "/" + encodeURI(crawling_text);

		dialogFunction("naver", crawling_category, url);

	}

	if (textList[0] == '쇼핑' || textList[0] == '구매') {
		var crawling_api = 'search';
		var crawling_category = 'shop';
		var crawling_text = $('#content3').val().substring(
				textList[0].length + 1);
		var url = "/assignment/api/" + "naver" + "/" + crawling_api + "/"
				+ crawling_category + "/" + encodeURI(crawling_text);

		dialogFunction("naver", crawling_category, url);

	}
	
	//네이버 크롤링
	if (textList[0] == '지도' || textList[0] == '위치') {
		var crawling_category = 'geocode';
		var crawling_text = $('#content3').val().substring(
				textList[0].length + 1);
		var url = "/assignment/api/browser/crawling/geocode/"
				+ encodeURI(crawling_text);
		 dialogFunction("naver", crawling_category, url);

//		$.ajax({
//			type : "GET",
//			contentType : "application/json; charset=UTF-8",
//			processData : false,
//			contentType : false,
//			url : url,
//			async : false,
//			success : function(result) {
//				if (result != '') {
//					$("#" + "naver").dialog({
//						autoOpen : false,
//						open : function() {
//							$(this).load(result);
//						},
//						width : 400,
//						height : 600,
//						resizable : false,
//						draggable : true
//					});
//					$("#" + "naver").dialog("open");
//				}
//			},
//			error : function(xhr, status, error) {
//				alert(' ');
//			}
//		});
	}
	
	//네이버 크롤링
	if (textList[0] == '단어' || textList[0] == '영어단어') {
		var crawling_category = 'dictionary';
		var crawling_text = $('#content3').val().substring(
				textList[0].length + 1);
		var url = "/assignment/api/browser/crawling/dictionary/"
				+ encodeURI(crawling_text);
		 dialogFunction("naver", crawling_category, url);
	}
	
	//네이버 크롤링
	if (textList[0] == '맛집' || textList[0] == '음식점') {
		var crawling_category = 'place';
		var crawling_text = $('#content3').val().substring(
				textList[0].length + 1);
		var url = "/assignment/api/browser/crawling/place/"
				+ encodeURI(crawling_text);
		 dialogFunction("naver", crawling_category, url);
	}

});

function dialogFunction(name, crawling_category, url) {
	$("#dialog").dialog({
				open : function() {
					$(this).load(url);
					$(this).parents(".ui-dialog").find(".ui-dialog-titlebar")
							.append(name + " " + crawling_category);
				},
				width : 400,
				height : 600,
				resizable : false,
				draggable : true
			});

}
