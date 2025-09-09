package io.github.wuwen5.hessian.test.dubbo;

import java.io.Serializable;
import java.sql.Time;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import lombok.experimental.Accessors;

/**
 * @author wuwen
 */
@Getter
@Setter
@Accessors(chain = true)
@EqualsAndHashCode(exclude = "cyclicRef")
@ToString(exclude = "cyclicRef")
public class BasicTypeBean implements Serializable {
    private int anInt;
    private long aLong;
    private double aDouble;
    private double[] aDoubleArray;
    private float aFloat;
    private float[] aFloatArray;
    private boolean aBoolean;
    private String aString;
    private byte[] aByteArray;
    private byte aByte;
    private Byte byteObject;
    private char aChar;
    private short aShort;
    private short[] aShortArray;
    private Integer anInteger;
    private Long aLongObject;
    private Double aDoubleObject;
    private Float aFloatObject;
    private Boolean aBooleanObject;
    private List<String> aList;
    private Map<String, String> map;
    private Map<String, BasicTypeBean> cyclicRef;
    private Date date;
    private java.sql.Date sqlDate;
    private BaseUser baseUser;
    private BaseUser[] baseUsers;
    private Number number;
    private Character[] character;
    private char[] chars;
    private String[] stringArray;
    private boolean[] booleanArray;
    private int[] intArray;
    private Boolean[] booleans;
    private List<Boolean> booleanList;
    private Time time;
    private Timestamp timestamp;
    private Calendar calendar;
    private Calendar myCalendar;

    public static BasicTypeBean create() {
        BasicTypeBean bean = new BasicTypeBean();
        List<String> list = new ArrayList<>();
        list.add("item1");
        list.add("item2");
        Map<String, String> map = new HashMap<>();
        map.put("key1", "value1");

        Map<String, BasicTypeBean> cyclicRef = new HashMap<>();
        cyclicRef.put("self", bean);

        bean.setAnInt(1)
                .setCyclicRef(cyclicRef)
                .setALong(2L)
                .setADouble(3.0)
                .setADoubleArray(new double[] {1.0, 2.0, 3.0})
                .setAFloat(4.0f)
                .setAFloatArray(new float[] {1.0f, 2.0f, 3.0f})
                .setABoolean(true)
                .setAString("test")
                .setAByteArray(new byte[] {1, 2, 3})
                .setAChar('c')
                .setAShort((short) 5)
                .setAShortArray(new short[] {1, 2, 3})
                .setAnInteger(6)
                .setALongObject(7L)
                .setADoubleObject(8.0)
                .setAFloatObject(9.0f)
                .setABooleanObject(false)
                .setAList(list)
                .setMap(map)
                .setDate(new Date())
                .setSqlDate(new java.sql.Date(System.currentTimeMillis()))
                .setAByte((byte) 1)
                .setBaseUser(new BaseUser())
                .setBaseUsers(new BaseUser[] {new BaseUser(), new BaseUser()})
                .setNumber(10)
                .setIntArray(new int[] {1, 2, 3})
                .setCharacter(new Character[] {'a', 'b', 'c'})
                .setChars(new char[] {'x', 'y', 'z'})
                .setStringArray(new String[] {"str1", "str2", "str3"})
                .setBooleanArray(new boolean[] {true, false, true})
                .setBooleans(new Boolean[] {true, false, true})
                .setTime(new Time(System.currentTimeMillis()))
                .setTimestamp(new Timestamp(System.currentTimeMillis()))
                .setCalendar(Calendar.getInstance())
                .setMyCalendar(new MyCalendar())
                .setBooleanList(new ArrayList<>() {
                    {
                        add(true);
                        add(false);
                    }
                })
                .setByteObject(Byte.valueOf("1"));
        return bean;
    }

    public static class MyCalendar extends Calendar {
        private static final long serialVersionUID = 6733329372790268463L;

        @Override
        protected void computeTime() {
            setTimeInMillis(System.currentTimeMillis());
        }

        @Override
        protected void computeFields() {}

        @Override
        public void add(int field, int amount) {
            throw new UnsupportedOperationException();
        }

        @Override
        public void roll(int field, boolean up) {
            throw new UnsupportedOperationException();
        }

        @Override
        public int getMinimum(int field) {
            return 0;
        }

        @Override
        public int getMaximum(int field) {
            return 0;
        }

        @Override
        public int getGreatestMinimum(int field) {
            return 0;
        }

        @Override
        public int getLeastMaximum(int field) {
            return 0;
        }
    }

    @Getter
    @Setter
    @EqualsAndHashCode
    @ToString
    public static class BaseUser implements Serializable {
        private static final long serialVersionUID = 9104092580669691633L;
        private Integer userId;
        private String userName;
    }
}
