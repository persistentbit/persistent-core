package com.persistentbit.core.exceptions;

import com.persistentbit.core.printing.PrintableText;

/**
 * TODOC
 *
 * @author petermuys
 * @since 10/01/17
 */
@FunctionalInterface
public interface ExceptionPrinter{

	PrintableText asPrintable(Throwable exception);

	default <T extends Throwable> ExceptionPrinter orIf(Class<T> cls, SpecificExceptionPrinter<T> ep) {
		return (exception) -> {
			if(cls.isAssignableFrom(exception.getClass())) {
				return ep.asPrintable((T) exception, this);
			}
			return asPrintable(exception);
		};
	}

	default void print(Throwable e) {
		System.out.println(asPrintable(e).printToString());
	}

	default ExceptionPrinter registerAsGlobalHandler() {
		Thread.setDefaultUncaughtExceptionHandler((thread, exception) -> print(exception));
		return this;
	}


}
