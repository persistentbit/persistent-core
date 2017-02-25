package com.persistentbit.core.easyscript;

import com.persistentbit.core.collections.PList;
import com.persistentbit.core.utils.ImTools;
import com.persistentbit.core.utils.StrPos;

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
            return EEvalResult.failure(context,pos,"Can't get child '" + name + "' from a null object");
        }
		if(parent instanceof Integer) {
			return getIntegerChild(context, pos, (Integer) parent, name);
		}
		else if(parent instanceof String) {
			return getStringChild(context, pos, (String) parent, name);
		}
		else if(parent instanceof Boolean) {
			return getBooleanChild(context, pos, (Boolean) parent, name);
		}
		else {
			return getJavaObjectChild(context, pos, parent, name);
		}
	}

	public static EEvalResult getJavaObjectChild(EvalContext context, StrPos pos, Object parent, String name) {
		Optional<Method> optGetter = ImTools.get(parent.getClass()).getGetterMethod(name);
		if(optGetter.isPresent()) {
			try {
				return EEvalResult.success(context, optGetter.get().invoke(parent));
			} catch(IllegalAccessException | InvocationTargetException e) {
				return EEvalResult
					.failure(context, new EvalException(pos, "Exception while getting child '" + name + "' from " + parent, e));
			}
		}
		PList<Method> methods = PList.empty();
		for(Method m : parent.getClass().getMethods()) {
			if(m.getName().equals(name) && Modifier.isPublic(m.getModifiers())) {
				methods = methods.plus(m);
			}
		}
		if(methods.isEmpty() == false) {
			return EEvalResult.success(context, new EJavaObjectMethod(pos, parent, methods));
		}
		return EEvalResult.failure(context, pos, "Don't know how to get child '" + name + "' from " + parent);
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
		}
		if(callable == null) {
			return getJavaObjectChild(context, pos, value, name);
		}
		return EEvalResult.success(context, callable);
	}

	public static EEvalResult getStringChild(EvalContext context, StrPos pos, String value, String name) {
		ECallable callable = null;
		switch(name) {
			case "+":
				callable = args -> value + args[0];
				break;
		}
		if(callable == null) {
			return getJavaObjectChild(context, pos, value, name);
		}
		return EEvalResult.success(context, callable);
	}

	public static EEvalResult getBooleanChild(EvalContext context, StrPos pos, Boolean value, String name) {
		ECallable callable = null;
		switch(name) {

		}
		if(callable == null) {
			return getJavaObjectChild(context, pos, value, name);
		}
		return EEvalResult.success(context, callable);
	}
}
