package io.github.wuwen5.hessian.io;

import static org.junit.jupiter.api.Assertions.*;

import java.io.IOException;
import java.util.Locale;
import org.junit.jupiter.api.Test;

/**
 * Test for Locale script preservation during serialization/deserialization
 */
public class LocaleScriptTest extends SerializeTestBase {

    @Test
    void testLocaleWithScript() throws IOException {
        // Create a Locale with script
        Locale zhCNHans = new Locale.Builder()
                .setLanguage("zh")
                .setRegion("CN")
                .setScript("Hans")
                .build();

        // Serialize and deserialize
        Locale result = baseHessian2Serialize(zhCNHans);

        // The script should be preserved
        assertEquals(zhCNHans.getLanguage(), result.getLanguage(), "Language should be preserved");
        assertEquals(zhCNHans.getCountry(), result.getCountry(), "Country should be preserved");
        assertEquals(zhCNHans.getScript(), result.getScript(), "Script should be preserved");
        assertEquals(zhCNHans.toString(), result.toString(), "String representation should match");
        assertEquals(zhCNHans, result, "Locales should be equal");
    }

    @Test
    void testLocaleWithScriptAndVariant() throws IOException {
        // Create a Locale with script and variant
        Locale complex = new Locale.Builder()
                .setLanguage("en")
                .setRegion("US")
                .setScript("Latn")
                .setVariant("POSIX")
                .build();

        // Serialize and deserialize
        Locale result = baseHessian2Serialize(complex);

        // All components should be preserved
        assertEquals(complex.getLanguage(), result.getLanguage(), "Language should be preserved");
        assertEquals(complex.getCountry(), result.getCountry(), "Country should be preserved");
        assertEquals(complex.getScript(), result.getScript(), "Script should be preserved");
        assertEquals(complex.getVariant(), result.getVariant(), "Variant should be preserved");
        assertEquals(complex.toString(), result.toString(), "String representation should match");
        assertEquals(complex, result, "Locales should be equal");
    }

    @Test
    void testVariousScriptLocales() throws IOException {
        // Test different script codes
        Locale[] testLocales = {
            new Locale.Builder()
                    .setLanguage("ar")
                    .setRegion("EG")
                    .setScript("Arab")
                    .build(),
            new Locale.Builder()
                    .setLanguage("ja")
                    .setRegion("JP")
                    .setScript("Jpan")
                    .build(),
            new Locale.Builder()
                    .setLanguage("ru")
                    .setRegion("RU")
                    .setScript("Cyrl")
                    .build(),
            new Locale.Builder()
                    .setLanguage("hi")
                    .setRegion("IN")
                    .setScript("Deva")
                    .build()
        };

        for (Locale original : testLocales) {
            Locale result = baseHessian2Serialize(original);
            assertEquals(original.getScript(), result.getScript(), "Script should be preserved for " + original);
            assertEquals(original, result, "Locales should be equal for " + original);
        }
    }
}
