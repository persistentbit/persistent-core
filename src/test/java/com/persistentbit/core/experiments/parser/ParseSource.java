package com.persistentbit.core.experiments.parser;

import com.persistentbit.core.collections.PList;

import java.util.Iterator;
import java.util.Objects;

/**
 * TODOC
 *
 * @author petermuys
 * @since 17/02/17
 */
public class ParseSource{
	public static final int EOF = -1;

	private final Iterator<Character> iterator;
	private final int currentChar;
	private final Position position;
	private String snapshotExtra;
	private final ParseSource snapshot;



	private ParseSource(Iterator<Character> iterator, int currentChar,
					   Position position, String snapshotExtra, ParseSource snapshot
	) {
		this.iterator = Objects.requireNonNull(iterator);
		this.currentChar = currentChar;
		this.position = Objects.requireNonNull(position);
		this.snapshotExtra = Objects.requireNonNull(snapshotExtra);
		this.snapshot = snapshot;
	}

	public ParseSource(String sourceName, Iterator<Character> iterator){
		this(
			iterator,
			iterator.hasNext() ? (int)iterator.next() : EOF,
			new Position(sourceName,1, 1),
			"",
			null
		);
	}

	public static ParseSource asSource(String name, String source){
		return new ParseSource(
			name,
			new Iterator<Character>(){
				int pos = 0;
				@Override
				public boolean hasNext() {
					return pos<source.length();
				}

				@Override
				public Character next() {
					return source.charAt(pos++);
				}
			}
		);
	}
	private void addChar(char c){
		snapshotExtra += c;
	}

	public ParseSource	withSnapshot(){
		return new ParseSource(
			iterator,currentChar,position,snapshotExtra,this
		);
	}
	public ParseSource getSnapshot(){
		if(snapshot == null){
			return this;
		}
		if(snapshot.handled.isEmpty()){
			return this;
		}
		return new ParseSource(
			new Iterator<Character>(){
				private Iterator<Integer> backIter = snapshot.handled.iterator();
				private Iterator<Character> nextIter = iterator;
				@Override
				public boolean hasNext() {
					return backIter.hasNext() || nextIter.hasNext();
				}

				@Override
				public Character next() {
					return backIter.hasNext()
						? (char)backIter.next().intValue()
						: nextIter.next();
				}
			},
			snapshot.currentChar,
			snapshot.position,
			null
		);
	}

	public ParseSource	resolved(){
		return new ParseSource(
			iterator,currentChar,position,null
		);
	}

	public Position	getPosition(){
		return position;
	}
	public int current(){
		return currentChar;
	}
	public ParseSource next() {
		int newChar = iterator.hasNext()? iterator.next() : EOF;
		Position newPos = position.incForChar(newChar);
		if(snapshot != null){
			return new ParseSource(iterator,newChar,newPos,new Snapshot(snapshot.currentChar,snapshot.position,snapshot.handled.plus(newChar)));
		}
		return new ParseSource(iterator,newChar,newPos,null);
	}

	@Override
	public String toString() {
		return "ParseSource[" + getPosition() +", current=" + (char)current() + "]";
	}
}
