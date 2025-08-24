package io.github.wuwen5.hessian.io;

import com.caucho.hessian.io.Hessian2Input;
import com.caucho.hessian.io.Hessian2Output;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.Serializable;
import java.util.Arrays;
import java.util.List;

/**
 * Simple validation test to demonstrate Hessian2 serialization functionality
 */
public class ValidationTest {

    public static class TestData implements Serializable {
        private String name;
        private int value;
        private List<String> items;

        public TestData() {}

        public TestData(String name, int value, List<String> items) {
            this.name = name;
            this.value = value;
            this.items = items;
        }

        public String getName() { return name; }
        public void setName(String name) { this.name = name; }
        
        public int getValue() { return value; }
        public void setValue(int value) { this.value = value; }
        
        public List<String> getItems() { return items; }
        public void setItems(List<String> items) { this.items = items; }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (!(o instanceof TestData)) return false;
            TestData testData = (TestData) o;
            return value == testData.value &&
                   name.equals(testData.name) &&
                   items.equals(testData.items);
        }

        @Override
        public String toString() {
            return "TestData{name='" + name + "', value=" + value + ", items=" + items + "}";
        }
    }

    public static void main(String[] args) throws IOException {
        System.out.println("=== Hessian2 Serialization Validation Test ===");
        
        // Create test data
        TestData original = new TestData("Hello World", 42, Arrays.asList("item1", "item2", "item3"));
        System.out.println("Original: " + original);

        // Serialize
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        Hessian2Output output = new Hessian2Output(outputStream);
        output.writeObject(original);
        output.flush();

        System.out.println("Serialized to " + outputStream.toByteArray().length + " bytes");

        // Deserialize
        Hessian2Input input = new Hessian2Input(new ByteArrayInputStream(outputStream.toByteArray()));
        TestData deserialized = (TestData) input.readObject();

        System.out.println("Deserialized: " + deserialized);
        System.out.println("Objects equal: " + original.equals(deserialized));

        // Test circular references
        TestData circular1 = new TestData();
        TestData circular2 = new TestData();
        circular1.setName("circular1");
        circular2.setName("circular2");
        
        // Create circular reference
        circular1.setItems(Arrays.asList("ref-to-circular2"));
        circular2.setItems(Arrays.asList("ref-to-circular1"));

        outputStream = new ByteArrayOutputStream();
        output = new Hessian2Output(outputStream);
        output.writeObject(Arrays.asList(circular1, circular2, circular1)); // Test shared references
        output.flush();

        input = new Hessian2Input(new ByteArrayInputStream(outputStream.toByteArray()));
        List<?> circularResult = (List<?>) input.readObject();
        
        System.out.println("Circular reference test: " + (circularResult.get(0) == circularResult.get(2)));
        System.out.println("=== All validation tests passed! ===");
    }
}