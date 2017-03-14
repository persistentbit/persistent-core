package com.persistentbit.core.glasgolia.repl;

import com.persistentbit.core.glasgolia.gexpr.GExprParser;
import com.persistentbit.core.parser.Parser;
import com.persistentbit.core.parser.Scan;

/**
 * TODO: Add comment
 *
 * @author Peter Muys
 * @since 7/03/2017
 */
public class GGReplCmdParser {
    public GGReplCmdParser(){

    }

    private Parser<String> ws() {
        return Scan.whiteSpace;
    }


    private Parser<String> commandName(String name) {
        return Scan.term(":").skipAnd(Scan.keyword(name)).skip(ws());
    }

    private Parser<String> name() { return Scan.identifier.skip(ws()); }

    private Parser<String> term(String term) { return Scan.term(term).skip(ws());}


	private Parser<GGReplCmd> showContext() {
		return term("context")
			.map(s -> new GGReplCmd("show", "context"));

	}

	private Parser<GGReplCmd> showMembers(GExprParser exprParser) {
		return term("members").skipAnd(exprParser.parseExpr())
							  .map(s -> new GGReplCmd("show", "members", s));
	}

	private Parser<GGReplCmd> show(GExprParser exprParser) {
		return commandName("show").skipAnd(showContext().or(showMembers(exprParser)));
	}
    private Parser<GGReplCmd> exit(){
        return commandName("exit").map(s -> new GGReplCmd("exit"));
    }

    private Parser<GGReplCmd> reload(){
        return commandName("rl").or(commandName("reload")).map(s -> new GGReplCmd("reload"));
    }

	private Parser<GGReplCmd> saveSession() {
		return
			commandName("save").skipAnd(Scan.stringLiteral("\"", false).optional())
							   .map(file -> new GGReplCmd("save", file.orElse("session.glasg")));
	}

	private Parser<GGReplCmd> loadSession() {
		return
			commandName("load").skipAnd(Scan.stringLiteral("\"", false).optional())
							   .map(file -> new GGReplCmd("load", file.orElse("session.glasg")));
	}

	private Parser<GGReplCmd> resetSession() {
		return commandName("reset").map(r -> new GGReplCmd("reset"));
	}


    public Parser<GGReplCmd> command(GExprParser exprParser) {
		return show(exprParser)
			.or(exit())
			.or(reload())
			.or(saveSession())
			.or(loadSession())
			.or(resetSession());
	}
}
