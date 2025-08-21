package com.caucho.hessian.io;

import io.github.wuwen5.hessian.io.HessianServiceException;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Field;
import java.util.HashMap;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

/**
 * @author wuwen
 */
@Slf4j
public class HessianRpcInput extends Hessian2Input implements AbstractHessianInput {
    private static Field detailMessageField;

    /**
     * -- GETTER --
     * Returns any reply fault.
     */
    @Getter
    private Throwable replyFault;

    /**
     * the method for a call
     * -- GETTER --
     * Returns the calls method
     */
    @Getter
    private String method;

    public HessianRpcInput() {
        super();
    }

    /**
     * Creates a new Hessian input stream, initialized with an
     * underlying input stream.
     *
     * @param is the underlying input stream.
     */
    public HessianRpcInput(InputStream is) {
        super(is);
    }

    /**
     * Starts reading the call
     *
     * <p>A successful completion will have a single value:
     *
     * <pre>
     * string
     * </pre>
     */
    @Override
    public String readMethod() throws IOException {
        method = readString();

        return method;
    }

    public Object[] readArguments() throws IOException {
        int len = readInt();

        Object[] args = new Object[len];

        for (int i = 0; i < len; i++) {
            args[i] = readObject();
        }

        return args;
    }

    /**
     * Returns the number of method arguments
     *
     * <pre>
     * int
     * </pre>
     */
    @Override
    public int readMethodArgLength() throws IOException {
        return readInt();
    }

    /**
     * Reads a reply as an object.
     * If the reply has a fault, throws the exception.
     */
    @Override
    public Object readReply(Class<?> expectedClass) throws Throwable {
        int tag = read();

        if (tag == 'R') {
            return readObject(expectedClass);
        } else if (tag == 'F') {
            HashMap map = (HashMap) readObject(HashMap.class);

            throw prepareFault(map);
        } else {
            StringBuilder sb = new StringBuilder();
            sb.append((char) tag);

            try {
                int ch;

                while ((ch = read()) >= 0) {
                    sb.append((char) ch);
                }
            } catch (IOException e) {
                log.debug(e.toString(), e);
            }

            throw error("expected hessian reply at " + codeName(tag) + "\n" + sb);
        }
    }

    /**
     * Starts reading the reply
     *
     * <p>A successful completion will have a single value:
     *
     * <pre>
     * r
     * </pre>
     */
    @Override
    public void startReply() throws Throwable {
        // XXX: for variable length (?)

        readReply(Object.class);
    }

    /**
     * Starts reading the envelope
     *
     * <pre>
     * E major minor
     * </pre>
     */
    public int readEnvelope() throws IOException {
        int tag = read();
        int version = 0;

        if (tag == 'H') {
            int major = read();
            int minor = read();

            version = (major << 16) + minor;

            tag = read();
        }

        if (tag != 'E') {
            throw error("expected hessian Envelope ('E') at " + codeName(tag));
        }

        return version;
    }

    /**
     * Completes reading the envelope
     *
     * <p>A successful completion will have a single value:
     *
     * <pre>
     * Z
     * </pre>
     */
    public void completeEnvelope() throws IOException {
        int tag = read();

        if (tag != 'Z') {
            error("expected end of envelope at " + codeName(tag));
        }
    }

    /**
     * Completes reading the message
     *
     * <p>A successful completion will have a single value:
     *
     * <pre>
     * z
     * </pre>
     */
    public void completeMessage() throws IOException {
        int tag = read();

        if (tag != 'Z') {
            error("expected end of message at " + codeName(tag));
        }
    }

    /**
     * Completes reading the call
     *
     * <p>A successful completion will have a single value:
     *
     * <pre>
     * z
     * </pre>
     */
    public void completeValueReply() throws IOException {
        int tag = read();

        if (tag != 'Z') {
            error("expected end of reply at " + codeName(tag));
        }
    }

    /**
     * Starts reading a packet
     *
     * <pre>
     * p major minor
     * </pre>
     */
    public int startMessage() throws IOException {
        int tag = read();

        if (tag != 'p' && tag != 'P') {
            throw error("expected Hessian message ('p') at " + codeName(tag));
        }

        int major = read();
        int minor = read();

        return (major << 16) + minor;
    }

    @Override
    public void setRemoteResolver(HessianRemoteResolver resolver) {
        super.setRemoteResolver(resolver);
    }

    @Override
    public void setSerializerFactory(SerializerFactory ser) {
        super.setSerializerFactory(ser);
    }

    /**
     * Prepares the fault.
     */
    private Throwable prepareFault(HashMap fault) {
        Object detail = fault.get("detail");
        String message = (String) fault.get("message");

        if (detail instanceof Throwable) {
            replyFault = (Throwable) detail;

            Field detailMessageField = getDetailMessageField();

            if (message != null && detailMessageField != null) {
                try {
                    detailMessageField.set(replyFault, message);
                } catch (Throwable ignored) {
                }
            }

            return replyFault;
        } else {
            String code = (String) fault.get("code");

            replyFault = new HessianServiceException(message, code, detail);

            return replyFault;
        }
    }

    private static Field getDetailMessageField() {
        if (detailMessageField == null) {
            try {
                detailMessageField = Throwable.class.getDeclaredField("detailMessage");
                detailMessageField.setAccessible(true);
            } catch (Throwable ignored) {
            }
        }

        return detailMessageField;
    }
}
