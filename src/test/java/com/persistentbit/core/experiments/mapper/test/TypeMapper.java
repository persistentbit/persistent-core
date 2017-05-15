package com.persistentbit.core.experiments.mapper.test;

import com.persistentbit.core.collections.PList;
import com.persistentbit.core.omapper.OMapper;
import com.persistentbit.core.result.Result;

/**
 * TODOC
 *
 * @author petermuys
 * @since 13/05/17
 */
public class TypeMapper{

	static OMapper.ValueMapper<TypeA,TypeADTO> typeAMapper = (mapper, value, destType)->
		Result.function(value).code(l ->
			mapper.map(value.valueB,String.class)
				.flatMap(bstr ->
						mapper.mapPList(value.numArr,String.class)
							.map(arr -> new TypeADTO(value.name,bstr,arr))
				)
		)
	;

	static OMapper.ValueMapper<TypeB,String> typeBMapper = (mapper, value, destType) -> {
		return value == null ? Result.empty()
			: Result.success(value.typeBName);
	};

	public static OMapper register(OMapper mapper){
		return mapper.register(TypeA.class, TypeADTO.class,typeAMapper)
			.register(TypeB.class, String.class,typeBMapper)
			.register(Integer.class,String.class,(omap,value,destType)-> Result.success("ToString(" + value +")"));
	}

	public static void main(String[] args) {
		TypeA a = new TypeA("typeAName",new TypeB("typeBName"), PList.val(1,2,10));
		OMapper mapper = register(new OMapper());
		TypeADTO dto = mapper.map(a,TypeADTO.class).orElseThrow();
		System.out.println(dto);
	}
}
