$(function() {
	function guid() {
		  function s4() {
		    return Math.floor((1 + Math.random()) * 0x10000)
		      .toString(16)
		      .substring(1);
		  }
		  return s4() + s4() + '-' + s4() + '-' + s4() + '-' + s4() + '-' + s4() + s4() + s4();
	}
	var lines = $('#content3').val().split("\n");
	var firstLine = lines[0];
	var textList = firstLine.split(' ');
	
	if (textList[0] == '책' || textList[0] == '도서') {
		var crawling_api = 'search';
		var crawling_category = 'book';
		var crawling_text = '';
		for(var i = 1 ; i < textList.length ; i ++) {
			crawling_text = textList[i];
		}
		var url = "/assignment/api/naver/" + crawling_api + "/"
				+ crawling_category + "/" + encodeURI(crawling_text);

		dialogFunction("naver", crawling_category, url);
	}

	if (textList[0] == '영화') {
		var crawling_api = 'search';
		var crawling_category = 'movie';
		var crawling_category_kakao = 'web';
		var crawling_text = '';
		for(var i = 1 ; i < textList.length ; i ++) {
			crawling_text = textList[i];
		}
		var url = "/assignment/api/naver/" + crawling_api + "/"
				+ crawling_category + "/" + encodeURI(crawling_text);

		dialogFunction("naver", crawling_category, url);
	}

	if (textList[0] == '뉴스') {
		var crawling_api = 'search';
		var crawling_category = 'news';
		var crawling_text = '';
		for(var i = 1 ; i < textList.length ; i ++) {
			crawling_text = textList[i];
		}
		var url = "/assignment/api/naver/" + crawling_api + "/"
				+ crawling_category + "/" + encodeURI(crawling_text);

		dialogFunction("naver", crawling_category, url);

	}

	if (textList[0] == '쇼핑' || textList[0] == '구매') {
		var crawling_api = 'search';
		var crawling_category = 'shop';
		var crawling_text = '';
		for(var i = 1 ; i < textList.length ; i ++) {
			crawling_text = textList[i];
		}
		var url = "/assignment/api/naver/" + crawling_api + "/"
				+ crawling_category + "/" + encodeURI(crawling_text);

		dialogFunction("naver", crawling_category, url);

	}
	
	//네이버 크롤링
	if (textList[0] == '지도' || textList[0] == '위치') {
		var crawling_category = 'geocode';
		var crawling_text = '';
		for(var i = 1 ; i < textList.length ; i ++) {
			crawling_text = textList[i];
		}
		var url = "/assignment/api/browser/crawling/geocode/"
				+ encodeURI(crawling_text);
		 dialogFunction("naver", crawling_category, url);
	}
	
	//네이버 크롤링
	if (textList[0] == '단어' || textList[0] == '영어단어') {
		var crawling_category = 'dictionary';
		var crawling_text = '';
		for(var i = 1 ; i < textList.length ; i ++) {
			crawling_text = textList[i];
		}
		var url = "/assignment/api/browser/crawling/dictionary/"
				+ encodeURI(crawling_text);
		 dialogFunction("naver", crawling_category, url);
	}
	
	//네이버 크롤링
	if (textList[0] == '맛집' || textList[0] == '음식점') {
		var crawling_category = 'place';
		var crawling_text = '';
		for(var i = 1 ; i < textList.length ; i ++) {
			crawling_text = textList[i];
		}
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
