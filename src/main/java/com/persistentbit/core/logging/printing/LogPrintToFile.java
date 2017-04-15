package com.persistentbit.core.logging.printing;

import com.persistentbit.core.io.IOFiles;
import com.persistentbit.core.io.IOStreams;
import com.persistentbit.core.logging.entries.LogEntry;
import com.persistentbit.core.printing.PrintableText;

import java.io.File;
import java.io.Writer;
import java.nio.charset.Charset;
import java.util.function.Function;

/**
 * TODO: Add comment
 *
 * @author Peter Muys
 * @since 18/01/2017
 */
public class LogPrintToFile implements LogPrint{

	private final LogFormatter          logFormatter;
	private final Charset               charset;
	private final Function<Object,File> fileSupplier;

	public LogPrintToFile(Charset charset, LogFormatter logFormatter, Function<Object, File> fileSupplier) {
		this.charset = charset;
		this.logFormatter = logFormatter;
		this.fileSupplier = fileSupplier;
    }

	public static LogPrintToFile perDay(LogFormatter logFormatter, File rootPath, String appName) {
		return new LogPrintToFile(
			Charset.defaultCharset(),
			logFormatter,
			dayFileSupplier(rootPath,appName + "_",".log.txt")
        );
    }

    public static Function<Object,File> dayFileSupplier(File rootPath, String prefix, String postfix){
        return value -> IOFiles.createDayFile(rootPath,prefix,postfix)
							   .logFunction(rootPath,prefix,postfix)
							   .orElseThrow();
    }


    @Override
    public void print(LogEntry logEntry) {
		doPrint(logEntry, logFormatter.printableLog(logEntry));
	}

    @Override
    public void print(Throwable exception) {
		doPrint(exception, logFormatter.printableException(exception));
	}

    private File            currentFile;
    private Writer currentWriter;
    private synchronized void doPrint(Object value, PrintableText printableText){
        try {
            File f = fileSupplier.apply(value);
            if (f.equals(currentFile) == false) {
                if (currentFile != null) {
                    currentWriter.close();
                    currentWriter = null;
                }
                currentFile = null;

                currentWriter = IOStreams.fileToWriter(f, charset,true).orElseThrow();
                currentFile = f;
            }
            if(currentWriter != null) {
                currentWriter.write(printableText.printToString());
                currentWriter.flush();
            }
        }catch(Throwable t){
			System.err.println(logFormatter.printableException(t));
		}
    }
}