package com.persistentbit.core.sourcegen;

import java.io.PrintWriter;

/**
 * TODOC
 * @author Peter Muys
 * @since 22/12/2015
 */
public class SourceOut{

  private final PrintWriter writerOut;
  private final boolean     blockOpenSameLine;
  private String indent = "";
  private boolean needIndentPrint;

  public SourceOut(PrintWriter out) {
	this.writerOut = out;
	this.blockOpenSameLine = true;
  }

  public SourceOut indent() {
	indent += "\t";
	return this;
  }

  public SourceOut outdent() {
	indent = indent.substring(1);
	return this;
  }

  public SourceOut bs() {
	if(blockOpenSameLine) {
	  println(" {");
	}
	else {
	  println();
	  println("{");
	}
	indent += "\t";
	return this;
  }

  public SourceOut println(Object obj) {
	print(obj);
	return println();
  }

  public SourceOut println() {
	try {
	  writerOut.println();
	  needIndentPrint = true;
	  return this;
	} catch(Exception e) {
	  throw new RuntimeException(e);
	}
  }

  public SourceOut print(Object obj) {
	try {
	  if(needIndentPrint) {
		writerOut.print(indent);
		needIndentPrint = false;
	  }
	  if(obj == null) {
		obj = "null";
	  }
	  writerOut.print(obj);
	  return this;
	} catch(Exception e) {
	  throw new RuntimeException(e);
	}
  }

  public SourceOut be() {
	return be("}");
  }

  public SourceOut be(String be) {
	indent = indent.substring(1);
	return println(be);
  }

}