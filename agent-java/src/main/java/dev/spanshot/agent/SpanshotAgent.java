package dev.spanshot.agent;

import java.lang.instrument.Instrumentation;

/**
 * Entry point when the app is started with {@code -javaagent:spanshot-agent.jar}.
 */
public final class SpanshotAgent {

    private SpanshotAgent() {
    }

    public static void premain(String args, Instrumentation instrumentation) {
        System.out.println("[spanshot] agent loaded (args=" + args + ")");
    }
}
