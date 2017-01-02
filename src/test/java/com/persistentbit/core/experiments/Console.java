package com.persistentbit.core.experiments;

import com.persistentbit.core.Nothing;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

/**
 * TODOC
 *
 * @author petermuys
 * @since 28/12/16
 */
public class Console{

	private static BufferedReader br = new BufferedReader(new InputStreamReader(System.in));

	public static FIO<String> readLine(Nothing nothing) {
		return () -> {
			try {
				return br.readLine();
			} catch(IOException e) {
				throw new IllegalStateException(e);
			}
		};
	}

	public static FIO<Nothing> printLine(Object value) {
		return () -> {
			System.out.println(value);
			return Nothing.inst;
		};
	}

	public static void main(String[] args) {
		FIO<Nothing> script = sayHello();
		script.run();
	}

	private static FIO<Nothing> sayHello() {
		return Console.printLine("Enter your name:")
			.flatMap(Console::readLine)
			.map(name -> buildMessage(name))
			.flatMap(Console::printLine);
	}

	private static String buildMessage(String name) {
		return "Hello " + name;
	}
}
