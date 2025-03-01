package com.autotest.sonicclient.utils;


import java.lang.reflect.Array;
import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;


public class Assert {
    public static final String ARRAY_MISMATCH_TEMPLATE = "arrays differ firstly at element [%d]; expected value is <%s> but was <%s>. %s";

    protected Assert() {
    }

    public static void assertTrue(boolean condition, String message) {
        if (!condition) {
            failNotEquals(condition, Boolean.TRUE, message);
        }

    }

    public static void assertTrue(boolean condition) {
        assertTrue(condition, (String)null);
    }

    public static void assertFalse(boolean condition, String message) {
        if (condition) {
            failNotEquals(condition, Boolean.FALSE, message);
        }

    }

    public static void assertFalse(boolean condition) {
        assertFalse(condition, (String)null);
    }

    public static void fail(String message, Throwable realCause) {
        AssertionError ae = new AssertionError(message);
        ae.initCause(realCause);
        throw ae;
    }

    public static void fail(String message) {
        throw new AssertionError(message);
    }

    public static void fail() {
        fail((String)null);
    }

    public static void assertEquals(Object actual, Object expected, String message) {
        if (expected != null && expected.getClass().isArray()) {
            assertArrayEquals(actual, expected, message);
        } else {
            assertEqualsImpl(actual, expected, message);
        }
    }

    private static boolean areEqual(Object actual, Object expected) {
        return expected != null && expected.getClass().isArray() ? areArraysEqual(actual, expected) : areEqualImpl(actual, expected);
    }

    private static void assertEqualsImpl(Object actual, Object expected, String message) {
        boolean equal = areEqualImpl(actual, expected);
        if (!equal) {
            failNotEquals(actual, expected, message);
        }

    }

    private static void assertNotEqualsImpl(Object actual, Object expected, String message) {
        boolean notEqual = areNotEqualImpl(actual, expected);
        if (!notEqual) {
            failEquals(actual, expected, message);
        }

    }

    private static boolean areNotEqualImpl(Object actual, Object expected) {
        if (expected == null) {
            return actual != null;
        } else if (actual == null) {
            return true;
        } else {
            return !expected.equals(actual);
        }
    }

    private static boolean areEqualImpl(Object actual, Object expected) {
        if (expected == null && actual == null) {
            return true;
        } else if (expected != null && actual != null) {
            return expected.equals(actual) && actual.equals(expected);
        } else {
            return false;
        }
    }

    private static String getArrayNotEqualReason(Object actual, Object expected) {
        if (Objects.equals(actual, expected)) {
            return null;
        } else if (null == expected) {
            return "expected a null array, but not null found";
        } else if (null == actual) {
            return "expected not null array, but null found";
        } else if (!actual.getClass().isArray()) {
            return "not an array";
        } else {
            int expectedLength = Array.getLength(expected);
            if (expectedLength != Array.getLength(actual)) {
                return "array lengths are not the same";
            } else {
                for(int i = 0; i < expectedLength; ++i) {
                    Object _actual = Array.get(actual, i);
                    Object _expected = Array.get(expected, i);
                    if (!areEqual(_actual, _expected)) {
                        return "(values at index " + i + " are not the same)";
                    }
                }

                return null;
            }
        }
    }

    private static boolean areArraysEqual(Object actual, Object expected) {
        return getArrayNotEqualReason(actual, expected) == null;
    }

    private static void assertArrayEquals(Object actual, Object expected, String message) {
        String reason = getArrayNotEqualReason(actual, expected);
        if (null != reason) {
            failNotEquals(actual, expected, message == null ? "" : message + " (" + message + ")");
        }

    }

    private static void assertArrayNotEquals(Object actual, Object expected, String message) {
        String reason = getArrayNotEqualReason(actual, expected);
        if (null == reason) {
            failEquals(actual, expected, message);
        }

    }

    public static void assertEquals(byte[] actual, byte[] expected) {
        assertEquals(actual, expected, "");
    }

    public static void assertEquals(byte[] actual, byte[] expected, String message) {
        if (!checkRefEqualityAndLength(actual, expected, message)) {
            for(int i = 0; i < expected.length; ++i) {
                if (expected[i] != actual[i]) {
                    fail(String.format("arrays differ firstly at element [%d]; expected value is <%s> but was <%s>. %s", i, Byte.toString(expected[i]), Byte.toString(actual[i]), message));
                }
            }

        }
    }

    public static void assertEquals(short[] actual, short[] expected) {
        assertEquals(actual, expected, "");
    }

    public static void assertEquals(short[] actual, short[] expected, String message) {
        if (!checkRefEqualityAndLength(actual, expected, message)) {
            for(int i = 0; i < expected.length; ++i) {
                if (expected[i] != actual[i]) {
                    fail(String.format("arrays differ firstly at element [%d]; expected value is <%s> but was <%s>. %s", i, Short.toString(expected[i]), Short.toString(actual[i]), message));
                }
            }

        }
    }

    public static void assertEquals(int[] actual, int[] expected) {
        assertEquals(actual, expected, "");
    }

    public static void assertEquals(int[] actual, int[] expected, String message) {
        if (!checkRefEqualityAndLength(actual, expected, message)) {
            for(int i = 0; i < expected.length; ++i) {
                if (expected[i] != actual[i]) {
                    fail(String.format("arrays differ firstly at element [%d]; expected value is <%s> but was <%s>. %s", i, Integer.toString(expected[i]), Integer.toString(actual[i]), message));
                }
            }

        }
    }

    public static void assertEquals(boolean[] actual, boolean[] expected) {
        assertEquals(actual, expected, "");
    }

    public static void assertEquals(boolean[] actual, boolean[] expected, String message) {
        if (!checkRefEqualityAndLength(actual, expected, message)) {
            for(int i = 0; i < expected.length; ++i) {
                if (expected[i] != actual[i]) {
                    fail(String.format("arrays differ firstly at element [%d]; expected value is <%s> but was <%s>. %s", i, Boolean.toString(expected[i]), Boolean.toString(actual[i]), message));
                }
            }

        }
    }

    public static void assertEquals(char[] actual, char[] expected) {
        assertEquals(actual, expected, "");
    }

    public static void assertEquals(char[] actual, char[] expected, String message) {
        if (!checkRefEqualityAndLength(actual, expected, message)) {
            for(int i = 0; i < expected.length; ++i) {
                if (expected[i] != actual[i]) {
                    fail(String.format("arrays differ firstly at element [%d]; expected value is <%s> but was <%s>. %s", i, Character.toString(expected[i]), Character.toString(actual[i]), message));
                }
            }

        }
    }

    public static void assertEquals(float[] actual, float[] expected) {
        assertEquals(actual, expected, "");
    }

    public static void assertEquals(float[] actual, float[] expected, String message) {
        if (!checkRefEqualityAndLength(actual, expected, message)) {
            for(int i = 0; i < expected.length; ++i) {
                assertEquals(actual[i], expected[i], String.format("arrays differ firstly at element [%d]; expected value is <%s> but was <%s>. %s", i, Float.toString(expected[i]), Float.toString(actual[i]), message));
            }

        }
    }

    public static void assertEquals(float[] actual, float[] expected, float delta) {
        assertEquals(actual, expected, delta, "");
    }

    public static void assertEquals(float[] actual, float[] expected, float delta, String message) {
        if (!checkRefEqualityAndLength(actual, expected, message)) {
            for(int i = 0; i < expected.length; ++i) {
                assertEquals(actual[i], expected[i], delta, String.format("arrays differ firstly at element [%d]; expected value is <%s> but was <%s>. %s", i, Float.toString(expected[i]), Float.toString(actual[i]), message));
            }

        }
    }

    public static void assertEquals(double[] actual, double[] expected) {
        assertEquals(actual, expected, "");
    }

    public static void assertEquals(double[] actual, double[] expected, String message) {
        if (!checkRefEqualityAndLength(actual, expected, message)) {
            for(int i = 0; i < expected.length; ++i) {
                assertEquals(actual[i], expected[i], String.format("arrays differ firstly at element [%d]; expected value is <%s> but was <%s>. %s", i, Double.toString(expected[i]), Double.toString(actual[i]), message));
            }

        }
    }

    public static void assertEquals(double[] actual, double[] expected, double delta) {
        assertEquals(actual, expected, delta, "");
    }

    public static void assertEquals(double[] actual, double[] expected, double delta, String message) {
        if (!checkRefEqualityAndLength(actual, expected, message)) {
            for(int i = 0; i < expected.length; ++i) {
                assertEquals(actual[i], expected[i], delta, String.format("arrays differ firstly at element [%d]; expected value is <%s> but was <%s>. %s", i, Double.toString(expected[i]), Double.toString(actual[i]), message));
            }

        }
    }

    public static void assertEquals(long[] actual, long[] expected) {
        assertEquals(actual, expected, "");
    }

    public static void assertEquals(long[] actual, long[] expected, String message) {
        if (!checkRefEqualityAndLength(actual, expected, message)) {
            for(int i = 0; i < expected.length; ++i) {
                if (expected[i] != actual[i]) {
                    fail(String.format("arrays differ firstly at element [%d]; expected value is <%s> but was <%s>. %s", i, Long.toString(expected[i]), Long.toString(actual[i]), message));
                }
            }

        }
    }

    private static boolean checkRefEqualityAndLength(Object actualArray, Object expectedArray, String message) {
        if (expectedArray == actualArray) {
            return true;
        } else {
            if (null == expectedArray) {
                fail("expectedArray a null array, but not null found. " + message);
            }

            if (null == actualArray) {
                fail("expectedArray not null array, but null found. " + message);
            }

            assertEquals(Array.getLength(actualArray), Array.getLength(expectedArray), "arrays don't have the same size. " + message);
            return false;
        }
    }

    public static void assertEquals(Object actual, Object expected) {
        assertEquals((Object)actual, (Object)expected, (String)null);
    }

    public static void assertEquals(String actual, String expected, String message) {
        assertEquals((Object)actual, (Object)expected, message);
    }

    public static void assertEquals(String actual, String expected) {
        assertEquals((String)actual, (String)expected, (String)null);
    }

    private static boolean areEqual(double actual, double expected, double delta) {
        if (Double.isInfinite(expected)) {
            if (expected != actual) {
                return false;
            }
        } else if (Double.isNaN(expected)) {
            if (!Double.isNaN(actual)) {
                return false;
            }
        } else if (!(Math.abs(expected - actual) <= delta)) {
            return false;
        }

        return true;
    }

    public static void assertEquals(double actual, double expected, double delta, String message) {
        if (!areEqual(actual, expected, delta)) {
            failNotEquals(actual, expected, message);
        }

    }

    public static void assertEquals(double actual, double expected, double delta) {
        assertEquals(actual, expected, delta, (String)null);
    }

    public static void assertEquals(double actual, double expected, String message) {
        if (Double.isNaN(expected)) {
            if (!Double.isNaN(actual)) {
                failNotEquals(actual, expected, message);
            }
        } else if (actual != expected) {
            failNotEquals(actual, expected, message);
        }

    }

    public static void assertEquals(Double actual, Double expected, String message) {
        assertEquals((Object)actual, (Object)expected, message);
    }

    public static void assertEquals(double actual, double expected) {
        assertEquals(actual, expected, (String)null);
    }

    public static void assertEquals(Double actual, double expected) {
        assertEquals((Double)actual, (Double)expected, (String)null);
    }

    public static void assertEquals(double actual, Double expected) {
        assertEquals((Double)actual, (Double)expected, (String)null);
    }

    public static void assertEquals(Double actual, Double expected) {
        assertEquals((Double)actual, (Double)expected, (String)null);
    }

    private static boolean areEqual(float actual, float expected, float delta) {
        if (Float.isInfinite(expected)) {
            if (expected != actual) {
                return false;
            }
        } else if (Float.isNaN(expected)) {
            if (!Float.isNaN(actual)) {
                return false;
            }
        } else if (!(Math.abs(expected - actual) <= delta)) {
            return false;
        }

        return true;
    }

    public static void assertEquals(float actual, float expected, float delta, String message) {
        if (!areEqual(actual, expected, delta)) {
            failNotEquals(actual, expected, message);
        }

    }

    public static void assertEquals(float actual, float expected, float delta) {
        assertEquals(actual, expected, delta, (String)null);
    }

    public static void assertEquals(float actual, float expected, String message) {
        if (Float.isNaN(expected)) {
            if (!Float.isNaN(actual)) {
                failNotEquals(actual, expected, message);
            }
        } else if (actual != expected) {
            failNotEquals(actual, expected, message);
        }

    }

    public static void assertEquals(Float actual, Float expected, String message) {
        assertEquals((Object)actual, (Object)expected, message);
    }

    public static void assertEquals(float actual, float expected) {
        assertEquals(actual, expected, (String)null);
    }

    public static void assertEquals(Float actual, float expected) {
        assertEquals((Float)actual, (Float)expected, (String)null);
    }

    public static void assertEquals(float actual, Float expected) {
        assertEquals((Float)actual, (Float)expected, (String)null);
    }

    public static void assertEquals(Float actual, Float expected) {
        assertEquals((Float)actual, (Float)expected, (String)null);
    }

    public static void assertEquals(Long actual, Long expected, String message) {
        assertEquals((Object)actual, (Object)expected, message);
    }

    public static void assertEquals(long actual, long expected) {
        assertEquals(actual, expected, (String)null);
    }

    public static void assertEquals(Long actual, long expected) {
        assertEquals((Long)actual, (Long)expected, (String)null);
    }

    public static void assertEquals(long actual, Long expected) {
        assertEquals((Long)actual, (Long)expected, (String)null);
    }

    public static void assertEquals(Long actual, Long expected) {
        assertEquals((Long)actual, (Long)expected, (String)null);
    }

    public static void assertEquals(Boolean actual, Boolean expected, String message) {
        assertEquals((Object)actual, (Object)expected, message);
    }

    public static void assertEquals(boolean actual, boolean expected) {
        assertEquals(actual, expected, (String)null);
    }

    public static void assertEquals(Boolean actual, boolean expected) {
        assertEquals((Boolean)actual, (Boolean)expected, (String)null);
    }

    public static void assertEquals(Boolean actual, Boolean expected) {
        assertEquals((Boolean)actual, (Boolean)expected, (String)null);
    }

    public static void assertEquals(boolean actual, Boolean expected) {
        assertEquals((Boolean)actual, (Boolean)expected, (String)null);
    }

    public static void assertEquals(Byte actual, Byte expected, String message) {
        assertEquals((Object)actual, (Object)expected, message);
    }

    public static void assertEquals(byte actual, byte expected) {
        assertEquals((byte)actual, (byte)expected, (String)null);
    }

    public static void assertEquals(Byte actual, byte expected) {
        assertEquals((Byte)actual, (Byte)expected, (String)null);
    }

    public static void assertEquals(Byte actual, Byte expected) {
        assertEquals((Byte)actual, (Byte)expected, (String)null);
    }

    public static void assertEquals(byte actual, Byte expected) {
        assertEquals((Byte)actual, (Byte)expected, (String)null);
    }

    public static void assertEquals(Character actual, Character expected, String message) {
        assertEquals((Object)actual, (Object)expected, message);
    }

    public static void assertEquals(char actual, char expected) {
        assertEquals((char)actual, (char)expected, (String)null);
    }

    public static void assertEquals(Character actual, char expected) {
        assertEquals((Character)actual, (Character)expected, (String)null);
    }

    public static void assertEquals(char actual, Character expected) {
        assertEquals((Character)actual, (Character)expected, (String)null);
    }

    public static void assertEquals(Character actual, Character expected) {
        assertEquals((Character)actual, (Character)expected, (String)null);
    }

    public static void assertEquals(Short actual, Short expected, String message) {
        assertEquals((Object)actual, (Object)expected, message);
    }

    public static void assertEquals(short actual, short expected) {
        assertEquals((short)actual, (short)expected, (String)null);
    }

    public static void assertEquals(Short actual, short expected) {
        assertEquals((Short)actual, (Short)expected, (String)null);
    }

    public static void assertEquals(short actual, Short expected) {
        assertEquals((Short)actual, (Short)expected, (String)null);
    }

    public static void assertEquals(Short actual, Short expected) {
        assertEquals((Short)actual, (Short)expected, (String)null);
    }

    public static void assertEquals(Integer actual, Integer expected, String message) {
        assertEquals((Object)actual, (Object)expected, message);
    }

    public static void assertEquals(int actual, int expected) {
        assertEquals((int)actual, (int)expected, (String)null);
    }

    public static void assertEquals(Integer actual, int expected) {
        assertEquals((Integer)actual, (Integer)expected, (String)null);
    }

    public static void assertEquals(int actual, Integer expected) {
        assertEquals((Integer)actual, (Integer)expected, (String)null);
    }

    public static void assertEquals(Integer actual, Integer expected) {
        assertEquals((Integer)actual, (Integer)expected, (String)null);
    }

    public static void assertNotNull(Object object) {
        assertNotNull(object, (String)null);
    }

    public static void assertNotNull(Object object, String message) {
        if (object == null) {
            String formatted = "";
            if (message != null) {
                formatted = message + " ";
            }

            fail(formatted + "expected object to not be null");
        }

    }

    public static void assertNull(Object object) {
        assertNull(object, (String)null);
    }

    public static void assertNull(Object object, String message) {
        if (object != null) {
            failNotSame(object, (Object)null, message);
        }

    }

    public static void assertSame(Object actual, Object expected, String message) {
        if (expected != actual) {
            failNotSame(actual, expected, message);
        }
    }

    public static void assertSame(Object actual, Object expected) {
        assertSame(actual, expected, (String)null);
    }

    public static void assertNotSame(Object actual, Object expected, String message) {
        if (expected == actual) {
            failSame(actual, expected, message);
        }

    }

    public static void assertNotSame(Object actual, Object expected) {
        assertNotSame(actual, expected, (String)null);
    }

    private static void failSame(Object actual, Object expected, String message) {
        String formatted = "";
        if (message != null) {
            formatted = message + " ";
        }

        fail(formatted + AssertInterface.ASSERT_LEFT2 + expected + AssertInterface.ASSERT_MIDDLE + actual + AssertInterface.ASSERT_RIGHT);
    }

    private static void failNotSame(Object actual, Object expected, String message) {
        String formatted = "";
        if (message != null) {
            formatted = message + " ";
        }

        fail(formatted + AssertInterface.ASSERT_EQUAL_LEFT + expected + AssertInterface.ASSERT_MIDDLE + actual + AssertInterface.ASSERT_RIGHT);
    }

    private static void failNotEquals(Object actual, Object expected, String message) {
        fail(format(actual, expected, message, true));
    }

    private static void failEquals(Object actual, Object expected, String message) {
        fail(format(actual, expected, message, false));
    }

    static String format(Object actual, Object expected, String message, boolean isAssertEquals) {
        String formatted = "";
        if (null != message) {
            formatted = message + " ";
        }

        return isAssertEquals ? formatted + AssertInterface.ASSERT_EQUAL_LEFT + expected + AssertInterface.ASSERT_MIDDLE + actual + AssertInterface.ASSERT_RIGHT : formatted + AssertInterface.ASSERT_UNEQUAL_LEFT + expected + AssertInterface.ASSERT_MIDDLE + actual + AssertInterface.ASSERT_RIGHT;
    }

    public static void assertEquals(Collection<?> actual, Collection<?> expected) {
        assertEquals((Collection)actual, (Collection)expected, (String)null);
    }

    public static void assertEquals(Collection<?> actual, Collection<?> expected, String message) {
        if (actual != expected) {
            if (actual == null || expected == null) {
                if (message != null) {
                    fail(message);
                } else {
                    fail("Collections not equal: expected: " + expected + " and actual: " + actual);
                }
            }

            int var10000 = actual.size();
            int var10001 = expected.size();
            String var10002 = message == null ? "" : message + ": ";
            assertEquals(var10000, var10001, var10002 + "lists don't have the same size");
            Iterator<?> actIt = actual.iterator();
            Iterator<?> expIt = expected.iterator();
            int i = -1;

            while(actIt.hasNext() && expIt.hasNext()) {
                ++i;
                Object e = expIt.next();
                Object a = actIt.next();
                String explanation = "Lists differ at element [" + i + "]: " + e + " != " + a;
                String errorMessage = message == null ? explanation : message + ": " + explanation;
                assertEqualsImpl(a, e, errorMessage);
            }

        }
    }

    public static void assertEquals(Iterator<?> actual, Iterator<?> expected) {
        assertEquals((Iterator)actual, (Iterator)expected, (String)null);
    }

    public static void assertEquals(Iterator<?> actual, Iterator<?> expected, String message) {
        if (actual != expected) {
            if (actual == null || expected == null) {
                String msg = message != null ? message : "Iterators not equal: expected: " + expected + " and actual: " + actual;
                fail(msg);
            }

            int i = -1;

            while(actual.hasNext() && expected.hasNext()) {
                ++i;
                Object e = expected.next();
                Object a = actual.next();
                String explanation = "Iterators differ at element [" + i + "]: " + e + " != " + a;
                String errorMessage = message == null ? explanation : message + ": " + explanation;
                assertEqualsImpl(a, e, errorMessage);
            }

            String explanation;
            String errorMessage;
            if (actual.hasNext()) {
                explanation = "Actual iterator returned more elements than the expected iterator.";
                errorMessage = message == null ? explanation : message + ": " + explanation;
                fail(errorMessage);
            } else if (expected.hasNext()) {
                explanation = "Expected iterator returned more elements than the actual iterator.";
                errorMessage = message == null ? explanation : message + ": " + explanation;
                fail(errorMessage);
            }

        }
    }

    public static void assertEquals(Iterable<?> actual, Iterable<?> expected) {
        assertEquals((Iterable)actual, (Iterable)expected, (String)null);
    }

    public static void assertEquals(Iterable<?> actual, Iterable<?> expected, String message) {
        if (actual != expected) {
            if (actual == null || expected == null) {
                if (message != null) {
                    fail(message);
                } else {
                    fail("Iterables not equal: expected: " + expected + " and actual: " + actual);
                }
            }

            Iterator<?> actIt = actual.iterator();
            Iterator<?> expIt = expected.iterator();
            assertEquals(actIt, expIt, message);
        }
    }

    public static void assertEquals(Object[] actual, Object[] expected, String message) {
        if (!Arrays.equals(actual, expected)) {
            if (actual == null && expected != null || actual != null && expected == null) {
                if (message != null) {
                    fail(message);
                } else {
                    String var10000 = Arrays.toString(expected);
                    fail("Arrays not equal: expected: " + var10000 + " and actual: " + Arrays.toString(actual));
                }
            }

            if (actual.length != expected.length) {
                failAssertNoEqual("Arrays do not have the same size:" + actual.length + " != " + expected.length, message);
            }

            for(int i = 0; i < expected.length; ++i) {
                Object e = expected[i];
                Object a = actual[i];
                String explanation = "Arrays differ at element [" + i + "]: " + e + " != " + a;
                String errorMessage = message == null ? explanation : message + ": " + explanation;
                if (a != null || e != null) {
                    if (a == null && e != null || a != null && e == null) {
                        failNotEquals(a, e, message);
                    }

                    if (e.getClass().isArray()) {
                        assertEquals(a, e, errorMessage);
                    } else {
                        assertEqualsImpl(a, e, errorMessage);
                    }
                }
            }

        }
    }

    private static String toString(Iterator<?> iterator) {
        if (iterator == null) {
            return null;
        } else {
            Iterable<Object> iterable = () -> {
                return (Iterator<Object>) iterator;
            };
            return (String)StreamSupport.stream(iterable.spliterator(), false).map(Object::toString).collect(Collectors.joining(", "));
        }
    }

    private static void failAssertNoEqual(String defaultMessage, String message) {
        if (message != null) {
            fail(message);
        } else {
            fail(defaultMessage);
        }

    }

    public static void assertEquals(Object[] actual, Object[] expected) {
        assertEquals((Object[])actual, (Object[])expected, (String)null);
    }

    public static void assertEquals(Set<?> actual, Set<?> expected) {
        assertEquals((Set)actual, (Set)expected, (String)null);
    }

    private static String getNotEqualReason(Collection<?> actual, Collection<?> expected) {
        if (actual == expected) {
            return null;
        } else if (actual != null && expected != null) {
            return !Objects.equals(actual, expected) ? "Collections differ: expected " + expected + " but got " + actual : getNotEqualReason(actual.iterator(), expected.iterator());
        } else {
            return "Collections not equal: expected: " + expected + " and actual: " + actual;
        }
    }

    private static String getNotEqualReason(Iterator<?> actual, Iterator<?> expected) {
        if (actual == expected) {
            return null;
        } else {
            String var10000;
            if (actual != null && expected != null) {
                while(actual.hasNext() && expected.hasNext()) {
                    if (!Objects.equals(actual.next(), expected.next())) {
                        var10000 = toString(expected);
                        return "Iterators not same element order: expected: " + var10000 + " and actual: " + toString(actual);
                    }
                }

                return null;
            } else {
                var10000 = toString(expected);
                return "Iterators not equal: expected: " + var10000 + " and actual: " + toString(actual);
            }
        }
    }

    private static String getNotEqualReason(Set<?> actual, Set<?> expected) {
        if (actual == expected) {
            return null;
        } else if (actual != null && expected != null) {
            return !Objects.equals(actual, expected) ? "Sets differ: expected " + expected + " but got " + actual : null;
        } else {
            return "Sets not equal: expected: " + expected + " and actual: " + actual;
        }
    }

    public static void assertEquals(Set<?> actual, Set<?> expected, String message) {
        String notEqualReason = getNotEqualReason(actual, expected);
        if (null != notEqualReason) {
            if (message == null) {
                fail(notEqualReason);
            } else {
                fail(message);
            }
        }

    }

    private static String getNotEqualDeepReason(Set<?> actual, Set<?> expected) {
        if (Objects.equals(actual, expected)) {
            return null;
        } else if (actual != null && expected != null) {
            if (expected.size() != actual.size()) {
                return "Sets not equal: expected: " + expected + " and actual: " + actual;
            } else {
                Iterator<?> actualIterator = actual.iterator();
                Iterator<?> expectedIterator = expected.iterator();

                while(expectedIterator.hasNext()) {
                    Object expectedValue = expectedIterator.next();
                    Object value = actualIterator.next();
                    if (expectedValue.getClass().isArray()) {
                        String arrayNotEqualReason = getArrayNotEqualReason(value, expectedValue);
                        if (arrayNotEqualReason != null) {
                            return arrayNotEqualReason;
                        }
                    } else if (!areEqualImpl(value, expected)) {
                        return "Sets not equal: expected: " + expectedValue + " and actual: " + value;
                    }
                }

                return null;
            }
        } else {
            return "Sets not equal: expected: " + expected + " and actual: " + actual;
        }
    }

    public static void assertEqualsDeep(Set<?> actual, Set<?> expected, String message) {
        String notEqualDeepReason = getNotEqualDeepReason(actual, expected);
        if (notEqualDeepReason != null) {
            if (message == null) {
                fail(notEqualDeepReason);
            } else {
                fail(message);
            }
        }

    }

    public static void assertEquals(Map<?, ?> actual, Map<?, ?> expected) {
        assertEquals((Map)actual, (Map)expected, (String)null);
    }

    private static String getNotEqualReason(Map<?, ?> actual, Map<?, ?> expected) {
        if (Objects.equals(actual, expected)) {
            return null;
        } else if (actual != null && expected != null) {
            if (actual.size() != expected.size()) {
                int var10000 = actual.size();
                return "Maps do not have the same size:" + var10000 + " != " + expected.size();
            } else {
                Set<?> entrySet = actual.entrySet();
                Iterator var3 = entrySet.iterator();

                Object value;
                Object expectedValue;
                String assertMessage;
                do {
                    if (!var3.hasNext()) {
                        return null;
                    }

                    Object anEntrySet = var3.next();
                    Map.Entry<?, ?> entry = (Map.Entry)anEntrySet;
                    Object key = entry.getKey();
                    value = entry.getValue();
                    expectedValue = expected.get(key);
                    assertMessage = "Maps do not match for key:" + key + " actual:" + value + " expected:" + expectedValue;
                } while(areEqualImpl(value, expectedValue));

                return assertMessage;
            }
        } else {
            return "Maps not equal: expected: " + expected + " and actual: " + actual;
        }
    }

    public static void assertEquals(Map<?, ?> actual, Map<?, ?> expected, String message) {
        String notEqualReason = getNotEqualReason(actual, expected);
        if (notEqualReason != null) {
            if (message == null) {
                fail(notEqualReason);
            } else {
                fail(message);
            }
        }

    }

    public static void assertEqualsDeep(Map<?, ?> actual, Map<?, ?> expected) {
        assertEqualsDeep((Map)actual, (Map)expected, (String)null);
    }

    private static String getNotEqualDeepReason(Map<?, ?> actual, Map<?, ?> expected) {
        if (Objects.equals(actual, expected)) {
            return null;
        } else if (actual != null && expected != null) {
            if (actual.size() != expected.size()) {
                int var10000 = actual.size();
                return "Maps do not have the same size:" + var10000 + " != " + expected.size();
            } else {
                Set<?> entrySet = actual.entrySet();
                Iterator var3 = entrySet.iterator();

                while(var3.hasNext()) {
                    Object anEntrySet = var3.next();
                    Map.Entry<?, ?> entry = (Map.Entry)anEntrySet;
                    Object key = entry.getKey();
                    Object value = entry.getValue();
                    Object expectedValue = expected.get(key);
                    String assertMessage = "Maps do not match for key:" + key + " actual:" + value + " expected:" + expectedValue;
                    if (expectedValue.getClass().isArray()) {
                        if (!areArraysEqual(value, expectedValue)) {
                            return assertMessage;
                        }
                    } else if (!areEqualImpl(value, expectedValue)) {
                        return assertMessage;
                    }
                }

                return null;
            }
        } else {
            return "Maps not equal: expected: " + expected + " and actual: " + actual;
        }
    }

    public static void assertEqualsDeep(Map<?, ?> actual, Map<?, ?> expected, String message) {
        String notEqualDeepReason = getNotEqualDeepReason(actual, expected);
        if (notEqualDeepReason != null) {
            if (message == null) {
                fail(notEqualDeepReason);
            } else {
                fail(message);
            }
        }

    }

    public static void assertNotEquals(Object actual, Object expected, String message) {
        if (expected != null && expected.getClass().isArray()) {
            assertArrayNotEquals(actual, expected, message);
        } else {
            assertNotEqualsImpl(actual, expected, message);
        }
    }

    public static void assertNotEquals(Object[] actual, Object[] expected, String message) {
        assertArrayNotEquals(actual, expected, message);
    }

    public static void assertNotEquals(Iterator<?> actual, Iterator<?> expected, String message) {
        String notEqualReason = getNotEqualReason(actual, expected);
        if (notEqualReason == null) {
            fail(format(actual, expected, message, false));
        }

    }

    public static void assertNotEquals(Collection<?> actual, Collection<?> expected, String message) {
        String notEqualReason = getNotEqualReason(actual, expected);
        if (notEqualReason == null) {
            fail(format(actual, expected, message, false));
        }

    }

    public static void assertNotEquals(Object actual, Object expected) {
        assertNotEquals((Object)actual, (Object)expected, (String)null);
    }

    public static void assertNotEquals(Collection<?> actual, Collection<?> expected) {
        assertNotEquals((Collection)actual, (Collection)expected, (String)null);
    }

    public static void assertNotEquals(Iterator<?> actual, Iterator<?> expected) {
        assertNotEquals((Iterator)actual, (Iterator)expected, (String)null);
    }

    static void assertNotEquals(String actual, String expected, String message) {
        assertNotEquals((Object)actual, (Object)expected, message);
    }

    static void assertNotEquals(String actual, String expectec) {
        assertNotEquals((String)actual, (String)expectec, (String)null);
    }

    static void assertNotEquals(long actual, long expected, String message) {
        assertNotEquals((Object)actual, (Object)expected, message);
    }

    static void assertNotEquals(long actual, long expected) {
        assertNotEquals(actual, expected, (String)null);
    }

    static void assertNotEquals(boolean actual, boolean expected, String message) {
        assertNotEquals((Object)actual, (Object)expected, message);
    }

    static void assertNotEquals(boolean actual, boolean expected) {
        assertNotEquals(actual, expected, (String)null);
    }

    static void assertNotEquals(byte actual, byte expected, String message) {
        assertNotEquals((Object)actual, (Object)expected, message);
    }

    static void assertNotEquals(byte actual, byte expected) {
        assertNotEquals((byte)actual, (byte)expected, (String)null);
    }

    static void assertNotEquals(char actual, char expected, String message) {
        assertNotEquals((Object)actual, (Object)expected, message);
    }

    static void assertNotEquals(char actual, char expected) {
        assertNotEquals((char)actual, (char)expected, (String)null);
    }

    static void assertNotEquals(short actual, short expected, String message) {
        assertNotEquals((Object)actual, (Object)expected, message);
    }

    static void assertNotEquals(short actual, short expected) {
        assertNotEquals((short)actual, (short)expected, (String)null);
    }

    static void assertNotEquals(int actual, int expected, String message) {
        assertNotEquals((Object)actual, (Object)expected, message);
    }

    static void assertNotEquals(int actual, int expected) {
        assertNotEquals((int)actual, (int)expected, (String)null);
    }

    public static void assertNotEquals(float actual, float expected, float delta, String message) {
        if (areEqual(actual, expected, delta)) {
            fail(format(actual, expected, message, false));
        }

    }

    public static void assertNotEquals(float actual, float expected, float delta) {
        assertNotEquals(actual, expected, delta, (String)null);
    }

    public static void assertNotEquals(double actual, double expected, double delta, String message) {
        if (areEqual(actual, expected, delta)) {
            fail(format(actual, expected, message, false));
        }

    }

    public static void assertNotEquals(Set<?> actual, Set<?> expected) {
        assertNotEquals((Set)actual, (Set)expected, (String)null);
    }

    public static void assertNotEquals(Set<?> actual, Set<?> expected, String message) {
        String notEqualReason = getNotEqualReason(actual, expected);
        if (notEqualReason == null) {
            fail(format(actual, expected, message, false));
        }

    }

    public static void assertNotEqualsDeep(Set<?> actual, Set<?> expected) {
        assertNotEqualsDeep((Set)actual, (Set)expected, (String)null);
    }

    public static void assertNotEqualsDeep(Set<?> actual, Set<?> expected, String message) {
        String notEqualDeepReason = getNotEqualDeepReason(actual, expected);
        if (notEqualDeepReason == null) {
            fail(format(actual, expected, message, false));
        }

    }

    public static void assertNotEquals(Map<?, ?> actual, Map<?, ?> expected) {
        assertNotEquals((Map)actual, (Map)expected, (String)null);
    }

    public static void assertNotEquals(Map<?, ?> actual, Map<?, ?> expected, String message) {
        String notEqualReason = getNotEqualReason(actual, expected);
        if (notEqualReason == null) {
            fail(format(actual, expected, message, false));
        }

    }

    public static void assertNotEqualsDeep(Map<?, ?> actual, Map<?, ?> expected) {
        assertNotEqualsDeep((Map)actual, (Map)expected, (String)null);
    }

    public static void assertNotEqualsDeep(Map<?, ?> actual, Map<?, ?> expected, String message) {
        String notEqualDeepReason = getNotEqualDeepReason(actual, expected);
        if (notEqualDeepReason == null) {
            fail(format(actual, expected, message, false));
        }

    }

    public static void assertNotEquals(double actual, double expected, double delta) {
        assertNotEquals(actual, expected, delta, (String)null);
    }

    public static void assertThrows(ThrowingRunnable runnable) {
        assertThrows(Throwable.class, runnable);
    }

    public static <T extends Throwable> void assertThrows(Class<T> throwableClass, ThrowingRunnable runnable) {
        expectThrows(throwableClass, runnable);
    }

    public static <T extends Throwable> void assertThrows(String message, Class<T> throwableClass, ThrowingRunnable runnable) {
        expectThrows(message, throwableClass, runnable);
    }

    public static <T extends Throwable> T expectThrows(Class<T> throwableClass, ThrowingRunnable runnable) {
        return expectThrows((String)null, throwableClass, runnable);
    }

    public static <T extends Throwable> T expectThrows(String message, Class<T> throwableClass, ThrowingRunnable runnable) {
        try {
            runnable.run();
        } catch (Throwable var5) {
            Throwable t = var5;
            if (throwableClass.isInstance(t)) {
                return (T) throwableClass.cast(t);
            }

            String mismatchMessage = String.format("Expected %s to be thrown, but %s was thrown", throwableClass.getSimpleName(), t.getClass().getSimpleName());
            throw new AssertionError(message != null ? message : mismatchMessage, t);
        }

        String nothingThrownMessage = String.format("Expected %s to be thrown, but nothing was thrown", throwableClass.getSimpleName());
        throw new AssertionError(message != null ? message : nothingThrownMessage);
    }

    public interface ThrowingRunnable {
        void run() throws Throwable;
    }

    static class AssertInterface {
        public static final Character OPENING_CHARACTER = '[';
        public static final Character CLOSING_CHARACTER = ']';
        public static final String ASSERT_EQUAL_LEFT;
        public static final String ASSERT_UNEQUAL_LEFT;
        public static final String ASSERT_LEFT2;
        public static final String ASSERT_MIDDLE;
        public static final String ASSERT_RIGHT;

        public AssertInterface() {
        }

        static {
            ASSERT_EQUAL_LEFT = "expected " + OPENING_CHARACTER;
            ASSERT_UNEQUAL_LEFT = "did not expect " + OPENING_CHARACTER;
            ASSERT_LEFT2 = "expected not same " + OPENING_CHARACTER;
            ASSERT_MIDDLE = CLOSING_CHARACTER + " but found " + OPENING_CHARACTER;
            ASSERT_RIGHT = Character.toString(CLOSING_CHARACTER);
        }
    }
}
