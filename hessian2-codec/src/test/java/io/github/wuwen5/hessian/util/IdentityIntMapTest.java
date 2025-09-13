package io.github.wuwen5.hessian.util;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

/**
 * @author wuwen
 */
class IdentityIntMapTest {

    @Test
    void testToString() {
        IdentityIntMap map = new IdentityIntMap(16);
        assertEquals("IntMap[]", map.toString());
        map.put("test", Integer.MAX_VALUE, false);
        assertEquals("IntMap[test:2147483647]", map.toString());
    }

    @Test
    void testGetBiggestPrime() {
        assertEquals(2, IdentityIntMap.getBiggestPrime(0));
    }
}
