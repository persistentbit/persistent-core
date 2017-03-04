package com.persistentbit.core.parser.source;

import com.persistentbit.core.parser.Parser;
import com.persistentbit.core.utils.StrPos;
import com.persistentbit.core.utils.UString;

import java.util.Objects;

/**
 * Represents the source and position in the source for a Parser.
 *
 * @author petermuys
 * @see Parser
 * @since 17/02/17
 */
public abstract class Source{

	public static final char EOF = 0;



	public final char current;
	public final StrPos position;


	protected Source(StrPos position, char current) {
		this.position = Objects.requireNonNull(position);
		this.current = current;
	}


	public static Source asSource(String source) {
		return asSource(StrPos.inst,source);
	}

	public static Source asSource(String name, String source) {
		return asSource(new StrPos(name), source);
	}
	public static Source asSource(StrPos pos, String source) {
		return new SourceFromString(source, 0, pos);
	}

	public boolean isEOF() {
		return current == EOF;
	}

	public abstract Source next();

	public Source next(int count) {
		Source res = this;
		for(int t = 0; t < count; t++) {
			res = res.next();
		}
		return res;
	}

	public Source plus(Source right){
		if(isEOF()){
			return right;
		}
		return new SourcePlusSource(this,right);
	}

	public abstract String rest();

	@Override
	public String toString() {
		return "Source[" + position + ", current=" + UString
			.present(UString.escapeToJavaString(rest()), 20, "...") + "]";
	}
}
