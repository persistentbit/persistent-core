package com.persistentbit.core.logging;

/**
 * TODOC
 *
 * @author petermuys
 * @since 18/12/16
 */
public class AnsiTerminalColors{

	private enum ColorType{bg, fg}

	class ColorCmd{

		private final ColorType colorType;
		public final  String    black;
		public final  String    red;
		public final  String    green;
		public final  String    yellow;
		public final  String    blue;
		public final  String    magenta;
		public final  String    cyan;
		public final  String    white;

		public ColorCmd(ColorType colorType) {
			this.colorType = colorType;
			int offset = colorType == ColorType.fg ? 30 : 40;
			black = toAnsi(offset + 0);
			red = toAnsi(offset + 1);
			green = toAnsi(offset + 2);
			yellow = toAnsi(offset + 3);
			blue = toAnsi(offset + 4);
			magenta = toAnsi(offset + 5);
			cyan = toAnsi(offset + 6);
			white = toAnsi(offset + 7);

		}

	}

	public final ColorCmd fg = new ColorCmd(ColorType.fg);
	public final ColorCmd bg = new ColorCmd(ColorType.bg);


	public final String reset     = toAnsi(0);
	public final String bold      = toAnsi(1);
	public final String faint     = toAnsi(2);
	public final String italic    = toAnsi(3);
	public final String underline = toAnsi(4);
	public final String blinkSlow = toAnsi(5);
	public final String blinkFast = toAnsi(6);
	public final String reverse   = toAnsi(7);


	private String toAnsi(int colorCode) {
		return "\u001B[" + colorCode + "m";
	}
}
