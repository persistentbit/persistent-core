package com.persistentbit.core.logging;

/**
 * Helper class for styling text in terminal using ansi codes.
 *
 * @author Peter Muys
 * @since 20/12/2016
 */
public class AnsiColor {
    private final boolean active;
    private final String cmdString;

    public AnsiColor(boolean active, String cmdString) {
        this.active = active;
        this.cmdString = cmdString;
    }



    public AnsiColor(boolean active) {
        this(active, "");
    }

    public AnsiColor() {
        this(true);
    }

    public boolean isActive() {
        return active;
    }

    private AnsiColor add(String cmdCode) {
        return new AnsiColor(active, cmdString.isEmpty() ? cmdCode : cmdString + ";" + cmdCode);
    }

    public AnsiColor fgBlack() {
        return add("30");
    }

    public AnsiColor bgBlack() {
        return add("40");
    }

    public AnsiColor fgRed() {
        return add("31");
    }

    public AnsiColor bgRed() {
        return add("41");
    }

    public AnsiColor fgGreen() {
        return add("32");
    }

    public AnsiColor bgGreen() {
        return add("42");
    }

    public AnsiColor fgYellow() {
        return add("33");
    }

    public AnsiColor bgYellow() {
        return add("43");
    }

    public AnsiColor fgBlue() {
        return add("34");
    }

    public AnsiColor bgBlue() {
        return add("44");
    }

    public AnsiColor fgMagenta() {
        return add("35");
    }

    public AnsiColor bgMagenta() {
        return add("45");
    }

    public AnsiColor fgCyan() {
        return add("36");
    }

    public AnsiColor bgCyan() {
        return add("46");
    }

    public AnsiColor fgWhite() {
        return add("37");
    }

    public AnsiColor bgWhite() {
        return add("47");
    }

    public AnsiColor fgDefault() {
        return add("39");
    }

    public AnsiColor bgDefault() {
        return add("49");
    }


    public AnsiColor reset() {
        return add("0");
    }

    public AnsiColor bold() {
        return add("1");
    }

    public AnsiColor boldOff() {
        return add("21");
    }

    public AnsiColor faint() {
        return add("2");
    }

    public AnsiColor faintOff() {
        return add("22");
    }

    public AnsiColor italic() {
        return add("3");
    }

    public AnsiColor italicOff() {
        return add("23");
    }

    public AnsiColor underline() {
        return add("4");
    }

    public AnsiColor underlineOff() {
        return add("24");
    }

    public AnsiColor blinkSlow() {
        return add("5");
    }

    public AnsiColor blinkFast() {
        return add("6");
    }

    public AnsiColor blinkOff() {
        return add("25");
    }

    public AnsiColor reverse() {
        return add("7");
    }

    public AnsiColor reverseOff() {
        return add("27");
    }

    public AnsiColor invisible() {
        return add("8");
    }

    public AnsiColor invisibleOff() {
        return add("28");
    }

    public AnsiColor crossedOut() {
        return add("9");
    }

    public AnsiColor crossedOutOff() {
        return add("29");
    }


    public String toString() {
        return active ? "\u001B[" + cmdString + "m" : "";
    }


    static public void main(String... args) {
        AnsiColor col = new AnsiColor();
        System.out.println(col.fgYellow().underline() + "Bravo" + col.underlineOff().crossedOut().italic().fgRed().bold().blinkSlow() + " test");
        System.out.println(col.fgRed() + "Gewoon rood " + col.bold().fgRed() + " met bold");
        System.out.println(col.fgWhite() + "Gewoon wit " + col.bold().fgWhite() + " met bold");
        System.out.println(col.fgYellow() + "Gewoon geel " + col.bold().fgYellow() + " met bold");
        System.out.println(col.fgBlue() + "Gewoon blauw " + col.bold().fgBlue() + " met bold");
        System.out.println(col.fgCyan() + "Gewoon cyan " + col.bold().fgCyan() + " met bold");
        System.out.println(col.fgGreen() + "Gewoon groen " + col.bold().fgGreen() + " met bold");
        System.out.println(col.fgMagenta() + "Gewoon magenta " + col.bold().fgMagenta() + " met bold");

        System.out.println(col.fgRed().faintOff().italic() + "faintOff italic rood " + col.bold().fgRed().faint() + " met bold");
        System.out.println(col.fgWhite() + "Gewoon wit " + col.bold().fgWhite() + " met bold");
        System.out.println(col.fgYellow() + "Gewoon geel " + col.bold().fgYellow() + " met bold");
        System.out.println(col.fgBlue() + "Gewoon blauw " + col.bold().fgBlue() + " met bold");
        System.out.println(col.fgCyan() + "Gewoon cyan " + col.bold().fgCyan() + " met bold");
        System.out.println(col.fgGreen() + "Gewoon groen " + col.bold().fgGreen() + " met bold");
        System.out.println(col.fgMagenta() + "Gewoon magenta " + col.bold().fgMagenta() + " met bold");
    }
}
