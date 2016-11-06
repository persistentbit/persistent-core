package com.persistentbit.core.exceptions;

/**
 * Unchecked Mapper to be used around an {@link ClassNotFoundException}
 *
 * @author petermuys
 * @since 6/11/16
 */
public class RtClassNotFoundException extends RuntimeException{

	public RtClassNotFoundException(String message, ClassNotFoundException cause) {
		super(message, cause);
	}

	public RtClassNotFoundException(ClassNotFoundException cause) {
		super(cause);
	}

	public static void map(ClassNotFoundException cause) {
		throw new RtClassNotFoundException(cause);
	}

	public static void map(String message, ClassNotFoundException cause) {
		throw new RtClassNotFoundException(message, cause);
	}

	public static <T> T tryRun(ClassNotFoundCode<T> code) {
		try {
			return code.run();
		} catch(ClassNotFoundException ex) {
			throw new RtClassNotFoundException(ex);
		}
	}

	public static void tryRun(ClassNotFoundCodeNoResult code) {
		try {
			code.run();
		} catch(ClassNotFoundException ex) {
			throw new RtClassNotFoundException(ex);
		}
	}

	@FunctionalInterface
	public interface ClassNotFoundCode<T>{

		T run() throws ClassNotFoundException;
	}

	@FunctionalInterface
	public interface ClassNotFoundCodeNoResult{

		void run() throws ClassNotFoundException;
	}

}

