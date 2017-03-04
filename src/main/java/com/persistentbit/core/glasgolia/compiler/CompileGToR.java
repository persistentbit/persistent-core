package com.persistentbit.core.glasgolia.compiler;

import com.persistentbit.core.collections.PList;
import com.persistentbit.core.collections.PSet;
import com.persistentbit.core.glasgolia.CompileException;
import com.persistentbit.core.glasgolia.ETypeSig;
import com.persistentbit.core.glasgolia.compiler.rexpr.*;
import com.persistentbit.core.glasgolia.gexpr.GExpr;
import com.persistentbit.core.tuples.Tuple2;
import com.persistentbit.core.utils.StrPos;
import com.persistentbit.core.utils.ToDo;
import com.persistentbit.core.utils.UNumber;
import com.persistentbit.core.utils.UReflect;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Optional;

/**
 * TODOC
 *
 * @author petermuys
 * @since 2/03/17
 */
public class CompileGToR{


	private CompileContext ctx = new CompileContext();
	private final RStack runtimeStack;

	public CompileGToR(RStack runtimeStack) {
		this.runtimeStack = runtimeStack;
	}

	public CompileGToR() {
		this(new RStack.RuntimeStack());
	}

	public RExpr compile(GExpr g) {
		return g.match(
			this::compileChildOp,
			this::exprList,
			this::compileGroup,
			this::compileConst,
			this::compileName,
			this::compileApply,
			this::compileValVar,
			this::compileBinOp,
			matchCast -> null,
			this::compileCustom,
			this::compileLambda
		);
	}


	public RExpr compileLambda(GExpr.Lambda g) {
		//System.out.println("Before Lambda: " + ctx);
		ctx = ctx.addFrame();
		//System.out.println("After add frame: " + ctx);
		//Add parameter in order to the frame
		for(GExpr.TypedName param : g.params) {
			ctx = ctx.addVar(true, param.name, getType(param.type));
		}
		RExpr code = compile(g.code);

		//System.out.println("After compile code: " + ctx);

		System.out.println(code);

		PSet<CompileContext.ValVar> undeclared = ctx.findUndeclaredInFrame();
		//undeclared.forEach(u -> ctx.addVal(false,u.name,u.type));
		//code = compile(g.code);
		int frameSize = ctx.getFrameSize();
		ctx = ctx.popFrame();

		System.out.println("Undeclared: " + undeclared.toString(", "));
		PList<Tuple2<Integer, RExpr>> initFreeList = PList.empty();
		for(CompileContext.ValVar vv : undeclared) {
			boolean isParam = g.params.find(v -> v.name.equals(vv.name)).isPresent();
			if(isParam == false) {
				initFreeList = initFreeList.plus(Tuple2.of(vv.id, ctx.bindName(g.getPos(), runtimeStack, vv.name)));
			}
		}
		return new RLambdaCreate(g.getPos(), g.params.size(), initFreeList, frameSize, code, runtimeStack);
	}


	public RExpr compileValVar(GExpr.ValVar g) {
		String name = g.name.name;
		if(ctx.findInCurrentNameContext(name).isPresent()) {
			throw new CompileException("variable name '" + g.name + "' is already declared");
		}

		ETypeSig varTypeSig = g.getType() == ETypeSig.any ? g.initial.getType() : g.getType();
		RExpr    right      = compile(g.initial);
		Class type = varTypeSig == ETypeSig.any
			? right.getType() : getType(varTypeSig);

		switch(g.valVarType) {
			case val:
				ctx = ctx.addVal(true, name, type);
				break;
			case var:
				ctx = ctx.addVar(true, name, type);
				break;
			default:
				throw new RuntimeException("Unknown type : " + g.valVarType);
		}
		RExpr left = ctx.bindName(g.getPos(), runtimeStack, name);

		RExpr res = new RAssign((RAssignable) left, right);
		return res;
	}


	private RExpr compileAssign(GExpr.BinOp g) {
		RExpr left = compile(g.left);
		if(left instanceof RAssignable == false) {
			throw new CompileException("Don't know how to assign to " + g.left, g.left.getPos());
		}
		RAssignable assignable = (RAssignable) left;
		return new RAssign(assignable, compile(g.right));
	}

	private RExpr compileName(GExpr.Name g) {
		Optional<CompileContext.ValVar> vv = ctx.findInCurrentFrame(g.name);
		if(vv.isPresent() == false) {
			if(ctx.findJavaClass(g.name).isPresent() == false) {
				vv = ctx.findInAllFrames(g.name);
				if(vv.isPresent()) {
					//Not in current frame, but in a prev frame, so declare it...
					//System.out.println(ctx);
					ctx = ctx.addVal(false, vv.get().name, vv.get().type);
					//System.out.println(ctx);
				}
			}
		}
		RExpr bind = ctx.bindName(g.getPos(), runtimeStack, g.name);
		//System.out.println(ctx);
		Class nameType = getType(g.getType());
		Class bindType = bind.getType();
		if(nameType.isAssignableFrom(bindType) == false) {
			return createCast(bind, bindType);
		}
		return bind;
	}
	public RExpr compileApply(GExpr.Apply g) {
		RExpr left = compile(g.function);
		return new RApply(g.getPos(), left, g.parameters.map(this::compile).toImmutableArray());
	}

	public RExpr compileChildOp(GExpr.Child g) {
		RExpr left = compile(g.left);
		if(left.isConst()) {
			if(left.getType() == Class.class) {
				//We have a static class child
				return getConstJavaClassChild(g.getPos(), (Class) left.get(), g.childName.name);
			}
			return getConstJavaObjectChild(g.getPos(), left.get(), g.childName.name, true);
		}
		else {
			return new RObjectChild(g.getPos(), left, g.childName.name);
		}
	}

	public static RExpr getConstJavaObjectChild(StrPos pos, Object obj, String name, boolean parentIsConst) {
		if(obj == null) {
			throw new CompileException("Can't get child '" + name + "' from a null object");
		}
		Class           cls      = obj.getClass();
		Optional<Field> optField = UReflect.getField(cls, name);
		if(optField.isPresent()) {
			Field f = optField.get();
			if(Modifier.isPublic(f.getModifiers()) && Modifier.isStatic(f.getModifiers()) == false) {
				return new RJavaField(pos, f, obj);
			}
		}

		PList<Method> methods = PList.empty();
		for(Method m : cls.getMethods()) {
			if(Modifier.isStatic(m.getModifiers()) == false && Modifier.isPublic(m.getModifiers()) && m.getName()
																									   .equals(name)) {
				methods = methods.plus(m);
			}
		}
		if(methods.isEmpty() == false) {
			return new RJavaMethods(pos, methods.toImmutableArray(), obj, parentIsConst);
		}
		throw new CompileException("Can't get child '" + name + "' from object " + obj);
	}

	public static RExpr getConstJavaClassChild(StrPos pos, Class cls, String name) {
		if(cls == null) {
			throw new CompileException("Can't get child '" + name + "' from a null class");
		}
		Optional<Field> optField = UReflect.getField(cls, name);
		if(optField.isPresent()) {
			Field f = optField.get();
			if(Modifier.isPublic(f.getModifiers()) && Modifier.isStatic(f.getModifiers())) {
				return new RJavaField(pos, f, null);
			}
		}

		PList<Method> methods = PList.empty();
		for(Method m : cls.getMethods()) {
			if(Modifier.isStatic(m.getModifiers()) && Modifier.isPublic(m.getModifiers()) && m.getName().equals(name)) {
				methods = methods.plus(m);
			}
		}
		if(methods.isEmpty() == false) {
			return new RJavaMethods(pos, methods.toImmutableArray(), null, true);
		}
		throw new CompileException("Can't get child '" + name + "' from class " + cls);
	}


	public CompileGToR addImport(String importName) {
		ctx = ctx.addImport(importName);
		return this;
	}


	private RExpr compileCustom(GExpr.Custom g) {
		switch(g.name) {
			case "import":
				return compileImport(g);
			default:
				throw new ToDo("custom " + g.name);
		}
	}

	private RExpr compileImport(GExpr.Custom g) {
		GExpr nameExpr = g.arguments.get(0);
		if(nameExpr instanceof GExpr.Const == false || getType(nameExpr.getType()) != String.class) {
			throw new CompileException("Expected a java package or class name string:" + nameExpr, nameExpr.getPos());
		}
		String name = (String) ((GExpr.Const) nameExpr).value;
		ctx = ctx.addImport(name);
		return new RConst(nameExpr.getPos(), String.class, name);
	}


	private RExpr compileGroup(GExpr.Group g) {
		//if(g.groupType == GExpr.Group.GroupType.group)
		ctx = ctx.addNameContext();
		RExpr res = compile(g.expr);
		ctx = ctx.popNameContext();
		return res;
	}

	private RExpr compileBinOp(GExpr.BinOp g) {
		if(g.op.equals("=")) {
			return compileAssign(g);
		}
		RExpr left  = compile(g.left);
		RExpr right = compile(g.right);
		if(left.getType() != String.class) {
			Tuple2<RExpr, RExpr> unified = unify(left, right).orElse(null);
			if(unified == null) {

				if(left.isConst() && right.isConst()) {
					throw new CompileException("Can't unify " + left.getType() + " with " + right
						.getType() + " for binary operator " + g.op, g.getPos());
				}
				unified = Tuple2.of(left, new RCast(right, left.getType()));
			}
			left = unified._1;
			right = unified._2;
		}


		Class leftType = left.getType();
		if(leftType == Integer.class) {
			return RInteger.createBinOp(left, g.op, right);
		}
		else if(leftType == Long.class) {
			return RLong.createBinOp(left, g.op, right);
		}
		else if(leftType == Boolean.class) {
			return RBoolean.createBinOp(left, g.op, right);
		}
		else if(leftType == String.class) {
			return RString.createBinOp(left, g.op, right);
		}
		return new RDynamicBinOp(left, g.op, right);
	}

	private RExpr createCast(RExpr r, Class castTo) {
		if(castTo == Integer.class) {
			return new RInteger.CastToInt(r);
		}
		if(castTo == Boolean.class) {
			return new RBoolean.CastToBoolean(r);
		}
		if(castTo == String.class) {
			return new RString.CastToString(r);
		}
		throw new ToDo("java object cast");
	}

	private RExpr exprList(GExpr.ExprList exprList) {
		PList<RExpr> res = PList.empty();
		for(GExpr g : exprList.expressions) {
			res = res.plus(compile(g));
		}
		return new RExprList(exprList.getPos(), res.toImmutableArray());
	}

	private RExpr compileConst(GExpr.Const g) {
		return new RConst(g.getPos(), getType(g.getType()), g.value);
	}

	private Class getType(ETypeSig typeSig) {
		return typeSig.match(
			mAny -> Object.class,
			cls -> cls.cls,
			mName -> {
				try {
					String name = mName.clsName;
					if(name.indexOf('.') < 0) {
						name = "java.lang." + name;
					}
					return Class.forName(name);
				} catch(ClassNotFoundException e) {
					throw new CompileException(e);
				}
			},
			mWG -> getType(mWG.name),
			mArr -> {throw new ToDo();},
			mFun -> getType(mFun.returnType),
			mbound -> {throw new ToDo();}
		);
	}

	private Optional<Tuple2<RExpr, RExpr>> unify(RExpr left, RExpr right) {
		if(left.isAssignableFrom(right)) {
			return Optional.of(Tuple2.of(left, right));
		}
		if(Integer.class.isAssignableFrom(left.getType())) {
			if(right.getType() == Long.class) {
				return unify(right, left)
					.map(t -> Tuple2.of(t._2, t._1));
			}
			if(UNumber.isNaturalNumberClass(right.getType())) {
				return Optional.of(Tuple2.of(left, new RInteger.CastToInt(right)));
			}
			return Optional.empty();
		}
		if(Long.class.isAssignableFrom(left.getType())) {
			if(UNumber.isNaturalNumberClass(right.getType())) {
				return Optional.of(Tuple2.of(left, new RLong.CastToLong(right)));
			}
			return Optional.empty();
		}
		return Optional.empty();
	}
}
