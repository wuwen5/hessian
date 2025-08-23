package io.github.wuwen5.hessian.io;

import com.caucho.hessian.io.Hessian2Input;
import com.caucho.hessian.io.Hessian2Output;
import io.vavr.control.Try;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.lang.reflect.Field;
import java.util.function.Function;
import org.slf4j.spi.LocationAwareLogger;

/**
 * @author wuwen
 */
public abstract class SerializeTestBase {

    void enableLog(Class<?> cls) {
        try {
            Field log = cls.getDeclaredField("log");
            log.setAccessible(true);
            Object simpleLogger = log.get(cls);
            // currentLogLevel
            Field currentLogLevel = simpleLogger.getClass().getDeclaredField("currentLogLevel");
            currentLogLevel.setAccessible(true);
            currentLogLevel.set(simpleLogger, LocationAwareLogger.TRACE_INT);
        } catch (NoSuchFieldException | IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }

    void disableLog(Class<?> cls) {
        try {
            Field log = cls.getDeclaredField("log");
            log.setAccessible(true);
            Object simpleLogger = log.get(cls);
            // currentLogLevel
            Field currentLogLevel = simpleLogger.getClass().getDeclaredField("currentLogLevel");
            currentLogLevel.setAccessible(true);
            currentLogLevel.set(simpleLogger, LocationAwareLogger.WARN_INT);
        } catch (NoSuchFieldException | IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }

    protected <T> T baseHessian2Serialize(T data) throws IOException {
        return hessianIO(out -> Try.run(() -> out.writeObject(data)), in -> Try.of(() -> (T) in.readObject())
                .get());
    }

    protected <T> T baseHessian2Serialize(T data, Class<T> cls) throws IOException {
        return hessianIO(out -> Try.run(() -> out.writeObject(data)), in -> Try.of(() -> (T) in.readObject(cls))
                .get());
    }

    <T> T hessianIO(Function<HessianEncoder, Object> outFun, Function<HessianDecoder, T> inFun) throws IOException {
        return hessianIOBeanSerializeFactory(outFun, inFun, null);
    }

    <T> T hessianIOBeanSerializeFactory(
            Function<HessianEncoder, Object> outFun,
            Function<HessianDecoder, T> inFun,
            SerializerFactory serializerFactory)
            throws IOException {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        Hessian2Output output = new Hessian2Output(outputStream);
        if (serializerFactory != null) {
            output.setSerializerFactory(serializerFactory);
        }
        outFun.apply(output);
        output.flush();
        Hessian2Input input = new Hessian2Input(new ByteArrayInputStream(outputStream.toByteArray()));
        if (serializerFactory != null) {
            input.setSerializerFactory(serializerFactory);
        }
        return inFun.apply(input);
    }
}
