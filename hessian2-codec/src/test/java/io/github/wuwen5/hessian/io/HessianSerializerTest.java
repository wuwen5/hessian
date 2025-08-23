package io.github.wuwen5.hessian.io;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNull;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import lombok.Getter;
import org.junit.jupiter.api.Test;

class HessianSerializerTest {

    @Getter
    static class Person implements java.io.Serializable {
        private String name;
        private int age;
        // transient不应被序列化
        private transient String ignoreField;

        public Person() {}

        public Person(String name, int age, String ignoreField) {
            this.name = name;
            this.age = age;
            this.ignoreField = ignoreField;
        }
    }

    @Test
    void testHessianSerializerInputOutput() throws IOException {
        Person person = new Person("Alice", 30, "secret");

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        HessianSerializerOutput out = new HessianSerializerOutput(baos);

        // 使用writeObject而不是直接writeObjectImpl
        out.writeObject(person);
        out.flush();
        out.close();

        ByteArrayInputStream bais = new ByteArrayInputStream(baos.toByteArray());
        HessianSerializerInput in = new HessianSerializerInput(bais);

        Object obj = in.readObject(Person.class);
        assertInstanceOf(Person.class, obj);
        Person result = (Person) obj;

        assertEquals("Alice", result.getName());
        assertEquals(30, result.getAge());
        // transient字段不会被序列化
        assertNull(result.getIgnoreField());
    }
}
