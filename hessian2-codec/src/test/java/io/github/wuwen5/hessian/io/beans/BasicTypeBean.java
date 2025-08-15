package io.github.wuwen5.hessian.io.beans;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

/**
 * @author wuwen
 */
@Getter
@Setter
@Accessors(chain = true)
@EqualsAndHashCode
public class BasicTypeBean implements Serializable {
    private int anInt;
    private long aLong;
    private double aDouble;
    private float aFloat;
    private boolean aBoolean;
    private String aString;
    private byte[] aByteArray;
    private byte aByte;
    private Byte byteObject;
    private char aChar;
    private short aShort;
    private Integer anInteger;
    private Long aLongObject;
    private Double aDoubleObject;
    private Float aFloatObject;
    private Boolean aBooleanObject;
    private List<String> aList;
    private Map<String, String> map;
    private Date date;

    public static BasicTypeBean create() {
        BasicTypeBean bean = new BasicTypeBean();
        List<String> list = new ArrayList<>();
        list.add("item1");
        list.add("item2");
        Map<String, String> map = new HashMap<>();
        map.put("key1", "value1");
        bean.setAnInt(1)
                .setALong(2L)
                .setADouble(3.0)
                .setAFloat(4.0f)
                .setABoolean(true)
                .setAString("test")
                .setAByteArray(new byte[] {1, 2, 3})
                .setAChar('c')
                .setAShort((short) 5)
                .setAnInteger(6)
                .setALongObject(7L)
                .setADoubleObject(8.0)
                .setAFloatObject(9.0f)
                .setABooleanObject(false)
                .setAList(list)
                .setMap(map)
                .setDate(new Date())
                .setAByte((byte) 1)
                .setByteObject(Byte.valueOf("1"));
        return bean;
    }
}
