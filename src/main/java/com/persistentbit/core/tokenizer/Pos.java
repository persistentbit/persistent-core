package com.persistentbit.core.tokenizer;

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

  @Override
  public String toString() {
	return "source '" + name + "' line " + lineNumber + " column " + column;
  }
}
