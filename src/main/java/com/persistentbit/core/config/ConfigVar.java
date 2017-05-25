package com.persistentbit.core.config;

import com.persistentbit.core.collections.PList;
import com.persistentbit.core.function.ResultSupplier;
import com.persistentbit.core.result.Result;
import com.persistentbit.core.utils.BaseValueClass;
import com.persistentbit.core.validation.Validator;

import java.util.function.BiConsumer;
import java.util.function.Function;
import java.util.function.Supplier;

/**
 * A Config var is a container for a variable with:
 * <ul>
 *     <li>A name</li>
 *     <li>Setter/Getter using strings</li>
 *     <li>Extra info or description</li>
 *     <li>An optional Validator for the value</li>
 *     <li>A change watchers</li>
 * </ul>
 *
 *
 * @author Peter Muys
 * @since 15/05/2017
 */
public class ConfigVar<T> extends BaseValueClass implements ResultSupplier<T> {
    private final String name;
    private final Function<String,Result<T>> fromString;
    private final Function<T,Result<String>> toString;
    private final String info;
    private final Validator<T> validator;
    private PList<BiConsumer<Result<T>, ConfigVar<T>>> watchers;
    private Result<T> value;

	public ConfigVar(String name,
					 Function<String, Result<T>> fromString,
					 Function<T, Result<String>> toString,
					 String info,
					 Validator<T> validator,
					 PList<BiConsumer<Result<T>, ConfigVar<T>>> watchers,
					 Result<T> value
	) {
		this.name = name;
		this.fromString = fromString;
		this.toString = toString;
		this.info = info;
		this.validator = validator;
		this.watchers = watchers;
		this.value = value;
		checkNullFields();
	}

	public ConfigVar(String name,Function<String, Result<T>> fromString, Function<T,Result<String>> toString){
		this(name,fromString,toString,"",Validator.ok(),PList.empty(),Result.empty("Not Initialized:" + name));
	}
	public ConfigVar(String name,Function<String, Result<T>> fromString){
		this(name,fromString,v -> Result.result(v.toString()));
	}

	/**
	 * Get the value of this var.<br>
	 * The value returned is validated.<br>
	 * @return The value Result
	 */
    public Result<T> get() {
		if(value.isError()){
			return value;
		}
        return validator.validateToResult(name,value.orElse(null));
    }

	/**
	 * Convert this instance to a {@link Supplier}
	 * @return
	 */
    public Supplier<Result<T>> asSupplier() {
		return this::get;
	}

	/**
	 * Get the info for this var or an empty string
	 * @return The info
	 */
    public String getInfo() {
        return info;
    }


	/**
	 * Get the name of this var
	 * @return The name
	 */
	public String getName() {
        return name;
    }

	/**
	 * Create a new version of this instance with updated info
	 * @param info The new info
	 * @return a new ConfigVar
	 */
    public ConfigVar<T> withInfo(String info) {
        return copyWith("info",info);
    }

	/**
	 * Create a new version of this instance with updated validator
	 * @param validator The new validator
	 * @return a new ConfigVar
	 */
	public ConfigVar<T> withValidator(Validator<T> validator){
		return copyWith("validator",validator);
	}

	/**
	 * Add a value change watcher to this config var.
	 * @param watcher BiConsumer with a Result<T> containing the previous value and this var.
	 * @return this instance with added watcher
	 */
    public ConfigVar<T> addWatcher(BiConsumer<Result<T>, ConfigVar<T>> watcher){

		return copyWith("watchers", watchers.plus(watcher));
	}

	/**
	 * Validate the given string value by calling the from
	 * @param stringValue The string value to validate
	 * @return
	 */
	public Result<T> validateStringValue(String stringValue){
    	return fromString.apply(stringValue)
			.flatMap(this::validateValue);
	}

	public Result<T> validateValue(T value){
		return validator.validateToResult(name,value);
	}

	public ConfigVar<T> setValue(T value){
		return setResult(Result.result(value));
	}

    public ConfigVar<T> setResult(Result<T> value) {
    	Result<T> prevValue = this.value;
        if(value == null){
            this.value = Result.failure("Can't set to null:" + name);
        } else {
            this.value = value;
        }
        watchers.forEach(w -> w.accept(prevValue,this));
        return this;
    }


    public ConfigVar<T> setString(String value) {
        return setResult(fromString.apply(value));
    }


    public Result<String> getStringValue() {
        return get().flatMap(toString);
    }


	@Override
	public String toString() {
		return "ConfigVar[" + name + " = " + get() + "]";
	}
}
