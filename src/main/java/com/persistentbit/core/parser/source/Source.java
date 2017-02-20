package com.persistentbit.core.parser.source;

import com.persistentbit.core.parser.Parser;
import com.persistentbit.core.utils.StringUtils;

import java.util.Objects;

/**
 * Represents the source and position in the source for a Parser.
 *
 * @author petermuys
 * @see Parser
 * @since 17/02/17
 */
public class Source{

	public static final char EOF = 0;

	private final String source;
	private final int sourcePos;

	public final char current;
	public final Position position;

	private Source(String source, int sourcePos, Position position) {
		this.source = Objects.requireNonNull(source);
		this.sourcePos = sourcePos;
		this.position = Objects.requireNonNull(position);
		this.current = sourcePos < source.length()
			? source.charAt(sourcePos)
			: EOF;
	}



	public static Source asSource(String name, String source) {
		return new Source(source, 0, new Position(name, 1, 1));
	}

	public boolean isEOF() {
		return sourcePos >= source.length();
	}

	public Source next() {
		if(isEOF()) {
			return this;
		}
		return new Source(source, sourcePos + 1, position.incForChar(current));
	}

	public Source next(int count) {
		Source res = this;
		for(int t = 0; t < count; t++) {
			res = res.next();
		}
		return res;
	}

	public String rest() {
		return isEOF() ? "" : source.substring(sourcePos);
	}

	@Override
	public String toString() {
		return "Source[" + position + ", current=" + StringUtils.escapeToJavaString("" + current) + "]";
	}
}
