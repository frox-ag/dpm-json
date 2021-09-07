package com.frox.opendpm.processtools.json

import org.junit.Test

import static com.frox.opendpm.processtools.json.JsonProxyUtils.wrap as dpmJson
import static org.junit.Assert.*


class SpinJsonNodeProxyTest {

    @Test
    void testReadWithoutArguments() {
        def obj = dpmJson()
        assertEquals("{}", obj.toString())
    }

    @Test
    void testReadNullJson() {
        def obj = dpmJson(null)
        assertEquals("{}", obj.toString())
    }

    @Test
    void testReadEmptyJson() {
        def obj = dpmJson("")
        assertEquals("{}", obj.toString())
    }

    @Test
    void testReadEmptyRootJson() {
        def obj = dpmJson("{}")
        assertEquals("{}", obj.toString())
    }

    @Test
    void testReadEmptyArrayJson() {
        def obj = dpmJson("[]")
        assertEquals("[]", obj.toString())
    }

    @Test(expected = IllegalArgumentException.class)
    void testReadNotValidJson() {
        dpmJson("{{}}")
    }

    @Test(expected = IllegalArgumentException.class)
    void testReadSimpleString() {
        def obj = dpmJson("test")
        assertEquals("test", obj.toString())
    }

    @Test
    void testReadJsonString() {
        def json = "{\"name\":[\"John\"]}"
        def obj = dpmJson(json)
        assertEquals(json, obj.toString())
    }

    @Test
    void testReadMap() {
        def obj = dpmJson(["test": 500])
        assertEquals("{\"test\":500}", obj.toString())
    }

    @Test
    void testReadList() {
        def list = ["test1", "test2"]
        def obj = dpmJson(list)
        assertEquals(list, obj.value())
    }

    @Test
    void testReadObject() {
        TestDto testObject = initTestObject()
        def obj = dpmJson(testObject)
        assertEquals(testObject.toString(), obj.toString())
    }

    @Test
    void testReadMapValue() {
        def obj = dpmJson(["test": 500])
        assertEquals(500, obj.test.value())
    }

    @Test
    void testReadMapNotExistValue() {
        def obj = dpmJson(["test": 500])
        assertNull(obj.test2.value())
    }

    @Test
    void testReadMapNotExistValue2() {
        def obj = dpmJson(["test": 500])
        assertNull(obj.test.test2.value())
    }

    @Test
    void testReadMapNotExistValue3() {
        def obj = dpmJson(["test": 500])
        assertNull(obj.test2.test3.value())
    }

    @Test
    void testReadMapNotExistArrayValue() {
        def obj = dpmJson(["test": 500])
        assertNull(obj.test2[2].value())
    }

    @Test(expected = UnsupportedOperationException.class)
    void testReadMapNotArrayValue() {
        def obj = dpmJson(["test": 500])
        assertNull(obj.test[0].value())
    }

    @Test
    void testReadNestedArrayValueByIndex() {
        def obj = dpmJson(["test": ["arr": [1, 2, 3]]])
        assertEquals(1, obj.test.arr[0].value())
    }

    @Test(expected = ArrayIndexOutOfBoundsException)
    void testReadNestedArrayValueByInvalidIndex() {
        def obj = dpmJson(["test": ["arr": [1, 2, 3]]])
        obj.test.arr[5].value()
    }

    @Test(expected = ArrayIndexOutOfBoundsException)
    void testReadNestedArrayValueByInvalidIndex2() {
        def obj = dpmJson(["test": ["arr": [1, 2, 3]]])
        obj.test.arr[-1].value()
    }

    @Test
    void testReadNestedArrayPrimitiveStringValue() {
        def arr = ["test1", "test2", "test3"]
        def obj = dpmJson(["test": ["arr": arr]])
        assertEquals(arr, obj.test.arr.value())
    }

    @Test
    void testReadNestedArrayPrimitiveNumberValue() {
        def arr = [1, 2, 3]
        def obj = dpmJson(["test": ["arr": arr]])
        assertEquals(arr, obj.test.arr.value())
    }

    @Test
    void testReadNestedArrayComplexValue() {
        def arr = [["v": 1], ["v": 2], ["v": 3]]
        def obj = dpmJson(["test": ["arr": arr]])
        assertEquals(
                [dpmJson(["v": 1]), dpmJson(["v": 2]), dpmJson(["v": 3])].toString(),
                obj.test.arr.value().toString()
        )
    }

    @Test
    void testReadNestedArrayFirstValue() {
        def arr = [["v": 1], ["v": 2], ["v": 3]]
        def obj = dpmJson(["arr": arr])
        assertEquals(arr[0].v, obj.arr.v.value())
    }

    @Test
    void testReadRootArrayValueByIndex() {
        def obj = dpmJson([1, 2, 3])
        assertEquals(1, obj[0].value())
    }

    @Test(expected = ArrayIndexOutOfBoundsException)
    void testReadRootArrayValueByInvalidIndex() {
        def obj = dpmJson([1, 2, 3])
        obj[5].value()
    }

    @Test(expected = ArrayIndexOutOfBoundsException)
    void testReadRootArrayValueByInvalidIndex2() {
        def obj = dpmJson([1, 2, 3])
        obj[-1].value()
    }

    @Test
    void testReadRootArrayPrimitiveStringValue() {
        def arr = ["test1", "test2", "test3"]
        def obj = dpmJson(arr)
        assertEquals(arr, obj.value())
    }

    @Test
    void testReadRootArrayPrimitiveNumberValue() {
        def arr = [1, 2, 3]
        def obj = dpmJson(arr)
        assertEquals(arr, obj.value())
    }

    @Test
    void testReadRootArrayComplexValue() {
        def arr = [["v": 1], ["v": 2], ["v": 3]]
        def obj = dpmJson(arr)
        assertEquals(
                [dpmJson(["v": 1]), dpmJson(["v": 2]), dpmJson(["v": 3])].toString(),
                obj.value().toString()
        )
    }

    @Test
    void testReadRootArrayFirstValue() {
        def arr = [["v": 1], ["v": 2], ["v": 3]]
        def obj = dpmJson(arr)
        assertEquals(arr[0].v, obj.v.value())
    }

    @Test
    void testSetProperty() {
        TestDto testObject = initTestObject()
        def obj = dpmJson(testObject)
        obj.test = "testValue"
        assertEquals("testValue", obj.test.value())
    }

    @Test
    void testOverrideProperty() {
        TestDto testObject = initTestObject()
        def obj = dpmJson(testObject)
        assertEquals("tStringValue", obj.tString.value())
        obj.tString = "testValue"
        assertEquals("testValue", obj.tString.value())
    }

    @Test
    void testSetPropertyChain() {
        TestDto testObject = initTestObject()
        def obj = dpmJson(testObject)
        obj.t1.t2.test = "testValue"
        assertEquals("testValue", obj.t1.t2.test.value())
    }

    @Test
    void testSetPropertyChainWithArrayInitialization() {
        TestDto testObject = initTestObject()
        def obj = dpmJson(testObject)
        obj.t1.t2.test = []
        obj.t1.t2.test.push("testValue")
        assertEquals("testValue", obj.t1.t2.test[0].value())
    }

    @Test(expected = UnsupportedOperationException)
    void testSetPropertyIntoNotInitializedArray() {
        TestDto testObject = initTestObject()
        def obj = dpmJson(testObject)
        obj.test
        obj.test.push("testValue")
    }

    @Test
    void testSetPropertyIntoArray() {
        TestDto testObject = initTestObject()
        def obj = dpmJson(testObject)
        assertEquals(testObject.tColl, obj.tColl.value())
        obj.tColl.push("testValue")
        assertEquals(["test1", "test2", "test3", "testValue"], obj.tColl.value())
    }

    @Test
    void testSetPropertyIntoNonExistArray() {
        TestDto testObject = initTestObject()
        def obj = dpmJson(testObject)
        obj.test = ["testValue1", "testValue2"]
        obj.test.push("testValue3")
        assertEquals(["testValue1", "testValue2", "testValue3"], obj.test.value())
    }

    @Test
    void testOverridePropertyIntoArray() {
        TestDto testObject = initTestObject()
        def obj = dpmJson(testObject)
        assertEquals(testObject.tColl, obj.tColl.value())
        obj.tColl[1] = "testValue"
        assertEquals(["test1", "testValue", "test3"], obj.tColl.value())
    }

    @Test
    void testSetPropertyIntoMap() {
        TestDto testObject = initTestObject()
        def obj = dpmJson(testObject)
        assertEquals(mapToJsonString(testObject.tMap), obj.tMap.toString())
        obj.tMap.test = "testValue"
        assertEquals(mapToJsonString(["test1": "testValue1", "test2": "testValue2", "test": "testValue"]), obj.tMap.toString())
    }

    @Test
    void testOverridePropertyIntoMap() {
        TestDto testObject = initTestObject()
        def obj = dpmJson(testObject)
        assertEquals(mapToJsonString(testObject.tMap), obj.tMap.toString())
        obj.tMap.test1 = "testValue"
        assertEquals(mapToJsonString(["test1": "testValue", "test2": "testValue2"]), obj.tMap.toString())
    }

    @Test
    void testAccessPropertyIntoMapWithBracketsOperator() {
        TestDto testObject = initTestObject()
        def obj = dpmJson(testObject)
        assertEquals(mapToJsonString(testObject.tMap), obj.tMap.toString())
        obj.tMap["test"] = "testValue"
        assertEquals(mapToJsonString(["test1": "testValue1", "test2": "testValue2", "test": "testValue"]), obj.tMap.toString())
        assertEquals("testValue", obj.tMap["test"].value())
    }

    @Test
    void testIsMapOperator() {
        TestDto testObject = initTestObject()
        def obj = dpmJson(testObject)
        assertTrue(obj.tMap.isMap())
        assertFalse(obj.tColl.isMap())
        assertFalse(obj.tString.isMap())
        assertFalse(obj.tLong.isMap())
        assertFalse(obj.test.tMap.isMap())

        obj = dpmJson(["test1": "testValue1", "test2": "testValue2"])
        assertTrue(obj.isMap())

        obj = dpmJson("{\"name\":[\"John\"]}")
        assertTrue(obj.isMap())
        assertFalse(obj.name.isMap())

        obj = dpmJson(null)
        assertTrue(obj.isMap())

        obj = dpmJson("{}")
        assertTrue(obj.isMap())

        obj = dpmJson("[]")
        assertFalse(obj.isMap())
    }

    @Test
    void testExistsOperator() {
        TestDto testObject = initTestObject()
        def obj = dpmJson(testObject)
        assertTrue(obj.exists())
        assertTrue(obj.tColl.exists())
        assertTrue(obj.tColl[0].exists())
        assertTrue(obj.tMap.exists())
        assertTrue(obj.tMap.test1.exists())
        assertTrue(obj.tString.exists())
        assertTrue(obj.tLong.exists())

        assertFalse(obj.test.tColl.exists())
        assertFalse(obj.tMap.test4.exists())
        assertFalse(obj.tColl[0].test.exists())

        obj = dpmJson(["test1", "test2", "test3"])
        assertTrue(obj.exists())
        assertTrue(obj[0].exists())
        assertFalse(obj[0].test.exists())
    }

    @Test
    void testIsListOperator() {
        TestDto testObject = initTestObject()
        def obj = dpmJson(testObject)
        assertTrue(obj.tColl.isList())
        assertFalse(obj.tMap.isList())
        assertFalse(obj.tString.isList())
        assertFalse(obj.tLong.isList())
        assertFalse(obj.test.tColl.isList())

        obj = dpmJson(["test1", "test2", "test3"])
        assertTrue(obj.isList())

        obj = dpmJson("{\"name\":[\"John\"]}")
        assertFalse(obj.isList())
        assertTrue(obj.name.isList())

        obj = dpmJson(null)
        assertFalse(obj.isList())

        obj = dpmJson("{}")
        assertFalse(obj.isList())

        obj = dpmJson("[]")
        assertTrue(obj.isList())
    }

    @Test
    void testMapSizeOperator() {
        TestDto testObject = initTestObject()
        def obj = dpmJson(testObject)
        assertEquals(6, obj.size())
        assertEquals(2, obj.tMap.size())
        assertEquals(0, obj.test.tMap.size())

        obj.tMap["test1"] = "testValue"
        assertEquals(2, obj.tMap.size())

        obj.tMap["test"] = "testValue"
        assertEquals(3, obj.tMap.size())

        obj = dpmJson("{}")
        assertEquals(0, obj.size())
        assertEquals(0, obj.test.size())

        obj = dpmJson("{\"name\":[\"John\"]}")
        assertEquals(1, obj.size())
        assertEquals(1, obj.name.size())
        assertEquals(0, obj.test.name.size())
    }

    @Test
    void testArraySizeOperator() {
        TestDto testObject = initTestObject()
        def obj = dpmJson(testObject)
        assertEquals(3, obj.tColl.size())

        obj.tColl[0] = "testValue"
        assertEquals(3, obj.tColl.size())

        obj.tColl.push("testValue2")
        assertEquals(4, obj.tColl.size())

        obj = dpmJson("[]")
        assertEquals(0, obj.size())
    }

    @Test
    void testMapClearOperator() {
        TestDto testObject = initTestObject()
        def obj = dpmJson(testObject)
        assertEquals(2, obj.tMap.size())

        obj.tMap.clear()
        assertEquals(0, obj.tMap.size())
    }

    @Test
    void testArrayClearOperator() {
        TestDto testObject = initTestObject()
        def obj = dpmJson(testObject)
        assertEquals(3, obj.tColl.size())

        obj.tColl.clear()
        assertEquals(0, obj.tColl.size())
    }

    @Test
    void testMapIterator() {
        def obj = dpmJson(["test": ["v1": 1, "v2": 2, "v3": 3]])
        def sum = 0
        obj.test.each { sum += it.value.value() }
        assertEquals(6, sum)

        sum = 0
        def indexSum = 0
        def keConcat = ""
        obj.test.eachWithIndex { entry, index -> keConcat += entry.key; sum += entry.value.value(); indexSum += index }
        assertEquals(6, sum)
        assertEquals(3, indexSum)
        assertEquals("v1v2v3", keConcat)
    }

    @Test
    void testMapIteratorInFor() {
        def obj = dpmJson(["test": [["val": 1], ["val": 2, "val1": 2], ["val": 3]]])

        def sum = 0
        for (item in obj.test) {
            sum += item.val.value()
            if (item.val1.exists()) {
                sum += item.val1.value()
            }
        }
        assertEquals(8, sum)
    }

    @Test
    void testArrayIterator() {
        def obj = dpmJson(["test": ["arr": [1, 2, 3]]])
        def sum = 0
        obj.test.arr.each { sum += it.value() }
        assertEquals(6, sum)

        sum = 0
        def indexSum = 0
        obj.test.arr.eachWithIndex { value, index -> sum += value.value(); indexSum += index }
        assertEquals(6, sum)
        assertEquals(3, indexSum)

        assertEquals(6, obj.test.arr.sum())
    }

    @Test
    void testArrayIteratorInFor() {
        def obj = dpmJson(["test": ["arr": [1, 2, 3]]])

        def sum = 0
        for (item in obj.test.arr) {
            sum += item.value()
        }
        assertEquals(6, sum)
    }

    @Test
    void testArrayIteratorWithComplexValues() {
        def arr = [["v": 1], ["v": 2], ["v": 3]]
        def obj = dpmJson(["arr": arr])
        def sum = 0
        obj.arr.each { sum += it.v.value() }
        assertEquals(6, sum)
    }

    @Test
    void testArrayIteratorWithComplexValuesInFor() {
        def arr = [["v": 1], ["v": 2], ["v": 3]]
        def obj = dpmJson(["arr": arr])

        def sum = 0
        for (item in obj.arr) {
            sum += item.v.value()
        }
        assertEquals(6, sum)
    }

    @Test
    void testRootArrayIterator() {
        def obj = dpmJson([1, 2, 3])
        def sum = 0
        obj.each { sum += it.value() }
        assertEquals(6, sum)

        sum = 0
        def indexSum = 0
        obj.eachWithIndex { value, index -> sum += value.value(); indexSum += index }
        assertEquals(6, sum)
        assertEquals(3, indexSum)

        assertEquals(6, obj.sum())
    }

    @Test
    void testUnique() {
        def obj = dpmJson([1, 2, 2, 3, 1, 3])
        def result = obj.unique()
        assertEquals([1, 2, 3], result.value())

        obj = dpmJson([1, 3, 2, 3])
        result = obj.unique(true)
        assertEquals([1, 3, 2], result.value())
        assertEquals([1, 3, 2], obj.value())

        obj = dpmJson([1, 3, 2, 3])
        result = obj.unique(false)
        assertEquals([1, 3, 2], result.value())
        assertEquals([1, 3, 2, 3], obj.value())

        obj = dpmJson([1, 3, 4, 5])
        result = obj.unique { it.value() % 2 }
        assertEquals([1, 4], result.value())

        obj = dpmJson([2, 3, 3, 4])
        result = obj.unique { a, b -> a <=> b }
        assertEquals([2, 3, 4], result.value())
    }

    @Test
    void testAddAll() {
        def obj = dpmJson([1, 2])
        def result = obj.addAll([3, 4])
        assertTrue(result)
        assertEquals([1, 2, 3, 4], obj.value())

        obj = dpmJson(["1", "2"])
        result = obj.addAll(["3", "4"])
        assertTrue(result)
        assertEquals(["1", "2", "3", "4"], obj.value())

        obj = dpmJson([1, 2])
        result = obj.addAll(1, [3, 4])
        assertTrue(result)
        assertEquals([1, 3, 4, 2], obj.value())

        obj = dpmJson(["1", "2"])
        result = obj.addAll(1, ["3", "4"])
        assertTrue(result)
        assertEquals(["1", "3", "4", "2"], obj.value())

        def arr = new String[2]
        arr[0] = "3"
        arr[1] = "4"

        obj = dpmJson(["1", "2"])
        result = obj.addAll(arr)
        assertTrue(result)
        assertEquals(["1", "2", "3", "4"], obj.value())

        obj = dpmJson(["1", "2"])
        result = obj.addAll(1, arr)
        assertTrue(result)
        assertEquals(["1", "3", "4", "2"], obj.value())

        obj = dpmJson([1, 2])
        result = obj.addAll(null)
        assertFalse(result)
        assertEquals([1, 2], obj.value())

        obj = dpmJson([1, 2])
        result = obj.addAll([])
        assertFalse(result)
        assertEquals([1, 2], obj.value())
    }

    @Test
    //TODO add more tests
    void testFindAll() {
        def obj = [
                Max: [
                        lastName: "Muster",
                        id: 34577,
                        hasPets: false,
                        carBrand: "VW"
                ],
                Anna: [
                        lastName: "Beispiel",
                        id: 12356,
                        hasPets: true
                ],
                Henry: [
                        lastName: "West",
                        id: 99348,
                        hasPets: false,
                        carBrand: "Ferrari"
                ],
                Farid: [
                        lastName: "Al-Shami",
                        id: 16528,
                        hasPets: false,
                        carBrand: "Mercedes"
                ],
                Chiara: [
                        lastName: "Bernasconi",
                        id: 93678,
                        hasPets: true,
                        carBrand: "Audi"
                ],
                Celine: [
                        lastName: "Foster",
                        id: 26735,
                        hasPets: true,
                        carBrand: "Jaguar"
                ]
        ]
        def people = dpmJson(obj)
        people = people.findAll { it.value.carBrand.exists() }
        assertEquals(5, people.size())
    }

    private String mapToJsonString(Map map) {
        if (map == null || map.size() == 0) {
            return ""
        }
        return "{" + map.collect { "\"" + it.key + "\":" + "\"" + it.value + "\"" }.join(",") + "}"
    }

    private String collectionToJsonString(Collection coll) {
        if (coll == null || coll.size() == 0) {
            return ""
        }
        return "[" + coll.collect { "\"" + it + "\"" }.join(",") + "]"
    }

    private TestDto initTestObject() {
        def testObject = new TestDto();
        testObject.tString = "tStringValue"
        testObject.tInteger = 501
        testObject.tLong = 502l
        testObject.tDouble = 503.03
        testObject.tColl = ["test1", "test2", "test3"]
        testObject.tMap = ["test1": "testValue1", "test2": "testValue2"]
        return testObject
    }

    private class TestDto {
        private String tString
        private Integer tInteger
        private Long tLong
        private Double tDouble
        private Collection<String> tColl
        private Map<String, Object> tMap

        String gettString() {
            return tString
        }

        void settString(String tString) {
            this.tString = tString
        }

        Long gettLong() {
            return tLong
        }

        void settLong(Long tLong) {
            this.tLong = tLong
        }

        Integer gettInteger() {
            return tInteger
        }

        void settInteger(Integer tInteger) {
            this.tInteger = tInteger
        }

        Double gettDouble() {
            return tDouble
        }

        void settDouble(Double tDouble) {
            this.tDouble = tDouble
        }

        Collection<String> gettColl() {
            return tColl
        }

        void settColl(Collection<String> tColl) {
            this.tColl = tColl
        }

        Map<String, Object> gettMap() {
            return tMap
        }

        void settMap(Map<String, Object> tMap) {
            this.tMap = tMap
        }


        @Override
        String toString() {
            return "{" +
                    "\"tString\":\"" + tString + "\"," +
                    "\"tInteger\":" + tInteger + "," +
                    "\"tLong\":" + tLong + "," +
                    "\"tDouble\":" + tDouble + "," +
                    "\"tColl\":" + SpinJsonNodeProxyTest.this.collectionToJsonString(tColl) + "," +
                    "\"tMap\":" + SpinJsonNodeProxyTest.this.mapToJsonString(tMap) +
                    "}";
        }
    }
}
