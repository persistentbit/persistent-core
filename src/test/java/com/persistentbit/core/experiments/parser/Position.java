package com.persistentbit.core.experiments.parser;

import com.persistentbit.core.utils.BaseValueClass;

/**
 * TODOC
 *
 * @author petermuys
 * @since 17/02/17
 */
public class Position extends BaseValueClass{
	private final String sourceName;
	private final int lineNumber;
	private final int columnNumber;

	public Position(String sourceName, int lineNumber, int columnNumber) {
		this.sourceName = sourceName;
		this.lineNumber = lineNumber;
		this.columnNumber = columnNumber;
	}

	public Position	incForChar(int c){
		if(c == '\n'){
			return new Position(sourceName,lineNumber+1, 1);
		}
		return new Position(sourceName,lineNumber,columnNumber+1);
	}

	@Override
	public String toString() {
		return "(" + sourceName + ": " + (lineNumber) + ", " + columnNumber + ")";
	}
}
