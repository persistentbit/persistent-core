package com.persistentbit.core.glasgolia.compiler;

import com.persistentbit.core.collections.PList;
import com.persistentbit.core.glasgolia.CompileException;
import com.persistentbit.core.glasgolia.ETypeSig;
import com.persistentbit.core.glasgolia.compiler.rexpr.*;
import com.persistentbit.core.glasgolia.gexpr.GExpr;
import com.persistentbit.core.tuples.Tuple2;
import com.persistentbit.core.utils.NumberUtils;
import com.persistentbit.core.utils.ReflectionUtils;
import com.persistentbit.core.utils.StrPos;
import com.persistentbit.core.utils.ToDo;

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
			matchLambda -> null
		);
	}


	public RExpr compileValVar(GExpr.ValVar g) {
		throw new ToDo();
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
				return getConstJavaClassChild(g.getPos(), (Class) left.get(), g.childName);
			}
			return getConstJavaObjectChild(g.getPos(), left.get(), g.childName, true);
		}
		else {
			return new RObjectChild(g.getPos(), left, g.childName);
		}
	}

	public static RExpr getConstJavaObjectChild(StrPos pos, Object obj, String name, boolean parentIsConst) {
		if(obj == null) {
			throw new CompileException("Can't get child '" + name + "' from a null object");
		}
		Class           cls      = obj.getClass();
		Optional<Field> optField = ReflectionUtils.getField(cls, name);
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
		Optional<Field> optField = ReflectionUtils.getField(cls, name);
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

	private RExpr compileName(GExpr.Name g) {
		RExpr bind     = ctx.bindName(g.getPos(), g.name);
		Class nameType = getType(g.getType());
		Class bindType = bind.getType();
		if(nameType.isAssignableFrom(bindType) == false) {
			return createCast(bind, bindType);
		}
		return bind;
	}

	private RExpr compileGroup(GExpr.Group g) {
		//if(g.groupType == GExpr.Group.GroupType.group)
		return compile(g.expr);
	}

	private RExpr compileBinOp(GExpr.BinOp g) {
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
		throw new ToDo("java object bin op");
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
			mName -> null,
			mWG -> getType(mWG.name),
			mArr -> null,
			mFun -> getType(mFun.returnType),
			mbound -> null
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
			if(NumberUtils.isNaturalNumberClass(right.getType())) {
				return Optional.of(Tuple2.of(left, new RInteger.CastToInt(right)));
			}
			return Optional.empty();
		}
		if(Long.class.isAssignableFrom(left.getType())) {
			if(NumberUtils.isNaturalNumberClass(right.getType())) {
				return Optional.of(Tuple2.of(left, new RLong.CastToLong(right)));
			}
			return Optional.empty();
		}
		return Optional.empty();
	}
}
