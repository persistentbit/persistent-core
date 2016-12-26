package com.persistbit.core.logging;

import com.persistentbit.core.exceptions.Try;
import com.persistentbit.core.logging.FLog;
import com.persistentbit.core.logging.LogCollector;
import com.persistentbit.core.logging.Logged;

import java.util.concurrent.CompletableFuture;

/**
 * TODOC
 *
 * @author petermuys
 * @since 23/12/16
 */
public class MailServer{

	private final LogCollector logs;
	private final String       smtpServer;

	public MailServer(LogCollector logs, String smtpServer) {
		this.logs = logs;
		this.smtpServer = smtpServer;
		logs.fun(logs, smtpServer);
	}

	public CompletableFuture<Logged<Try<Boolean>>> send(String from, String to, String msg) {
		return CompletableFuture.supplyAsync(() -> logs.logged(() -> {
			FLog flog = logs.fun(from, to);
			Try<Boolean> res = Try.flatRun(() -> {
				if(to.equals("test@test.com")) {
					return flog.done(Try.failure(new RuntimeException("Can't send to test@test.com")));
				}
				return flog.done(Try.success(true));
			});
			return res;
		}));
	}
}
