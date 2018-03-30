<%@ page language="java" contentType="text/html; charset=utf-8"
	pageEncoding="utf-8"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=utf-8>
<title>Insert title here</title>
</head>
<body>
	<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<HTML>
<HEAD>
<TITLE>Diff, Match and Patch: Demo of Diff</TITLE>
<script src="//code.jquery.com/jquery-1.10.2.js"></script>
<script src="//code.jquery.com/ui/1.11.0/jquery-ui.js"></script>
<SCRIPT SRC="${pageContext.request.contextPath}/js/diff_match_patch.js"></SCRIPT>
</HEAD>

<BODY>
	<H1>Diff,</H1> 
	<H2>Demo of Diff</H2>

	<P>Diff takes two texts and finds the differences. This
		implementation works on a character by character basis. The result of
		any diff may contain 'chaff', irrelevant small commonalities which
		complicate the output. A post-diff cleanup algorithm factors out these
		trivial commonalities.  ${list}  </P>

	<SCRIPT>
		var dmp = new diff_match_patch();

		function launch() {
			var text1 = document.getElementById('text1').value;
			var text2 = document.getElementById('text2').value;
			
			dmp.Diff_Timeout = parseFloat(2);
			dmp.Diff_EditCost = parseFloat(4);
			dmp.Diff_Sensitive = 0;
			
			//체크 박스 만든 후 수정 예정 
// 			if(대소문자구분없을시) dmp.sensitive = 1;
// 			else 대소문자구분할시 dmp.sensitive = 0;
			
			var ms_start = (new Date()).getTime();
			var d = dmp.diff_main(text1, text2);
			var ms_end = (new Date()).getTime();

			dmp.diff_cleanupSemantic(d);

			var ds = dmp.diff_prettyHtml(d);
			//console.log(ds);
			document.getElementById('outputdiv').innerHTML = ds + '<BR>Time: '
					+ (ms_end - ms_start) / 1000 + 's';
			

			

		}
	</SCRIPT>

	<FORM action="#" onsubmit="return false">
		<TABLE WIDTH="100%">
			<TR>
				<TD WIDTH="50%">
					<H3>Text Version 1:</H3> <TEXTAREA ID="text1" STYLE="width: 100%"
						ROWS=10>I am the very model of a modern Major-General,
I've information vegetable, animal, and mineral,
I know the kings of England, and I quote the fights historical,
From Marathon to Waterloo, in order categorical.</TEXTAREA>
				</TD>
				<TD WIDTH="50%">
					<H3>Text Version 2:</H3> <TEXTAREA ID="text2" STYLE="width: 100%"
						ROWS=10>I am the very model of a cartoon individual,
My animation's comical, unusual, and whimsical,
I'm quite adept at funny gags, comedic theory I have read,
From wicked puns and stupid jokes to anvils that drop on your head.</TEXTAREA>
				</TD>
			</TR>
		</TABLE>

		<P>
			<INPUT TYPE="button" onClick="launch()" VALUE="Compute Diff">
		</P>
	</FORM>

	<DIV ID="outputdiv"></DIV>
</BODY>
</HTML>

</body>
</html>