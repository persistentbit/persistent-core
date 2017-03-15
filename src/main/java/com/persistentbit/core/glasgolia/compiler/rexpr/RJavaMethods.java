package com.persistentbit.core.glasgolia.compiler.rexpr;

import com.persistentbit.core.collections.ImmutableArray;
import com.persistentbit.core.collections.PList;
import com.persistentbit.core.collections.PStream;
import com.persistentbit.core.glasgolia.EvalException;
import com.persistentbit.core.glasgolia.compiler.JavaExecutableFinder;
import com.persistentbit.core.tuples.Tuple2;
import com.persistentbit.core.utils.StrPos;

import java.lang.reflect.Array;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Optional;

import static com.persistentbit.core.glasgolia.compiler.JavaExecutableFinder.tryMatch;

/**
 * TODOC
 *
 * @author petermuys
 * @since 2/03/17
 */
public class RJavaMethods implements RExpr, RFunction{

	private final StrPos pos;
	private final ImmutableArray<Method> methods;
	private final boolean parentIsConst;
	private final Object parentValue;

	static private final JavaExecutableFinder.Caster caster = JavaExecutableFinder.defaultCaster;

	public RJavaMethods(StrPos pos,
						ImmutableArray<Method> methods, Object parentValue, boolean parentIsConst
	) {
		this.pos = pos;
		this.methods = methods;
		methods.forEach(m -> m.setAccessible(true));
		this.parentValue = parentValue;
		this.parentIsConst = parentIsConst;
	}

	@Override
	public Class getType() {
		return RJavaMethods.class;
	}

	@Override
	public StrPos getPos() {
		return pos;
	}

	@Override
	public boolean isConst() {
		return parentIsConst;
	}

	@Override
	public Object get() {
		return this;
	}

	@Override
	public String toString() {
		return "RJavaMethods(" + methods.toString(",") + ")";
	}

	public ImmutableArray<Method> getMethods() {
		return methods;
	}


	@Override
	public Class getResultType(Class[] argTypes) {
		if(argTypes.length == 0) {
			for (Method c : methods) {
				if (c.getParameterCount() == argTypes.length && isPublic(c)) {
					return c.getReturnType();
				}
			}
		}
		if(methods.size() == 1){
			if(isPublic(methods.get(0)) == false) {
				throw new EvalException("No public method!", pos);
			}
			return methods.get(0).getReturnType();
		}
		System.out.println("todo:RJavaMethods. getResultType");
		return Object.class;
	}

	@Override
	public Object apply(Object[] resolvedArgs) {
		if(resolvedArgs.length == 0) {
			for(Method c : methods) {
				if(c.getParameterCount() == resolvedArgs.length && isPublic(c)) {
					return invoke(c, resolvedArgs);
				}
			}
		}

		if(methods.size() == 1) {
			if(isPublic(methods.get(0)) == false) {
				throw new EvalException("No public method!", pos);
			}
			Class[]                                           paramTypes  = methods.get(0).getParameterTypes();
			Tuple2<JavaExecutableFinder.MatchLevel, Object[]> matchResult =
				tryMatch(caster, paramTypes, resolvedArgs, methods.get(0).isVarArgs());
			checkMatch(pos, matchResult, resolvedArgs, paramTypes);
			return invoke(methods.get(0), matchResult._2);
		}

		Method        first          = null;
		PList<Method> otherPossibles = PList.empty();
		for(Method m : methods) {
			if((m.isVarArgs()|| m.getParameterCount() == resolvedArgs.length) && isPublic(m)) {
				if(first == null) {
					first = m;
				}
				else {
					otherPossibles = otherPossibles.plus(m);
				}
			}
		}
		if(first == null) {
			throw new EvalException("Can't find method", pos);
		}
		if(otherPossibles.isEmpty()) {
			//We have only 1 method with the same number of arguments
			Class[]                                           paramTypes  = first.getParameterTypes();
			Tuple2<JavaExecutableFinder.MatchLevel, Object[]> matchResult =
				tryMatch(caster, paramTypes, resolvedArgs, first.isVarArgs());
			checkMatch(pos, matchResult, resolvedArgs, paramTypes);
			return invoke(first, matchResult._2);
		}
		otherPossibles = otherPossibles.plus(first);

		//otherPosibles is now all methods with the same number of arguments
		//So we have to select the correct one for the argument types;

		Class[] argClasses = new Class[resolvedArgs.length];
		for(int t = 0; t < resolvedArgs.length; t++) {
			argClasses[t] = resolvedArgs[t] == null ? null : resolvedArgs[t].getClass();
		}
		Method   partial            = null;
		Object[] partialArgs        = null;
		boolean  hasMultiplePartial = false;


		for(Method m : otherPossibles) {
			if(isPublic(m) == false) {
				continue;
			}
			Tuple2<JavaExecutableFinder.MatchLevel, Object[]> matchResult =
				tryMatch(JavaExecutableFinder.defaultCaster, m.getParameterTypes(), resolvedArgs, m.isVarArgs());
			if(matchResult._1 == JavaExecutableFinder.MatchLevel.full) {
				return invoke(m, matchResult._2);
			}
			if(matchResult._1 == JavaExecutableFinder.MatchLevel.partial) {
				if(partial != null) {
					hasMultiplePartial = true;
				}
				else {
					partial = m;
					partialArgs = matchResult._2;
				}
			}

		}
		if(hasMultiplePartial) {
			throw new EvalException("Multiple overloaded methods!", pos);
		}
		if(partial != null) {
			return invoke(partial, partialArgs);
		}

		//Try match with varargs...
		for(Method m : methods){
			if(m.isVarArgs() == false){
				continue;
			}
			Class[] paramTypes = m.getParameterTypes();
			if(paramTypes.length> resolvedArgs.length){
				continue;
			}
			Tuple2<JavaExecutableFinder.MatchLevel, Object[]> matchResult =
				tryMatch(caster, paramTypes, resolvedArgs, paramTypes.length - 1);
			if(matchResult._1 == JavaExecutableFinder.MatchLevel.not) {
				continue;
			}
			//Found one...
			Class itemClass = paramTypes[paramTypes.length-1].getComponentType();
			int restLength = resolvedArgs.length-(paramTypes.length-1);
			Object[] varArgs = (Object[])Array.newInstance(itemClass, restLength);
			boolean ok= true;
			for(int t=0; t<varArgs.length; t++){
				Object           value  = resolvedArgs[paramTypes.length-1+t];
				Optional<Object> casted = caster.apply(value, itemClass);
				if(casted.isPresent() == false){
					ok = false;
					break;
				}
				varArgs[t] = casted.get();
			}
			if(ok) {
				Object[] newSet = new Object[paramTypes.length];
				System.arraycopy(matchResult._2, 0, newSet, 0, paramTypes.length - 1);
				newSet[paramTypes.length - 1] = varArgs;

				return invoke(m, newSet);
			}
		}

		throw new EvalException("Method arguments don't match for java method application!", pos);
	}

	private Object invoke(Method m, Object... args) {

		try {

			return m.invoke(parentValue, args);
		} catch(IllegalAccessException | InvocationTargetException e) {
			throw new EvalException(e, pos);
		}
	}




	private boolean isPublic(Method c) {
		return Modifier.isPublic(c.getModifiers());
	}


	private static void checkMatch(StrPos pos, Tuple2<JavaExecutableFinder.MatchLevel, Object[]> mr, Object[] arguments,
								   Class[] types
	) {
		if(mr._1 == JavaExecutableFinder.MatchLevel.not) {
			String args   = PStream.from(arguments).toString(", ");
			String params = PStream.from(types).map(v -> v.getName()).toString(", ");
			throw new EvalException("Can't match parameters (" + args + ") with (" + params + ")", pos);
		}
	}


}
