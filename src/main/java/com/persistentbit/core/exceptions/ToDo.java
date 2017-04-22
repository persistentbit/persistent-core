package com.persistentbit.core.exceptions;

/**
 * @author petermuys
 * @since 26/09/16
 */
@SuppressWarnings("ExceptionClassNameDoesntEndWithException")
public class ToDo extends RuntimeException{

  public ToDo() {
	this("Not Yet Implemented");
  }

  public ToDo(String message) {
	super(message);
  }
}
