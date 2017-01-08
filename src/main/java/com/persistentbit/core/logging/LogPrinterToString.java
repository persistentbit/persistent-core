package com.persistentbit.core.logging;

import java.io.ByteArrayOutputStream;
import java.nio.charset.Charset;

/**
 * TODOC
 *
 * @author petermuys
 * @since 8/01/17
 */
public class LogPrinterToString extends LogPrinterBuffered{

	private LogPrinterToString(RealPrinterSupplier realPrinterSupplier,
							   Charset charset
	) {
		super(new ByteArrayOutputStream(), realPrinterSupplier, charset);
	}

	static LogPrinterToString toStringPrinter(RealPrinterSupplier realPrinterSupplier,
											  Charset charset
	) {
		return new LogPrinterToString(realPrinterSupplier, charset);
	}

	public String toString() {
		ByteArrayOutputStream bout = (ByteArrayOutputStream) realOutputStream;
		return new String(bout.toByteArray(), charset);
	}
}
