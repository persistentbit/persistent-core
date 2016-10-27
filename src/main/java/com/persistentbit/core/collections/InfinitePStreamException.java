package com.persistentbit.core.collections;

/**
 * User: petermuys
 * Date: 9/07/16
 * Time: 09:49
 */
public class InfinitePStreamException extends UnsupportedOperationException{

  public InfinitePStreamException() {
	super("This operation is not supported on Infinite PStreams");
  }
}
