package io.github.wuwen5.hessian.io;

import static org.junit.jupiter.api.Assertions.*;

import com.caucho.hessian.HessianUnshared;
import com.caucho.hessian.io.Hessian2Input;
import com.caucho.hessian.io.Hessian2Output;
import com.caucho.hessian.io.SerializerFactory;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.Serializable;
import lombok.Getter;
import org.junit.jupiter.api.Test;

class HessianUnsharedAnnotationTest {

    @Getter
    @HessianUnshared
    static class UnsharedPerson implements Serializable {
        private String name;
        private int age;

        public UnsharedPerson() {}

        public UnsharedPerson(String name, int age) {
            this.name = name;
            this.age = age;
        }
    }

    @Test
    void testHessianUnsharedBehavior() throws IOException {
        UnsharedPerson person = new UnsharedPerson("Bob", 30);

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        Hessian2Output out = new Hessian2Output(baos);
        SerializerFactory factory = new SerializerFactory();
        out.setSerializerFactory(factory);

        // write twice
        out.writeObject(person);
        out.writeObject(person);
        out.close();

        ByteArrayInputStream bais = new ByteArrayInputStream(baos.toByteArray());
        Hessian2Input in = new Hessian2Input(bais);
        in.setSerializerFactory(factory);

        // read twice
        UnsharedPerson p1 = (UnsharedPerson) in.readObject();
        UnsharedPerson p2 = (UnsharedPerson) in.readObject();

        assertEquals("Bob", p1.getName());
        assertEquals(30, p1.getAge());
        assertEquals("Bob", p2.getName());
        assertEquals(30, p2.getAge());
        // verification: The two objects are not the same reference
        assertNotSame(p1, p2);
    }
}
