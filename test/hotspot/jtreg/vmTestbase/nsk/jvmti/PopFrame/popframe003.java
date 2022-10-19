/*
 * Copyright (c) 2003, 2018, Oracle and/or its affiliates. All rights reserved.
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER.
 *
 * This code is free software; you can redistribute it and/or modify it
 * under the terms of the GNU General Public License version 2 only, as
 * published by the Free Software Foundation.
 *
 * This code is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License
 * version 2 for more details (a copy is included in the LICENSE file that
 * accompanied this code).
 *
 * You should have received a copy of the GNU General Public License version
 * 2 along with this work; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301 USA.
 *
 * Please contact Oracle, 500 Oracle Parkway, Redwood Shores, CA 94065 USA
 * or visit www.oracle.com if you need additional information or have any
 * questions.
 */

package nsk.jvmti.PopFrame;

import nsk.share.Wicket;
import java.io.PrintStream;

/**
 * This test checks that after popping a method's frame by the JVMTI
 * function <code>PopFrame()</code>:
 * <li>the method arguments will be added back and any changes to
 * the arguments, which occurred in the called method, will remain
 * <li>changes to global state, which occurred in the called method,
 * will remain
 * <li>no JVMTI events are generated by the function <code>PopFrame()</code>
 * <br><br>The test was changed due to the bug 4448675.
 */
public class popframe003 {
    public static final int PASSED = 0;
    public static final int FAILED = 2;
    static final int JCK_STATUS_BASE = 95;

    private popframe003p popFrameClsThr;

    static {
        try {
            System.loadLibrary("popframe003");
        } catch (UnsatisfiedLinkError e) {
            System.err.println("Could not load popframe003 library");
            System.err.println("java.library.path:" +
                System.getProperty("java.library.path"));
            throw e;
        }
    }

    native static int doPopFrame(popframe003p popFrameClsThr);
    native static int suspThread(popframe003p popFrameClsThr);
    native static int resThread(popframe003p popFrameClsThr);

    public static void main(String[] argv) {
        argv = nsk.share.jvmti.JVMTITest.commonInit(argv);

        System.exit(run(argv, System.out) + JCK_STATUS_BASE);
    }

    public static int run(String argv[], PrintStream out) {
        return new popframe003().runIt(argv, out);
    }

    private int runIt(String argv[], PrintStream out) {
        int retValue = 0;

        popFrameClsThr = new popframe003p("Tested Thread", out);
        // start the child thread
        popFrameClsThr.start();
        popFrameClsThr.startingBarrier.waitFor();
        // pause until the child thread exits notification-block
        synchronized (popFrameClsThr.barrier) {
        }

        out.println("Going to suspend the thread...");
        retValue = suspThread(popFrameClsThr);
        if (retValue != PASSED) {
            out.println("TEST: failed to suspend thread");
            return FAILED;
        }

        // pop the frame
        out.println("Going to pop a frame...");
        retValue = doPopFrame(popFrameClsThr);

        popFrameClsThr.popFrameHasBeenDone();

        if (retValue != PASSED) {
            out.println("TEST: failed to pop frame");
            resThread(popFrameClsThr);
            return FAILED;
        }

        out.println("Going to resume the thread...");
        retValue = resThread(popFrameClsThr);
        if (retValue != PASSED) {
            out.println("TEST: failed to resume thread");
            return FAILED;
        }

        try {
            popFrameClsThr.join();
        } catch (InterruptedException e) {
            out.println("TEST INCOMPLETE: caught " + e);
            return FAILED;
        }

        /* check that any changes for the static global fields,
         *  which occurred in the called method, remain
         */
        if (popframe003p.bytePubStatGlFld != 2 || popframe003p.shortPubStatGlFld != 3 ||
            popframe003p.intPubStatGlFld != 4 || popframe003p.longPubStatGlFld != 5L ||
            popframe003p.floatPubStatGlFld != 6.2F || popframe003p.doublePubStatGlFld != 7.35D ||
            popframe003p.charPubStatGlFld != 'b' || popframe003p.booleanPubStatGlFld != true ||
            !popframe003p.strPubStatGlFld.equals("sttc glbl fld")) {
            out.println("TEST FAILED: changes for the static fields of a class,\n" +
                "\twhich have been made in the popped frame's method, did not remain:\n" +
                "\tstatic fields values:\n\t\tbytePubStatGlFld=" + popframe003p.bytePubStatGlFld + "\texpected: 2\n" +
                "\t\tshortPubStatGlFld=" + popframe003p.shortPubStatGlFld + "\texpected: 3\n" +
                "\t\tintPubStatGlFld=" + popframe003p.intPubStatGlFld + "\texpected: 4\n" +
                "\t\tlongPubStatGlFld=" + popframe003p.longPubStatGlFld + "\texpected: 5\n" +
                "\t\tfloatPubStatGlFld=" + popframe003p.floatPubStatGlFld + "\texpected: 6.2\n" +
                "\t\tdoublePubStatGlFld=" + popframe003p.doublePubStatGlFld + "\texpected: 7.35\n" +
                "\t\tcharPubStatGlFld='" + popframe003p.charPubStatGlFld + "'\texpected: 'b'\n" +
                "\t\tbooleanPubStatGlFld=" + popframe003p.booleanPubStatGlFld + "\texpected: true\n" +
                "\t\tstrPubStatGlFld=\"" + popframe003p.strPubStatGlFld + "\"\texpected: \"sttc glbl fld\"");
            return FAILED;
        } else {
            out.println("Check #6 PASSED: changes for the static fields of a class,\n" +
                    "\twhich have been made in the popped frame's method, remained\n");
        }

        return popframe003p.totRes;
    }
}
