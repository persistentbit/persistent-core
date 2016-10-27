package com.persistentbit.core.tokenizer;

/**
 * Exception class used by {@link SimpleTokenizer}
 *
 * @author Peter Muys
 */
public class TokenizerException extends RuntimeException{

  private final Pos pos;

  public TokenizerException(Pos pos, String message) {
	super(asMessage(pos, message));
	this.pos = pos;
  }

  private static String asMessage(Pos pos, String msg) {
	return "Tokenizer error in '" + pos.name + "' at " + pos + ": " + msg;
  }

  public TokenizerException(Pos pos, Throwable cause) {
	this(pos, asMessage(pos, cause.getMessage()), cause);
  }

  public TokenizerException(Pos pos, String message, Throwable cause) {
	super(asMessage(pos, message), cause);
	this.pos = pos;
  }

  public Pos getPos() {
	return pos;
  }
}
