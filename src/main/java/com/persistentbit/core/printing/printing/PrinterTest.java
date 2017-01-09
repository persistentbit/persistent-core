package com.persistentbit.core.printing.printing;

import com.persistentbit.core.testing.TestCase;
import com.persistentbit.core.testing.TestRunner;

public class PrinterTest {
    static private final PrintableText text = out -> {
        out.println();
        out.println("Before Indent");
        out.println(PrintableText.inset(iout-> {
            iout.println("First indent");
            iout.println("And The last Line");
            iout.println(PrintableText.inset(iout2 -> {
                iout2.println("The second indent");
                iout2.println("Before BulletList");
                iout2.println(new PrintableList().addItem(bout -> {
                    bout.println("dit is bullet1");
                    bout.println("en de 2e lijn in bullet1");
                }).addItem(bout -> {
                    bout.println("dit is bullet 2");
                    bout.println("en de 2e lijn in bullet 2");
                    bout.println("en de 3e lijn in bullet 2");
                }));
                iout2.println("After BulletList");
            }));
            iout.println("After second indent, in first");
        }));
        out.println("After first indent");

    };


    static final TestCase indentTest = TestCase.name("indent").code(tr -> {
        String txt = text.printToString();
        System.out.println(txt);
    });

    public static void main(String... args) throws Exception {
        TestRunner.runAndPrint(PrinterTest.class);
    }
}
