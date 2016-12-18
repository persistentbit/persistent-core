package com.persistentbit.core.logging;

/**
 * TODOC
 *
 * @author petermuys
 * @since 16/12/16
 */
public interface LogEntryVisitor{

	void visit(FunctionStartLogEntry le);

	void visit(FunctionEndLogEntry le);

	void visit(FunctionThrowsLogEntry le);

	void visit(MessageLogEntry le);
}
