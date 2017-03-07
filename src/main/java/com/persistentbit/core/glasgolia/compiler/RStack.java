package com.persistentbit.core.glasgolia.compiler;

import com.persistentbit.core.collections.LList;
import com.persistentbit.core.printing.PrintTextWriter;
import com.persistentbit.core.printing.PrintableText;
import com.persistentbit.core.utils.ToDo;

import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * TODOC
 *
 * @author petermuys
 * @since 3/03/17
 */
public interface RStack extends PrintableText{

	void addFrame(int size);

	default void addFrame(Object[] frame) {
		addFrame(frame.length);
		for(int t = 0; t < frame.length; t++) {
			set(t, frame[t]);
		}
	}

	RStack reset();

	void popFrame();

	Object get(int index);

	void set(int index, Object value);

	class ReplStack implements RStack{

		private LList<Map<Integer, Object>> frames = LList.<Map<Integer, Object>>empty().plus(new HashMap<>());

		@Override
		public RStack reset() {
			return new ReplStack();
		}

		@Override
		public void addFrame(int size) {
			frames = frames.prepend(new HashMap<>());
		}

		@Override
		public void popFrame() {
			frames = frames.tail();
		}

		@Override
		public Object get(int index) {
			return frames.head().get(index);
		}

		@Override
		public String toString() {
			Map<Integer, Object> thisFrame = frames.head();
			String str = thisFrame.entrySet().stream().sorted((a, b) -> a.getKey().compareTo(b.getKey()))
								  .map(e -> "" + e.getKey() + "=" + e.getValue()).collect(Collectors
					.joining(", "));
			return "RFrame(" + str + ")";
		}

		@Override
		public void set(int index, Object value) {
			frames.head().put(index, value);
		}


		@Override
		public void print(PrintTextWriter out) {
			out.println("ReplStack { ");
			out.indent(print(frames));
			out.println("}");

		}

		private PrintableText print(LList<Map<Integer, Object>> frames) {
			return out -> {
				if(frames.isEmpty()) {
					return;
				}
				Map<Integer, Object> thisFrame = frames.head();
				List<String>         items     =
					thisFrame.entrySet().stream().sorted(Comparator.comparing(Map.Entry::getKey))
							 .map(e -> "" + e.getKey() + "=" + e.getValue()).collect(Collectors.toList());
				items.forEach(item -> out.println(item));
				out.indent(print(frames.tail()));
			};
		}

	}

	class RuntimeStack implements RStack{

		private ThreadLocal<LList<Object[]>> threadLocalFrames = new ThreadLocal<>();
		private ThreadLocal<Object[]> current = new ThreadLocal<>();

		public void addFrame(int size) {
			addFrame(new Object[size]);
		}

		public void addFrame(Object[] frame) {
			if(current != null) {
				LList<Object[]> localStack = threadLocalFrames.get();
				localStack = localStack == null ? LList.<Object[]>empty().plus(frame) : localStack.prepend(frame);
				threadLocalFrames.set(localStack);
			}
			current.set(frame);
		}
		@Override
		public RStack reset() {
			return new RuntimeStack();
		}
		@Override
		public void popFrame() {
			LList<Object[]> localStack = threadLocalFrames.get();
			current.set(localStack.head());
			localStack = localStack.tail();
			if(localStack.isEmpty()) {
				threadLocalFrames.set(null);
			}
			else {
				threadLocalFrames.set(localStack);
			}
		}

		@Override
		public Object get(int index) {
			return current.get()[index];
		}

		@Override
		public void set(int index, Object value) {
			current.get()[index] = value;
		}

		@Override
		public void print(PrintTextWriter out) {
			throw new ToDo();
		}

	}
}
