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
<SCRIPT SRC="js/diff_match_patch.js"></SCRIPT>
</HEAD>

<BODY>
	<H1>Diff, Match and Patch</H1>
	<H2>Demo of Diff</H2>

	<P>Diff takes two texts and finds the differences. This
		implementation works on a character by character basis. The result of
		any diff may contain 'chaff', irrelevant small commonalities which
		complicate the output. A post-diff cleanup algorithm factors out these
		trivial commonalities.</P>

	<SCRIPT>
		var dmp = new diff_match_patch();

		function launch() {
			var text1 = document.getElementById('text1').value;
			var text2 = document.getElementById('text2').value;
			dmp.Diff_Timeout = parseFloat(document.getElementById('timeout').value);
			dmp.Diff_EditCost = parseFloat(document.getElementById('editcost').value);

			var ms_start = (new Date()).getTime();
			var d = dmp.diff_main(text1, text2);
			var ms_end = (new Date()).getTime();

			if (document.getElementById('semantic').checked) {
				dmp.diff_cleanupSemantic(d);
			}
			if (document.getElementById('efficiency').checked) {
				dmp.diff_cleanupEfficiency(d);
			}
			var ds = dmp.diff_prettyHtml(d);
			document.getElementById('outputdiv').innerHTML = ds + '<BR>Time: '
					+ (ms_end - ms_start) / 1000 + 's';
		}
	</SCRIPT>

	<FORM action="#" onsubmit="return false">
		<TABLE WIDTH="100%">
			<TR>
				<TD WIDTH="50%">
					<H3>Text Version 1:</H3> <TEXTAREA ID="text1" STYLE="width: 100%"
						ROWS=10>${history.firstContent}</TEXTAREA>
				</TD>
				<TD WIDTH="50%">
					<H3>Text Version 2:</H3> <TEXTAREA ID="text2" STYLE="width: 100%"
						ROWS=10>${history.secondContent}</TEXTAREA>
				</TD>
			</TR>
		</TABLE>

		<H3>Diff timeout:</H3>
		<P>
			<INPUT TYPE="text" SIZE=3 MAXLENGTH=5 VALUE="1" ID="timeout">
			seconds<BR> If the mapping phase of the diff computation takes
			longer than this, then the computation is truncated and the best
			solution to date is returned. While guaranteed to be correct, it may
			not be optimal. A timeout of '0' allows for unlimited computation.
		</P>

		<H3>Post-diff cleanup:</H3>
		<DL>
			<DT>
				<INPUT TYPE="radio" NAME="cleanup" ID="semantic" CHECKED> <LABEL
					FOR="semantic">Semantic Cleanup</LABEL>
			</DT>
			<DD>Increase human readability by factoring out commonalities
				which are likely to be coincidental.</DD>
			<DT>
				<INPUT TYPE="radio" NAME="cleanup" ID="efficiency"> <LABEL
					FOR="efficiency">Efficiency Cleanup</LABEL>, edit cost: <INPUT
					TYPE="text" SIZE=3 MAXLENGTH=5 VALUE="4" ID="editcost">
			<DD>Increase computational efficiency by factoring out short
				commonalities which are not worth the overhead. The larger the edit
				cost, the more agressive the cleanup.</DD>
			<DT>
				<INPUT TYPE="radio" NAME="cleanup" ID="raw"> <LABEL
					FOR="raw">No Cleanup</LABEL>
			</DT>
			<DD>Raw output.</DD>
		</DL>

		<P>
			<INPUT TYPE="button" onClick="launch()" VALUE="Compute Diff">
		</P>
	</FORM>

	<DIV ID="outputdiv"></DIV>

	<HR>
	Back to
	<A HREF="https://github.com/google/diff-match-patch">Diff, Match
		and Patch</A>

</BODY>
</HTML>

</body>
</html>