package com.persistentbit.core.parser.source;

import com.persistentbit.core.collections.PList;
import com.persistentbit.core.utils.IO;

import java.io.IOException;
import java.io.Reader;
import java.util.Objects;
import java.util.Optional;

/**
 * TODOC
 *
 * @author petermuys
 * @since 19/02/17
 */
public class ReaderImmutableIterator implements ImmutableIterator<Character>{

	private static class Inst{

		private Reader reader;
		private PList<Character> read;

		public Inst(Reader reader) {
			this.reader = Objects.requireNonNull(reader);
			this.read = PList.empty();
		}

		public boolean hasPos(int pos) {
			readUntilPos(pos);
			return pos < read.size();
		}

		public char getPos(int pos) {
			readUntilPos(pos);
			return read.get(pos);
		}

		public char getPosOrElse(int pos, char elseValue) {
			return hasPos(pos) ? getPos(pos) : elseValue;
		}

		private synchronized void readUntilPos(int pos) {
			if(reader == null) {
				return;
			}
			while(pos < read.size()) {
				try {
					int c = reader.read();
					if(c == -1) {
						IO.close(reader).orElseThrow();
						reader = null;
						return;
					}
					read = read.plus((char) c);
				} catch(IOException e) {
					throw new RuntimeException("IO Exception while reading", e);
				}
			}
		}
	}

	private final Inst inst;
	private final int pos;

	private ReaderImmutableIterator(Inst inst, int pos) {
		this.inst = inst;
		this.pos = pos;
	}

	public ReaderImmutableIterator(Reader reader) {
		this(new Inst(reader), 0);
	}

	@Override
	public Optional<Character> getOpt() {
		return inst.hasPos(pos)
			? Optional.of(inst.getPos(pos))
			: Optional.empty();
	}

	@Override
	public Character orElseThrow() {
		return inst.getPos(pos);
	}

	@Override
	public Character orElse(Character elseValue) {
		return inst.hasPos(pos)
			? inst.getPos(pos)
			: elseValue;
	}

	@Override
	public ImmutableIterator<Character> next() {
		return new ReaderImmutableIterator(inst, pos + 1);
	}
}
