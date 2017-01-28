package com.persistentbit.core.utils;

import com.persistentbit.core.NotNullable;
import com.persistentbit.core.collections.PList;
import com.persistentbit.core.logging.Log;

import java.text.Normalizer;
import java.util.Objects;
import java.util.function.Function;

/**
 * General String utilities, because we all have to have our own  StringUtils version
 */
public final class StringUtils{

	/**
	 * Takes a raw string and converts it to a java code string:<br>
	 * <ul>
	 * <li>tab to \t</li>
	 * <li>newline to \n</li>
	 * <li>cr to \r</li>
	 * <li>\ to \\</li>
	 * <li>backspace to \b</li>
	 * <li>" to \"</li>
	 * <li>\ to \'</li>
	 * </ul>
	 *
	 * @param s The unescaped string (can't be null)
	 *
	 * @return The escaped string
	 *
	 * @see #unEscapeJavaString(String)
	 */
	public static String escapeToJavaString(String s) {
		return Log.function(s).code(l -> {
			Objects.requireNonNull(s, "Can't escape a null string");
			StringBuilder sb = new StringBuilder(s.length() + 4);
			for(int t = 0; t < s.length(); t++) {
				char c = s.charAt(t);
				if(c == '\t') {
					sb.append("\\t");
				}
				else if(c == '\n') {
					sb.append("\\n");
				}
				else if(c == '\r') {
					sb.append("\\r");
				}
				else if(c == '\\') {
					sb.append("\\\\");
				}
				else if(c == '\b') {
					sb.append("\\b");
				}
				else if(c == '\"') {
					sb.append("\\\"");
				}
				else if(c == '\'') {
					sb.append("\\\'");
				}
				else {
					sb.append(c);
				}
			}
			return sb.toString();
		});
	}

	/**
	 * Does the reverse of {@link #escapeToJavaString(String)}
	 *
	 * @param s The java source escaped string
	 *
	 * @return The unescaped string
	 */
	public static String unEscapeJavaString(String s) {
		return Log.function(s).code(l -> {
			Objects.requireNonNull(s, "Can't unescape a null string");
			StringBuilder sb          = new StringBuilder(10);
			boolean       prevSpecial = false;
			for(int t = 0; t < s.length(); t++) {
				char c = s.charAt(t);
				if(prevSpecial) {
					switch(c) {
						case 't':
							sb.append('\t');
							break;
						case '\\':
							sb.append('\\');
							break;
						case 'n':
							sb.append('\n');
							break;
						case 'r':
							sb.append('\r');
							break;
						case 'b':
							sb.append('\b');
							break;
						case '\"':
							sb.append('\"');
							break;
						case '\'':
							sb.append('\'');
							break;
						default:
							sb.append('\\').append(c);
							break;
					}
					prevSpecial = false;
				}
				else {
					if(c == '\\') {
						prevSpecial = true;
					}
					else {
						//TOFIX  prevSpecial is always false here
						if(prevSpecial) {
							sb.append('\\');
							prevSpecial = false;
						}
						sb.append(c);
					}
				}

			}
			if(prevSpecial) {
				sb.append('\\');
			}

			return sb.toString();
		});
	}

	/**
	 * Convert the first character in the given string to UpperCase.
	 *
	 * @param s String to convert, can't be null
	 *
	 * @return The new string with the first character in uppercase and the rest as it was.
	 */
	public static String firstUpperCase(@NotNullable String s) {
		Objects.requireNonNull(s);
		if(s.isEmpty()) { return s; }
		return Character.toUpperCase(s.charAt(0)) + s.substring(1);
	}

	/**
	 * Convert the first character in the given string to LowerCase.
	 *
	 * @param s String to convert, can't be null
	 *
	 * @return The new string with the first character in lowercase and the rest as it was.
	 */
	public static String firstLowerCase(@NotNullable String s) {
		Objects.requireNonNull(s);
		if(s.isEmpty()) { return s; }
		return Character.toLowerCase(s.charAt(0)) + s.substring(1);
	}

	/**
	 * Drop the last charCount chars from a string
	 *
	 * @param txt       A Non null string
	 * @param charCount The number of characters to drop
	 *
	 * @return the string with dropped chars.
	 */
	public static String dropLast(@NotNullable String txt, int charCount) {
		Objects.requireNonNull(txt);
		if(txt.length() <= charCount) {
			return "";
		}
		return txt.substring(0, txt.length() - charCount);
	}

	/**
	 * Splits a string on a combination of \r\n \n or \r.
	 *
	 * @param s The String to split
	 *
	 * @return A PList of Strings without the nl or cr characters
	 */
	public static PList<String> splitInLines(String s) {
		Objects.requireNonNull(s);
		PList<String> res = PList.empty();
		for(String line : s.split("\\r\\n|\\n|\\r")) {
			res = res.plus(line);
		}
		return res;
	}

	/**
	 * converts aStringInCamelCase to a_string_in_snake
	 *
	 * @param s The Non null string in camelCase
	 *
	 * @return The snake version of the name
	 * @see #snake_toCamelCase(String)
	 */
	public static String camelCaseTo_snake(String s) {
		Objects.requireNonNull(s);
		return s.replaceAll("([a-z])([A-Z]+)", "$1_$2");
	}

	/**
	 * converts a_string_in_snake to aStringInSnake
	 *
	 * @param s The Non null string in snake
	 *
	 * @return The camelCase version of the name
	 *
	 * @see #camelCaseTo_snake(String)
	 */
	public static String snake_toCamelCase(String s) {
		Objects.requireNonNull(s);
		StringBuilder res       = new StringBuilder();
		boolean       nextUpper = false;
		for(int t = 0; t < s.length(); t++) {
			char c = s.charAt(t);
			if(nextUpper) {
				c = Character.toUpperCase(c);
				nextUpper = false;
			}
			if(c == '_') {
				nextUpper = true;
			}
			else {
				res.append(c);
			}
		}
		return res.toString();
	}

	/**
	 * Make the given string have a minimum length by left padding the String with the given char
	 *
	 * @param str     The string
	 * @param length  The minimum length
	 * @param padding The padding char
	 *
	 * @return The new string
	 */
	public static String padLeft(String str, int length, char padding) {
		while(str.length() < length) {
			str = padding + str;
		}
		return str;
	}

	/**
	 * Make the given string have a minimum length by right padding the String with the given char
	 *
	 * @param str     The string
	 * @param length  The minimum length
	 * @param padding The padding char
	 *
	 * @return The new string
	 */
	public static String padRight(String str, int length, char padding) {
		while(str.length() < length) {
			str += padding;
		}
		return str;
	}

	@Deprecated
	public static String present(String org, int maxLength){
		if(org == null){
			return null;
		}
		if(org.length() <=maxLength){
			return org;
		}
		String str = org.substring(0,Math.min(org.length(),maxLength-1));

		String kleiner = str;
		while(kleiner.length() != 0 && "\t\n\r ".contains(kleiner.substring(kleiner.length()-1)) == false){
			kleiner = kleiner.substring(0,kleiner.length()-1);
		}
		if(kleiner.length() == 0){
			kleiner = str;
		}
		return kleiner + "";
	}


	/**
	 * Split text on '-' and ' '.<br>
	 *
	 * @param longString The text to split
	 * @param maxLength  The maximum length per line
	 * @return the result stream
	 */
	public static PList<String> splitOnWhitespaceAndHyphen(String longString, int maxLength){
		return splitStringOnMaxLength(
				longString,
				"(?<=\\-)|(?<=\\s)",
				false,
				maxLength,
				s -> s.trim()
		);

	}



	/**
	 * Split a text into lines with a maximum length.<br>
	 * Expects a regular expression to find the split point.<br>
	 * This regular expression should keep te delimiter, so use something
	 * like (?=char) or (?&lt;=char) as regular expression.
	 * Example with space and '-' as delimiters: '(?&lt;=\-)|(?&lt;=\s)"
	 * @param longString      The String to split
	 * @param whiteSpaceRegEx Regular Expression for finding the split locations
	 * @param splitLongWords  Split words longer that the maximum length ?
	 * @param maxLength The maximum length per lines
	 * @param postProcessLine Function used for building the final line out of the constructed line. Could be used to trim the resulting line
	 * @return List of lines
	 */
	public static PList<String> splitStringOnMaxLength(
			String longString,
			String whiteSpaceRegEx,
			boolean splitLongWords,
			int maxLength,
			Function<String,String> postProcessLine

	){
		Objects.requireNonNull(longString,"longString");

		if(maxLength <1){
			throw new IllegalArgumentException("maxLength must be >=1");
		}

		if(longString.length()<=maxLength){
			return PList.val(longString);
		}

		PList<String> lines = PList.empty();
		String currentLine="";
		for(String word : longString.split(whiteSpaceRegEx)){
			String newLine = (currentLine.isEmpty() ? word : currentLine + word);
			String newLineProcessed = postProcessLine.apply(newLine);
			if(newLineProcessed.length() <= maxLength){
				currentLine = newLine;
			} else {
				newLineProcessed = postProcessLine.apply(currentLine);
				if(newLineProcessed.isEmpty() == false) {
					lines = lines.plus(newLineProcessed);
				}
				currentLine = word;

				if(splitLongWords) {
					newLineProcessed = postProcessLine.apply(currentLine);
					while (newLineProcessed.length() > maxLength) {
						lines = lines.plus(postProcessLine.apply(currentLine.substring(0, maxLength)));
						currentLine = currentLine.substring(maxLength);
						newLineProcessed = postProcessLine.apply(currentLine);
					}
				}
			}
		}
		if(currentLine.isEmpty() == false){
			lines = lines.plus(currentLine);
		}
		return lines;
	}

	public static String deleteWhitespace(String str){
		Objects.requireNonNull(str);
		int cnt = str.length();
		if(cnt == 0){
			return str;
		}
		char[] dest = new char[cnt];
		int resultLength = 0;
		for(int i=0; i<cnt; i++){
			if (!Character.isWhitespace(str.charAt(i))) {
				dest[resultLength++] = str.charAt(i);
			}
		}
		if(resultLength == cnt){
			return str;
		}
		return new String(dest,0,resultLength);
	}


	/**
	 * Convert a String to a Searchable version without accents,spaces, all uppercasel
	 * @param normalString The String to convert
	 * @return The Searchable version.
	 */
	public static String createSearchableString(String normalString)
	{
		Objects.requireNonNull(normalString, "omschrijving");
		if(normalString.trim().length() == 0){
			return "";
		}
		String alfaKey;

		alfaKey = deleteWhitespace(normalString);//delete alle whitespaces
		//replace alle speciale leestekens op een letter door de gewone letter
		alfaKey = alfaKey.replaceAll("\\p{Punct}", "");//delete alle andere non-word characters

		//ae oe ed. symbolen opvangen en replace door equivalent bv ae symbool word ae letters
		alfaKey = alfaKey.replaceAll("\306", "AE");
		alfaKey = alfaKey.replaceAll("\346", "ae");
		alfaKey = alfaKey.replaceAll("\330", "O");
		alfaKey = alfaKey.replaceAll("\370", "o");
		alfaKey = alfaKey.replaceAll("\226", "OE");
		alfaKey = alfaKey.replaceAll("\234", "oe");
		alfaKey = alfaKey.replaceAll("\320", "D");
		alfaKey = alfaKey.replaceAll("\360", "d");

		char[] normalized = Normalizer.normalize(alfaKey, Normalizer.Form.NFD).toCharArray();
		if (normalized.length > alfaKey.length())//accented letters vervangen door gewone letters (bv é -> e)
		{
			StringBuilder sb = new StringBuilder();
			for (int i = 0; i < normalized.length; i++)
			{
				String str = Character.toString(normalized[i]);
				str = str.replaceAll("\\W", "");//de accented vervangen door een lege string
				sb.append(str);
			}

			alfaKey = sb.toString();
		}

		return alfaKey.toUpperCase();
	}
}
