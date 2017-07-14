package com.persistentbit.core.collections;

import java.io.Serializable;
import java.util.Arrays;
import java.util.Iterator;
import java.util.function.Function;
import java.util.function.Supplier;

/**
 * An {@link IPList} of booleans where the 8 bits of data are stored per byte in a byte[].<br>
 *
 * @author petermuys
 * @since 7/11/16
 */
public class PBitList  extends AbstractIPList<Boolean, PBitList> implements Serializable{

	private static final PBitList emptyInstance = new PBitList(0,new byte[0]);
	private final int bitSize;
	private final byte[] data;

	private PBitList(int bitSize, byte[] data) {
		this.bitSize = bitSize;
		this.data = data;
	}

	@Override
	public String toString() {
		return "PBitList[" + bitSize + ", " + toBinaryString() + "]";
	}

	public String toBinaryString() {
		StringBuffer sb = new StringBuffer(bitSize);
		for(Boolean b : this){
			sb.append(b ? '1' : '0');
		}
		return sb.toString();
	}

	@Override
	public int size() {
		return bitSize;
	}

	public static PBitList empty() {
		return emptyInstance;
	}

	public static PBitList from(Iterable<Boolean> iterable) {
		if(iterable instanceof PBitList) {
			return (PBitList) iterable;
		}
		PStream<Boolean> stream  = PStream.from(iterable);
		int           count   = stream.size();
		byte[]        newData = new byte[bitSizeToByteSize(count)];
		int           index   = 0;
		for(Boolean b : stream) {
			newData[index/8] = setByteBit(newData[index/8],index % 8,b == null ? false : b);
			index++;
		}
		return new PBitList(count,newData);
	}

	public static int bitSizeToByteSize(int bitSize){
		return (bitSize+7)/8;
	}

	static public byte	setByteBit(byte byteData, int index, boolean value){
		int mask = 0x80 >>> index;
		if(value){
			return (byte)((byteData | mask) & 0xff);
		} else {
			return (byte)((byteData & (~mask)) & 0xff);
		}
	}

	static public boolean getByteBit(byte byteData, int index){
		int mask = 0x80 >>> index;
		return (byteData & mask) != 0;
	}



	public static PBitList val(boolean... values) {
		int           count   = values.length;
		byte[]        newData = new byte[bitSizeToByteSize(count)];
		int           index   = 0;
		for(boolean b : values) {
			newData[index/8] = setByteBit(newData[index/8],index % 8,b);
			index++;
		}
		return new PBitList(count,newData);
	}




	@Override
	public Boolean get(int index) {
		if(index<0 || index >= bitSize){
			throw new ArrayIndexOutOfBoundsException("Can't access bit " + index + " in bit list with " + bitSize + " bits");
		}
		return getByteBit(data[index/8],index % 8);
	}

	@Override
	public PBitList put(int index, Boolean value) {
		value = value == null ? false : value;
		if(get(index) == value){
			return this;
		}
		byte[] newData = Arrays.copyOf(data, data.length);
		newData[index/8] = setByteBit(newData[index/8],index % 8, value);
		return new PBitList(bitSize,newData);
	}

	@Override
	public Iterator<Boolean> iterator() {
		return new Iterator<Boolean>(){
			int index;

			@Override
			public boolean hasNext() {
				return index < bitSize;
			}

			@Override
			public Boolean next() {
				return get(index++);
			}
		};
	}

	@Override
	public PBitList clear() {
		return empty();
	}

	@Override
	public PBitList filterNulls() {
		return this;
	}


	@Override
	public PBitList plusAll(Iterable<? extends Boolean> iter) {
		byte[] newData;
		PStream<? extends Boolean> stream = PStream.from(iter);
		int count  = stream.size();
		int newLength = bitSizeToByteSize(bitSize + count);
		newData = Arrays.copyOf(data, newLength);
		int index = bitSize;
		for(Boolean b : stream) {
			b = b == null ? false : b;
			newData[index/8] = setByteBit(newData[index/8],index % 8, b);
			index++;
		}

		return new PBitList(bitSize + count,newData);
	}


	@Override
	protected PBitList toImpl(PStream<Boolean> lazy) {
		if(lazy instanceof PBitList) {
			return (PBitList) lazy;
		}
		return from(lazy);

	}


	@Override
	public boolean equals(Object o) {
		if(o == this) {
			return true;
		}
		if(o instanceof PBitList) {
			PBitList ba = (PBitList) o;
			return bitSize == ba.bitSize && Arrays.equals(data, ba.data);
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
			Iterator<Boolean> i1 = iterator();

			while(i1.hasNext() && i2.hasNext()) {
				Boolean   o1 = i1.next();
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
		return Arrays.hashCode(data) + bitSize;
	}

	@Override
	public <R> R match(Supplier<R> emptyList, Function<Boolean, R> singleton,
					   Function<IPList<Boolean>, R> list
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
