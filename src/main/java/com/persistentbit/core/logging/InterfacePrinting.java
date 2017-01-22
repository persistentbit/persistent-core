package com.persistentbit.core.logging;

import com.persistentbit.core.logging.printing.LogPrint;
import com.persistentbit.core.result.Result;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;

/**
 * TODOC
 *
 * @author petermuys
 * @since 22/01/17
 */
public class InterfacePrinting implements InvocationHandler{

	private final LogPrint printer;
	private final Object   instance;
	private final Class[]  subClasses;

	public InterfacePrinting(LogPrint printer, Object instance, Class... subClasses) {
		this.printer = printer;
		this.instance = instance;
		this.subClasses = subClasses;
	}

	@Override
	public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {

		try {
			Object result = method.invoke(instance, args);
			if(result instanceof Result) {
				Result r = (Result) result;

				printer.print(r.getLog());
				if(r.isPresent()) {
					Object value = r.orElseThrow();
					for(Class c : subClasses) {
						if(c.isAssignableFrom(value.getClass())) {
							return r.map(v -> create(printer, c, v, subClasses));
						}
					}

				}
			}
			return result;
		} catch(Exception e) {
			printer.print(e);
			throw e;
		}
	}

	@SuppressWarnings("unchecked")
	public static <T> T create(LogPrint printer, Class<T> interfaceClass, T instance, Class<?>... subClasses) {
		return (T) Proxy.newProxyInstance(
			instance.getClass().getClassLoader(),
			new Class<?>[]{interfaceClass},
			new InterfacePrinting(printer, instance, subClasses)
		);

	}
}
