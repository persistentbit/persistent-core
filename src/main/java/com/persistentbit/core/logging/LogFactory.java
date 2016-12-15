package com.persistentbit.core.logging;

import com.persistentbit.core.collections.PList;
import com.persistentbit.core.collections.PMap;
import com.persistentbit.core.collections.PStream;
import com.persistentbit.core.utils.BaseValueClass;

/**
 * TODOC
 *
 * @author petermuys
 * @since 14/12/16
 */
public class LogFactory extends BaseValueClass{

	private PMap<Long, PList<FLog>> threadLogs = PMap.empty();

	public FLog flog(Object... params) {
		Thread            thread       = Thread.currentThread();
		StackTraceElement stackElement = thread.getStackTrace()[2];
		FLog result = new FLog(
			thread.getId(),
			System.currentTimeMillis(),
			stackElement.getClassName(),
			stackElement.getMethodName(),
			stackElement.getLineNumber(),
			joinParams(params)
		);
		PList<FLog> list = threadLogs.getOrDefault(thread.getId(), PList.empty());
		list = list.plus(result);
		threadLogs = threadLogs.put(thread.getId(), list);
		return result;
	}

	private String joinParams(Object... params) {
		return PStream.from(params).toString(", ");
	}

}
