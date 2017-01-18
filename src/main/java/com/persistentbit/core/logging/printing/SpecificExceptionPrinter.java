package com.persistentbit.core.logging.printing;

import com.persistentbit.core.printing.PrintableText;

/**
 * TODOC
 *
 * @author petermuys
 * @since 10/01/17
 */
@FunctionalInterface
public interface SpecificExceptionPrinter<E extends Throwable>{


	PrintableText asPrintable(E exception, LogFormatter rootPrinter);


	default <T extends E> SpecificExceptionPrinter<E> orIf(Class<T> cls, SpecificExceptionPrinter<T> ep) {
		return (exception, rootPrinter) -> {
			if(cls.isAssignableFrom(exception.getClass())) {
				return ep.asPrintable((T) exception, rootPrinter);
			}
			return asPrintable(exception, rootPrinter);
		};
	}


}
