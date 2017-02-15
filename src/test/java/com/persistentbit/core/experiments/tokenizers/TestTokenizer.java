package com.persistentbit.core.experiments.tokenizers;


import com.persistentbit.core.ModuleCore;
import com.persistentbit.core.Nothing;
import com.persistentbit.core.collections.PList;
import com.persistentbit.core.collections.PStream;
import com.persistentbit.core.logging.printing.LogPrint;
import com.persistentbit.core.logging.printing.LogPrintStream;
import com.persistentbit.core.result.Result;
import com.persistentbit.core.testing.TestCase;
import com.persistentbit.core.testing.TestRunner;


/**
 * TODOC
 *
 * @author petermuys
 * @since 7/01/17
 */
public class TestTokenizer {


	private static SimpleTokenizer<Integer> createTokenizer() {
		SimpleTokenizer<Integer> tokenizer = new SimpleTokenizer<>(-10)
		.add(SimpleTokenizer.regExMatcher("(\\s)+", -1).ignore())
		.add(SimpleTokenizer.stringMatcher(-2, '\"', false))
		.add(SimpleTokenizer.stringMatcher(-2, '\'', false))
		.add("/\\*.*\\*/", -9) //comment
		.add("\\(", 2) // open bracket
		.add("\\)", 3) // close bracket
		.add("[+-]", 4) // plus or minus
		.add("[*/]", 5) // multiply or divide
		.add("\\^", 6) // raised
		.add("[0-9]+", 7) // integer number
		.add(SimpleTokenizer.regExMatcher("[a-zA-Z][a-zA-Z0-9_]*", 8).map(found -> {
			switch(found.text) {
				case "sin":
				case "cos":
					return Result.success(new TokenFound<>(found.text, 1, found.ignore));
				default:
					return Result.success(found);
			}
		})); // variable
		return tokenizer;
	}

	static final TestCase testSimpleTokenizer = TestCase.name("Test SimpleTokenizer").code(tr -> {
		SimpleTokenizer<Integer> tokenizer = createTokenizer();


		testTok(tr, " ",-10);
		testTok(tr, "",-10);
		testTok(tr, "sin 1234", 1, 7,-10);
		testTok(tr, "sin", 1,-10);
		testTok(tr, "cos", 1,-10);
		testTok(tr, "1234", 7,-10);
		testTok(tr, " hallo peter + (hoe is het) ", 8, 8, 4, 2, 8, 8, 8, 3,-10);
		tr.throwsException(() -> {
			testTok(tr, "!", 0);
			return Nothing.inst;
		});

	});

	private static void testTok(TestRunner tr, String text, Integer... tokenTypes) {
		SimpleTokenizer<Integer>        tokenizer = createTokenizer();
		PList<Token<Integer>> tokens=PStream.from(tokenizer.tokenize("test",text)).plist();
		tr.info(tokens);
		tr.isTrue(tokens.lastOpt().isPresent());
		tr.isTrue(tokens.lastOpt().get().result.rightOpt().get().type == -10);
		//tr.info(tokens);
		tr.isEquals(tokens.map(t -> t.result.rightOpt().get().type), PList.val(tokenTypes));
	}

	public void testAll() {
		TestRunner.runAndPrint(logPrint, TestTokenizer.class);
	}
	static final LogPrint logPrint = LogPrintStream.sysOut(ModuleCore.createLogFormatter(true)).registerAsGlobalHandler();
	public static void main(String[] args) {

		new TestTokenizer().testAll();
	}
}
