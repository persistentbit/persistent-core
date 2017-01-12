package com.persistentbit.core.logging.printing;

import com.persistentbit.core.printing.PrintableText;

/**
 * TODOC
 *
 * @author petermuys
 * @since 10/01/17
 */
public final class DefaultExceptionPrinter implements SpecificExceptionPrinter<Throwable> {

	private final LogEntryDefaultFormatting format;

	public DefaultExceptionPrinter(LogEntryDefaultFormatting format) {
		this.format = format;
	}

	@Override
	public PrintableText asPrintable(Throwable exception, LogPrinter rootPrinter) {
		return out -> {

			out.println(format.msgStyleException + exception.getMessage() + format.msgStyleInfo + " " + exception
					.getClass().getName());
			out.print(PrintableText.indent(indent -> {
				for(StackTraceElement element : exception.getStackTrace()) {
					indent.println(format.classStyle + element.getClassName() + "." + element.getMethodName()
							+ "(" + element.getFileName() + ":" + element.getLineNumber() + ")"
					);
				}
				Throwable cause = exception.getCause();
				if(cause != null) {
					out.println(format.msgStyleException + "caused by...");
					indent.println(rootPrinter.printableException(cause).printToString());
				}
			}));
		};
	}

}
