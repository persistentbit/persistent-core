package com.persistentbit.core.collections;

import com.persistentbit.core.utils.IO;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.Serializable;
import java.util.Arrays;
import java.util.Iterator;

/**
 * A IPList based on a java byte array byte[]
 *
 * @author petermuys
 * @since 7/11/16
 */
public final class PByteList extends AbstractIPList<Byte, PByteList> implements Serializable{

	private static final PByteList emptyInstance = new PByteList(new byte[0]);
	private final byte[] data;

	private PByteList(byte[] data) {
		this.data = data;
	}

	public static PByteList empty() {
		return emptyInstance;
	}

	public static PByteList from(Iterable<Byte> iterable) {
		if(iterable instanceof PByteList) {
			return (PByteList) iterable;
		}
		PStream<Byte> stream  = PStream.from(iterable);
		int           count   = stream.size();
		byte[]        newData = new byte[count];
		int           index   = 0;
		for(Byte b : stream) {
			newData[index++] = b == null ? 0 : b;
		}
		return new PByteList(newData);
	}

	public static PByteList val(byte... values) {
		return new PByteList(Arrays.copyOf(values, values.length));
	}

	public static PByteList from(InputStream in) {
		ByteArrayOutputStream bout = new ByteArrayOutputStream();
		IO.copy(in, bout);
		return new PByteList(bout.toByteArray());
	}

	public InputStream getInputStream() {
		return new ByteArrayInputStream(data);
	}

	@Override
	public Byte get(int index) {
		return data[index];
	}

	@Override
	public PByteList put(int index, Byte value) {
		byte[] newData = Arrays.copyOf(data, data.length);
		newData[index] = value == null ? 0 : value;
		return new PByteList(newData);
	}

	@Override
	public Iterator<Byte> iterator() {
		return new Iterator<Byte>(){
			int index;

			@Override
			public boolean hasNext() {
				return index < data.length;
			}

			@Override
			public Byte next() {
				return data[index++];
			}
		};
	}

	@Override
	public PByteList clear() {
		return empty();
	}

	@Override
	public PByteList filterNulls() {
		return this;
	}


	@Override
	public PByteList plusAll(Iterable<? extends Byte> iter) {
		byte[] newData;
		if(iter instanceof PByteList) {
			PByteList other = (PByteList) iter;
			newData = new byte[data.length + other.data.length];
			System.arraycopy(data, 0, newData, 0, data.length);
			System.arraycopy(other.data, 0, newData, data.length, other.data.length);
		}
		else {
			PStream<? extends Byte> stream = PStream.from(iter);
			int                     count  = stream.size();
			newData = Arrays.copyOf(data, data.length + count);
			int index = data.length;
			for(Byte b : stream) {
				newData[index++] = b == null ? 0 : b;
			}
		}

		return new PByteList(newData);
	}


	@Override
	protected PByteList toImpl(PStream<Byte> lazy) {
		if(lazy instanceof PByteList) {
			return (PByteList) lazy;
		}
		int    size    = lazy.size();
		byte[] newData = new byte[size];
		int    index   = 0;
		for(Byte b : lazy) {
			newData[index++] = b == null ? 0 : b;
		}

		return new PByteList(newData);
	}


	@Override
	public boolean equals(Object o) {
		if(o == this) {
			return true;
		}
		if(o instanceof PByteList) {
			PByteList ba = (PByteList) o;
			return Arrays.equals(data, ba.data);
		}
		else if(o instanceof PStream) {
			Iterator<?> i2;
			if(o instanceof IPList) {
				IPList p = (IPList) o;
				i2 = p.iterator();
			}
			else {
				return false;
			}
			Iterator<Byte> i1 = iterator();

			while(i1.hasNext() && i2.hasNext()) {
				Byte   o1 = i1.next();
				Object o2 = i2.next();
				if(o2 == null) { o2 = 0; }
				if(!o1.equals(o2)) {
					return false;
				}
			}
			return !(i1.hasNext() || i2.hasNext());
		}

		return false;
	}
}
