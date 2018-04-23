$(function(){
	launch();
});
function launch(){
	var dmp = new diff_match_patch();
    var text1 = document.getElementById('text1').value;
    var text2 = document.getElementById('text2').value;
     
    dmp.Diff_Timeout = parseFloat(1);
    dmp.Diff_EditCost = parseFloat(4);
    dmp.Diff_Sensitive = 0;
//    dmp.Diff_Temp = 0;
        
// TODO 체크 박스 만든 후 수정 예정 
//   if(대소문자구분없을시) dmp.sensitive = 1;
//     else 대소문자구분할시 dmp.sensitive = 0;
        
    var ms_start = (new Date()).getTime();
    var d1 = dmp.diff_main(text1, text2);
//    dmp.diff_cleanupSemantic(d1);
    var ms_end = (new Date()).getTime();

    var d2 = dmp.diff_main(text2, text1);
//    dmp.diff_cleanupSemantic(d2);
    var ds = dmp.diff_prettyHtml(d1,d2);
    
    document.getElementById('outputdivLeft').innerHTML = ds[0]
    document.getElementById('outputdivRight').innerHTML = ds[1]
}
		

