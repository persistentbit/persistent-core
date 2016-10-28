package com.persistentbit.core.exceptions;

import java.io.IOException;

/**
 * Unchecked Mapper to be used around an IO Exception
 *
 * @author petermuys
 * @since 28/10/16
 */
public class RtIOException extends RuntimeException{

  public RtIOException(String message, IOException cause) {
	super(message, cause);
  }

  public RtIOException(IOException cause) {
	super(cause);
  }

  public static void map(IOException  cause){
	throw new RtIOException(cause);
  }
  public static void map(String message, IOException  cause){
	throw new RtIOException(message, cause);
  }

  @FunctionalInterface
  public interface IOExceptionCode<T>{
		T run() throws IOException;
  }
  @FunctionalInterface
  public interface IOExceptionCodeNoResult{
	void run() throws IOException;
  }
  public static <T> T tryRun(IOExceptionCode<T> code){
	try{
	  return code.run();
	}catch(IOException io){
	  throw new RtIOException(io);
	}
  }
  public static void tryRun(IOExceptionCodeNoResult code){
	try{
	  code.run();
	}catch(IOException io){
	  throw new RtIOException(io);
	}
  }

}
