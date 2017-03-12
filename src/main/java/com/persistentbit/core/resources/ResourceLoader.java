package com.persistentbit.core.resources;

import com.persistentbit.core.collections.PByteList;
import com.persistentbit.core.function.Memoizer;
import com.persistentbit.core.result.Result;

import java.io.File;
import java.util.function.Function;
import java.util.function.Predicate;

/**
 * General Resource Loader.<br>
 * Used to load byte arrays from a file,classpath,url,...
 *
 * @author petermuys
 * @see FileResourceLoader
 * @see URLResourceLoader
 * @see ClassPathResourceLoader
 * @since 6/02/17
 */
public interface ResourceLoader extends Function<String, Result<PByteList>>{


	ResourceLoader empty = name -> Result.empty("Resource '" + name + "' not found");

	/**
	 * A Resource loader that first search in the root folder and then tries the classpath
	 *
	 * @see #rootAndClassPath
	 */
	ResourceLoader classPathAndRoot =
		ClassPathResourceLoader.inst.orTry(new FileResourceLoader(new File(".").getAbsoluteFile()));

	/**
	 * A Resource loader that first search in classpath and then in the root folder
	 *
	 * @see #classPathAndRoot
	 */
	ResourceLoader rootAndClassPath =
		ClassPathResourceLoader.inst.orTry(new FileResourceLoader(new File(".").getAbsoluteFile()));

	/**
	 * Create a cached version of this resource loader, using the resource name as key
	 *
	 * @return A new Resource loader that caches the loaded resources
	 */
	default ResourceLoader cached() {
		ResourceLoader                      self     = this;
		Function<String, Result<PByteList>> memoizer = Memoizer.of(this::apply);
		return new ResourceLoader(){
			@Override
			public Result<PByteList> apply(String name) {
				return memoizer.apply(name);
			}

			@Override
			public String toString() {
				return self + ".cached()";
			}
		};

	}

	/**
	 * Filter this resource using the name of the resource.
	 *
	 * @param namePredicate Must return true if this loader should try to handle the resource
	 *
	 * @return The new loader
	 */
	default ResourceLoader forNames(Predicate<String> namePredicate) {
		ResourceLoader self = this;
		return new ResourceLoader(){
			@Override
			public Result<PByteList> apply(String name) {
				if(namePredicate.test(name)) {
					return self.apply(name);
				}
				return Result.empty("Name '" + name + "' does not match for " + self);
			}

			@Override
			public String toString() {
				return self + ".forNames(" + namePredicate + ")";
			}
		};

	}

	/**
	 * Maps the name of the resource prior to passing it to the loader
	 *
	 * @param nameMapper Mapper for the resource name
	 *
	 * @return The new Resource loader
	 */
	default ResourceLoader withNameMapper(Function<String, String> nameMapper) {
		ResourceLoader self = this;
		return new ResourceLoader(){
			@Override
			public Result<PByteList> apply(String name) {
				//System.out.println("MAPPING NAME " + name + " to " + nameMapper.apply(name));
				return self.apply(nameMapper.apply(name));
			}

			@Override
			public String toString() {
				return self + ".withNameMapper(" + nameMapper + ")";
			}
		};

	}

	/**
	 * Try this loader and if the result is not a success, try loading the resource using the supplied loader
	 *
	 * @param resourceLoader The loader to try afther this
	 *
	 * @return the new Resource loader
	 */
	default ResourceLoader orTry(ResourceLoader resourceLoader) {
		ResourceLoader self = this;
		return new ResourceLoader(){
			@Override
			public Result<PByteList> apply(String name) {
				return self.apply(name)
						   .flatMapNoSuccess((res, exception) -> resourceLoader.apply(name))
					;
			}

			@Override
			public String toString() {
				return self + " orTry " + resourceLoader;
			}
		};
	}

}
