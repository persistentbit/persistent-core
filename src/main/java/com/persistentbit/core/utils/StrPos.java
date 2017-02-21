package com.persistentbit.core.utils;

import com.persistentbit.core.parser.ParseResult;
import com.persistentbit.core.parser.Parser;
import com.persistentbit.core.parser.source.Source;

import java.util.Objects;

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
public class StrPos extends BaseValueClass implements Comparable<StrPos>{

	private final String sourceName;
	private final int lineNumber;
	private final int columnNumber;

	public StrPos(String sourceName, int lineNumber, int columnNumber) {
		this.sourceName = Objects.requireNonNull(sourceName);
		this.lineNumber = lineNumber;
		this.columnNumber = columnNumber;
	}
	public StrPos(String sourceName){
		this(sourceName,1,1);
	}
	static public final StrPos inst = new StrPos("");

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
	public StrPos incForChar(char c) {
		if(c == Source.EOF) {
			return this;
		}
		if(c == '\n') {
			return new StrPos(sourceName, lineNumber + 1, 1);
		}
		return new StrPos(sourceName, lineNumber, columnNumber + 1);
	}
	public StrPos incForString(String s){
		StrPos res = this;
		for(int t=0; t<s.length(); t++){
			res = res.incForChar(s.charAt(t));
		}
		return res;
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
		return "(" + sourceName + (sourceName.isEmpty()? "" : ": ") + (lineNumber) + ", " + columnNumber + ")";
	}

	@Override
	public int compareTo(StrPos o) {
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
