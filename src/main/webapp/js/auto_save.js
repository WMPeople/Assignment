$(document).ready(function(){

(function () {
  var count =0; // db 접근 횟수
  var autoSave = new Object();
  (function (obj) {
    obj.configuration = {
      interval: 1 // second(s)
    };
    obj.bindTimer = function() {
      var textEle = document.querySelector('#content');
      var textVal = textEle.value;
      var ref1, ref2, ref3; // Newer -&gt; Older
      

      // Save to localStorage
      var encodedTextVal = btoa(unescape(encodeURIComponent(textVal)));
      ref1 = window.localStorage.getItem('textval-01');
      ref2 = window.localStorage.getItem('textval-02');

      if ((window.localStorage) && (encodedTextVal != ref1)){
        window.localStorage.setItem('textval-01', encodedTextVal);
        window.localStorage.setItem('textval-02', ref1);
        window.localStorage.setItem('textval-03', ref2);
        var formData = new FormData($("#fileForm")[0]);
       
        formData = new FormData($("#fileForm")[0]);
		$.ajax({
			type : "POST",
			contentType : "application/json; charset=UTF-8",
			data : formData,
			processData : false,
			contentType : false,
			url : "/assignment/boards/autosavewithoutfile",
			success : function(result) {
				if (result.result == 'success') {
					console.log("자동 저장 성공");
				} else {
					alert("자동 저장 실패");
				}
			},
			error : function(xhr, status, error) {
				alert(error);
			}
		});
		 count++;
	     console.log(count);
      }
     
      else if (!window.localStorage) {
        console.log('Error' + ': Your browser not support')
        return false;
      }
    };

    obj.start = function() {
      obj.bindTimer();
      setTimeout(function() {
        obj.start();
      }, obj.configuration.interval * 1000);
    };
    obj.start();
  })(autoSave);
})();

});