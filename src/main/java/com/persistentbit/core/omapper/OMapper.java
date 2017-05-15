package com.persistentbit.core.omapper;

import com.persistentbit.core.collections.PList;
import com.persistentbit.core.collections.PMap;
import com.persistentbit.core.collections.PStream;
import com.persistentbit.core.result.Result;
import com.persistentbit.core.utils.BaseValueClass;

import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Collectors;

/**
 * Register with Object mappers.<br>
 *
 * @author petermuys
 * @since 13/05/17
 */
public class OMapper {
	public interface DestType<T>{

	}
	public static class ClsDestType<T> extends BaseValueClass implements DestType<T>{
		private final Class<T> cls;

		public ClsDestType(Class<T> cls) {
			this.cls = cls;
		}
	}



	@FunctionalInterface
	public interface ValueMapper<V,R>{
		Result<R> map(OMapper mapper, V value, DestType<R> destType);
	}
	static private class PredMap {
		public final Predicate valuePred;
		public final ValueMapper mapper;

		public PredMap(Predicate valuePred, ValueMapper mapper) {
			this.valuePred = valuePred;
			this.mapper = mapper;
		}
	}

	private final PMap<DestType,PList<PredMap>> mappers;


	public OMapper(
		PMap<DestType, PList<PredMap>> mappers
	) {
		this.mappers = mappers;
	}
	public OMapper() {
		this(PMap.empty());
	}
	static public <V> Predicate<V> valueClassPred(Class<V> cls){
		return v -> v != null && cls.isAssignableFrom(v.getClass());
	}

	public <V,R> OMapper register(Predicate<V> valuePred, DestType<R> destType, ValueMapper<V,R> mapper){
		PList<PredMap> l = mappers.getOrDefault(destType,PList.empty());
		l = l.plus(new PredMap(valuePred,mapper));
		return new OMapper(this.mappers.put(destType,l));
	}
	public <V,R> OMapper register(Class<V> valueCls, Class<R> destCls, ValueMapper<V,R> mapper){

		return register(
			valueClassPred(valueCls),
			new ClsDestType<>(destCls),
			mapper
		);
	}



	public <V,R> Result<R> map(V value, DestType<R> destType){
		return Result.function(value,destType).code(l -> {
			PList<PredMap> ml = mappers.getOrDefault(destType,null);
			if(ml == null){
				return Result.failure("No Mapper defined for destination type " + destType);
			}
			for(PredMap pm : ml){
				if(pm.valuePred.test(value)){
					return pm.mapper.map(this,value,destType);
				}
			}
			if(value == null){
				return Result.empty("Value is null for destination " + destType);
			}
			return Result.failure("No mapper defined for value" + value);
		});
	}
	public <V,R> Result<PList<R>> mapPList(PList<V> value, Class<R> destCls){
		return mapPList(value, new ClsDestType<>(destCls));
	}

	public <V,R> Result<PList<R>> mapPList(PList<V> value, DestType<R> itemDestType){
		return Result.function(value,itemDestType).code(l -> {
			PList<Result<R>> resRes = value.map(item -> map(item,itemDestType));
			Result<PStream<R>> resStream = Result.fromSequence(resRes);
			return resStream.map(PStream::plist);
		});

	}
	public <V,R> Result<List<R>> mapList(List<V> value, DestType<R> itemDestType){
		return Result.function(value,itemDestType).code(l -> {
			List<Result<R>> resRes = value.stream().map(item -> map(item,itemDestType)).collect(Collectors.toList());
			return Result.fromSequence(resRes);
		});
	}
	public <V,R> Result<List<R>> mapList(List<V> value, Class<R> destCls){
		return mapList(value, new ClsDestType<>(destCls));
	}
	public <V,R> Result<R> mapResult(Result<V> value, DestType<R> itemDestType){
		return value.flatMap(v -> map(v,itemDestType));
	}
	public <V,R> Result<R> mapResult(Result<V> value, Class<R> itemDestCls){
		return mapResult(value,new ClsDestType<>(itemDestCls));
	}
	public <V,R> Result<R> map(V value, Class<R> destCls){
		return map(value,new ClsDestType<>(destCls));
	}

}
