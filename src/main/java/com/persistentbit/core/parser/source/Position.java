package com.persistentbit.core.parser.source;

import com.persistentbit.core.parser.ParseResult;
import com.persistentbit.core.parser.Parser;
import com.persistentbit.core.utils.BaseValueClass;

/**
 * Represents an immutable position in a parser Source.<br>
 * The position is defined by the source name, the line number (starting from 1) and the column number (starting from 1).<br>
 *
 * @author petermuys
 * @see Source
 * @see ParseResult
 * @see Parser
 * @since 17/02/17
 */
public class Position extends BaseValueClass implements Comparable<Position>{

	private final String sourceName;
	private final int lineNumber;
	private final int columnNumber;

	public Position(String sourceName, int lineNumber, int columnNumber) {
		this.sourceName = sourceName;
		this.lineNumber = lineNumber;
		this.columnNumber = columnNumber;
	}

	/**
	 * Create a new Position from this position by advancing line/column according to the character.<br>
	 * When the character is a new-line: move to the next line first column.<br>
	 * When the character is an eof character: do nothing.<br>
	 * For all other characters: go to the next column.
	 *
	 * @param c the character
	 *
	 * @return The new position
	 */
	public Position incForChar(char c) {
		if(c == Source.EOF) {
			return this;
		}
		if(c == '\n') {
			return new Position(sourceName, lineNumber + 1, 1);
		}
		return new Position(sourceName, lineNumber, columnNumber + 1);
	}

	public String getSourceName() {
		return sourceName;
	}

	public int getLineNumber() {
		return lineNumber;
	}

	public int getColumnNumber() {
		return columnNumber;
	}

	@Override
	public String toString() {
		return "(" + sourceName + ": " + (lineNumber) + ", " + columnNumber + ")";
	}

	@Override
	public int compareTo(Position o) {
		int c = sourceName.compareTo(o.getSourceName());
		if(c != 0) {
			return c;
		}
		c = Integer.compare(lineNumber, o.getLineNumber());
		if(c != 0) {
			return c;
		}
		return Integer.compare(columnNumber, o.getColumnNumber());
	}
}
