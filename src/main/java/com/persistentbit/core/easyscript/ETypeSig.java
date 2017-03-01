package com.persistentbit.core.easyscript;

import com.persistentbit.core.collections.PList;

import java.util.function.Function;

/**
 * TODO: Add comment
 *
 * @author Peter Muys
 * @since 28/02/2017
 */
public abstract class ETypeSig {
    private ETypeSig(){}


    public abstract <T> T match(
        Function<Any,T> matchAny,
        Function<Cls,T> matchCls,
        Function<Name,T> matchName,
        Function<WithGenerics,T> matchWithGenerics,
        Function<Array,T> matchArray,
        Function<Fun,T> matchFunction,
        Function<Bound,T> matchBound

    );

    static public class Any extends ETypeSig{
        @Override
        public String toString() {
			return "?";
		}



        @Override
        public <T> T match(Function<Any, T> matchAny, Function<Cls, T> matchCls, Function<Name, T> matchName, Function<WithGenerics, T> matchWithGenerics, Function<Array, T> matchArray, Function<Fun, T> matchFunction, Function<Bound,T> matchBound) {
            return matchAny.apply(this);
        }
    }

    /**
     *
     */
    static public class Bound extends ETypeSig{
        public enum Type{
            boundExtends, boundSuper
        }
        public final Type type;
        public final ETypeSig left;
        public final PList<ETypeSig> bounds;

        public Bound(Type type, ETypeSig left, PList<ETypeSig> bounds) {
            this.type = type;
            this.left = left;
            this.bounds = bounds;
        }

        @Override
        public String toString() {
			return left + (type == Type.boundExtends ? " extends " : " super ") + bounds.toString(" & ");
		}

        @Override
        public <T> T match(Function<Any, T> matchAny, Function<Cls, T> matchCls, Function<Name, T> matchName, Function<WithGenerics, T> matchWithGenerics, Function<Array, T> matchArray, Function<Fun, T> matchFunction, Function<Bound, T> matchBound) {
            return matchBound.apply(this);
        }
    }


    /**
     *
     */
    static public class Cls extends ETypeSig{
        public final Class cls;

        public Cls(Class cls) {
            this.cls = cls;
        }

        @Override
        public String toString() {
            return cls.getName();
        }


        @Override
        public <T> T match(Function<Any, T> matchAny, Function<Cls, T> matchCls, Function<Name, T> matchName, Function<WithGenerics, T> matchWithGenerics, Function<Array, T> matchArray, Function<Fun, T> matchFunction, Function<Bound,T> matchBound) {
            return matchCls.apply(this);
        }
    }

    /**
     *
     */
    static public class Name extends ETypeSig{
        public final String clsName;

        public Name(String clsName) {
            this.clsName = clsName;
        }

        @Override
        public String toString() {
            return clsName;
        }


        @Override
        public <T> T match(Function<Any, T> matchAny, Function<Cls, T> matchCls, Function<Name, T> matchName, Function<WithGenerics, T> matchWithGenerics, Function<Array, T> matchArray, Function<Fun, T> matchFunction, Function<Bound,T> matchBound) {
            return matchName.apply(this);
        }
    }

    /**
     *
     */
    static public class WithGenerics extends ETypeSig{

		public final ETypeSig.Name name;
		public final PList<ETypeSig>  generics;

		public WithGenerics(ETypeSig.Name name, PList<ETypeSig> generics) {
			this.name = name;
			this.generics = generics;
        }

        @Override
        public String toString() {
			return name + generics.toString("<", ",", ">");
		}


        @Override
        public <T> T match(Function<Any, T> matchAny, Function<Cls, T> matchCls, Function<Name, T> matchName, Function<WithGenerics, T> matchWithGenerics, Function<Array, T> matchArray, Function<Fun, T> matchFunction, Function<Bound,T> matchBound) {
            return matchWithGenerics.apply(this);
        }
    }


    /**
     *
     */
    static public class Array extends ETypeSig{
        public final ETypeSig arrayOf;

        public Array(ETypeSig arrayOf) {
            this.arrayOf = arrayOf;
        }

        @Override
        public String toString() {
			return "[" + arrayOf + "]";
		}


        @Override
        public <T> T match(Function<Any, T> matchAny, Function<Cls, T> matchCls, Function<Name, T> matchName, Function<WithGenerics, T> matchWithGenerics, Function<Array, T> matchArray, Function<Fun, T> matchFunction, Function<Bound,T> matchBound) {
            return matchArray.apply(this);
        }
    }
    static public class Fun extends ETypeSig{
        public final ETypeSig   returnType;
        public final PList<ETypeSig> arguments;

        public Fun(ETypeSig returnType, PList<ETypeSig> arguments) {
            this.returnType = returnType;
            this.arguments = arguments;
        }



        @Override
        public <T> T match(Function<Any, T> matchAny, Function<Cls, T> matchCls, Function<Name, T> matchName, Function<WithGenerics, T> matchWithGenerics, Function<Array, T> matchArray, Function<Fun, T> matchFunction, Function<Bound,T> matchBound) {
            return matchFunction.apply(this);
        }

        @Override
        public String toString() {
			return arguments.toString("(", ",", ")") + "->" + returnType;
		}
    }


}
