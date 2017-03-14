package com.persistentbit.core.glasgolia.repl;

import com.persistentbit.core.collections.PList;

/**
 * TODOC
 *
 * @author petermuys
 * @since 13/03/17
 */
public interface ReplInterface{

	enum ReplAction{
		reload, exit
	}

	ReplAction startRepl(PList<String> execute);

	PList<String> getHistory();

}
