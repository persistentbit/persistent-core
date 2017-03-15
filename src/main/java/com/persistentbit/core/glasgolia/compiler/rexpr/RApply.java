package com.persistentbit.core.glasgolia.compiler.rexpr;

import com.persistentbit.core.collections.ImmutableArray;
import com.persistentbit.core.collections.PList;
import com.persistentbit.core.collections.PStream;
import com.persistentbit.core.glasgolia.EvalException;
import com.persistentbit.core.glasgolia.compiler.JavaExecutableFinder;
import com.persistentbit.core.tuples.Tuple2;
import com.persistentbit.core.utils.StrPos;

import java.lang.reflect.Constructor;
import java.lang.reflect.Modifier;

import static com.persistentbit.core.glasgolia.compiler.JavaExecutableFinder.tryMatch;

/**
 * TODOC
 *
 * @author petermuys
 * @since 2/03/17
 */
public class RApply implements RExpr{

	private final StrPos pos;
	private final RExpr parent;
	private final ImmutableArray<RExpr> arguments;
	static private final JavaExecutableFinder.Caster caster = JavaExecutableFinder.defaultCaster;
	public RApply(StrPos pos, RExpr parent,
				  ImmutableArray<RExpr> arguments
	) {
		this.pos = pos;
		this.parent = parent;
		this.arguments = arguments;
	}

	@Override
	public Class getType() {
		if(parent instanceof RFunction){
			RFunction fun = (RFunction) parent;
			return fun.getResultType(arguments.map(r -> r.getType()).toArray(new Class[arguments.size()]));
		}
		return Object.class;
	}

	@Override
	public StrPos getPos() {
		return pos;
	}

	@Override
	public boolean isConst() {
		return false;
	}

	@Override
	public String toString() {
		return "RApply(" + parent + "(" + arguments.toString(", ") + "))";
	}

	@Override
	public Object get() {
		Object parentValue = parent.get();
		if(parentValue == null) {
			throw new EvalException("Can't apply on null", pos);
		}
		if(parentValue instanceof RFunction) {
			return ((RFunction) parentValue).apply(arguments.map(a -> a.get()).toArray());
		}
		Class cls = parentValue.getClass();
		if(cls == Class.class) {
			//We need to construct a class
			return newObject((Class) parentValue);
		}
		throw new EvalException("Can't apply on " + parentValue, pos);
	}

	private Object invoke(Constructor c, Object... args) {
		try {
			return c.newInstance(args);
		} catch(Exception e) {
			throw new EvalException(e, pos);
		}
	}

	private boolean isPublic(Constructor c) {
		return Modifier.isPublic(c.getModifiers());
	}

	private Object newObject(Class cls) {
		Constructor[] constructors = cls.getConstructors();
		Object[]      resolvedArgs = arguments.map(e -> e.get()).toArray();
		if(arguments.size() == 0) {
			for(Constructor c : constructors) {
				if(c.getParameterCount() == arguments.size() && isPublic(c)) {
					return invoke(c, resolvedArgs);
				}
			}
		}
		if(constructors.length == 1) {
			if(isPublic(constructors[0]) == false) {
				throw new EvalException("No public constructor!", pos);
			}
			Class[]                                           paramTypes  = constructors[0].getParameterTypes();
			Tuple2<JavaExecutableFinder.MatchLevel, Object[]> matchResult =
				tryMatch(caster, paramTypes, resolvedArgs, constructors[0].isVarArgs());
			checkMatch(pos, matchResult, resolvedArgs, paramTypes);
			return invoke(constructors[0], matchResult._2);
		}
		Constructor        first          = null;
		PList<Constructor> otherPossibles = PList.empty();
		for(Constructor m : constructors) {
			if(m.getParameterCount() == arguments.size() && isPublic(m)) {
				if(first == null) {
					first = m;
				}
				else {
					otherPossibles = otherPossibles.plus(m);
				}
			}
		}
		if(first == null) {
			throw new EvalException("Can't find construct for class  '" + cls.getName() + "' with " + arguments
				.size() + " parameters", pos);
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
		Constructor partial            = null;
		Object[]    partialArgs        = null;
		boolean     hasMultiplePartial = false;

		for(Constructor m : otherPossibles) {
			if(isPublic(m) == false) {
				continue;
			}
			Tuple2<JavaExecutableFinder.MatchLevel, Object[]> matchResult =
				tryMatch(caster, m.getParameterTypes(), resolvedArgs, m.isVarArgs());
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
			throw new EvalException("Multiple overloaded constructors!", pos);
		}
		if(partial != null) {
			return invoke(partial, partialArgs);
		}
		throw new EvalException("Method arguments don't match for java method application!", pos);
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
