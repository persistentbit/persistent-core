package com.persistentbit.core.easyscript;

import com.persistentbit.core.utils.StrPos;

import java.util.function.Function;

/**
 * TODO: Add comment
 *
 * @author Peter Muys
 * @since 22/02/2017
 */
public abstract class EEvalResult {
    private final EvalContext   context;
    private EEvalResult(EvalContext context){
        this.context = context;
    }

	public static EEvalResult success(EvalContext context, Object value) {
		return new Success(context,value);
    }

	public static EEvalResult failure(EvalContext context, EvalException error) {
		return new Failure(context,error);
    }

	public static EEvalResult failure(EvalContext context, StrPos pos, String error) {
		return new Failure(context,new EvalException(pos,error));
    }

	public static EEvalResult todo(EvalContext context) {
		return failure(context, StrPos.inst,"TODO");
    }
    public abstract boolean isSuccess();
    public boolean isError() {
        return isSuccess() == false;
    }

    public EvalContext getContext(){
        return context;
    }

    public abstract EEvalResult flatMap(Function<Object,EEvalResult> map);
    public abstract EEvalResult mapSuccess(Function<EEvalResult, EEvalResult> map);
    public abstract EEvalResult withContext(EvalContext context);
    public abstract Object getValue();
    public abstract EvalException getError();

	public static class Success extends EEvalResult{

		private final Object value;
        public Success(EvalContext context,Object value) {
            super(context);
            this.value = value;
        }

        @Override
        public boolean isSuccess() {
            return true;
        }

        @Override
        public EEvalResult flatMap(Function<Object, EEvalResult> map) {
            return map.apply(value);
        }

        @Override
        public EEvalResult withContext(EvalContext context) {
            return new Success(context,value);
        }

        @Override
        public Object getValue() {
            return value;
        }

        @Override
        public EvalException getError() {
            throw new RuntimeException("No error (until now)!");
        }

        @Override
        public EEvalResult mapSuccess(Function<EEvalResult, EEvalResult> map) {
            return map.apply(this);
        }

		@Override
		public String toString() {
			return "EvalResult.success(" + value + ")";
		}
	}

	public static class Failure extends EEvalResult{

		private final EvalException    error;
        public Failure(EvalContext context,EvalException error) {
            super(context);
            this.error = error;
        }

        @Override
        public EEvalResult withContext(EvalContext context) {
            return new Failure(context,error);
        }

        @Override
        public boolean isSuccess() {
            return false;
        }

        @Override
        public EEvalResult flatMap(Function<Object, EEvalResult> map) {
            return this;
        }

        @Override
        public Object getValue() {
            throw new RuntimeException("Error result",getError());
        }

        @Override
        public EvalException getError() {
            return error;
        }

        @Override
        public EEvalResult mapSuccess(Function<EEvalResult, EEvalResult> map) {
            return this;
        }
    }
}
