package com.persistentbit.core.easyscript;

import com.persistentbit.core.collections.PList;
import com.persistentbit.core.utils.StrPos;
import com.persistentbit.core.utils.UReflect;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Optional;

/**
 * TODO: Add comment
 *
 * @author Peter Muys
 * @since 23/02/2017
 */
public class ERuntimeChild {

	public static EEvalResult evalGetChild(Object parent, String name, StrPos pos, EvalContext context) {
		if(parent == null){
			throw new EvalException(pos, "Can't get child '" + name + "' from a null object");
		}
		if(parent instanceof Integer) {
			return getIntegerChild(context, pos, (Integer) parent, name);
		}
		else if(parent instanceof String) {
			return getStringChild(context, pos, (String) parent, name);
		}
		else if(parent instanceof Boolean) {
			return getBooleanChild(context, pos, (Boolean) parent, name);
		} else {
			return getJavaObjectChild(context, pos, parent, name);
		}
	}

	public static EEvalResult getJavaObjectChild(EvalContext context, StrPos pos, Object parent, String name) {
		Class cls = null;
		if(parent instanceof Class){
			cls = (Class)parent;
			return getJavaClassChild(context,pos,cls,name);
		}
		cls = parent.getClass();

		PList<Method> methods = PList.empty();
		for(Method m : cls.getMethods()) {
			if(m.getName().equals(name) && Modifier.isPublic(m.getModifiers()) && Modifier.isStatic(m.getModifiers())==false) {
				methods = methods.plus(m);
			}
		}
		if(methods.isEmpty() == false) {
			return new EEvalResult(context, new EJavaObjectMethod(pos, parent, methods));
		}

		Optional<EEvalResult> optGetter1 = tryGetter(context, pos, parent, name);
		if(optGetter1.isPresent()) return optGetter1.get();
		try {
			Field f = cls.getField(name);
			return new EEvalResult(context, f.get(parent));
		} catch(NoSuchFieldException | IllegalAccessException e) {
			throw new EvalException(pos, "Don't know how to get child '" + name + "' from " + parent);
		}


	}

	private static Optional<EEvalResult> tryGetter(EvalContext context, StrPos pos, Object parent, String name) {
		Class cls;
		if(parent instanceof Class){
			cls = (Class)parent;
			parent = null;
		} else {
			cls = parent.getClass();
		}
		Optional<Method> optGetter = UReflect.getGetter(cls, name);
		if(optGetter.isPresent()) {
			try {
				return Optional.of(new EEvalResult(context, optGetter.get().invoke(parent)));
			} catch(IllegalAccessException | InvocationTargetException e) {
				throw new EvalException(pos, "Exception while getting child '" + name + "' from " + parent, e);
			}
		}
		return Optional.empty();
	}

	public static EEvalResult getJavaClassChild(EvalContext context, StrPos pos, Class cls, String name) {


		PList<Method> methods = PList.empty();
		for(Method m : cls.getMethods()) {
			if(m.getName().equals(name) && Modifier.isPublic(m.getModifiers()) && Modifier.isStatic(m.getModifiers())) {
				methods = methods.plus(m);
			}
		}
		if(methods.isEmpty() == false) {
			return new EEvalResult(context, new EJavaObjectMethod(pos, null, methods));
		}
		Optional<EEvalResult> optGetter1 = tryGetter(context, pos, cls, name);
		if(optGetter1.isPresent()) return optGetter1.get();
		try {
			Field f = cls.getField(name);
			return new EEvalResult(context, f.get(null));
		} catch(NoSuchFieldException | IllegalAccessException e) {
			throw new EvalException(pos, "Don't know how to get child '" + name + "' from " + cls.getName());
		}
	}

	public static <T> T cast(StrPos pos, Object value, Class<T> cls) {
		if(value == null) {
			return null;
		}
		if(cls.isAssignableFrom(value.getClass())) {
			return (T) value;
		}
		throw new EvalException(pos, "Can't convert " + value + " to " + cls.getName());
	}

	public static EEvalResult getIntegerChild(EvalContext context, StrPos pos, Integer value, String name) {
		ECallable callable = null;

		switch(name) {
			case "+":
				callable = args -> value + cast(pos, args[0], Number.class).intValue();
				break;
			case "-":
				callable = args -> value - cast(pos, args[0], Number.class).intValue();
				break;
			case "*":
				callable = args -> value * cast(pos, args[0], Number.class).intValue();
				break;
			case "/":
				callable = args -> value / cast(pos, args[0], Number.class).intValue();
				break;
			case "==":
				callable = args -> value == cast(pos, args[0], Number.class).intValue() ;
				break;
			case "!=":
				callable = args -> value != cast(pos, args[0], Number.class).intValue();
				break;
			case "<=":
				callable = args -> value <= cast(pos, args[0], Number.class).intValue();
				break;
			case ">=":
				callable = args -> value >= cast(pos, args[0], Number.class).intValue();
				break;
			case ">":
				callable = args -> value > cast(pos, args[0], Number.class).intValue();
				break;
			case "<":
				callable = args -> value < cast(pos, args[0], Number.class).intValue();
				break;
			case "%":
				callable = args -> value % cast(pos, args[0], Number.class).intValue();
				break;
			case ">>":
				callable = args -> value >> cast(pos, args[0], Number.class).intValue();
				break;
			case "<<":
				callable = args -> value >> cast(pos, args[0], Number.class).intValue();
				break;
			case "|":
				callable = args -> value | cast(pos, args[0], Number.class).intValue();
				break;
			case "&":
				callable = args -> value & cast(pos, args[0], Number.class).intValue();
				break;
		}
		if(callable == null) {
			return getJavaObjectChild(context, pos, value, name);
		}
		return new EEvalResult(context, callable);
	}

	public static EEvalResult getStringChild(EvalContext context, StrPos pos, String value, String name) {
		ECallable callable = null;
		switch(name) {
			case "+":
				callable = args -> value + args[0];
				break;
			case "==":
				callable = args -> value.equals(args[0]) ;
				break;
			case "!=":
				callable = args -> value.equals(args[0]) == false;
				break;
			case "<=":
				callable = args -> value.compareTo("" + args[0]) <= 0;
				break;
			case ">=":
				callable = args -> value.compareTo("" + args[0]) >= 0;
				break;
			case ">":
				callable = args -> value.compareTo("" + args[0]) > 0;
				break;
			case "<":
				callable = args -> value.compareTo("" + args[0]) < 0;
				break;

		}
		if(callable == null) {
			return getJavaObjectChild(context, pos, value, name);
		}
		return new EEvalResult(context, callable);
	}

	public static EEvalResult getBooleanChild(EvalContext context, StrPos pos, Boolean value, String name) {
		ECallable callable = null;
		switch(name) {
			case "==":
				callable = args -> value == cast(pos, args[0], Boolean.class).booleanValue() ;
				break;
			case "!=":
				callable = args -> value != cast(pos, args[0], Boolean.class).booleanValue();
				break;
			case "&&":
				callable = args -> value && cast(pos, args[0], Boolean.class);
				break;

			case "||":
				callable = args -> value || cast(pos, args[0], Boolean.class);
				break;

		}
		if(callable == null) {
			return getJavaObjectChild(context, pos, value, name);
		}
		return new EEvalResult(context, callable);
	}
}
