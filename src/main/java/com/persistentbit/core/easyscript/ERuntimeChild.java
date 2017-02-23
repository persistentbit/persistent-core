package com.persistentbit.core.easyscript;

import com.persistentbit.core.utils.StrPos;
import com.persistentbit.core.utils.ToDo;

/**
 * TODO: Add comment
 *
 * @author Peter Muys
 * @since 23/02/2017
 */
public class ERuntimeChild {
    public static class EObjectChild implements ECallable{
        private final Object parent;
        private final String name;

        public EObjectChild(Object parent, String name) {
            this.parent = parent;
            this.name = name;
        }

		@Override
		public Object apply(Object... arguments) {
			throw new ToDo();
		}

    }
    public static EEvalResult  eval(Object parent, String name, StrPos pos, EvalContext context){
        if(parent == null){
            return EEvalResult.failure(context,pos,"Can't get child '" + name + "' from a null object");
        }
        return EEvalResult.success(context,new EObjectChild(parent,name));
    }


}
