package io.github.wuwen5.hessian.io;

import io.github.wuwen5.hessian.io.beans.BasicTypeBean;
import java.io.ByteArrayInputStream;
import java.util.Base64;
import java.util.Map;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class HessianUnknowTypeTest {

    @Test
    void testUnknowType() throws Exception {
        // no classes in the classpath
        String base64 =
                "Qw1CYXNpY1R5cGVCZWFusAVhbkludAVhTG9uZwdhRG91YmxlBmFGbG9hdAhhQm9vbGVhbgdhU3RyaW5nBWFCeXRlCmJ5dGVPYmplY3QFYUNoYXIGYVNob3J0CWFuSW50ZWdlcgthTG9uZ09iamVjdA1hRG91YmxlT2JqZWN0DGFGbG9hdE9iamVjdA5hQm9vbGVhbk9iamVjdAZudW1iZXIMYURvdWJsZUFycmF5C2FGbG9hdEFycmF5CmFCeXRlQXJyYXkLYVNob3J0QXJyYXkFYUxpc3QDbWFwBGRhdGUIYmFzZVVzZXIJYmFzZVVzZXJzCWNoYXJhY3RlcgVjaGFycwtzdHJpbmdBcnJheQxib29sZWFuQXJyYXkIaW50QXJyYXkIYm9vbGVhbnMLYm9vbGVhbkxpc3RgkeJdA10EVAR0ZXN0kUMwJmlvLmdpdGh1Yi53dXdlbjUuaGVzc2lhbi5pby5CeXRlSGFuZGxlkQZfdmFsdWVhkQFjlZbnXQhDMCdpby5naXRodWIud3V3ZW41Lmhlc3NpYW4uaW8uRmxvYXRIYW5kbGWRBl92YWx1ZWJdCUaacwdbZG91YmxlXF0CXQNzBltmbG9hdFxdAl0DIwECA3MGW3Nob3J0kZKTegVpdGVtMQVpdGVtMkgEa2V5MQZ2YWx1ZTFaSgAAAZjVZNCBQzAqaW8uZ2l0aHViLnd1d2VuNS5oZXNzaWFuLmlvLmJlYW5zLkJhc2VVc2VykgZ1c2VySWQIdXNlck5hbWVjTk5yMCtbaW8uZ2l0aHViLnd1d2VuNS5oZXNzaWFuLmlvLmJlYW5zLkJhc2VVc2VyY05OY05OcxRbamF2YS5sYW5nLkNoYXJhY3RlcgFhAWIBYwN4eXpzB1tzdHJpbmcEc3RyMQRzdHIyBHN0cjNzCFtib29sZWFuVEZUcwRbaW50kZKTcxJbamF2YS5sYW5nLkJvb2xlYW5URlRyD0Jhc2ljVHlwZUJlYW4kMVRG";

        HessianDecoder decoder =
                new HessianDecoder(new ByteArrayInputStream(Base64.getDecoder().decode(base64)));

        Object o = decoder.readObject();

        Assertions.assertTrue(Map.class.isAssignableFrom(o.getClass()));

        decoder =
                new HessianDecoder(new ByteArrayInputStream(Base64.getDecoder().decode(base64)));

        BasicTypeBean o1 = (BasicTypeBean) decoder.readObject(BasicTypeBean.class);

        Assertions.assertEquals(BasicTypeBean.create().getAString(), o1.getAString());
    }
}
