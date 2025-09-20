package io.github.wuwen5.hessian.io;

import static org.junit.jupiter.api.Assertions.assertEquals;

import io.vavr.control.Try;
import java.io.File;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.Locale;
import org.junit.jupiter.api.Test;

/**
 * @author wuwen
 */
public class ExtendedTypeIOTest extends SerializeTestBase {

    @Test
    void testFile() throws IOException {
        File file = new File("test.txt");
        Object ret = hessianIO(output -> Try.run(() -> output.writeObject(file)), input -> Try.of(input::readObject)
                .get());

        assertEquals("test.txt", ((File) ret).getPath());
    }

    @Test
    void testBigDecimal() throws IOException {
        BigDecimal bigDecimal = new BigDecimal("1234567890123456789012345678901234567890");
        Object ret =
                hessianIO(output -> Try.run(() -> output.writeObject(bigDecimal)), input -> Try.of(input::readObject)
                        .get());

        assertEquals(bigDecimal, ret);
    }

    @Test
    void testLocale() throws IOException {
        Locale en = Locale.ENGLISH;
        Locale zh = Locale.SIMPLIFIED_CHINESE;
        Locale zhCNHans = new Locale.Builder()
                .setLanguage("zh")
                .setRegion("CN")
                .setScript("Hans") // 设置脚本
                .build();

        Object enRet = hessianIO(output -> Try.run(() -> output.writeObject(en)), input -> Try.of(input::readObject)
                .get());

        Object zhRet = hessianIO(output -> Try.run(() -> output.writeObject(zh)), input -> Try.of(input::readObject)
                .get());

        Object zhCnHansRet =
                hessianIO(output -> Try.run(() -> output.writeObject(zhCNHans)), input -> Try.of(input::readObject)
                        .get());

        assertEquals(en.toString(), enRet.toString());
        assertEquals(zh.toString(), zhRet.toString());
        assertEquals(zhCNHans.toString(), zhCnHansRet.toString());
    }
}
