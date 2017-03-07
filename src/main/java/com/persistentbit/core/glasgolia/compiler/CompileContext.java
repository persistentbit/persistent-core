package com.persistentbit.core.glasgolia.compiler;

import com.persistentbit.core.collections.PMap;
import com.persistentbit.core.collections.PSet;
import com.persistentbit.core.glasgolia.CompileException;
import com.persistentbit.core.glasgolia.compiler.rexpr.RConst;
import com.persistentbit.core.glasgolia.compiler.rexpr.RExpr;
import com.persistentbit.core.glasgolia.compiler.rexpr.RVal;
import com.persistentbit.core.glasgolia.compiler.rexpr.RVar;
import com.persistentbit.core.printing.PrintTextWriter;
import com.persistentbit.core.printing.PrintableText;
import com.persistentbit.core.utils.StrPos;
import com.persistentbit.core.utils.UReflect;

import java.util.Optional;

/**
 * TODOC
 *
 * @author petermuys
 * @since 2/03/17
 */
public class CompileContext implements PrintableText{


	public static class ValVar{

		public final boolean isVal;
		public final String name;
		public final Class type;
		public final boolean isDeclared;
		public final int id;

		public ValVar(boolean isVal, String name, Class type, boolean isDeclared, int id) {
			this.isVal = isVal;
			this.name = name;
			this.type = type;
			this.isDeclared = isDeclared;
			this.id = id;
		}

		@Override
		public String toString() {
			return "ValVar(id:" + id + ", isVal:" + isVal + ", name:" + name + ", decl:" + isDeclared + ", type:" + UReflect
				.present(type) + ")";
		}
		public String show() {
			String type = UReflect.present(this.type);
			return (isVal ? "val " : "var ") + name + ":" + type;
		}
	}

	private static class NameContext implements PrintableText{

		private final NameContext parent;
		private final PMap<String, ValVar> lookup;
		private final JavaImports javaImports;

		public NameContext(NameContext parent, PMap<String, ValVar> lookup, JavaImports javaImports) {
			this.parent = parent;
			this.lookup = lookup;
			this.javaImports = javaImports;
		}

		@Override
		public String toString() {
			if(parent != null) {
				return "NameContext(" + parent + ", " + "thisContext:" + lookup.toString(",") + ")";
			}
			return "NameContext(" + lookup.toString(",") + ")";
		}

		@Override
		public void print(PrintTextWriter out) {
			out.println("NameContext {");
			out.indent(PrintableText.fromString(lookup.values().toString(", ")));
			if(parent != null) { out.indent(parent); }
			out.println("}");
		}

		public NameContext(NameContext parent, JavaImports javaImports) {
			this(parent, PMap.empty(), javaImports);
		}

		public NameContext addNameContext() {
			return new NameContext(this, new JavaImports());
		}

		public NameContext popNameContext() {
			PSet<ValVar> undec = findUndeclared();
			if(undec == null) {
				return parent;
			}
			NameContext res = parent;
			for(ValVar vv : undec) {
				res = res.addValVar(vv);
			}
			return res;
		}

		public NameContext addValVar(ValVar valVar) {
			return new NameContext(parent, lookup.put(valVar.name, valVar), javaImports);
		}

		public Optional<ValVar> find(String name) {
			Optional<ValVar> opt = lookup.getOpt(name);
			if(opt.isPresent()) {
				return opt;
			}
			if(parent != null) {
				return parent.find(name);
			}
			return Optional.empty();
		}

		public Optional<ValVar> findInCurrentNameContext(String name) {
			return lookup.getOpt(name);
		}

		public NameContext addImport(String packageOrClass) {
			return new NameContext(parent, lookup, javaImports.add(packageOrClass));
		}

		public Optional<Class> findJavaClass(String name) {
			Optional<Class> javaName = javaImports.getClass(name);
			if(javaName.isPresent()) {
				return javaName;
			}
			return parent == null
				? Optional.empty()
				: parent.findJavaClass(name);
		}

		public PSet<ValVar> findUndeclared() {
			return getAllValVars().filter(vv -> vv.isDeclared == false);
		}

		public PSet<ValVar> getAllValVars(){
			PSet<ValVar> res = lookup.values().pset();
			if(parent != null) {
				res = res.plusAll(parent.getAllValVars());
			}
			return res;
		}
	}

	public static class Frame implements PrintableText{

		public final Frame parent;
		public final int nextId;
		public final NameContext nameContext;

		public Frame(Frame parent, int nextId, NameContext nameContext) {
			this.parent = parent;
			this.nextId = nextId;
			this.nameContext = nameContext;
		}

		public Frame(Frame parent) {
			this.parent = parent;
			this.nextId = 0;
			this.nameContext = new NameContext(null, new JavaImports());
		}
		public PSet<ValVar> getAllValVars(){
			return this.nameContext.getAllValVars();
		}
		@Override
		public void print(PrintTextWriter out) {
			out.println("Frame {");
			out.indent(nameContext);
			if(parent != null) {
				out.indent(parent);
			}
			out.println("}");
		}

		@Override
		public String toString() {
			return printToString();
		}

		public Frame() {
			this(null);
		}

		public Frame addNameContext() {
			return new Frame(parent, nextId, nameContext.addNameContext());
		}

		public Frame popNameContext() {
			return new Frame(parent, nextId, nameContext.popNameContext());
		}

		public Frame addFrame() {
			return new Frame(this);
		}

		public Frame popFrame() {
			return parent;
		}

		public Frame addValVar(boolean isVal, String name, Class type, boolean isDeclared) {
			ValVar valVar = new ValVar(isVal, name, type, isDeclared, nextId);
			return new Frame(parent, nextId + 1, nameContext.addValVar(valVar));
		}

		public Optional<ValVar> findInCurrentFrame(String name) {
			return nameContext.find(name);
		}

		public Optional<ValVar> findInCurrentNameContext(String name) {
			return nameContext.findInCurrentNameContext(name);
		}

		public PSet<ValVar> findUndeclaredInFrame() {
			return nameContext.findUndeclared();
		}

		public Optional<ValVar> findInAllFrames(String name) {
			Optional<ValVar> res = findInCurrentFrame(name);
			if(res.isPresent()) {
				return res;
			}
			if(parent != null) {
				return parent.findInAllFrames(name);
			}
			return Optional.empty();
		}

		public Frame addImport(String packageOrClassName) {
			return new Frame(parent, nextId, nameContext.addImport(packageOrClassName));
		}

		public Optional<Class> findJavaClass(String name) {
			Optional<Class> opt = nameContext.findJavaClass(name);
			if(opt.isPresent()) {
				return opt;
			}
			return parent != null
				? parent.findJavaClass(name)
				: Optional.empty();
		}

		public int getFrameSize() {
			return nextId;
		}
	}

	private final Frame currentFrame;

	public CompileContext(Frame currentFrame) {
		this.currentFrame = currentFrame;
	}

	public CompileContext() {
		this(new Frame());
	}

	public Frame getCurrentFrame() {
		return currentFrame;
	}

	public int getFrameSize() {
		return currentFrame.getFrameSize();
	}

	public CompileContext withFrame(Frame f) {
		return new CompileContext(f);
	}

	public CompileContext addImport(String importName) {
		return withFrame(currentFrame.addImport(importName));
	}

	public CompileContext addNameContext() {
		return withFrame(currentFrame.addNameContext());
	}

	public CompileContext popNameContext() {
		return withFrame(currentFrame.popNameContext());
	}

	public CompileContext addFrame() {
		return withFrame(currentFrame.addFrame());
	}

	public CompileContext popFrame() {
		return withFrame(currentFrame.popFrame());
	}


	public CompileContext addVal(boolean isDeclared, String name, Class type) {
		return withFrame(currentFrame.addValVar(true, name, type, isDeclared));
	}

	public CompileContext addVar(boolean isDeclared, String name, Class type) {
		return withFrame(currentFrame.addValVar(false, name, type, isDeclared));
	}

	public Optional<ValVar> findInCurrentFrame(String name) {
		return currentFrame.findInCurrentFrame(name);
	}

	public Optional<ValVar> findInCurrentNameContext(String name) {
		return currentFrame.findInCurrentNameContext(name);
	}

	public PSet<ValVar> findUndeclaredInFrame() {
		return currentFrame.findUndeclaredInFrame();
	}

	public Optional<ValVar> findInAllFrames(String name) {
		return currentFrame.findInAllFrames(name);
	}

	public Optional<Class> findJavaClass(String name) {
		return currentFrame.findJavaClass(name);
	}

	public RExpr bindName(StrPos pos, RStack runtimeStack, String name) {
		Optional<ValVar> optvv = findInCurrentFrame(name);
		if(optvv.isPresent() == false) {
			Optional<Class> optClass = findJavaClass(name);
			if(optClass.isPresent()) {
				return new RConst(pos, Class.class, optClass.get());
			}
			throw new CompileException("Can't bind name " + name, pos);
		}
		ValVar vv = optvv.get();
		return vv.isVal
			? new RVal(pos, vv.type, runtimeStack, vv.id)
			: new RVar(pos, vv.type, runtimeStack, vv.id);
	}

	@Override
	public void print(PrintTextWriter out) {
		out.println("CompileContext{");
		out.indent(currentFrame);
		out.println("}");
	}

	@Override
	public String toString() {
		return printToString();
	}
}
