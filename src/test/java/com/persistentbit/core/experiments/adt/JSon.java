package com.persistentbit.core.experiments.adt;

import com.persistentbit.core.collections.PList;
import com.persistentbit.core.collections.PMap;
import com.persistentbit.core.printing.PrintableText;
import com.persistentbit.core.utils.UString;

import java.util.function.Function;

/**
 * TODOC
 *
 * @author petermuys
 * @since 19/01/17
 */
public abstract class JSon{

	private JSon() {}

	abstract <T> T match(
		Function<JNull, T> aNull,
		Function<JNumber, T> aNumber,
		Function<JObject, T> aObject,
		Function<JList, T> aList,
		Function<JString, T> aString
	);

	public static final class JNull extends JSon{

		@Override
		<T> T match(Function<JNull, T> aNull, Function<JNumber, T> aNumber, Function<JObject, T> aObject,
					Function<JList, T> aList, Function<JString, T> aString
		) {
			return aNull.apply(this);
		}
	}

	public static final class JNumber extends JSon{

		public final Number value;

		public JNumber(Number value) {
			this.value = value;
		}

		@Override
		<T> T match(Function<JNull, T> aNull, Function<JNumber, T> aNumber, Function<JObject, T> aObject,
					Function<JList, T> aList, Function<JString, T> aString
		) {
			return aNumber.apply(this);
		}
	}

	public static final class JObject extends JSon{

		public final PMap<String, JSon> properties;

		public JObject(
			PMap<String, JSon> properties
		) {
			this.properties = properties;
		}

		@Override
		<T> T match(Function<JNull, T> aNull, Function<JNumber, T> aNumber, Function<JObject, T> aObject,
					Function<JList, T> aList, Function<JString, T> aString
		) {
			return aObject.apply(this);
		}
	}

	public static final class JList extends JSon{

		public final PList<JSon> values;

		public JList(PList<JSon> values) {
			this.values = values;
		}

		@Override
		<T> T match(Function<JNull, T> aNull, Function<JNumber, T> aNumber, Function<JObject, T> aObject,
					Function<JList, T> aList, Function<JString, T> aString
		) {
			return aList.apply(this);
		}
	}

	public static final class JString extends JSon{

		public final String value;

		public JString(String value) {
			this.value = value;
		}

		@Override
		<T> T match(Function<JNull, T> aNull, Function<JNumber, T> aNumber, Function<JObject, T> aObject,
					Function<JList, T> aList, Function<JString, T> aString
		) {
			return aString.apply(this);
		}
	}

	public static JNull jnull() {
		return new JNull();
	}

	public static JNumber jnumber(Number n) {
		return new JNumber(n);
	}

	public static JObject jobject() {
		return new JObject(PMap.empty());
	}

	public static JList jlist(JSon... values) {
		return new JList(PList.val(values));
	}

	public static JString jstring(String value) {
		return new JString(value);
	}

	public static PrintableText prettyPrint(JSon json) {
		return json.match(
			aNull -> PrintableText.fromString("null"),
			aNumber -> PrintableText.fromString(aNumber.toString()),
			aObject -> printer -> {
				printer.println("{");
				printer.print(PrintableText.indent(ip -> {
													   aObject.properties.forEach(nameValue -> {
														   ip.println("\"" + nameValue._1 + "\" : " + prettyPrint(nameValue._2).printToString());
													   });
												   }
				));
			},
			aList -> PrintableText
				.fromString(aList.values.map(item -> prettyPrint(item).printToString()).toString("[", ", ", "]")),
			aString -> PrintableText.fromString("\"" + UString.escapeToJavaString(aString.value) + "\"")

		);
	}
}
