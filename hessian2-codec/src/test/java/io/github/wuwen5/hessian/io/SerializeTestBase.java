package io.github.wuwen5.hessian.io;

import com.caucho.hessian.io.AbstractHessianInput;
import com.caucho.hessian.io.AbstractHessianOutput;
import com.caucho.hessian.io.Hessian2Input;
import com.caucho.hessian.io.Hessian2Output;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.function.Function;

/**
 * @author wuwen
 */
public abstract class SerializeTestBase {

    <T> T hessianIO(Function<AbstractHessianOutput, Object> outFun, Function<AbstractHessianInput, T> inFun)
            throws IOException {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        Hessian2Output output = new Hessian2Output(outputStream);
        outFun.apply(output);
        output.flush();
        Hessian2Input input = new Hessian2Input(new ByteArrayInputStream(outputStream.toByteArray()));
        return inFun.apply(input);
    }
}
