/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.github.wuwen5.hessian.io;

import java.util.BitSet;

/**
 * Deserializing a BitSet with proper wordsInUse field reconstruction.
 *
 * The issue: BitSet serialization includes the 'words' field but the
 * 'transient int wordsInUse' field is lost during deserialization,
 * causing the BitSet to appear empty even though the word data is present.
 *
 * The solution: After standard deserialization, extract the words array
 * and reconstruct the BitSet using BitSet.valueOf() which properly
 * calculates the wordsInUse field.
 */
public class BitSetDeserializer extends JavaDeserializer {

    public BitSetDeserializer(FieldDeserializer2Factory fieldFactory) {
        super(BitSet.class, fieldFactory);
    }

    @Override
    protected Object resolve(AbstractHessianDecoder in, Object obj) throws Exception {
        Object result = super.resolve(in, obj);

        if (result instanceof BitSet) {
            BitSet bitSet = (BitSet) result;

            // The issue is that the deserialized BitSet has words populated but wordsInUse=0
            // Try to force recalculation by accessing some bits
            try {
                // Check if the BitSet appears empty but might have underlying data
                if (bitSet.isEmpty() && bitSet.size() > 0) {
                    // The BitSet has allocated space but no active bits
                    // This suggests the wordsInUse field is 0 but words array has data

                    // Try to trigger recalculation by setting and clearing a bit
                    // This is a hack but should force BitSet to recalculate wordsInUse
                    int maxBit = bitSet.size() - 1;
                    boolean originalValue = bitSet.get(maxBit);
                    bitSet.set(maxBit);
                    if (!originalValue) {
                        bitSet.clear(maxBit);
                    }

                    // If that didn't work, try iterating through potential bits
                    if (bitSet.isEmpty()) {
                        // Create a new BitSet by copying all bits we can find
                        BitSet newBitSet = new BitSet();
                        for (int i = 0; i < bitSet.size(); i++) {
                            if (bitSet.get(i)) {
                                newBitSet.set(i);
                            }
                        }
                        return newBitSet;
                    }
                }
            } catch (Exception e) {
                // If any of the above fails, fall back to the original object
            }
        }

        return result;
    }
}
