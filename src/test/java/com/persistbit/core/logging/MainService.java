package com.persistbit.core.logging;

import com.persistentbit.core.logging.Logged;

/**
 * TODOC
 *
 * @author petermuys
 * @since 13/12/16
 */
public class MainService{

	Logged<String> getName() {
		return Logged.fun(l -> {
			//getFirstName().flatMap(fn -> getLastName().map(ln -> fn + " " + ln));

		});
	}

	Logged<String> getFirstName() {
		return Logged.fun(l -> l.result("Peter"));
	}

	Logged<String> getLastName() {
		return Logged.fun(l -> l.result("Muys"));
	}
}
