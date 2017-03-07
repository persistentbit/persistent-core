package com.persistentbit.core.glasgolia.repl;

import com.persistentbit.core.ModuleCore;
import com.persistentbit.core.collections.PList;
import com.persistentbit.core.glasgolia.Glasgolia;
import com.persistentbit.core.glasgolia.compiler.CompileContext;
import com.persistentbit.core.glasgolia.compiler.CompileGToR;
import com.persistentbit.core.logging.printing.LogPrint;
import com.persistentbit.core.logging.printing.LogPrintStream;
import com.persistentbit.core.parser.ParseExceptionEOF;
import com.persistentbit.core.parser.ParseResult;
import com.persistentbit.core.parser.source.Source;
import com.persistentbit.core.result.Result;

import java.io.BufferedReader;
import java.io.InputStreamReader;

/**
 * TODO: Add comment
 *
 * @author Peter Muys
 * @since 7/03/2017
 */
public class GGRepl {

    public static final LogPrint lp =
            LogPrintStream.sysOut(ModuleCore.createLogFormatter(true)).registerAsGlobalHandler();

    private Glasgolia gg = new Glasgolia();
    private GGReplCmdParser cmdParser = new GGReplCmdParser();

    private PList<String> history = PList.empty();

    public String read(String existingCode, BufferedReader in) throws Exception {
        String code = existingCode;
        while(true) {
            if(code.isEmpty() == false) {
                System.out.print(".. ");
            }
            else {
                System.out.print(">> ");
            }

            System.out.flush();
            String line = in.readLine();
            if(line == null) {
                break;
            }
            code = code + line;
            if(true) break;
            //if(line.trim().isEmpty()) {
            //	break;
            //}
            //code += UString.NL + line;
        }
        return code;
    }

    public void repl(){
        while(true){
            try{
                doRepl();
            }catch (ReloadException reload){
                System.out.println("RELOADING");
                gg = gg.restart();
                for(String eval : history){
                    System.out.println(">> " + eval);
                    System.out.println(gg.eval("repl",eval));
                }
                System.out.println("Done reloading");
            }catch (Exception e){
                lp.print(e);
            }
        }

    }

    private void doRepl() throws Exception {
        BufferedReader bin  = new BufferedReader(new InputStreamReader(System.in));
        String         code = "";
        while(true) {
            code = read(code, bin);

            if(code.startsWith(":")){
                ParseResult<GGReplCmd> cmdResult = cmdParser.command().parse(Source.asSource(code));
                if(cmdResult.isFailure()){
                    throw cmdResult.getError();
                }
                execCmd(cmdResult.getValue());

            } else {
                Result<Object> evalResult = gg.eval("repl", code);
                if (evalResult.isError()) {
                    Throwable error = evalResult.getEmptyOrFailureException().orElse(null);
                    if (error instanceof ParseExceptionEOF && code.endsWith("\n") == false) {
                        code = code + "\n";
                        continue; //try next line
                    }
                    lp.print(evalResult.getEmptyOrFailureException().get());
                } else {
                    history = history.plus(code);
                    System.out.println("Success:" + evalResult.orElse(null));
                }
            }
            code = "";
            System.out.flush();
        }
    }

    private void execCmd(GGReplCmd cmd){
        switch (cmd.name){
            case "exit": System.exit(0);return;
            case "show": showCmd(cmd);return ;
            case "reload": reloadCmd(cmd); return ;
            default:
                System.out.println("Unknown command:" + cmd.name);
        }
    }
    class ReloadException extends RuntimeException{

    }
    private void reloadCmd(GGReplCmd cmd){
        throw new ReloadException();
    }

    private void showCmd(GGReplCmd cmd) {
        CompileGToR compiler = gg.getCompiler();
        CompileContext ctx = compiler.getContext();
        PList<CompileContext.ValVar> all = ctx.getCurrentFrame().getAllValVars().plist();
        all.forEach(vv -> {
            System.out.println(vv.show() + " = " + gg.eval("repl.show",vv.name).orElse("?"));
        });
    }



    public GGRepl loadAndEval(String sourceName) {
        gg.loadAndEval(sourceName).throwOnError().orElse(null);
        return this;
    }


    public GGRepl() {
        loadAndEval("repl.gg");
    }

    public static void main(String[] args) throws Exception {
        GGRepl repl = new GGRepl();

        repl.repl();

    }
}
