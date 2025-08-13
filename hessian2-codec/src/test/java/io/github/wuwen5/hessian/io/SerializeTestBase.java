package io.github.wuwen5.hessian.io;

import com.caucho.hessian.io.Hessian2Input;
import com.caucho.hessian.io.Hessian2Output;
import io.vavr.control.Try;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.function.Function;

/**
 * @author wuwen
 */
public abstract class SerializeTestBase {

    protected <T> T baseHessian2Serialize(T data) throws IOException {
        return hessianIO(out -> Try.run(() -> out.writeObject(data)), in -> Try.of(() -> (T) in.readObject())
                .get());
    }

    <T> T hessianIO(Function<HessianEncoder, Object> outFun, Function<HessianDecoder, T> inFun) throws IOException {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        Hessian2Output output = new Hessian2Output(outputStream);
        outFun.apply(output);
        output.flush();
        Hessian2Input input = new Hessian2Input(new ByteArrayInputStream(outputStream.toByteArray()));
        return inFun.apply(input);
    }
}
