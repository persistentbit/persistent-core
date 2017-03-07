package com.persistentbit.core.glasgolia.repl;

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

    private Parser<GGReplCmd> show() {
        return commandName("show").skipAnd(term("context"))
                .map(s -> new GGReplCmd("show","context"));
    }
    private Parser<GGReplCmd> exit(){
        return commandName("exit").map(s -> new GGReplCmd("exit"));
    }

    private Parser<GGReplCmd> reload(){
        return commandName("rl").or(commandName("reload")).map(s -> new GGReplCmd("reload"));
    }



    public Parser<GGReplCmd> command() {
        return show().or(exit()).or(reload());
    }
}
