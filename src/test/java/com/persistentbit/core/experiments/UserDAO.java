package com.persistentbit.core.experiments;

import com.persistentbit.core.Result;
import com.persistentbit.core.logging.Logged;
import com.persistentbit.core.utils.BaseValueClass;

/**
 * TODOC
 *
 * @author petermuys
 * @since 1/01/17
 */
public class UserDAO{
	static public class User extends BaseValueClass{
		private final int id;
		private final String userName;

		public User(int id, String userName) {
			this.id = id;
			this.userName = userName;
		}
		static public User of(int id, String userName){
			return Logged.function(id, userName).log(l -> {
				if(userName == null){
					throw new IllegalArgumentException("userName can't be null");
				}
				return new User(id,userName);
			});
		}
	}

	public Result<User>	getUserById(int id){
		return Logged.function(id).<Result<User>>log(l -> {
			if(id == 1){
				return Result.success(User.of(id,"Peter Muys"),l.getLog());
			}
			if(id == 2){
				return Result.success(User.of(id,null),l.getLog());
			}
			if(id == 3){
				return Result.failure("Error retrieving user with id " + id,l.getLog());
			}
			return Result.empty("No user with id " + id,l.getLog());
		});
	}
}
