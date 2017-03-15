package com.persistentbit.core.glasgolia.compiler;

import com.persistentbit.core.collections.*;
import com.persistentbit.core.glasgolia.CompileException;
import com.persistentbit.core.glasgolia.ETypeSig;
import com.persistentbit.core.glasgolia.compiler.frames.*;
import com.persistentbit.core.glasgolia.compiler.rexpr.*;
import com.persistentbit.core.glasgolia.gexpr.GExpr;
import com.persistentbit.core.glasgolia.gexpr.GExprParser;
import com.persistentbit.core.parser.ParseResult;
import com.persistentbit.core.parser.source.Source;
import com.persistentbit.core.resources.ResourceLoader;
import com.persistentbit.core.result.Result;
import com.persistentbit.core.tuples.Tuple2;
import com.persistentbit.core.utils.*;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Optional;

/**
 * TODOC
 *
 * @author petermuys
 * @since 10/03/17
 */
public class GlasgoliaCompiler{

	private ResourceLoader resourceLoader;
	private GExprParser parser;
	private final RStack runtimeStack;
	private CompileFrame ctx;


	public GlasgoliaCompiler(GExprParser parser, ResourceLoader resourceLoader, CompileFrame compileFrame,
							 RStack runtimeStack
	) {
		this.resourceLoader = resourceLoader;
		this.parser = parser;
		this.ctx = compileFrame;
		this.runtimeStack = runtimeStack;
	}

	public Result<RExpr> compileCode(String code) {
		Result<GExpr> ge =
			parser.ws.skipAnd(parser.parseExprList()).andEof().parse(Source.asSource("repl", code)).asResult();
		return ge.map(v -> compile(v));
	}

	public static GlasgoliaCompiler replCompiler(GExprParser parser, ResourceLoader resourceLoader) {
		ReplCompileFrame  compileFrame = new ReplCompileFrame();
		GlasgoliaCompiler result       =
			new GlasgoliaCompiler(parser, resourceLoader, compileFrame, compileFrame.getStack());
		result.compile(result.getModuleInit()).get();
		return result;
	}

	public static GlasgoliaCompiler replCompiler(ResourceLoader resourceLoader) {
		return replCompiler(new GExprParser(), resourceLoader);
	}

	public CompileFrame getCompileFrame() {
		return ctx;
	}

	public static GlasgoliaCompiler replCompiler() {
		return replCompiler(ResourceLoader.rootAndClassPath);
	}

	/**
	 * Add a name/value to the current compile context
	 * @param name
	 * @param value
	 * @return
	 */
	public GlasgoliaCompiler addConstValue(String name, Object value){
		StrPos pos = new StrPos("GlasgoliaCompiler.addConstValue");
		RConst constValue = new RConst(pos,value == null ?Object.class : value.getClass(),value);
		CompileFrame.NameDef nameDef    =
				new CompileFrame.NameDef(pos, name, false, GGAccess.publicAccess, constValue.getType());
		ctx.addName(nameDef);
		RExpr getter = ctx.bind(pos,name);
		RExpr assign = new RAssign(getter,constValue);
		assign.get(); //execute assign statement
		return this;
	}


	private Optional<Source> findSource(String moduleName) {
		//make name end with '.glasg'
		if(moduleName.endsWith(".glasg") == false) {
			moduleName = moduleName + ".glasg";
		}
		//Try in root
		PByteList bl = resourceLoader.apply(moduleName).orElse(null);
		if(bl == null) {
			//Try in subfolders
			moduleName = moduleName.substring(0, moduleName.length() - ".glasg".length()).replace('.', '/') + ".glasg";
			bl = resourceLoader.apply(moduleName).orElse(null);
		}
		if(bl == null) {
			return Optional.empty();
		}
		return Optional.of(Source.asSource(moduleName, bl.toText(IO.utf8)));
	}

	private PMap<String, GGModule> compiledModules = PMap.empty();
	private PSet<String> currentlyCompiling = PSet.empty();
	private GExpr moduleInit = null;

	private GExpr getModuleInit() {
		if(moduleInit == null) {
			Source source = findSource("gg.moduleInit").orElse(Source.asSource("null;"));
			moduleInit = parser.ws.skipAnd(parser.parseExprList()).parse(source).getValue();
		}
		return moduleInit;
	}
	private GGModule compileModule(String moduleName, Source source) {
		if(currentlyCompiling.contains(moduleName)) {
			throw new CompileException("Circular dependency for module '" + moduleName + "'");
		}
		if(compiledModules.containsKey(moduleName)) {
			return compiledModules.get(moduleName);
		}
		currentlyCompiling = currentlyCompiling.plus(moduleName);
		CompileFrame currentFrame = ctx;
		try {
			GGModule             mod      = new GGModule(moduleName, PMap.empty());
			GGModuleCompileFrame modFrame = new GGModuleCompileFrame(mod);
			ctx = modFrame;
			ParseResult<GExpr> parseResult = parser.ws.skipAnd(parser.parseExprList()).andEof().parse(source);
			if(parseResult.isFailure()) {
				throw parseResult.getError();
			}
			RExpr moduleInitCode = compile(getModuleInit());
			RExpr initCode       = compile(parseResult.getValue());
			modFrame
				.createModule(runtimeStack, new RExprList(StrPos.inst, ImmutableArray.val(moduleInitCode, initCode)));
			compiledModules.put(moduleName, mod);
			return mod;

		} catch(Exception e) {
			throw new CompileException("Error while compiling module '" + moduleName + "'", e);
		} finally {
			ctx = currentFrame;
			currentlyCompiling = currentlyCompiling.filter(n -> n.equals(moduleName));
		}
	}


	public RExpr compile(GExpr g) {
		try {
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
		} catch(CompileException ce) {
			throw ce;
		} catch(Exception e) {
			throw new CompileException(e, g.getPos());
		}
	}


	public RExpr compileLambda(GExpr.Lambda g) {
		CompileFrame       currentFrame = ctx;
		LambdaCompileFrame lambdaFrame  = new LambdaCompileFrame(currentFrame, runtimeStack);
		try {
			ctx = lambdaFrame;
			Class[]  paramTypes = new Class[g.params.size()];
			String[] paramNames = new String[g.params.size()];
			int      index      = 0;
			for(GExpr.TypedName param : g.params) {
				Class paramType = getType(param.pos, param.type);
				ctx.addName(new CompileFrame.NameDef(param.pos, param.name, false, GGAccess.publicAccess, paramType));
				paramNames[index] = param.name;
				paramTypes[index++] = paramType;
			}
			RExpr code = compile(g.code);
			return lambdaFrame.createLambdaCreate(g.getPos(), g.params.size(), paramNames, paramTypes, code);
		} finally {
			ctx = currentFrame;
		}

	}



	public RExpr compileValVar(GExpr.ValVar g) {
		String name = g.name.name;
		if(ctx.canDefineLocal(name) == false) {
			throw new CompileException("variable name '" + g.name + "' is already declared");
		}


		ETypeSig             varTypeSig = g.getType() == ETypeSig.any ? g.initial.getType() : g.getType();
		CompileFrame.NameDef nameDef    =
			new CompileFrame.NameDef(g.getPos(), name, false, GGAccess.publicAccess, getType(g.getPos(), varTypeSig));
		ctx.addName(nameDef);
		RExpr right = compile(g.initial);
		Class type = varTypeSig == ETypeSig.any
			? right.getType() : getType(g.getPos(), varTypeSig);

		nameDef = nameDef.withType(type);
		switch(g.valVarType) {
			case val:
				ctx.addName(nameDef.withIsVal(true));
				break;
			case var:
				ctx.addName(nameDef.withIsVal(false));
				break;
			default:
				throw new RuntimeException("Unknown type : " + g.valVarType);
		}
		RExpr left = ctx.bind(g.getPos(), name);

		RExpr res = new RAssign(left, right);
		return res;
	}


	private RExpr compileAssign(GExpr.BinOp g) {
		RExpr left = compile(g.left);
		//if(left instanceof RAssignable == false) {
		//throw new CompileException("Don't know how to assign to " + g.left, g.left.getPos());
		//}
		//RAssignable assignable = (RAssignable) left;
		return new RAssign(left, compile(g.right));
	}

	private RExpr compileName(GExpr.Name g) {

		RExpr bind = ctx.bind(g.getPos(), g.name);
		//System.out.println(ctx);
		Class nameType = getType(g.getPos(), g.getType());
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
		//Could be a sub class...
		return UReflect.getClass(cls.getName() + "$" + name)
					   .map(scls -> new RConst(pos, Class.class, scls))
					   .orElseThrow(() -> new CompileException("Can't get child '" + name + "' from class " + cls));

	}


	private RExpr compileCustom(GExpr.Custom g) {
		switch(g.name) {
			case "import":
				return compileImport(g);
			case "while":
				return compileWhile(g);
			case "if":
			case "ifElse":
				return compileIf(g);

			default:
				throw new ToDo("custom " + g.name);
		}
	}

	private RExpr compileWhile(GExpr.Custom g) {
		RExpr cond = compile((GExpr) g.arguments.get(0));
		RExpr code = compile((GExpr) g.arguments.get(1));
		return new RWhile(cond, code);
	}

	private RExpr compileIf(GExpr.Custom g) {
		RExpr cond      = compile((GExpr) g.arguments.get(0));
		RExpr trueCode  = compile((GExpr) g.arguments.get(1));
		RExpr falseCode = g.arguments.size() == 2 ? null : compile((GExpr) g.arguments.get(2));
		return new RIf(cond, trueCode, falseCode);
	}

	private RExpr compileImport(GExpr.Custom g) {
		String typeName = (String) g.arguments.get(0); //'java' or 'module'
		String name     = compile((GExpr) g.arguments.get(1)).get().toString(); //module or package name
		//String as = g.arguments.get(2) == null
		//	   ? null
		//	   : compile((GExpr)g.arguments.get(2)).get().toString();
		switch(typeName) {
			case "java":
				ctx.addImported(new ImportedJava(name));
				return new RConst(g.getPos(), Object.class, null);
			case "module": {
				Source source = findSource(name).orElse(null);
				if(source == null) {
					throw new CompileException("Can't find module '" + name + "'", g.getPos());
				}
				GGModule mod = compileModule(name, source);
				ctx.addImported(new ImportedModule(mod));
				return mod.asRExpr();
			}
			default:
				throw new CompileException("Unknown import type:'" + typeName + "'", g.getPos());
		}
	}


	private RExpr compileGroup(GExpr.Group g) {
		//if(g.groupType == GExpr.Group.GroupType.group)
		CompileFrame current = ctx;
		try {
			ctx = new BlockFrame(runtimeStack, current);
			RExpr res = compile(g.expr);
			return res;
		} finally {
			ctx = current;
		}
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
		else if(leftType == Double.class){
			return RDouble.createBinOp(left,g.op,right);
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
		if(castTo == Double.class){
			return new RDouble.CastToDouble(r);
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
		return new RConst(g.getPos(), getType(g.getPos(), g.getType()), g.value);
	}

	private Class getType(StrPos pos, ETypeSig typeSig) {
		return typeSig.match(
			mAny -> Object.class,
			cls -> cls.cls,
			mName -> {
				String name = mName.clsName;
				name = name.replace('.', '$');
				Class foundCls = ctx.getClassForTypeName(name).orElse(null);
				if(foundCls == null) {
					throw new CompileException("Can't find class '" + name + "'", pos);
				}
				return foundCls;

			},
			mWG -> getType(pos, mWG.name),
			mArr -> {throw new ToDo();},
			mFun -> getType(pos, mFun.returnType),
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
		if(Double.class.isAssignableFrom(left.getType())){
			if(UNumber.isNumberClass(right.getType())) {
				return Optional.of(Tuple2.of(left, new RDouble.CastToDouble(right)));
			}
			return Optional.empty();
		}
		return Optional.empty();
	}


	public GExprParser getParser() {
		return parser;
	}
}
