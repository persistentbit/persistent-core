package com.persistentbit.core.collections;


import java.io.Serializable;
import java.util.Collections;
import java.util.Iterator;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Function;
import java.util.function.Supplier;

/**
 * @author Peter Muys
 * @since 9/06/2016
 */
@SuppressWarnings({"AbstractClassNamingConvention"})
public abstract class LList<E> extends AbstractIPList<E, LList<E>> implements Serializable{

  private static final LList empty = new LList(){
	@Override
	public int size() {
	  return 0;
	}

	@Override
	public Optional headOpt() {
	  return Optional.empty();
	}

	@Override
	public Optional<LList> tailOption() {
	  return Optional.empty();
	}

	@Override
	public boolean isEmpty() {
	  return true;
	}

	@Override
	public Iterator iterator() {
	  return Collections.emptyIterator();
	}

	@Override
	public String toString() {
	  return "Nil";
	}


	@Override
	public Object get(int index) {
	  throw new IndexOutOfBoundsException("empty LList");
	}


	@Override
	public IPList put(int index, Object value) {
	  throw new IndexOutOfBoundsException("empty LList");
	}

	@Override
	public LList plusAll(Iterable iter) {

	  return PStream.from(iter).llist();

	}

	@SuppressWarnings("unchecked")
	@Override
	public LList cons(Object item) {
	  return prepend(item);
	}

	@Override
	public LList constAll(Iterable right) {
	  return PStream.from(right).llist();
	}

	  @Override
	  public Object match(Supplier emptyList, Function singleton, Function headTail
	  ) {
		  return emptyList.get();
	  }
  };

  @Override
  protected LList<E> toImpl(PStream<E> lazy) {
	return lazy.llist();
  }

  @SuppressWarnings("unchecked")
  public LList<E> cons(E item) {

	LList<E> r = LList.empty.prepend(item);
	for(E v : this.lazy().reversed()) {
	  r = r.prepend(v);
	}
	return r;
  }

  public LList<E> prepend(E item) {
	return new LListImpl<>(item, this);
  }

  public LList<E> constAll(Iterable<E> right) {
	return plusAll(right).llist();
  }

  @Override
  public LList<E> reversed() {
	LList<E> r = LList.empty();
	for(E e : this) {
	  r = r.prepend(e);
	}
	return r;
  }

  @SuppressWarnings("unchecked")
  public static <T> LList<T> empty() {
	return (LList<T>) empty;
  }

  @Override
  public abstract Optional<E> headOpt();

  @Override
  @SuppressWarnings("OptionalGetWithoutIsPresent")
  public LList<E> tail() {
	return tailOption().get();
  }

  public abstract Optional<LList<E>> tailOption();

  @Override
  public abstract boolean isEmpty();

  private static final class LListImpl<E> extends LList<E>{

	private final E        head;
	private final LList<E> tail;

	private LListImpl(E head, LList<E> tail) {
	  this.head = head;
	  this.tail = Objects.requireNonNull(tail, "tail");

	}


	@Override
	public Optional<E> headOpt() {
	  return Optional.ofNullable(head);
	}

	@Override
	public Optional<LList<E>> tailOption() {
	  return Optional.of(tail);
	}

	@Override
	public boolean isEmpty() {
	  return false;
	}

	@Override
	public E get(int index) {
	  Iterator<E> iter = iterator();
	  while(iter.hasNext()) {
		if(index == 0) {
		  return iter.next();
		}
		iter.next();
		index--;
	  }
	  throw new IndexOutOfBoundsException(String.valueOf(index));
	}

	@Override
	public Iterator<E> iterator() {
	  return new Iterator<E>(){
		private LList<E> next;

		@Override
		public boolean hasNext() {
		  if(next == null) {
			return true;
		  }
		  return next.isEmpty() == false;
		}

		@Override
		public E next() {
		  if(next == null) {
			next = tail;
			return head;
		  }
		  E item = next.head();
		  next = next.tail();
		  return item;
		}
	  };
	}

	@Override
	public IPList<E> put(int index, E value) {
	  LList<E> r = LList.empty();
	  if(index < 0) {
		throw new IndexOutOfBoundsException("< 0");
	  }
	  for(E v : this) {
		if(index == 0) {
		  r = r.prepend(value);
		}
		else {
		  r = r.prepend(v);
		}
		index--;
	  }
	  if(index >= 0) {
		throw new IndexOutOfBoundsException();
	  }
	  return reversed();
	}

	@Override
	public String toString() {
	  return head + " :: " + tail;
	}

	  @Override
	  public <R> R match(Supplier<R> emptyList, Function<E, R> singleton, Function<E, Function<IPList<E>, R>> headTail
	  ) {
		  if(tail.isEmpty()){
		  	return singleton.apply(head);
		  }
		  return headTail.apply(head).apply(tail);
	  }
  }


}
