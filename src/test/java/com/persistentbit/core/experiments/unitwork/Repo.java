package com.persistentbit.core.experiments.unitwork;

import com.persistentbit.core.collections.PMap;
import com.persistentbit.core.result.Result;

/**
 * TODOC
 *
 * @author petermuys
 * @since 5/01/17
 */
public class Repo{
	private PMap<Integer,Person> rows = PMap.empty();
	public Result<Person> insert(Person p){
		return Result.function(p).code(l -> {
			l.info("Insert person " + p);
			rows = rows.put(p.getId(),p);
			return Result.success(p);
		});
	}
	public Result<Person> find(int personId){
		return Result.function(personId).code(l-> {
			return Result.fromOpt(rows.getOpt(personId));
		});
	}
	public Result<Person> update(Person p){
		return Result.function(p).code(log -> {
			return find(p.getId())
				.flatMap(this::delete)
				.flatMap(id -> insert(p));
		});
	}
	public Result<Integer> delete(Person p){
		return Result.function(p).code(l -> {
			return Result.success(find(p.getId())
				.map(found -> 1)
				.orElse(0));
		});
	}
	<R> Result<R> submit(DbWork<R> work){
		return work.apply(this);
	}
}
