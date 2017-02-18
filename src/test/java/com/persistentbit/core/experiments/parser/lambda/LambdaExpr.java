package com.persistentbit.core.experiments.parser.lambda;

/**
 * TODOC
 *
 * @author petermuys
 * @since 18/02/17
 */
public abstract class LambdaExpr{
	private LambdaExpr(){}

	static public class Var extends LambdaExpr{
		public final String name;

		public Var(String name) {
			this.name = name;
		}

		@Override
		public String toString() {
			return name;
		}
	}
	static public class Apply extends LambdaExpr{
		public final LambdaExpr function;
		public final LambdaExpr argument;

		public Apply(LambdaExpr function, LambdaExpr argument) {
			this.function = function;
			this.argument = argument;
		}

		@Override
		public String toString() {
			return "(" + function + " " + argument + ")";
		}
	}

	static public class Lambda extends LambdaExpr{
		public final String name;
		public final LambdaExpr body;

		public Lambda(String name, LambdaExpr body) {
			this.name = name;
			this.body = body;
		}

		@Override
		public String toString() {
			return "λ" + name + "." + body;
		}
	}

	static public class Define extends LambdaExpr{
		public final String name;
		public final LambdaExpr definition;

		public Define(String name, LambdaExpr definition) {
			this.name = name;
			this.definition = definition;
		}

		@Override
		public String toString() {
			return "def " + name + " = " + definition;
		}
	}

}
