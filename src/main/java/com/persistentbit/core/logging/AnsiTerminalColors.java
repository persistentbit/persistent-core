package com.persistentbit.core.logging;

/**
 * TODOC
 *
 * @author petermuys
 * @since 18/12/16
 */
public class AnsiTerminalColors{
	private final boolean colorsOn;
	public final ColorCmd fg;
	public final ColorCmd bg;


	public final String  reset;
	public final OnOfCmd bold;
	public final OnOfCmd faint;
	public final OnOfCmd italic;
	public final OnOfCmd underline;
	public final OnOfCmd blinkSlow;
	public final OnOfCmd blinkFast;
	public final OnOfCmd reverse;
	public final OnOfCmd invisible;
	public final OnOfCmd crossedOut;
	public AnsiTerminalColors(boolean colorsOn){
		this.colorsOn = colorsOn;
		this.reset     = toAnsi(0);
		this.bold     = new OnOfCmd(1,21);
		this.faint     =  new OnOfCmd(2,22);
		this.italic    =  new OnOfCmd(3,23);
		this.underline =  new OnOfCmd(4,24);
		this.blinkSlow =  new OnOfCmd(5,25);
		this.blinkFast =  new OnOfCmd(6,25);
		this.reverse   =  new OnOfCmd(7,27);
		this.invisible =  new OnOfCmd(8,28);
		this.crossedOut = new OnOfCmd(9,29);
		this.fg = new ColorCmd(ColorType.fg);
		this.bg = new ColorCmd(ColorType.bg);
	}

	public AnsiTerminalColors() {
		this(true);
	}

	private enum ColorType{bg, fg}

	public class ColorCmd{

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
	public class OnOfCmd{
		public final String on;
		public final String off;
		public OnOfCmd(int onCode, int offCode){
			this.on = toAnsi(onCode);
			this.off = toAnsi(offCode);
		}
		public String toString() {
			return on;
		}
	}






	private String toAnsi(int colorCode) {
		return colorsOn ? "\u001B[" + colorCode + "m" : "";
	}

	static public void main(String...args){
		AnsiTerminalColors col = new AnsiTerminalColors(true);
		System.out.println(col.fg.green + "OK message");
		System.out.println(col.fg.yellow + col.bold + "functionCall(a,b)");
		System.out.println(col.reset + "This is normal " + col.reset);
		System.out.println(col.bold + "This is Bold " + col.reset);
		System.out.println(col.faint.on + "This is Faint " + col.reset);
		System.out.println(col.underline.on + "This is Underline " + col.reset);
		System.out.println(col.italic.on + "This is Italic " + col.reset);
		System.out.println(col.crossedOut.on + "This is CrossedOut " + col.reset);
		System.out.println(col.reverse.on + "This is reverse " + col.reset);
		System.out.println(col.fg.red + "This red");
		System.out.println(col.fg.yellow + "This yellow");
		System.out.println(col.fg.green + "This green");
		System.out.println(col.fg.black + "This black");
		System.out.println(col.fg.blue + "This blue");
		System.out.println(col.fg.cyan + "This cyan");
		System.out.println(col.fg.magenta + "This magenta");
		System.out.println(col.fg.white+ "This white");

		System.out.println(col.bold + col.fg.red + "This is bold red");
		System.out.println(col.fg.red + "This red");
		System.out.println(col.fg.yellow + "This yellow");
		System.out.println(col.fg.green + "This green");
		System.out.println(col.fg.black + "This black");
		System.out.println(col.fg.blue + "This blue");
		System.out.println(col.fg.cyan + "This cyan");
		System.out.println(col.fg.magenta + "This magenta");
		System.out.println(col.fg.white+ "This white");

	}
}
