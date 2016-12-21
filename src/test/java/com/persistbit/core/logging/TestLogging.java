package com.persistbit.core.logging;

import com.persistentbit.core.logging.AnsiColor;
import com.persistentbit.core.utils.IndentOutputStream;
import com.persistentbit.core.utils.IndentPrintStream;

/**
 * TODOC
 *
 * @author petermuys
 * @since 13/12/16
 */
public class TestLogging{

    public static void main(String... args) throws Exception {
        IndentPrintStream ips = new IndentPrintStream(
                new IndentOutputStream(System.out)
        );
        System.setOut(ips);
        print("14:28:32,234","StandaardWoningficheService","324","deleteHernummering","(1234)","15ms","");
        ips.indent();
        print("14:28:33,199","StandaardWoningficheService","188","updateWoningfiches","( Persoon[rrn=RRN[V 54.12.04 254-68] )","15ms","");
        ips.outdent();
        ips.indent();
        print("14:28:38,223","StandaardWoningficheService","188","updateWoningfiches","( Persoon[rrn=RRN[V 53.11.28 198-55] )","15ms","");
        ips.indent();
        print("14:28:38,225","JpaWoningficheDAO","456","getOrCreateNew","( adresVandaag )","18ms","");
        ips.indent();

        print("14:28:38,225","JpaWoningficheDAO","456","","","","Aanmaken nieuwe woningfiche voor adres (2050) Hulsen 40 , 2490 Balen");
        ips.outdent();
        ips.outdent();
        print("14:28:33,199","StandaardWoningficheService","188","updateWoningfiches","( Persoon[rrn=RRN[V 54.12.04 254-68] )","15ms","");
        ips.indent();
        ips.outdent();

        ips.outdent();

        print("15:01:22,001","StandaardWoningficheService","324","deleteHernummering","(810)","15ms","");
        ips.indent();
        print("15:01:23,021","StandaardWoningficheService","484","","","","ERRWoningfiche voor hernummering van WoningfichePO{id=5273, adres=(3100) Molsesteenweg 94 , 2490 Balen} kan niet gewist worden");

        ips.outdent();



        //INFO 14:53:20,613 StandaardWoningficheService(429)>> Adressen die gerefreshed worden: (Adres[(2050) Hulsen 51 , 2490 Balen],Adres[(2050) Hulsen 40 , 2490 Balen])
        //INFO 14:53:20,644 StandaardWoningficheService(188)>> updateWoningfiches Persoon[rrn=RRN[M 62.03.23 253-89]  naam=NaamPO[naam="Dillen", voornaam="Danny Gerard Eugenia Jozef"] id=17531]
        //INFO 14:53:20,738 StandaardWoningficheService(188)>> updateWoningfiches Persoon[rrn=RRN[M 56.05.06 277-75]  naam=NaamPO[naam="Van Roy", voornaam="Jozef Maria Victor"] id=14891]
        //INFO 14:53:20,753 JpaWoningficheDAO(456)>> Aanmaken nieuwe woningfiche voor adres (2050) Hulsen 40 , 2490 Balen



    }
    static private final void print(String time, String cls, String lineNr, String functionName, String functionParams, String duration, String msg){
        AnsiColor col = new AnsiColor(true);
        //String timeStyle = col.faint().fgBlue().toString();
        String timeStyle = col.faint().fgWhite().toString();
        //String classStyle = col.faint().fgMagenta().toString();
        String classStyle = col.faint().fgWhite().toString();
        String functionStyle = col.bold().fgYellow().toString();
        String functionParamsStyle = col.fgYellow().toString();
        //String durationStyle = col.faint().fgBlue().toString();
        String durationStyle = col.faint().fgWhite().toString();
        String msgStyle = col.bold().fgGreen().toString();

        if(msg != null && msg.startsWith("ERR")){
            msg = msg.substring(3);
            msgStyle = col.bold().fgRed().toString();
        }

        //System.out.println(timeStyle + time + " " + classStyle + cls + "(" + lineNr + ")." + functionStyle +  functionName  + functionParamsStyle + functionParams + durationStyle + "15ms" + functionStyle + "» " + msgStyle +  msg );
        System.out.println(functionStyle +  functionName  + functionParamsStyle + functionParams +  msgStyle +  msg  +  timeStyle + " … " + time + durationStyle + "(15ms)" + " "  + classStyle  +  cls + "(" + lineNr + ").");
    }
}
