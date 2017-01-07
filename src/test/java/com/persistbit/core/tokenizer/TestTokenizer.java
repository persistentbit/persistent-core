package com.persistbit.core.tokenizer;

import com.persistentbit.core.Nothing;
import com.persistentbit.core.collections.PList;
import com.persistentbit.core.logging.LogPrinter;
import com.persistentbit.core.result.Result;
import com.persistentbit.core.testing.TestCase;
import com.persistentbit.core.testing.TestRunner;
import com.persistentbit.core.tokenizer.SimpleTokenizer;
import com.persistentbit.core.tokenizer.Token;
import com.persistentbit.core.tokenizer.TokenFound;

/**
 * TODOC
 *
 * @author petermuys
 * @since 7/01/17
 */
public class TestTokenizer{


	private static SimpleTokenizer<Integer> createTokenizer() {
		SimpleTokenizer<Integer> tokenizer = new SimpleTokenizer<>();
		tokenizer.add(SimpleTokenizer.regExMatcher("(\\s)+", -1).ignore());
		tokenizer.add(SimpleTokenizer.stringMatcher(-2, '\"', false));
		tokenizer.add(SimpleTokenizer.stringMatcher(-2, '\'', false));
		tokenizer.add("/\\*.*\\*/", -9); //comment
		tokenizer.add("\\(", 2); // open bracket
		tokenizer.add("\\)", 3); // close bracket
		tokenizer.add("[+-]", 4); // plus or minus
		tokenizer.add("[*/]", 5); // multiply or divide
		tokenizer.add("\\^", 6); // raised
		tokenizer.add("[0-9]+", 7); // integer number
		tokenizer.add(SimpleTokenizer.regExMatcher("[a-zA-Z][a-zA-Z0-9_]*", 8).map(found -> {
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


		testTok(tr, "sin 1234", 1, 7);
		testTok(tr, "");
		testTok(tr, "sin", 1);
		testTok(tr, "cos", 1);
		testTok(tr, "1234", 7);
		tr.throwsException(() -> {
			testTok(tr, "!", 0);
			return Nothing.inst;
		});

	});

	private static void testTok(TestRunner tr, String text, Integer... tokenTypes) {
		SimpleTokenizer<Integer> tokenizer = createTokenizer();
		PList<Token<Integer>>    tokens    =
			Result.fromSequence(tokenizer.tokenize("test", text)).orElseThrow().plist();
		tr.info(tokens);
		tr.isEquals(tokens.map(t -> t.type), PList.val(tokenTypes));
	}

	public void testAll() {
		TestRunner.runAndPrint(TestTokenizer.class);
	}

	public static void main(String[] args) {
		LogPrinter.consoleInColor().registerAsGlobalHandler();
		new TestTokenizer().testAll();
	}
}
