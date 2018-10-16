package de.bwl.bwfla.common.utils.jaxb;

import static org.junit.Assert.*;

import javax.xml.bind.ValidationException;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;

import org.junit.Test;

public class JaxbValidatorTest {
    @Test(expected = ValidationException.class)
    public void testValidateRequiredWithXML() throws Throwable {
        MockXMLElement mockXML = new MockXMLElement();
        JaxbValidator.validateRequired(mockXML);
        fail();
    }

    @Test
    public void testValidateRequiredWithXML2() throws Throwable {
        MockXMLElement mockXML = new MockXMLElement();
        mockXML.setNillFalseNillable(false);
        mockXML.setNillTrueNillable(true);
        mockXML.setReqAndBool(true);
        mockXML.setReqAndByte((byte) 100);
        mockXML.setReqAndChar("c".charAt(0));
        mockXML.setReqAndDouble(123.4);
        mockXML.setReqAndFloat(10.2f);
        mockXML.setReqAndInt(3);
        mockXML.setReqAndInteger(2);
        mockXML.setReqAndShort((short) 10000);
        mockXML.setReqAndString("A String");
        mockXML.setRequiredTrue("true");
        mockXML.setRequiredNotTrue("false");
        mockXML.setReqAndLong(10000L);
        JaxbValidator.validate(mockXML);
    }

    @Test
    public void nullCheck() throws Throwable {
        MockXMLElement mockXML = new MockXMLElement();
        mockXML.setNillFalseNillable(false);
        mockXML.setRequiredTrue("");
        mockXML.setNillTrueNillable(true);
        JaxbValidator.validate(mockXML);
    }

    @Test
    public void test1() throws Throwable {
        Float float0 = new Float((double) (short) (-1));
        Class<Float> class0 = Float.class;
        JaxbValidator.validateNillable(float0);
    }

    @Test
    public void test2() throws Throwable {
        Long long0 = new Long((-2L));
        Class<Long> class0 = Long.class;
        JaxbValidator.validateRequired(long0);
    }

    @Test
    public void test3() throws Throwable {
        Float float0 = new Float(2047.4F);
        Class<Float> class0 = Float.class;
        JaxbValidator.validate(float0);
    }

    @Test
    public void testDefault() throws Throwable {
        MockDefaults mockXML = new MockDefaults();
        JaxbValidator.validate(mockXML);
        assertEquals("some string", mockXML.requiredString);
        assertEquals(Byte.valueOf((byte)42), mockXML.requiredByte);
        assertEquals(Short.valueOf((short)30000), mockXML.requiredShort);
        assertEquals(Integer.valueOf(3000000), mockXML.requiredInteger);
        assertEquals(Long.valueOf(8000000L), mockXML.requiredLong);
        assertEquals(Float.valueOf(42.23f), mockXML.requiredFloat);
        assertEquals(Double.valueOf(42.23), mockXML.requiredDouble);
        assertEquals(Character.valueOf('x'), mockXML.requiredCharacter);
        assertEquals(true, mockXML.requiredBoolean);

        // primitive types have 0 as default and they cannot be differentiated
        // from null
        assertEquals(0, mockXML.requiredBytePrimitive);
        assertEquals(0, mockXML.requiredShortPrimitive);
        assertEquals(0, mockXML.requiredIntegerPrimitive);
        assertEquals(0L, mockXML.requiredLongPrimitive);
        assertEquals(0.0f, mockXML.requiredFloatPrimitive, Math.ulp(0.0f));
        assertEquals(0.0, mockXML.requiredDoublePrimitive, Math.ulp(0.0d));
        assertEquals('\0', mockXML.requiredCharacterPrimitive);
        assertEquals(false, mockXML.requiredBooleanPrimitive);
    }
    
    @Test(expected = ValidationException.class)
    public void testRecursionWithNullChild() throws Throwable {
        MockOuter<MockInner> outer = new MockOuter<>();
        assertNull(outer.inner);
        JaxbValidator.validate(outer);
        fail();
    }

    @Test(expected = ValidationException.class)
    public void testRecursionWithChild() throws Throwable {
        MockOuter<MockInner> outer = new MockOuter<>();
        outer.inner = new MockInner();
        assertNull(outer.inner.s);
        JaxbValidator.validate(outer);
        fail();
    }

    @Test
    public void testRecursion() throws Throwable {
        MockOuter<MockInner> outer = new MockOuter<>();
        outer.inner = new MockInner();
        outer.inner.s = "some string";
        JaxbValidator.validate(outer);
        assertEquals("some string", outer.inner.s);
    }
    
    @Test
    public void testRecursionDefault() throws Throwable {
        MockOuter<MockInnerWithDefault> outer = new MockOuter<>();
        outer.inner = new MockInnerWithDefault();
        assertNull(outer.inner.s);
        JaxbValidator.validate(outer);
        assertEquals("some default string", outer.inner.s);
    }
    
    @Test
    public void test4() throws Throwable {
        JaxbValidator jaxbValidator0 = new JaxbValidator();
    }
    
    @Test
    public void testInheitance() throws Throwable {
        InheritanceChild object = new InheritanceChild();
        JaxbValidator.validate(object);
        assertEquals("from parent", object.fromParent);
        assertEquals("from child", object.fromChild);
    }
}

class MockDefaults extends JaxbType {
    @XmlElement(required = true, defaultValue = "some string")
    public String requiredString;

    @XmlElement(required = true, defaultValue = "42")
    public Byte requiredByte;

    @XmlElement(required = true, defaultValue = "30000")
    public Short requiredShort;
    
    @XmlElement(required = true, defaultValue = "3000000")
    public Integer requiredInteger;
    
    @XmlElement(required = true, defaultValue = "8000000")
    public Long requiredLong;
    
    @XmlElement(required = true, defaultValue = "42.23")
    public Float requiredFloat;
    
    @XmlElement(required = true, defaultValue = "42.23")
    public Double requiredDouble;
    
    @XmlElement(required = true, defaultValue = "x")
    public Character requiredCharacter;
    
    @XmlElement(required = true, defaultValue = "true")
    public Boolean requiredBoolean;
    
    @XmlElement(required = true, defaultValue = "42")
    public byte requiredBytePrimitive;

    @XmlElement(required = true, defaultValue = "30000")
    public short requiredShortPrimitive;
    
    @XmlElement(required = true, defaultValue = "3000000")
    public int requiredIntegerPrimitive;
    
    @XmlElement(required = true, defaultValue = "8000000")
    public long requiredLongPrimitive;
    
    @XmlElement(required = true, defaultValue = "42.23")
    public float requiredFloatPrimitive;
    
    @XmlElement(required = true, defaultValue = "42.23")
    public double requiredDoublePrimitive;
    
    @XmlElement(required = true, defaultValue = "x")
    public char requiredCharacterPrimitive;
    
    @XmlElement(required = true, defaultValue = "true")
    public boolean requiredBooleanPrimitive;
}

@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "mockXMLElement")
class MockXMLElement {
    @XmlElement(required = true)
    public String requiredTrue;
    @XmlElement(required = false)
    public String requiredNotTrue;
    @XmlElement(required = true, defaultValue = "A String")
    public String reqAndString;
    @XmlElement(required = true, defaultValue = "2")
    public Integer reqAndInteger;
    @XmlElement(required = true, defaultValue = "3")
    public int reqAndInt;
    @XmlElement(required = true, defaultValue = "true")
    public Boolean reqAndBool;
    @XmlElement(required = true, defaultValue = "100")
    public Byte reqAndByte;
    @XmlElement(required = true, defaultValue = "10.2f")
    public Float reqAndFloat;
    @XmlElement(required = true, defaultValue = "10000")
    public Short reqAndShort;
    @XmlElement(required = true, defaultValue = "1000L")
    public long reqAndLong;
    @XmlElement(required = true, defaultValue = "123.4")
    public Double reqAndDouble;
    @XmlElement(required = true, defaultValue = "c")
    public char reqAndChar;
    @XmlElement(required = true, nillable = true)
    public Boolean nillTrueNillable;
    @XmlElement(required = true, nillable = false)
    public Boolean nillFalseNillable;

    public Boolean getNillTrueNillable() {
        return nillTrueNillable;
    }

    public void setNillTrueNillable(Boolean nillTrueNillable) {
        this.nillTrueNillable = nillTrueNillable;
    }

    public void setNillFalseNillableAsNull() {
        this.nillFalseNillable = null;
    }
    public Boolean getNillFalseNillble() {
        return nillFalseNillable;
    }

    public void setNillFalseNillable(Boolean nillFalseNillable) {
        this.nillFalseNillable = nillFalseNillable;
    }

    public String getRequiredTrue() {
        return requiredTrue;
    }

    public void setRequiredTrue(String requiredTrue) {
        this.requiredTrue = requiredTrue;
    }

    public String getRequiredNotTrue() {
        return requiredNotTrue;
    }

    public void setRequiredNotTrue(String requiredNotTrue) {
        this.requiredNotTrue = requiredNotTrue;
    }

    public String getReqAndString() {
        return reqAndString;
    }

    public void setReqAndString(String reqAndString) {
        this.reqAndString = reqAndString;
    }

    public Integer getReqAndInteger() {
        return reqAndInteger;
    }

    public void setReqAndInteger(Integer reqAndInteger) {
        this.reqAndInteger = reqAndInteger;
    }

    public int getReqAndInt() {
        return reqAndInt;
    }

    public void setReqAndInt(int reqAndInt) {
        this.reqAndInt = reqAndInt;
    }

    public Boolean getReqAndBool() {
        return reqAndBool;
    }

    public void setReqAndBool(Boolean reqAndBool) {
        this.reqAndBool = reqAndBool;
    }

    public Byte getReqAndByte() {
        return reqAndByte;
    }

    public void setReqAndByte(Byte reqAndByte) {
        this.reqAndByte = reqAndByte;
    }

    public Float getReqAndFloat() {
        return reqAndFloat;
    }

    public void setReqAndFloat(Float reqAndFloat) {
        this.reqAndFloat = reqAndFloat;
    }

    public Short getReqAndShort() {
        return reqAndShort;
    }

    public void setReqAndShort(Short reqAndShort) {
        this.reqAndShort = reqAndShort;
    }

    public Long getReqAndLong() {
        return reqAndLong;
    }

    public void setReqAndLong(Long reqAndLong) {
        this.reqAndLong = reqAndLong;
    }

    public Double getReqAndDouble() {
        return reqAndDouble;
    }

    public void setReqAndDouble(Double reqAndDouble) {
        this.reqAndDouble = reqAndDouble;
    }

    public char getReqAndChar() {
        return reqAndChar;
    }

    public void setReqAndChar(char reqAndChar) {
        this.reqAndChar = reqAndChar;
    }

}

class MockOuter<T> extends JaxbType {
    @XmlElement(required = true)
    public T inner;
}

class MockInner extends JaxbType {
    @XmlElement(required = true)
    public String s;
}

class MockInnerWithDefault extends JaxbType {
    @XmlElement(required = true, defaultValue = "some default string")
    public String s;
}

class InheritanceParent extends JaxbType {
    @XmlElement(required = true, defaultValue = "from parent")
    public String fromParent;
}

class InheritanceChild extends InheritanceParent {
    @XmlElement(required = true, defaultValue = "from child")
    public String fromChild;
}