package com.worksmobile.assignment.crawling;

import java.util.List;

import org.snu.ids.kkma.index.Keyword;
import org.snu.ids.kkma.index.KeywordExtractor;
import org.snu.ids.kkma.index.KeywordList;
import org.snu.ids.kkma.ma.MExpression;
import org.snu.ids.kkma.ma.MorphemeAnalyzer;
import org.snu.ids.kkma.ma.Sentence;
import org.snu.ids.kkma.util.Timer;

public class KkmaTest {

	public static void main(String[] args) {
		//		maTest("읽기 어린왕자");
		maTest(
			"국어와 관련된 연구를 수행할 때, 대량의 말뭉치를 필요로 하는 경우가 종종 있다. 세종 말뭉치는 질과 양 모든 면에서 매우 우수한 말뭉치이기는 하지만, 컴퓨터 프로그래밍 능력이 없는 사람은 이를 활용하기가 어렵다. 또한, 컴퓨터 프로그래밍에 익숙하다고 하더라도, 말뭉치의 구조를 파악하고 말뭉치를 처리할 수 있는 형태로 가공하는 과정이 필요하기 때문에 말뭉치를 활용하는데 어려움이 있다.");
		//		maTest("구매 키보드");
	}

	public static void maTest(String text) {
		try {
			MorphemeAnalyzer ma = new MorphemeAnalyzer();
			ma.createLogger(null);
			Timer timer = new Timer();
			timer.start();
			List<MExpression> ret = ma.analyze(text);
			timer.stop();
			timer.printMsg("Time");

			ret = ma.postProcess(ret);

			ret = ma.leaveJustBest(ret);

			List<Sentence> stl = ma.divideToSentences(ret);
			for (int i = 0; i < stl.size(); i++) {
				Sentence st = stl.get(i);
				System.out.println("=============================================  " + st.getSentence());
				for (int j = 0; j < st.size(); j++) {
					System.out.println(st.get(j));
				}
			}

			ma.closeLogger();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public static void keTest(String text) {
		KeywordExtractor ke = new KeywordExtractor();
		KeywordList kl = ke.extractKeyword(text, true);
		for (int i = 0; i < kl.size(); i++) {
			Keyword kwrd = kl.get(i);
			System.out.println(kwrd.getString() + "\t" + kwrd.getCnt());
		}
	}

}