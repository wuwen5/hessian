package io.github.wuwen5.hessian;

import java.io.Writer;
import java.util.function.Consumer;

public class LineFlushingWriter extends Writer {
    private final Consumer<String> lineLogger;
    private final StringBuilder buffer = new StringBuilder();

    public LineFlushingWriter(Consumer<String> logger) {
        this.lineLogger = logger;
    }

    @Override
    public void write(char[] cbuf, int off, int len) {
        for (int i = off; i < off + len; i++) {
            char ch = cbuf[i];
            if (ch == '\n') {
                flushLine();
            } else if (ch != '\r') {
                buffer.append(ch);
            }
        }
    }

    private void flushLine() {
        if (buffer.length() > 0) {
            lineLogger.accept(buffer.toString());
            buffer.setLength(0);
        }
    }

    @Override
    public void flush() {
        flushLine();
    }

    @Override
    public void close() {
        flushLine();
    }
}
