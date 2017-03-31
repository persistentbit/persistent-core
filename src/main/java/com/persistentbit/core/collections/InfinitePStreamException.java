package com.persistentbit.core.collections;

/**
 *
 * @author Peter Muys
 * @since 9/07/16
 */
public class InfinitePStreamException extends UnsupportedOperationException{

  public InfinitePStreamException() {
	super("This operation is not supported on Infinite PStreams");
  }
}
