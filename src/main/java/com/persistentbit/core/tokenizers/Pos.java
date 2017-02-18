package com.persistentbit.core.tokenizers;

import com.persistentbit.core.utils.BaseValueClass;

/**
 * Marks the position of a token in a source file.
 *
 * @author Peter Muys
 * @see SimpleTokenizer
 * @see Token
 */
public class Pos extends BaseValueClass{

	public final String name;
	public final int    lineNumber;
	public final int    column;

	public Pos(String name, int lineNumber, int column) {
		this.name = name;
		this.lineNumber = lineNumber;
		this.column = column;
	}

	public String getName() {
		return name;
	}

	public int getLineNumber() {
		return lineNumber;
	}

	public int getColumn() {
		return column;
	}

	public Pos withLineNumber(int lineNumber) {
		return new Pos(name, lineNumber, column);
	}

	public Pos withColumn(int column) {
		return new Pos(name, lineNumber, column);
	}

	@Override
	public String toString() {
		return "source '" + name + "' line " + lineNumber + " column " + column;
	}
}
