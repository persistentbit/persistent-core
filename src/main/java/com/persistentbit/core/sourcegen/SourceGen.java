package com.persistentbit.core.sourcegen;

import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

/**
 * TODOC
 * @author Peter Muys
 * @since 22/12/2015
 */
public class SourceGen{

  private final List<Consumer<SourceOut>> code = new ArrayList<>();


  public SourceGen add(SourceGen subSourceGen) {
	add(new CmdSubSource(subSourceGen));
	return subSourceGen;
  }


  private SourceGen add(Consumer<SourceOut> cmd) {
	code.add(cmd);
	return this;
  }

  public SourceGen bs() {
	return add(SourceOut::bs);
  }

  public SourceGen bs(Object txt) {
	return add(o -> o.print(txt).bs());
  }

  public SourceGen be() {
	return add(SourceOut::be);
  }

  public SourceGen be(String be) { return add(o -> o.be(be));}

  public SourceGen indent() {
	return add(SourceOut::indent);
  }

  public SourceGen outdent() {
	return add(SourceOut::outdent);
  }

  public String str(Object obj) {
	return "\"" + obj + "\"";
  }

  public SourceGen nl() {
	return println("");
  }

  public SourceGen println(Object obj) {
	return add(o -> o.println(obj));
  }

  public SourceGen print(Object obj) {
	return add(o -> o.print(obj));
  }

  public SourceGen subSource() {
	SourceGen sub = new SourceGen();
	add(new CmdSubSource(sub));
	return sub;
  }

  public String writeToString() {
	try(ByteArrayOutputStream bout = new ByteArrayOutputStream()) {
	  write(bout);
	  bout.flush();
	  return bout.toString();
	} catch(IOException e) {
	  throw new RuntimeException(e);
	}
  }

  public void write(OutputStream out) {
	try(Writer w = new OutputStreamWriter(out)) {
	  write(w);
	} catch(IOException e) {
	  throw new RuntimeException(e);
	}
  }

  public void write(Writer writer) {
	try(PrintWriter w = new PrintWriter(writer)) {
	  write(w);
	}
  }

  public void write(PrintWriter printWriter) {

	write(new SourceOut(printWriter));
  }

  public void write(SourceOut out) {
	for(Consumer<SourceOut> cmd : code) {
	  cmd.accept(out);
	}
  }

  private static final class CmdSubSource implements Consumer<SourceOut>{

	private final SourceGen sub;

	private CmdSubSource(SourceGen sub) {
	  this.sub = sub;
	}

	@Override
	public void accept(SourceOut out) {
	  sub.write(out);
	}

  }


}
