package com.persistentbit.core.exceptions;

import java.sql.SQLException;

/**
 * Unchecked Mapper to be used around an {@link SQLException}
 *
 * @author petermuys
 * @since 1/11/16
 */
@Deprecated
public class RtSqlException extends RuntimeException{

	public RtSqlException(String message, SQLException cause) {
		super(message, cause);
	}

	public RtSqlException(SQLException cause) {
		super(cause);
	}

	public static void map(SQLException cause) {
		throw new RtSqlException(cause);
	}

	public static void map(String message, SQLException cause) {
		throw new RtSqlException(message, cause);
	}

	public static <T> T tryRun(SqlExceptionCode<T> code) {
		try {
			return code.run();
		} catch(SQLException io) {
			throw new RtSqlException(io);
		}
	}

	public static void tryRun(SqlExceptionCodeNoResult code) {
		try {
			code.run();
		} catch(SQLException io) {
			throw new RtSqlException(io);
		}
	}

	@FunctionalInterface
	public interface SqlExceptionCode<T>{

		T run() throws SQLException;
	}

	@FunctionalInterface
	public interface SqlExceptionCodeNoResult{

		void run() throws SQLException;
	}

}
