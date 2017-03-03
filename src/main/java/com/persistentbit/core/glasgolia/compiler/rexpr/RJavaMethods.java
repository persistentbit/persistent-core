package com.persistentbit.core.glasgolia.compiler.rexpr;

import com.persistentbit.core.collections.ImmutableArray;
import com.persistentbit.core.collections.PList;
import com.persistentbit.core.collections.PStream;
import com.persistentbit.core.glasgolia.EvalException;
import com.persistentbit.core.glasgolia.compiler.JavaObjectMatcher;
import com.persistentbit.core.tuples.Tuple2;
import com.persistentbit.core.utils.StrPos;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;

import static com.persistentbit.core.glasgolia.compiler.JavaObjectMatcher.tryMatch;

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

	public RJavaMethods(StrPos pos,
						ImmutableArray<Method> methods, Object parentValue, boolean parentIsConst
	) {
		this.pos = pos;
		this.methods = methods;
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
				throw new EvalException("No public constructor!", pos);
			}
			Class[]                                        paramTypes  = methods.get(0).getParameterTypes();
			Tuple2<JavaObjectMatcher.MatchLevel, Object[]> matchResult = tryMatch(paramTypes, resolvedArgs);
			checkMatch(pos, matchResult, resolvedArgs, paramTypes);
			return invoke(methods.get(0), matchResult._2);
		}

		Method        first          = null;
		PList<Method> otherPossibles = PList.empty();
		for(Method m : methods) {
			if(m.getParameterCount() == resolvedArgs.length && isPublic(m)) {
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
			Class[]                                        paramTypes  = first.getParameterTypes();
			Tuple2<JavaObjectMatcher.MatchLevel, Object[]> matchResult = tryMatch(paramTypes, resolvedArgs);
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
			Tuple2<JavaObjectMatcher.MatchLevel, Object[]> matchResult = tryMatch(m.getParameterTypes(), resolvedArgs);
			if(matchResult._1 == JavaObjectMatcher.MatchLevel.full) {
				return invoke(m, matchResult._2);
			}
			if(matchResult._1 == JavaObjectMatcher.MatchLevel.partial) {
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
		throw new EvalException("Method arguments don't match for java method application!", pos);
	}

	private Object invoke(Method m, Object... args) {
		try {
			return m.invoke(parentValue, args);
		} catch(IllegalAccessException | InvocationTargetException e) {
			throw new EvalException(e, pos);
		}
	}


	private Object invoke(Constructor c, Object... args) {
		try {
			return c.newInstance(args);
		} catch(Exception e) {
			throw new EvalException(e, pos);
		}
	}

	private boolean isPublic(Method c) {
		return Modifier.isPublic(c.getModifiers());
	}


	private static void checkMatch(StrPos pos, Tuple2<JavaObjectMatcher.MatchLevel, Object[]> mr, Object[] arguments,
								   Class[] types
	) {
		if(mr._1 == JavaObjectMatcher.MatchLevel.not) {
			String args   = PStream.from(arguments).toString(", ");
			String params = PStream.from(types).map(v -> v.getName()).toString(", ");
			throw new EvalException("Can't match parameters (" + args + ") with (" + params + ")", pos);
		}
	}


}
