package com.persistentbit.core.collections;

import com.persistentbit.core.utils.IO;
import com.persistentbit.core.utils.UString;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.Serializable;
import java.nio.charset.Charset;
import java.util.Arrays;
import java.util.Base64;
import java.util.Iterator;
import java.util.function.Function;
import java.util.function.Supplier;

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

	/**
	 * Read an input stream in a new PByteList.<br>
	 * Closes the inputstream after reading.<br>
	 * @param in The InputStream to read
	 *
	 * @return The PByteList with the content from the inputStream.
	 *
	 * @see #getInputStream()
	 */
	public static PByteList from(InputStream in) {
		ByteArrayOutputStream bout = new ByteArrayOutputStream();
		IO.copy(in, bout);
		return new PByteList(bout.toByteArray());
	}

	public static PByteList from(byte[] bytes){
		return new PByteList(Arrays.copyOf(bytes,bytes.length));
	}

	public static PByteList from(byte[] bytes, int offset, int length) {
		byte[] newArr = new byte[length];
		System.arraycopy(bytes, offset, newArr, 0, length);
		return new PByteList(newArr);
	}

	/**
	 * Get the data as a InputStream.<br>
	 * @return The inputstream containing this byte data
	 * @see #from(InputStream)
	 */
	public InputStream getInputStream() {
		return new ByteArrayInputStream(data);
	}

	/**
	 * Convert this byte array to a base64 encoded string
	 *
	 * @return The encoded string
	 */
	public String toBase64String() {
		return Base64.getEncoder().encodeToString(data);
	}

	public static PByteList fromBase64String(String base64EncodedString) {
		return new PByteList(Base64.getDecoder().decode(base64EncodedString));
	}

	public String toHexString() {
		StringBuilder sb = new StringBuilder(size() * 2);
		forEach(b -> sb.append(UString.padLeft(Integer.toHexString(b & 0xff), 2, '0')));
		return sb.toString();
	}

	public byte[] toByteArray() {
		byte[] res = new byte[data.length];
		System.arraycopy(data,0,res,0,data.length);
		return res;
	}

	/**
	 * Convert this bytelist to a String
	 *
	 * @param encoding Charset encoding
	 *
	 * @return The String
	 */
	public String toText(Charset encoding) {
		return new String(data, encoding);
	}

	public static PByteList fromHexString(String hexEncodedString) {
		byte[] data = new byte[hexEncodedString.length() / 2];
		for(int t = 0; t < data.length; t++) {
			data[t] = Integer.valueOf(hexEncodedString.substring(t * 2, t * 2 + 2), 16).byteValue();
		}
		return new PByteList(data);
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

	@Override
	public int hashCode() {
		return Arrays.hashCode(data);
	}

	@Override
	public <R> R match(Supplier<R> emptyList, Function<Byte, R> singleton,
					   Function<IPList<Byte>, R> list
	) {
		if(isEmpty()){
			return emptyList.get();
		}
		if(size() == 1){
			return singleton.apply(head());
		}
		return list.apply(this);
	}
}
