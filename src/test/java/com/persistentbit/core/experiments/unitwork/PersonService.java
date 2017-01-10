package com.persistentbit.core.experiments.unitwork;

import com.persistentbit.core.result.Result;

/**
 * TODOC
 *
 * @author petermuys
 * @since 5/01/17
 */
public class PersonService{

	private Repo repo;

	public PersonService(Repo repo) {
		this.repo = repo;
	}

	public Result<Person> createPeter(){
		return Result.function().code(l ->
		  repo.submit(repo -> {
			Person p = new Person(1,"peter","muys");
			return repo.insert(p);
		}));
	}
	public Result<Integer> deletePeter() {
		return Result.function().code(l -> repo.submit((Repo repo) -> {
			return repo.find(1)
				.flatMap(p -> repo.delete(p));
		}));
	}
	public Result<Person> findPeter(){
		return Result.function().code(l -> repo.submit(repo -> {
			return repo.find(1);
		}));
	}
/*
	public static void main(String[] args) {
		PersonService   ps = new PersonService(new Repo());
		ps.findPeter().print(lp);
		ps.createPeter().print(lp);
		ps.findPeter().print(lp);
		ps.deletePeter().print(lp);

	}*/
}
