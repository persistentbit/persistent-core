package com.persistentbit.core.experiments.config;

import com.persistentbit.core.collections.PList;
import com.persistentbit.core.result.Result;
import com.persistentbit.core.utils.BaseValueClass;
import com.persistentbit.core.validation.Validator;

import java.util.function.BiConsumer;
import java.util.function.Function;
import java.util.function.Supplier;

/**
 * TODO: Add comment
 *
 * @author Peter Muys
 * @since 15/05/2017
 */
public class ConfigVar<T> extends BaseValueClass{
    private final String name;
    private final Function<String,Result<T>> fromString;
    private final Function<T,Result<String>> toString;
    private final String info;
    private final Validator<T> validator;
    private final PList<BiConsumer<Result<T>, ConfigVar<T>>> watchers;
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
    public Result<T> get() {
		if(value.isError()){
			return value;
		}
        return validator.validateToResult(name,value.orElse(null));
    }


    public Supplier<Result<T>> asSupplier() {
		return this::get;
	}


    public String getInfo() {
        return info;
    }


    public String getName() {
        return name;
    }


    public ConfigVar<T> withInfo(String info) {
        return copyWith("info",info);
    }

    public ConfigVar<T> withValidator(Validator<T> validator){
		return copyWith("validator",validator);
	}
    public ConfigVar<T> addWatcher(BiConsumer<Result<T>, ConfigVar<T>> watcher){
		return copyWith("watchers", watchers.plus(watcher));
	}
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
