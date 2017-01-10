package com.persistentbit.core.logging;

import com.persistentbit.core.Nothing;
import com.persistentbit.core.logging.entries.LogEntry;
import com.persistentbit.core.result.Result;

import java.io.ByteArrayOutputStream;
import java.io.OutputStream;
import java.nio.charset.Charset;
import java.util.Objects;
import java.util.function.Function;

/**
 * TODOC
 *
 * @author petermuys
 * @since 6/01/17
 */
public class LogPrinterBuffered implements LogEntryPrinter{

	public interface RealPrinterSupplier extends Function<OutputStream,Function<Charset,LogEntryPrinter>> {};

	protected final OutputStream        realOutputStream;
	private final   RealPrinterSupplier realPrinterSupplier;
	protected final Charset             charset;

	protected LogPrinterBuffered(OutputStream realOutputStream,
								 RealPrinterSupplier realPrinterSupplier, Charset charset
	) {
		this.realOutputStream = Objects.requireNonNull(realOutputStream);
		this.realPrinterSupplier = Objects.requireNonNull(realPrinterSupplier);
		this.charset = Objects.requireNonNull(charset);
	}

	static Result<LogEntryPrinter> buffered(OutputStream realOutputStream,
											RealPrinterSupplier realPrinterSupplier
	){
		return Result.function().code(l -> {
			if(realOutputStream == null){
				return Result.failure("OutputStream is null");
			}
			if(realPrinterSupplier == null){
				return Result.failure("Real Printer supplier is null");
			}
			return Result.success(new LogPrinterBuffered(realOutputStream,realPrinterSupplier,Charset.forName("UTF-8")));
		});
	}


	@Override
	public void print(LogEntry entry) {
		Log.function().code(l -> {
			ByteArrayOutputStream bout = new ByteArrayOutputStream();
			realPrinterSupplier.apply(bout).apply(charset).print(entry);
			realOutputStream.write(bout.toByteArray());
			return Nothing.inst;
		});
	}

	@Override
	public void print(Throwable exception) {
		Log.function().code(l -> {
			ByteArrayOutputStream bout = new ByteArrayOutputStream();
			realPrinterSupplier.apply(bout).apply(charset).print(exception);
			realOutputStream.write(bout.toByteArray());
			return Nothing.inst;
		});
	}
}
