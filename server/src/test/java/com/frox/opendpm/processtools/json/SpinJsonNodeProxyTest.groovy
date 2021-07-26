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
        assertEquals("", obj.toString())
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

    @Test
    void testReadNotValidJson() {
        assertEquals("{{}}", dpmJson("{{}}").toString())
    }

    @Test
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

        obj = dpmJson([1, 3, 4, 5])
        result = obj.unique(true) { it.value() % 2 }
        assertEquals([1, 4], result.value())
        assertEquals([1, 4], obj.value())

        obj = dpmJson([1, 3, 4, 5])
        result = obj.unique(false) { it.value() % 2 }
        assertEquals([1, 4], result.value())
        assertEquals([1, 3, 4, 5], obj.value())

        obj = dpmJson([2, 3, 3, 4])
        result = obj.unique({ a, b -> a <=> b } as Comparator)
        assertEquals([2, 3, 4], result.value())

        obj = dpmJson([2, 3, 3, 4])
        result = obj.unique(true, ({ a, b -> a <=> b } as Comparator))
        assertEquals([2, 3, 4], result.value())
        assertEquals([2, 3, 4], obj.value())

        obj = dpmJson([2, 3, 3, 4])
        result = obj.unique(false, ({ a, b -> a <=> b } as Comparator))
        assertEquals([2, 3, 4], result.value())
        assertEquals([2, 3, 3, 4], obj.value())
    }

    @Test(expected = UnsupportedOperationException.class)
    void testUniqueNotArrayType() {
        def obj = dpmJson(["test": 1])
        obj.unique()
    }

    @Test(expected = UnsupportedOperationException.class)
    void testUniqueNull() {
        def obj = dpmJson(null)
        obj.unique()
    }

    @Test
    void testToUnique() {
        def obj = dpmJson([1, 2, 2, 3, 1, 3])
        def result = obj.toUnique()
        assertEquals([1, 2, 3], result.value())

        obj = dpmJson([1, 3, 4, 5])
        result = obj.toUnique { it.value() % 2 == 0 }
        assertEquals([1, 4], result.value())

        obj = dpmJson([2, 3, 3, 4])
        result = obj.toUnique({ a, b -> a <=> b } as Comparator)
        assertEquals([2, 3, 4], result.value())

    }

    @Test(expected = UnsupportedOperationException.class)
    void testToUniqueNotArrayType() {
        def obj = dpmJson(["test": 1])
        obj.toUnique()
    }

    @Test(expected = UnsupportedOperationException.class)
    void testToUniqueNull() {
        def obj = dpmJson(null)
        obj.toUnique()
    }

    @Test
    void testReverseEach() {
        def obj = dpmJson([1, 2, 3])
        def result = []
        obj.reverseEach { result << it.value() }
        assertEquals([3, 2, 1], result)

        obj = dpmJson([[v: 1], [v: 2], [v: 3]])
        result = []
        obj.reverseEach { result << it.v.value() }
        assertEquals([3, 2, 1], result)

        obj = dpmJson(TEST_PEOPLE_MAP)
        result = []
        obj.reverseEach { result << it }
        assertEquals(6, result.size())
        assertEquals("Celine", result.first().key)
        assertEquals("Max", result.last().key)
    }

    @Test
    void testGrep() {
        def obj = dpmJson([1, 2, 3])
        def result = obj.grep(5)
        assertTrue(result.isEmpty())

        obj = dpmJson([1, 2, 3])
        result = obj.grep(2..3)
        assertEquals([2, 3], result.value())

        obj = dpmJson(['a', 'b', 'c', 'd'])
        result = obj.grep('a'..'c')
        assertEquals(['a', 'b', 'c'], result.value())

        obj = dpmJson([1, 2, 3, 3])
        result = obj.grep(3)
        assertEquals([3, 3], result.value())

        obj = dpmJson([1, 2, 3])
        result = obj.grep { it % 2 == 0 }
        assertEquals([2], result.value())
    }

    @Test
    void testToList() {
        def obj = dpmJson([1, 2, 3])
        def result = obj.toList().collect { it.value() }
        assertEquals([1, 2, 3], result)

        obj = dpmJson(["test1", "test2", "test3"])
        result = obj.toList().collect { it.value() }
        assertEquals(["test1", "test2", "test3"], result)

        obj = dpmJson([test: [arr: [1, 2, 3]]])
        result = obj.test.arr.toList().collect { it.value() }
        assertEquals([1, 2, 3], result)
    }


    @Test(expected = UnsupportedOperationException.class)
    void testToListNotArrayType() {
        def obj = dpmJson(["test": 1])
        obj.toList()
    }

    @Test(expected = UnsupportedOperationException.class)
    void testToListNull() {
        def obj = dpmJson(null)
        obj.toList()
    }

    @Test
    void testCollate() {
        def obj = dpmJson([1, 2, 3, 4])
        def result = obj.collate(3)
        assertEquals([[1, 2, 3], [4]].toString(), result.toList().toString())

        obj = dpmJson([1, 2, 3, 4])
        result = obj.collate(3, true)
        assertEquals([[1, 2, 3], [4]].toString(), result.toList().toString())

        obj = dpmJson([1, 2, 3, 4])
        result = obj.collate(3, false)
        assertEquals([[1, 2, 3]].toString(), result.toList().toString())

        obj = dpmJson([1, 2, 3, 4])
        result = obj.collate(3, 1)
        assertEquals([[1, 2, 3], [2, 3, 4], [3, 4], [4]].toString(), result.toList().toString())

        obj = dpmJson([1, 2, 3, 4])
        result = obj.collate(3, 1, true)
        assertEquals([[1, 2, 3], [2, 3, 4], [3, 4], [4]].toString(), result.toList().toString())

        obj = dpmJson([1, 2, 3, 4])
        result = obj.collate(3, 1, false)
        assertEquals([[1, 2, 3], [2, 3, 4]].toString(), result.toList().toString())
    }


    @Test(expected = UnsupportedOperationException.class)
    void testCollateNotArrayType() {
        def obj = dpmJson(["test": 1])
        obj.collate(2)
    }

    @Test(expected = UnsupportedOperationException.class)
    void testCollateNull() {
        def obj = dpmJson(null)
        obj.collate(3)
    }

    @Test
    void testCollect() {
        def obj = dpmJson([1, 2, 3])
        def result = obj.collect()
        assertEquals([1, 2, 3].toString(), result.toString())

        obj = dpmJson([1, 2, 3])
        result = obj.collect { it.value() + 1 }
        assertEquals([2, 3, 4], result.value())

        obj = dpmJson([1, 2, 3])
        result = obj.collect(new HashSet()) { it.value() + 1 }
        assertEquals([2, 3, 4] as HashSet, result.value() as HashSet)
    }

    /*@Test
    void testCollectNested() {
        def obj = dpmJson([1, [2, 3], [[4]]])
        def result = obj.collectNested { it.value() + 1 }
        assertEquals([2, [3, 4], [[5]]], result.value())

        obj = dpmJson([1, [2, 3], 4])
        result = obj.collectNested(new HashSet()) { it.value() + 1 }
        assertEquals(([2, [3, 4], 5] as HashSet), result.value() as HashSet)
    }*/

    @Test(expected = UnsupportedOperationException.class)
    void testCollectNestedNotArrayType() {
        def obj = dpmJson(["test": 1])
        obj.collectNested { it }
    }

    @Test(expected = UnsupportedOperationException.class)
    void testCollectNestedNull() {
        def obj = dpmJson(null)
        obj.collectNested { it }
    }

    @Test
    void testCollectMany() {
        def obj = dpmJson([1, 2])
        def result = obj.collectMany { [it.value(), it.value() + 1] }
        assertEquals([1, 2, 2, 3], result.value())

        obj = dpmJson(["a", "b"])
        result = obj.collectMany { [it.value(), it.value() + "z"] }
        assertEquals(["a", "az", "b", "bz"], result.value())

        obj = dpmJson([1, 2])
        result = obj.collectMany(new HashSet()) { [it.value(), it.value() + 1] }
        assertEquals([1, 2, 2, 3] as HashSet, result.value() as HashSet)

        obj = dpmJson([arr: [1, 2], arr1: [2, 3]])
        result = obj.collectMany(new ArrayList()) { [it.value << 3] }
        assertEquals([[1, 2, 3], [2, 3, 3]], result.asList().collect { it.value() })
    }

    /*@Test
    void testCollectEntries() {
        def obj = dpmJson([[1, 1], [2, 2]])
        def result = obj.collectEntries()
        assertEquals([1: 1, 2: 2], result.value())

        obj = dpmJson([1, 2])
        result = obj.collectEntries { index -> [index.value().toString(), index.value()] }
        assertEquals(["1": 1, "2": 2], result.value())

        obj = dpmJson([1, 2])
        result = obj.collectEntries(new HashMap()) { index -> [index.value().toString(), index.value()] }
        assertEquals(["1": 1, "2": 2], result.value())

        obj = dpmJson([[1, 1], [2, 2]])
        result = obj.collectEntries(new HashMap())
        assertEquals([1: 1, 2: 2], result.value())
    }*/

    @Test(expected = UnsupportedOperationException.class)
    void testCollectEntriesNotArrayType() {
        def obj = dpmJson(["test": 1])
        obj.collectEntries()
    }

    @Test(expected = UnsupportedOperationException.class)
    void testCollectEntriesNull() {
        def obj = dpmJson(null)
        obj.collectEntries()
    }

    @Test
    void testFindResult() {
        def obj = dpmJson([1, 2, 3])
        def result = obj.findResults { it }
        assertEquals([1, 2, 3].toString(), result.toString())

        obj = dpmJson([1, 2, 3])
        result = obj.findResults { it.value() + 1 }
        assertEquals([2, 3, 4], result.value())

        obj = dpmJson([test1: 1, test2: 2])
        result = obj.findResults { it.value.value() + 1 }
        assertEquals([2, 3], result.value())

        obj = dpmJson(arr: [1, 2])
        result = obj.arr.findResults { it.value() + 1 }
        assertEquals([2, 3], result.value())
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
    void testRemoveAll() {
        def obj = dpmJson([1, 2, 3])
        def result = obj.removeAll([1, 2, 3])
        assertTrue(result)
        assertEquals(0, obj.size())

        obj = dpmJson(["1", "2", "3"])
        result = obj.removeAll(["1", "2", "3"])
        assertTrue(result)
        assertEquals(0, obj.size())

        obj = dpmJson([1, 2, 3, 3, 4])
        result = obj.removeAll([2, 3])
        assertTrue(result)
        assertEquals([1, 4], obj.value())

        obj = dpmJson(["1", "2", "3", "3", "4"])
        result = obj.removeAll(["2", "3"])
        assertTrue(result)
        assertEquals(["1", "4"], obj.value())

        obj = dpmJson([1, 2, 3, 3, 4])
        result = obj.removeAll([2, 3, 3])
        assertTrue(result)
        assertEquals([1, 4], obj.value())

        obj = dpmJson([1, 2])
        result = obj.removeAll(null)
        assertFalse(result)
        assertEquals(2, obj.size())

        obj = dpmJson("test": ["arr": [1, 2, 3]])
        result = obj.test.arr.removeAll([1, 2])
        assertTrue(result)
        assertEquals([3], obj.test.arr.value())

        obj = dpmJson(["1", "2", "2"])
        def arr = new String[2]
        arr[0] = "2"
        arr[1] = "2"
        result = obj.removeAll(arr)
        assertTrue(result)
        assertEquals(["1"], obj.value())

        obj = dpmJson([1, 2, 3])
        result = obj.removeAll { it.value() > 2 }
        assertTrue(result)
        assertEquals([1, 2], obj.value())

        obj = dpmJson(["1", "2", "2", "3"])
        result = obj.removeAll { it.value().equals("2") }
        assertTrue(result)
        assertEquals(["1", "3"], obj.value())
    }

    @Test(expected = UnsupportedOperationException.class)
    void testRemoveAllNotArrayType() {
        def obj = dpmJson(["test": 1])
        obj.removeAll([1, 2])
    }

    @Test(expected = UnsupportedOperationException.class)
    void testRemoveAllNull() {
        def obj = dpmJson(null)
        obj.removeAll([1, 2])
    }

    @Test
    void testRetainAll() {
        def obj = dpmJson([1, 2, 3])
        def result = obj.retainAll([1, 2, 3])
        assertFalse(result)
        assertEquals(3, obj.size())

        obj = dpmJson(["1", "2", "3"])
        result = obj.retainAll(["1", "2", "3"])
        assertFalse(result)
        assertEquals(3, obj.size())

        obj = dpmJson([1, 2, 3, 3, 4])
        result = obj.retainAll([2, 3])
        assertTrue(result)
        assertEquals([2, 3, 3], obj.value())

        obj = dpmJson(["1", "2", "3", "3", "4"])
        result = obj.retainAll(["2", "3"])
        assertTrue(result)
        assertEquals(["2", "3", "3"], obj.value())

        obj = dpmJson([1, 2])
        result = obj.removeAll(null)
        assertFalse(result)
        assertEquals(2, obj.size())

        obj = dpmJson("test": ["arr": [1, 2, 3]])
        result = obj.test.arr.retainAll([1, 2])
        assertTrue(result)
        assertEquals([1, 2], obj.test.arr.value())

        obj = dpmJson(["1", "2", "2"])
        def arr = new String[2]
        arr[0] = "2"
        arr[1] = "2"
        result = obj.retainAll(arr)
        assertTrue(result)
        assertEquals(["2", "2"], obj.value())

        obj = dpmJson([1, 2, 3])
        result = obj.retainAll { it.value() > 2 }
        assertTrue(result)
        assertEquals([3], obj.value())

        obj = dpmJson(["1", "2", "2", "3"])
        result = obj.retainAll { it.value().equals("2") }
        assertTrue(result)
        assertEquals(["2", "2"], obj.value())
    }

    @Test(expected = UnsupportedOperationException.class)
    void testRetainAllNotArrayType() {
        def obj = dpmJson(["test": 1])
        obj.retainAll([1, 2])
    }

    @Test
    void testFindAll() {
        def people = dpmJson(TEST_PEOPLE_MAP)
        def result = people.findAll { it.value.carBrand.exists() }
        assertEquals(5, result.size())

        people = dpmJson(TEST_PEOPLE_MAP)
        result = people.findAll { !it.value.carBrand.exists() }
        assertEquals(1, result.size())

        people = dpmJson(TEST_PEOPLE_MAP)
        result = people.findAll { it.value.value1.exists() }
        assertEquals(0, result.size())

        people = dpmJson(TEST_PEOPLE_MAP)
        result = people.findAll { it.value.lastName.equals("Muster") }
        assertEquals(1, result.size())

        people = dpmJson(TEST_PEOPLE_MAP)
        result = people.findAll { it.value.hasPets.equals(true) }
        assertEquals(3, result.size())

        people = dpmJson(TEST_PEOPLE_MAP)
        result = people.findAll()
        assertEquals(6, result.size())
    }

    @Test
    void testSplitArray() {
        def range = dpmJson([1, 2, 3, 4, 5, 6, 7, 8, 9, 10])
        def (odd, even) = range.split { it.value() % 2 }
        assertEquals([2, 4, 6, 8, 10], even.value())
        assertEquals([1, 3, 5, 7, 9], odd.value())
    }

    @Test
    void testSplitMap() {
        def people = dpmJson(TEST_PEOPLE_MAP)
        def (peopleWithCarBrand, peopleWithoutCarBrand) = people.split { it.value.carBrand.exists() }
        assertEquals(5, peopleWithCarBrand.size())
        assertFalse(peopleWithCarBrand.first().carBrand.exists())
        assertEquals(1, peopleWithoutCarBrand.size())
        assertFalse(peopleWithoutCarBrand.first().carBrand.exists())
    }

    @Test
    void testCombinations() {
        def obj = dpmJson([[1, 2], [3, 4]])
        def result = obj.combinations()
        assertEquals(4, result.size())
        assertEquals([1, 3], result[0].value())
        assertEquals([2, 3], result[1].value())
        assertEquals([1, 4], result[2].value())
        assertEquals([2, 4], result[3].value())

        obj = dpmJson("test": ["arr": [[1, 2], [3, 4]]])
        result = obj.test.arr.combinations()
        assertEquals(4, result.size())
        assertEquals([1, 3], result[0].value())
        assertEquals([2, 3], result[1].value())
        assertEquals([1, 4], result[2].value())
        assertEquals([2, 4], result[3].value())

        obj = dpmJson([[1, 2], [3, 4]])
        result = obj.combinations { x, y -> x + y }
        assertEquals(4, result.size())
        assertEquals([4, 5, 5, 6], result.value())

        obj = dpmJson([["a", "b"], ["c", "d"]])
        result = obj.combinations()
        assertTrue(result.contains(["b", "c"]))
        assertEquals(4, result.size())
        assertEquals(["a", "c"], result[0].value())
        assertEquals(["b", "c"], result[1].value())
        assertEquals(["a", "d"], result[2].value())
        assertEquals(["b", "d"], result[3].value())

        obj = dpmJson([["a", "b"], [1, 2, 3]])
        result = obj.combinations()
        assertEquals(6, result.size())
        assertEquals(["a", 1], result[0].value())
        assertEquals(["b", 1], result[1].value())
        assertEquals(["a", 2], result[2].value())
        assertEquals(["b", 2], result[3].value())
        assertEquals(["a", 3], result[4].value())
        assertEquals(["b", 3], result[5].value())
    }

    @Test(expected = UnsupportedOperationException.class)
    void testCombinationNotArrayType() {
        def obj = dpmJson(["test": 1])
        obj.combinations()
    }

    @Test(expected = UnsupportedOperationException.class)
    void testCombinationNull() {
        def obj = dpmJson(null)
        obj.combinations()
    }

    @Test
    void testSubsequences() {
        def obj = dpmJson([1, 2])
        def result = obj.subsequences()
        assertEquals(3, result.size())

        obj = dpmJson("test": ["arr": [1, 2]])
        result = obj.test.arr.subsequences()
        assertEquals(3, result.size())
    }

    @Test(expected = UnsupportedOperationException.class)
    void testSubsequencesNotArrayType() {
        def obj = dpmJson(["test": 1])
        obj.subsequences()
    }

    @Test(expected = UnsupportedOperationException.class)
    void testSubsequencesNull() {
        def obj = dpmJson(null)
        obj.subsequences()
    }

    @Test
    void testPermutation() {
        def obj = dpmJson([1, 2])
        def result = obj.permutations()
        assertEquals(2, result.value().size())

        obj = dpmJson([1, 2])
        result = obj.permutations { it.collect { it.value() + 1 } }
        assertTrue(result.contains([2, 3]))

        obj = dpmJson([test: [arr: [1, 2]]])
        result = obj.test.arr.permutations { it.collect { it.value() + 1 } }
        assertTrue(result.contains([2, 3]))
    }

    @Test(expected = UnsupportedOperationException.class)
    void testPermutationNotArrayType() {
        def obj = dpmJson(["test": 1])
        obj.permutations()
    }

    @Test(expected = UnsupportedOperationException.class)
    void testPermutationNull() {
        def obj = dpmJson(null)
        obj.permutations()
    }

    @Test
    void testEachPermutation() {
        def obj = dpmJson([1, 2])
        def permutations = []
        def result = obj.eachPermutation { permutations << it.collect { it.value() } }
        assertEquals(2, permutations.size())
        assertTrue(permutations.contains([2, 1]))

        obj = dpmJson([test: [arr: [1, 2]]])
        permutations = []
        result = obj.test.arr.eachPermutation { permutations << it.collect { it.value() } }
        assertEquals(2, permutations.size())
        assertTrue(permutations.contains([2, 1]))
    }

    @Test(expected = UnsupportedOperationException.class)
    void testEachPermutationNotArrayType() {
        def obj = dpmJson(["test": 1])
        obj.eachPermutation { true }
    }

    @Test(expected = UnsupportedOperationException.class)
    void testEachPermutationNull() {
        def obj = dpmJson(null)
        obj.eachPermutation { true }
    }

    @Test
    void testTranspose() {
        def obj = dpmJson([[1, 2], [3, 4]])
        def result = obj.transpose()
        assertTrue(result.contains([1, 3]))

        obj = dpmJson([[1, 2, 3], [4, 5]])
        result = obj.transpose()
        assertTrue(result.contains([1, 4]))

    }

    @Test(expected = UnsupportedOperationException.class)
    void testTransposeNotArrayType() {
        def obj = dpmJson(["test": 1])
        obj.transpose()
    }

    @Test(expected = UnsupportedOperationException.class)
    void testTransposeNull() {
        def obj = dpmJson(null)
        obj.transpose()
    }

    @Test
    void testGroupBy() {
        def obj = dpmJson([1, 2, 3])
        def result = obj.groupBy { it.value() % 2 == 0 }
        assertEquals([1, 3], result[false].value())

        obj = dpmJson([1, 2, 3, 4, 5, 6])
        result = obj.groupBy { it.value() % 2 }
        assertEquals([2, 4, 6], result[0].value())
        assertEquals([1, 3, 5], result[1].value())

        obj = dpmJson(TEST_PEOPLE_MAP)
        result = obj.groupBy { it.value.lastName.value() }
        assertEquals(TEST_PEOPLE_MAP.Max.lastName, result["Muster"].Max.lastName.value())

        obj = dpmJson([1, 2, 3, 4])
        result = obj.groupBy { it.value() % 2 == 0 } { it.value() > 3 }
        assertEquals([1, 3], result["false"]["false"].value())
        assertEquals([2], result["true"]["false"].value())
        assertEquals([4], result["true"]["true"].value())
        obj = dpmJson([1, 2, 3, 4])

        result = obj.groupBy(([{ it.value() % 2 == 0 }, { it.value() > 3 }].toList()))
        assertEquals([1, 3], result["false"]["false"].value())
        assertEquals([2], result["true"]["false"].value())
        assertEquals([4], result["true"]["true"].value())

        obj = dpmJson(TEST_PEOPLE_MAP)
        result = obj.groupBy(
                { it.value.hasPets.value() },
                { it.value.carBrand.exists() },
                { it.key.charAt(0) }
        )
        assertEquals(2, result["true"]["true"]["C"].size())
        assertEquals(3, result["false"]["true"].size())
        assertEquals(['F', 'H', 'M'], result["false"]["true"].keySet().sort())
    }

    @Test
    void testInject() {
        def obj = dpmJson([1, 2, 3, 4])
        def result = obj.inject(1) { acc, val -> acc * val }
        assertEquals(24, result.value())

        obj = dpmJson([1, 2, 3, 4])
        result = obj.inject { acc, val -> acc * val }
        assertEquals(24, result.value())

        obj = dpmJson([1, 2, 3, 4])
        result = obj.inject(0) { acc, val -> acc + val }
        assertEquals(10, result.value())

        obj = dpmJson([1, 2, 3, 4])
        result = obj.inject { acc, val -> acc + val }
        assertEquals(10, result.value())

        obj = dpmJson(["bat", "rat", "cat"])
        result = obj.inject("aaa") { a, b -> [a, b].max() }
        assertEquals("rat", result.value())

        obj = dpmJson(["rat", "bat", "cat"])
        result = obj.inject("zzz") { min, next -> next < min ? next : min }
        assertEquals("bat", result.value())

        obj = dpmJson([1, 2, 3])
        result = obj.inject(4) { initial, val -> initial + val }
        assertEquals(10, result.value())

        obj = dpmJson([a: 1, b: 2, c: 3])
        result = obj.inject([]) { list, k, v -> list + [k] * v.value() }
        assertEquals(['a', 'b', 'b', 'c', 'c', 'c'], result.value())

        obj = dpmJson([hello: "Hello", world: "World!"])
        result = obj.inject("") { initial, key, val -> initial + val }
        assertEquals("HelloWorld!", result.value())
    }

    @Test
    void testCountBy() {
        def obj = dpmJson(["Alex", "Max", "Andrey"])
        obj = obj.countBy { it.value().charAt(0) }
        assertEquals(2, obj["A"].value())
        assertEquals(1, obj["M"].value())

        obj = dpmJson([1, 2, 1, 1])
        obj = obj.countBy { it.value() }
        assertEquals(3, obj["1"].value())
        assertEquals(1, obj["2"].value())

        obj = dpmJson(TEST_PEOPLE_MAP)
        obj = obj.countBy { it.value.carBrand.exists() }
        assertEquals(5, obj["true"].value())
        assertEquals(1, obj["false"].value())

        obj = dpmJson(null)
        obj.countBy { null }
        assertEquals("{}", obj.value().toString())
    }

    @Test
    void testGroupEntriesBy() {
        def obj = dpmJson([test1: "1", test2: "2", id: 34241])
        def result = obj.groupEntriesBy {
            key, value ->
                if (key.charAt(0).equals('t' as Character)) {
                    return "test"
                } else {
                    return "notTest"
                }
        }
        assertEquals(2, result.size())
        assertEquals(2, result["test"].size())
        assertEquals(1, result["notTest"].size())

        obj = dpmJson(TEST_PEOPLE_MAP)
        result = obj.groupEntriesBy {
            key, value ->
                if (value.carBrand.exists()) {
                    return "withCarBrand"
                } else {
                    return "withoutCarBrand"
                }
        }
        assertEquals(2, result.size())
        assertEquals(1, result["withoutCarBrand"].size())
        assertEquals(5, result["withCarBrand"].size())
    }

    @Test(expected = UnsupportedOperationException.class)
    void testGroupEntriesByNotMapType() {
        def obj = dpmJson([1, 2])
        obj.groupEntriesBy { true }
    }

    @Test
    void testSum() {
        def obj = dpmJson([])
        def result = obj.sum()
        assertNull(result)

        obj = dpmJson([1, 2, 3, 4])
        result = obj.sum()
        assertEquals(10, result)

        obj = dpmJson([1, -1, 1])
        result = obj.sum()
        assertEquals(1, result)

        obj = dpmJson(['a', 'b', 'c'])
        result = obj.sum()
        assertEquals("abc", result)

        obj = dpmJson([1, 2, 3])
        result = obj.sum(4)
        assertEquals(10, result)

        obj = dpmJson(['b', 'c'])
        result = obj.sum('a')
        assertEquals("abc", result)

        obj = dpmJson([initTestObject(), initTestObject()])
        result = obj.sum { it.tInteger.value() }
        assertEquals(1002, result)

        obj = dpmJson([initTestObject(), initTestObject()])
        result = obj.sum { it.tString.value() }
        assertEquals("tStringValuetStringValue", result)

        obj = dpmJson([initTestObject(), initTestObject()])
        result = obj.sum(1000) { it.tInteger.value() }
        assertEquals(2002, result)

        obj = dpmJson([initTestObject(), initTestObject()])
        result = obj.sum('a') { it.tString.value() }
        assertEquals("atStringValuetStringValue", result)
    }

    @Test(expected = UnsupportedOperationException.class)
    void testSumNotArrayType() {
        def obj = dpmJson(["test": 1])
        obj.sum()
    }

    @Test(expected = UnsupportedOperationException.class)
    void testSumNull() {
        def obj = dpmJson(null)
        obj.sum()
    }

    @Test
    void testJoin() {
        def obj = dpmJson([])
        def result = obj.join(' ')
        assertEquals("", result)

        obj = dpmJson([1, 2])
        result = obj.join(' ')
        assertEquals("1 2", result)

        obj = dpmJson([[1, 2], [3, 4]])
        result = obj.join(' ')
        assertEquals("[1, 2] [3, 4]", result)

        obj = dpmJson(["test1", "test2"])
        result = obj.join('|')
        assertEquals("test1|test2", result)

        obj = dpmJson([1, 2, 3])
        result = obj.join("-->")
        assertEquals("1-->2-->3", result)
    }

    @Test(expected = UnsupportedOperationException.class)
    void testJoinNotArrayType() {
        def obj = dpmJson(["test": 1])
        obj.join("")
    }

    @Test(expected = UnsupportedOperationException.class)
    void testJoinNull() {
        def obj = dpmJson(null)
        obj.join("")
    }

    @Test
    void testMin() {
        def obj = dpmJson([])
        def result = obj.min()
        assertNull(result)

        obj = dpmJson([1, 2, 2, 1, 3, 4])
        result = obj.min()
        assertEquals(1, result.value())

        obj = dpmJson([-1, -2, -2, -3, -4])
        result = obj.min()
        assertEquals(-4, result.value())

        obj = dpmJson([1.23, 1.22])
        result = obj.min()
        assertEquals(1.22, result.value())

        obj = dpmJson(["a", "b"])
        result = obj.min()
        assertEquals("a", result.value())

        obj = dpmJson(["B", "A"])
        result = obj.min()
        assertEquals("A", result.value())

        obj = dpmJson(["ab", "ac", "aab"])
        result = obj.min()
        assertEquals("aab", result.value())

        def testDto1 = initTestObject()
        testDto1.settInteger(0)
        def testDto2 = initTestObject()
        testDto2.settInteger(1)
        obj = dpmJson([testDto1, testDto2])
        result = obj.min { it.tInteger.value() }
        assertEquals(0, result.tInteger.value())

        obj = dpmJson([2, 1, 3])
        result = obj.min({ a, b -> a <=> b } as Comparator)
        assertEquals(1, result.value())

        testDto1 = initTestObject()
        testDto1.settInteger(0)
        testDto2 = initTestObject()
        testDto2.settInteger(1)
        obj = dpmJson([testDto1, testDto2])
        result = obj.min({ a, b -> a.tInteger.value() <=> b.tInteger.value() } as Comparator)
        assertEquals(0, result.tInteger.value())
    }

    @Test(expected = UnsupportedOperationException.class)
    void testMinNotArrayType() {
        def obj = dpmJson(["test": 1])
        obj.min()
    }

    @Test(expected = UnsupportedOperationException.class)
    void testMinNull() {
        def obj = dpmJson(null)
        obj.min()
    }

    @Test
    void testMax() {
        def obj = dpmJson([])
        def result = obj.max()
        assertNull(result)

        obj = dpmJson([1, 2, 2, 1, 3, 4])
        result = obj.max()
        assertEquals(4, result.value())

        obj = dpmJson([-1, -2, -2, -3, -4])
        result = obj.max()
        assertEquals(-1, result.value())

        obj = dpmJson([1.23, 1.22])
        result = obj.max()
        assertEquals(1.23, result.value())

        obj = dpmJson(["a", "b"])
        result = obj.max()
        assertEquals("b", result.value())

        obj = dpmJson(["B", "A"])
        result = obj.max()
        assertEquals("B", result.value())

        obj = dpmJson(["ab", "ac", "aab"])
        result = obj.max()
        assertEquals("ac", result.value())

        def testDto1 = initTestObject()
        testDto1.settInteger(0)
        def testDto2 = initTestObject()
        testDto2.settInteger(1)
        obj = dpmJson([testDto1, testDto2])
        result = obj.max { it.tInteger.value() }
        assertEquals(1, result.tInteger.value())

        obj = dpmJson([2, 1, 3])
        result = obj.max({ a, b -> a <=> b } as Comparator)
        assertEquals(3, result.value())

        testDto1 = initTestObject()
        testDto1.settInteger(0)
        testDto2 = initTestObject()
        testDto2.settInteger(1)
        obj = dpmJson([testDto1, testDto2])
        result = obj.max({ a, b -> a.tInteger.value() <=> b.tInteger.value() } as Comparator)
        assertEquals(1, result.tInteger.value())
    }

    @Test(expected = UnsupportedOperationException.class)
    void testMaxNotArrayType() {
        def obj = dpmJson(["test": 1])
        obj.min()
    }

    @Test(expected = UnsupportedOperationException.class)
    void testMaxNull() {
        def obj = dpmJson(null)
        obj.min()
    }

    @Test
    void testGetIndices() {
        def obj = dpmJson([])
        def result = obj.getIndices()
        assertEquals(0..<0, result)

        obj = dpmJson([1, 2, 3])
        result = obj.getIndices()
        assertEquals(0..<3, result)

        obj = dpmJson(['a', 'b', 'c'])
        result = obj.getIndices()
        assertEquals(0..<3, result)

        obj = dpmJson([initTestObject(), initTestObject()])
        result = obj.getIndices()
        assertEquals(0..<2, result)

        obj = dpmJson([[1, 2, 3], [1]])
        assertEquals(0..<2, obj.getIndices())
        assertEquals(0..<3, obj[0].getIndices())
        assertEquals(0..<1, obj[1].getIndices())

        obj = dpmJson(test: [arr: [1, 2, 3]])
        result = obj.test.arr.getIndices()
        assertEquals(0..<3, result)
    }

    @Test(expected = UnsupportedOperationException.class)
    void testGetIndicesNotArrayType() {
        def obj = dpmJson(["test": 1])
        obj.getIndices()
    }

    @Test(expected = UnsupportedOperationException.class)
    void testGetIndicesNull() {
        def obj = dpmJson(null)
        obj.getIndices()
    }

    @Test
    void testSubMap() {
        def map = [name: "Max", age: 20, hasPets: true]

        def obj = dpmJson(map)
        def result = obj.subMap(["name", "age"])
        assertEquals(["age", "name"], result.keySet().sort())
        assertEquals("Max", result.name.value())
        assertEquals(20, result.age.value())

        obj = dpmJson(map)
        result = obj.subMap("lastName")
        assertEquals(0, result.size())

        obj = dpmJson([m: map])
        result = obj.m.subMap(["name", "age"])
        assertEquals(["age", "name"], result.keySet().sort())
        assertEquals("Max", result.name.value())
        assertEquals(20, result.age.value())

        obj = dpmJson(map)
        result = obj.subMap("name", "age")
        assertEquals(["age", "name"], result.keySet().sort())
        assertEquals("Max", result.name.value())
        assertEquals(20, result.age.value())
    }

    @Test(expected = UnsupportedOperationException.class)
    void testSubMapNotMapType() {
        def obj = dpmJson([1, 2])
        obj.subMap("1")
    }


    @Test(expected = UnsupportedOperationException.class)
    void testSubMapNull() {
        def obj = dpmJson([1, 2])
        obj.subMap("1")
    }

    @Test
    void testGet() {
        def obj = dpmJson(null)
        def result = obj.get("")
        assertNull(result)

        obj = dpmJson([1, 2, 3])
        result = obj.get(2)
        assertEquals(3, result.value())

        obj = dpmJson(["A", "B", "C"])
        result = obj.get(1)
        assertEquals("B", result.value())

        obj = dpmJson(TEST_PEOPLE_MAP)
        result = obj.get("Max")
        assertEquals("Muster", result.lastName.value())

        obj = dpmJson(TEST_PEOPLE_MAP)
        result = obj.get("Alex")
        assertNull(result)

        obj = dpmJson(TEST_PEOPLE_MAP)
        result = obj.Max.get("lastName")
        assertEquals("Muster", result.value())

        obj = dpmJson(TEST_PEOPLE_MAP)
        result = obj.get("Alex", [lastName: "not found"])
        assertEquals("not found", result.lastName.value())
    }

    @Test(expected = IllegalArgumentException.class)
    void testGetNonIndexType() {
        def obj = dpmJson([1, 2, 3])
        obj.get("test")
    }

//todo Non iterable type get(), problem with comparator
    @Test
    void testSort() {
        def map = [name: "Max", age: 20, hasPets: true]

        def obj = dpmJson([])
        def result = obj.sort()
        assertEquals([], obj.value())
        assertEquals([], result.value())

        obj = dpmJson([3, 2, 1])
        result = obj.sort()
        assertEquals([3, 2, 1], obj.value())
        assertEquals([1, 2, 3], result.value())

        obj = dpmJson(map)
        result = obj.sort()
        assertEquals('{"name":"Max","age":20,"hasPets":true}', obj.toString())
        assertEquals('{"age":20,"hasPets":true,"name":"Max"}', result.toString())

        obj = dpmJson([3, 2, 1])
        result = obj.sort(true)
        assertEquals([1, 2, 3], obj.value())
        assertEquals([1, 2, 3], result.value())

        obj = dpmJson([arr: [3, 2, 1]])
        result = obj.arr.sort(true)
        assertEquals([1, 2, 3], obj.arr.value())
        assertEquals([1, 2, 3], result.value())

        obj = dpmJson([3, 2, 1])
        result = obj.sort(false)
        assertEquals([3, 2, 1], obj.value())
        assertEquals([1, 2, 3], result.value())

        obj = dpmJson([1, 2, 3])
        result = obj.sort { a, b -> b.value() <=> a.value() }
        assertEquals([1, 2, 3], obj.value())
        assertEquals([3, 2, 1], result.value())

        obj = dpmJson([1, 2, 3])
        result = obj.sort(true) { a, b -> b.value() <=> a.value() }
        assertEquals([3, 2, 1], obj.value())
        assertEquals([3, 2, 1], result.value())

//        obj = dpmJson([1, 2, 3])
//        result = obj.sort({ a, b -> b <=> a } as Comparator)
//        assertEquals([1, 2, 3], obj.value())
//        assertEquals([3, 2, 1], result.value())

        obj = dpmJson([1, 2, 3])
        result = obj.sort(true, { a, b -> b <=> a } as Comparator)
        assertEquals([3, 2, 1], obj.value())
        assertEquals([3, 2, 1], result.value())

    }

    @Test(expected = UnsupportedOperationException.class)
    void testSortNotArrayType() {
        def obj = dpmJson(TEST_PEOPLE_MAP)
        obj.sort(true)
    }

    @Test(expected = UnsupportedOperationException.class)
    void testSortNotNull() {
        def obj = dpmJson(null)
        obj.sort(true)
    }

    @Test
    void testToSorted() {
        def map = [name: "Max", age: 20, hasPets: true]

        def obj = dpmJson([])
        def result = obj.toSorted()
        assertEquals([], obj.value())
        assertEquals([], result.value())

        obj = dpmJson([3, 2, 1])
        result = obj.toSorted()
        assertEquals([3, 2, 1], obj.value())
        assertEquals([1, 2, 3], result.value())

//        //todo doesnt work with map(random)
//        obj = dpmJson(map)
//        result = obj.toSorted()
//        assertEquals('{"name":"Max","age":20,"hasPets":true}', obj.toString())
//        assertEquals('{"age":20,"hasPets":true,"name":"Max"}', result.toString())

        obj = dpmJson([1, 2, 3])
        result = obj.toSorted() { a, b -> b.value() <=> a.value() }
        assertEquals([1, 2, 3], obj.value())
        assertEquals([3, 2, 1], result.value())

        obj = dpmJson([1, 2, 3])
        result = obj.toSorted({ a, b -> b.value() <=> a.value() } as Comparator)
        assertEquals([1, 2, 3], obj.value())
        assertEquals([3, 2, 1], result.value())
    }

    @Test
    void testPop() {
        def obj = dpmJson([1, 2, 3])
        def result = obj.pop()
        assertEquals([1, 2], obj.value())
        assertEquals(3, result.value())

        obj = dpmJson(["A", "B", "C"])
        result = obj.pop()
        assertEquals(["A", "B"], obj.value())
        assertEquals("C", result.value())

        obj = dpmJson([[1, 2, 3], [3, 2, 1]])
        result = obj.pop()
        assertEquals([1, 2, 3], obj[0].value())
        assertEquals([3, 2, 1], result.value())
    }

    @Test(expected = UnsupportedOperationException.class)
    void testPopNotArrayType() {
        def obj = dpmJson(TEST_PEOPLE_MAP)
        obj.pop()
    }

    @Test(expected = UnsupportedOperationException.class)
    void testPopNull() {
        def obj = dpmJson(null)
        obj.pop()
    }

    @Test
    void testLast() {
        def obj = dpmJson([1, 2, 3])
        def result = obj.last()
        assertEquals([1, 2, 3], obj.value())
        assertEquals(3, result.value())

        obj = dpmJson(["a", "b", "c"])
        result = obj.last()
        assertEquals(["a", "b", "c"], obj.value())
        assertEquals("c", result.value())

        obj = dpmJson([[1], [2], [1, 2, 3]])
        result = obj.last()
        assertEquals([[1], [2], [1, 2, 3]], obj.asList().collect { it.value() })
        assertEquals([1, 2, 3], result.value())
    }

    @Test(expected = UnsupportedOperationException.class)
    void testLastNotArrayType() {
        def obj = dpmJson(TEST_PEOPLE_MAP)
        obj.last()
    }

    @Test(expected = UnsupportedOperationException.class)
    void testLastNull() {
        def obj = dpmJson(null)
        obj.last()
    }

    @Test
    void testFirst() {
        def obj = dpmJson([1, 2, 3])
        def result = obj.first()
        assertEquals([1, 2, 3], obj.value())
        assertEquals(1, result.value())

        obj = dpmJson(["a", "b", "c"])
        result = obj.first()
        assertEquals(["a", "b", "c"], obj.value())
        assertEquals("a", result.value())

        obj = dpmJson([[1], [2], [1, 2, 3]])
        result = obj.first()
        assertEquals([[1], [2], [1, 2, 3]], obj.asList().collect { it.value() })
        assertEquals([1], result.value())
    }

    @Test(expected = UnsupportedOperationException.class)
    void testFirstNotArrayType() {
        def obj = dpmJson(TEST_PEOPLE_MAP)
        obj.first()
    }

    @Test(expected = UnsupportedOperationException.class)
    void testFirstNull() {
        def obj = dpmJson(null)
        obj.first()
    }

    @Test
    void testHead() {
        def obj = dpmJson([1, 2, 3])
        def result = obj.head()
        assertEquals([1, 2, 3], obj.value())
        assertEquals(1, result.value())

        obj = dpmJson(["a", "b", "c"])
        result = obj.head()
        assertEquals(["a", "b", "c"], obj.value())
        assertEquals("a", result.value())

        obj = dpmJson([[1], [2], [1, 2, 3]])
        result = obj.head()
        assertEquals([[1], [2], [1, 2, 3]], obj.asList().collect { it.value() })
        assertEquals([1], result.value())
    }

    @Test(expected = UnsupportedOperationException.class)
    void testHeadNotArrayType() {
        def obj = dpmJson(TEST_PEOPLE_MAP)
        obj.head()
    }

    @Test(expected = UnsupportedOperationException.class)
    void testHeadNull() {
        def obj = dpmJson(null)
        obj.head()
    }

    @Test
    void testTail() {
        def obj = dpmJson([1, 2, 3])
        def result = obj.tail()
        assertEquals([1, 2, 3], obj.value())
        assertEquals([2, 3], result.value())

        obj = dpmJson(["a", "b", "c"])
        result = obj.tail()
        assertEquals(["a", "b", "c"], obj.value())
        assertEquals(["b", "c"], result.value())

        obj = dpmJson([1])
        result = obj.tail()
        assertEquals([1], obj.value())
        assertEquals([], result.value())

        obj = dpmJson([[1], [2], [1, 2, 3]])
        result = obj.tail()
        assertEquals([[1], [2], [1, 2, 3]], obj.asList().collect { it.value() })
        assertEquals([[2], [1, 2, 3]], result.asList().collect { it.value() })
    }


    @Test(expected = UnsupportedOperationException.class)
    void testTailNotArrayType() {
        def obj = dpmJson(TEST_PEOPLE_MAP)
        obj.tail()
    }

    @Test(expected = UnsupportedOperationException.class)
    void testTailNull() {
        def obj = dpmJson(null)
        obj.tail()
    }

    @Test
    void testInit() {
        def obj = dpmJson([1, 2, 3])
        def result = obj.init()
        assertEquals([1, 2, 3], obj.value())
        assertEquals([1, 2], result.value())

        obj = dpmJson(["a", "b", "c"])
        result = obj.init()
        assertEquals(["a", "b", "c"], obj.value())
        assertEquals(["a", "b"], result.value())

        obj = dpmJson([[1], [2], [1, 2, 3]])
        result = obj.init()
        assertEquals([[1], [2], [1, 2, 3]], obj.asList().collect { it.value() })
        assertEquals([[1], [2]], result.asList().collect { it.value() })
    }


    @Test(expected = UnsupportedOperationException.class)
    void testInitNotArrayType() {
        def obj = dpmJson(TEST_PEOPLE_MAP)
        obj.init()
    }

    @Test(expected = UnsupportedOperationException.class)
    void testInitNull() {
        def obj = dpmJson(null)
        obj.init()
    }

    @Test
    void testTake() {
        def obj = dpmJson(null)
        def result = obj.take(5)
        assertEquals("{}", result.value().toString())

        obj = dpmJson([1, 2, 3])
        result = obj.take(0)
        assertEquals([1, 2, 3], obj.value())
        assertEquals([], result.value())

        obj = dpmJson([1, 2, 3])
        result = obj.take(2)
        assertEquals([1, 2, 3], obj.value())
        assertEquals([1, 2], result.value())

        obj = dpmJson(["a", "b", "c"])
        result = obj.take(5)
        assertEquals(["a", "b", "c"], obj.value())
        assertEquals(["a", "b", "c"], result.value())

        obj = dpmJson([[1], [2], [1, 2, 3]])
        result = obj.take(2)
        assertEquals([[1], [2], [1, 2, 3]], obj.asList().collect { it.value() })
        assertEquals([[1], [2]], result.asList().collect { it.value() })

        obj = dpmJson(TEST_PEOPLE_MAP)
        result = obj.take(2)
        assertEquals(TEST_PEOPLE_MAP.Max.lastName, result.Max.lastName.value())
    }

    @Test
    void testTakeRight() {
        def obj = dpmJson([1, 2, 3])
        def result = obj.takeRight(0)
        assertEquals([1, 2, 3], obj.value())
        assertEquals([], result.value())

        obj = dpmJson([1, 2, 3])
        result = obj.takeRight(2)
        assertEquals([1, 2, 3], obj.value())
        assertEquals([2, 3], result.value())

        obj = dpmJson(["a", "b", "c"])
        result = obj.takeRight(5)
        assertEquals(["a", "b", "c"], obj.value())
        assertEquals(["a", "b", "c"], result.value())

        obj = dpmJson([[1], [2], [1, 2, 3]])
        result = obj.takeRight(1)
        assertEquals([[1], [2], [1, 2, 3]], obj.asList().collect { it.value() })
        assertEquals([[1, 2, 3]], result.asList().collect { it.value() })
    }

    @Test(expected = UnsupportedOperationException.class)
    void testTakeRightNotArrayType() {
        def obj = dpmJson(TEST_PEOPLE_MAP)
        obj.takeRight(1)
    }

    @Test(expected = UnsupportedOperationException.class)
    void testTakeRightNull() {
        def obj = dpmJson(null)
        obj.takeRight(1)
    }

    @Test
    void testTakeWhile() {
        def obj = dpmJson(null)
        def result = obj.takeWhile { true }
        assertEquals("{}", result.value().toString())

        obj = dpmJson([1, 2, 3])
        result = obj.takeWhile { it.value() < 1 }
        assertEquals([1, 2, 3], obj.value())
        assertEquals([], result.value())

        obj = dpmJson([1, 2, 3])
        result = obj.takeWhile { it.value() < 3 }
        assertEquals([1, 2, 3], obj.value())
        assertEquals([1, 2], result.value())

        obj = dpmJson(["a", "b", "c"])
        result = obj.takeWhile { ('a'..'b').contains(it.value()) }
        assertEquals(["a", "b", "c"], obj.value())
        assertEquals(["a", "b"], result.value())

        obj = dpmJson([[1], [2], [1, 2, 3]])
        result = obj.takeWhile { it.value().size() == 1 }
        assertEquals([[1], [2], [1, 2, 3]], obj.asList().collect { it.value() })
        assertEquals([[1], [2]], result.asList().collect { it.value() })

        obj = dpmJson(TEST_PEOPLE_MAP)
        result = obj.takeWhile { it.value.carBrand.exists() }
        assertEquals(1, result.size())
    }

    @Test
    void testDrop() {
        def obj = dpmJson(null)
        def result = obj.drop(3)
        assertEquals("{}", result.value().toString())

        obj = dpmJson([1, 2, 3])
        result = obj.drop(3)
        assertEquals([1, 2, 3], obj.value())
        assertEquals([], result.value())

        obj = dpmJson([1, 2, 3])
        result = obj.drop(1)
        assertEquals([1, 2, 3], obj.value())
        assertEquals([2, 3], result.value())

        obj = dpmJson(["a", "b", "c"])
        result = obj.drop(1)
        assertEquals(["a", "b", "c"], obj.value())
        assertEquals(["b", "c"], result.value())

        obj = dpmJson([[1], [2], [1, 2, 3]])
        result = obj.drop(2)
        assertEquals([[1], [2], [1, 2, 3]], obj.asList().collect { it.value() })
        assertEquals([[1, 2, 3]], result.asList().collect { it.value() })

        obj = dpmJson(TEST_PEOPLE_MAP)
        result = obj.drop(5)
        assertEquals(1, result.size())
        assertEquals(TEST_PEOPLE_MAP.Celine.lastName, result.Celine.lastName.value())
    }

    @Test
    void testDropRight() {
        def obj = dpmJson([1, 2, 3])
        def result = obj.dropRight(5)
        assertEquals([1, 2, 3], obj.value())
        assertEquals([], result.value())

        obj = dpmJson([1, 2, 3])
        result = obj.dropRight(1)
        assertEquals([1, 2, 3], obj.value())
        assertEquals([1, 2], result.value())

        obj = dpmJson(["a", "b", "c"])
        result = obj.dropRight(0)
        assertEquals(["a", "b", "c"], obj.value())
        assertEquals(["a", "b", "c"], result.value())

        obj = dpmJson([[1], [2], [1, 2, 3]])
        result = obj.dropRight(1)
        assertEquals([[1], [2], [1, 2, 3]], obj.asList().collect { it.value() })
        assertEquals([[1], [2]], result.asList().collect { it.value() })
    }

    @Test(expected = UnsupportedOperationException.class)
    void testDropRightNotArrayType() {
        def obj = dpmJson(TEST_PEOPLE_MAP)
        obj.dropRight(2)
    }

    @Test(expected = UnsupportedOperationException.class)
    void testDropRightNull() {
        def obj = dpmJson(null)
        obj.dropRight(0)
    }

    @Test
    void testDropWhile() {
        def obj = dpmJson(null)
        def result = obj.dropWhile { true }
        assertEquals("{}", result.value().toString())

        obj = dpmJson([1, 2, 3])
        result = obj.dropWhile { true }
        assertEquals([1, 2, 3], obj.value())
        assertEquals([], result.value())

        obj = dpmJson([1, 2, 3])
        result = obj.dropWhile { it.value() < 3 }
        assertEquals([1, 2, 3], obj.value())
        assertEquals([3], result.value())

        obj = dpmJson(["a", "b", "c"])
        result = obj.dropWhile { ('a'..'b').contains(it.value()) }
        assertEquals(["a", "b", "c"], obj.value())
        assertEquals(["c"], result.value())

        obj = dpmJson([[1], [2], [1, 2, 3]])
        result = obj.dropWhile { it.value().size() == 1 }
        assertEquals([[1], [2], [1, 2, 3]], obj.asList().collect { it.value() })
        assertEquals([[1, 2, 3]], result.asList().collect { it.value() })

        obj = dpmJson(TEST_PEOPLE_MAP)
        result = obj.dropWhile { it.value.carBrand.exists() }
        assertFalse(result.containsKey("Max"))
    }

    @Test
    void testReverse() {
        def obj = dpmJson([1, 2, 3])
        def result = obj.reverse()
        assertEquals([1, 2, 3], obj.value())
        assertEquals([3, 2, 1], result.value())

        obj = dpmJson(["a", "b", "c"])
        result = obj.reverse(false)
        assertEquals(["a", "b", "c"], obj.value())
        assertEquals(["c", "b", "a"], result.value())

        obj = dpmJson([1, 2, 3])
        result = obj.reverse(true)
        assertEquals([3, 2, 1], obj.value())
        assertEquals([3, 2, 1], result.value())

        obj = dpmJson([[1], [2], [1, 2, 3]])
        result = obj.reverse(true)
        assertEquals([[1, 2, 3], [2], [1]], obj.asList().collect { it.value() })
        assertEquals([[1, 2, 3], [2], [1]], result.asList().collect { it.value() })
    }

    @Test(expected = UnsupportedOperationException.class)
    void testReverseNotArrayType() {
        def obj = dpmJson(TEST_PEOPLE_MAP)
        obj.reverse()
    }

    @Test(expected = UnsupportedOperationException.class)
    void testReverseRightNull() {
        def obj = dpmJson(null)
        obj.reverse()
    }

    @Test
    void testMultiply() {
        def obj = dpmJson([1, 2, 3])
        def result = obj * 0
        assertEquals([1, 2, 3], obj.value())
        assertEquals([], result.value())

        obj = dpmJson([1, 2, 3])
        result = obj * 1
        assertEquals([1, 2, 3], obj.value())
        assertEquals([1, 2, 3], result.value())

        obj = dpmJson(["a", "b", "c"])
        result = obj * 2
        assertEquals(["a", "b", "c"], obj.value())
        assertEquals(["a", "b", "c", "a", "b", "c"], result.value())

        obj = dpmJson([[1], [2], [1, 2, 3]])
        result = obj * 2
        assertEquals([[1], [2], [1, 2, 3]], obj.asList().collect { it.value() })
        assertEquals([[1], [2], [1, 2, 3], [1], [2], [1, 2, 3]], result.asList().collect { it.value() })
    }

    @Test(expected = UnsupportedOperationException.class)
    void testMultiplyNotArrayType() {
        def obj = dpmJson(TEST_PEOPLE_MAP)
        obj * 1
    }

    @Test(expected = UnsupportedOperationException.class)
    void testMultiplyRightNull() {
        def obj = dpmJson(null)
        obj * 1
    }

    @Test
    void testIntersect() {
        def obj = dpmJson([1, 3, 4])
        def result = obj.intersect([1, 2, 4])
        assertEquals([1, 4], result.value())

        obj = dpmJson(["1", "2", "3"])
        result = obj.intersect(["2"])
        assertEquals(["2"], result.value())

        obj = dpmJson(["1", "2", "3"])
        result = obj.intersect(["2"] as Object)
        assertEquals(["2"], result.value())

        obj = dpmJson([1, 2, 3])
        result = obj.intersect([4])
        assertEquals([], result.value())

        def map = [test: 1, test2: 2, test3: 3]
        obj = dpmJson(map)
        result = obj.intersect([test: 1, test2: 2, test3: -3, test4: 4])
        assertEquals(2, result.size())
        assertEquals(map.test, result.test.value())
    }

    @Test
    void testDisjoint() {
        def obj = dpmJson([1, 3, 4])
        def result = obj.disjoint([5, 2])
        assertTrue(result)

        obj = dpmJson([1, 3, 4])
        result = obj.disjoint([1, 2, 4])
        assertFalse(result)

        obj = dpmJson(["1", "2", "3"])
        result = obj.disjoint(["2" as Character])
        assertFalse(result)
    }

    @Test(expected = UnsupportedOperationException.class)
    void testDisjointNotArrayType() {
        def obj = dpmJson(TEST_PEOPLE_MAP)
        obj.disjoint([1])
    }

    @Test(expected = UnsupportedOperationException.class)
    void testDisjointRightNull() {
        def obj = dpmJson(null)
        obj.disjoint([1])
    }

    /*@Test
    void testFlatten() {
        def obj = dpmJson([1, 2, 3])
        def result = obj.flatten()
        assertEquals([1, 2, 3], result.value())

        obj = dpmJson([1, 2, 3])
        result = obj.flatten { it.value() * 2 }
        assertEquals([2, 4, 6], result.value())


        obj = dpmJson([1, [2]])
        result = obj.flatten()
        assertEquals([1, 2], result.value())

        obj = dpmJson([1, [2]])
        result = obj.flatten { it.value() * 2 }
        assertEquals([2, 4], result.value())


        obj = dpmJson(["a", ["c"]])
        result = obj.flatten()
        assertEquals(["a", "c"], result.value())


        obj = dpmJson([1, [[[[4]]]]])
        result = obj.flatten()
        assertEquals([1, 4], result.value())

        obj = dpmJson([1, [1], [2], [1, 2, 3]])
        result = obj.flatten()
        assertEquals([1, 1, 2, 1, 2, 3], result.value())
    }*/

    @Test(expected = UnsupportedOperationException.class)
    void testFlattenNotArrayType() {
        def obj = dpmJson(TEST_PEOPLE_MAP)
        obj.flatten()
    }

    @Test(expected = UnsupportedOperationException.class)
    void testFlattenNull() {
        def obj = dpmJson(null)
        obj.flatten()
    }

    @Test
    void testLeftShift() {
        def obj = dpmJson([])
        def result = obj << 1
        assertEquals([1], obj.value())
        assertEquals([1], result.value())

        obj = dpmJson([1])
        result = obj << 2
        assertEquals([1, 2], obj.value())
        assertEquals([1, 2], result.value())

        obj = dpmJson(["1"])
        result = obj << "2"
        assertEquals(["1", 2], obj.value())
        assertEquals(["1", 2], result.value())

        obj = dpmJson([1])
        result = obj << [2]
        assertEquals(collectionToJsonString([1, [2]]), collectionToJsonString(obj.value()))
        assertEquals(collectionToJsonString([1, [2]]), collectionToJsonString(result.value()))

        obj = dpmJson([test: false])
        result = obj << [test: true]
        assertEquals([test: true], obj.getWrappedMap().collectEntries { [it.key, it.value.value()] })
        assertEquals([test: true], result.getWrappedMap().collectEntries { [it.key, it.value.value()] })

        obj = dpmJson([a: 1, b: 2])
        result = obj << [c: 3, d: 4]
        assertEquals([a: 1, b: 2, c: 3, d: 4], obj.getWrappedMap().collectEntries { [it.key, it.value.value()] })
        assertEquals([a: 1, b: 2, c: 3, d: 4], result.getWrappedMap().collectEntries { [it.key, it.value.value()] })
    }

    @Test
    void testContains() {
        def obj = dpmJson([1, 2])
        def result = obj.contains(1)
        assertTrue(result)

        obj = dpmJson([1, 2])
        result = obj.contains(3)
        assertFalse(result)

        obj = dpmJson(["1", "2"])
        result = obj.contains("1" as Character)
        assertTrue(result)

        obj = dpmJson([1, 2, [3]])
        result = obj.contains([3])
        assertTrue(result)
    }

    @Test(expected = UnsupportedOperationException.class)
    void testContainsNotArrayType() {
        def obj = dpmJson(TEST_PEOPLE_MAP)
        obj.contains(1)
    }

    @Test(expected = UnsupportedOperationException.class)
    void testContainsRightNull() {
        def obj = dpmJson(null)
        obj.contains(1)
    }

    @Test
    void testSwap() {
        def obj = dpmJson([1, 2, 3])
        def result = obj.swap(0, 2)
        assertEquals([3, 2, 1], obj.value())
        assertEquals([3, 2, 1], result.value())

        obj = dpmJson([1, 2, 3])
        result = obj.swap(1, 1)
        assertEquals([1, 2, 3], obj.value())
        assertEquals([1, 2, 3], result.value())

        obj = dpmJson(["1", "2"])
        result = obj.swap(0, 1)
        assertEquals(["2", "1"], obj.value())
        assertEquals(["2", "1"], result.value())

        obj = dpmJson(test: [arr: [1, 2]])
        result = obj.test.arr.swap(0, 1)
        assertEquals([2, 1], obj.test.arr.value())
        assertEquals([2, 1], result.value())
    }

    @Test(expected = UnsupportedOperationException.class)
    void testSwapNotArrayType() {
        def obj = dpmJson(TEST_PEOPLE_MAP)
        obj.swap(0, 1)
    }

    @Test(expected = UnsupportedOperationException.class)
    void testSwapNull() {
        def obj = dpmJson(null)
        obj.swap(0, 1)
    }

    @Test
    void testRemoveAt() {
        def obj = dpmJson([1, 2, 3])
        def result = obj.removeAt(0)
        assertEquals([2, 3], obj.value())
        assertEquals(1, result.value())

        obj = dpmJson([1, 2, 3])
        result = obj.removeAt(2)
        assertEquals([1, 2], obj.value())
        assertEquals(3, result.value())

        obj = dpmJson(["1", "2"])
        result = obj.removeAt(1)
        assertEquals(["1"], obj.value())
        assertEquals("2", result.value())

        obj = dpmJson(test: [arr: [1, 2]])
        result = obj.test.arr.removeAt(0)
        assertEquals([2], obj.test.arr.value())
        assertEquals(1, result.value())
    }

    @Test(expected = UnsupportedOperationException.class)
    void testRemoveAtNotArrayType() {
        def obj = dpmJson(TEST_PEOPLE_MAP)
        obj.removeAt(0)
    }

    @Test(expected = UnsupportedOperationException.class)
    void testRemoveAtNull() {
        def obj = dpmJson(null)
        obj.removeAt(0)
    }

    @Test
    void testRemoveElement() {
        def obj = dpmJson([1, 2, 3])
        def result = obj.removeElement(1)
        assertEquals([2, 3], obj.value())
        assertTrue(result)

        obj = dpmJson([1, 2, 3])
        result = obj.removeElement(5)
        assertEquals([1, 2, 3], obj.value())
        assertFalse(result)

        obj = dpmJson(["1", "2"])
        result = obj.removeElement("2" as Character)
        assertEquals(["1"], obj.value())
        assertTrue(result)

        obj = dpmJson(test: [arr: [1, 2]])
        result = obj.test.arr.removeElement(1)
        assertEquals([2], obj.test.arr.value())
        assertTrue(result)
    }

    @Test(expected = UnsupportedOperationException.class)
    void testRemoveElementNotArrayType() {
        def obj = dpmJson(TEST_PEOPLE_MAP)
        obj.removeElement(0)
    }

    @Test(expected = UnsupportedOperationException.class)
    void testRemoveElementNull() {
        def obj = dpmJson(null)
        obj.removeElement(0)
    }

    @Test
    void testIsEmpty() {
        def obj = dpmJson([])
        def result = obj.isEmpty()
        assertTrue(result)

        obj = dpmJson([1])
        result = obj.isEmpty()
        assertFalse(result)

        obj = dpmJson(["1"])
        result = obj.isEmpty()
        assertFalse(result)

        obj = dpmJson([[2]])
        result = obj.isEmpty()
        assertFalse(result)

        obj = dpmJson(TEST_PEOPLE_MAP)
        result = obj.isEmpty()
        assertFalse(result)

        obj = dpmJson([:])
        result = obj.isEmpty()
        assertTrue(result)

        obj = dpmJson(1)
        result = obj.isEmpty()
        assertTrue(result)
    }

    @Test
    void testAdd() {
        def obj = dpmJson([])
        def result = obj.add(null)
        assertEquals([], obj.value())
        assertFalse(result)

        obj = dpmJson([])
        result = obj.add(1)
        assertEquals([1], obj.value())
        assertTrue(result)

        obj = dpmJson([1, 2])
        result = obj.add(2)
        assertEquals([1, 2, 2], obj.value())
        assertTrue(result)

        obj = dpmJson(["a", "b"])
        result = obj.add("c" as Character)
        assertEquals(["a", "b", "c"], obj.value())
        assertTrue(result)

        obj = dpmJson([1])
        result = obj.add([2])
        assertEquals([1, [2]].toString(), obj.toString())
        assertTrue(result)

        obj = dpmJson([1, 2])
        obj.add(0, 0)
        assertEquals([0, 1, 2], obj.value())

        obj = dpmJson([initTestObject()])
        obj.add(0, initTestObject())
        assertEquals([initTestObject(), initTestObject()].toString(), obj.toString())
    }

    @Test(expected = UnsupportedOperationException.class)
    void testAddNotArrayType() {
        def obj = dpmJson(TEST_PEOPLE_MAP)
        obj.add(0)
    }

    @Test(expected = UnsupportedOperationException.class)
    void testAddNull() {
        def obj = dpmJson(null)
        obj.add(0)
    }

    @Test
    void testContainsAll() {
        def obj = dpmJson([1, 2, 3])
        def result = obj.containsAll([1, 2])
        assertTrue(result)

        obj = dpmJson([1, 2, 3])
        result = obj.containsAll([1, 2, 5])
        assertFalse(result)

        obj = dpmJson([test: [1, 2, 3]])
        result = obj.test.containsAll([1, 3])
        assertTrue(result)

        obj = dpmJson(["1", "2", "3"])
        result = obj.containsAll(["2" as Character])
        assertTrue(result)

        def arr = new Integer[2]
        arr[0] = 1
        arr[1] = 2
        obj = dpmJson([1, 2, 3])
        result = obj.containsAll(arr)
        assertTrue(result)
    }


    @Test(expected = UnsupportedOperationException.class)
    void testContainsAllNotArrayType() {
        def obj = dpmJson(TEST_PEOPLE_MAP)
        obj.containsAll([1])
    }

    @Test(expected = UnsupportedOperationException.class)
    void testContainsAllNull() {
        def obj = dpmJson(null)
        obj.containsAll([1])
    }

    @Test
    void testSet() {
        def obj = dpmJson([2, 3])
        def result = obj.set(0, 1)
        assertEquals([1, 3], obj.value())
        assertEquals(2, result.value())

        obj = dpmJson(["1"])
        result = obj.set(0, "2" as Character)
        assertEquals(["2"], obj.value())
        assertEquals("1", result.value())

        obj = dpmJson([1, 2])
        result = obj.set(1, [1, 2, 3])
        assertEquals([1, [1, 2, 3]].toString(), obj.asList().toString())
        assertEquals(2, result.value())
    }

    @Test(expected = UnsupportedOperationException.class)
    void testSetNotArrayType() {
        def obj = dpmJson(TEST_PEOPLE_MAP)
        obj.set(0, 1)
    }

    @Test(expected = UnsupportedOperationException.class)
    void testSetNull() {
        def obj = dpmJson(null)
        obj.set(0, 1)
    }

    @Test
    void testRemove() {
        def obj = dpmJson([1, 2, 3])
        def result = obj.remove(0)
        assertEquals([2, 3], obj.value())
        assertEquals(1, result.value())

        obj = dpmJson([1, 2, 3])
        result = obj.remove(2)
        assertEquals([1, 2], obj.value())
        assertEquals(3, result.value())

        obj = dpmJson(["1", "2"])
        result = obj.remove(1)
        assertEquals(["1"], obj.value())
        assertEquals("2", result.value())

        obj = dpmJson(test: [arr: [1, 2]])
        result = obj.test.arr.remove(0)
        assertEquals([2], obj.test.arr.value())
        assertEquals(1, result.value())
    }

    @Test(expected = UnsupportedOperationException.class)
    void testRemoveNotArrayType() {
        def obj = dpmJson(TEST_PEOPLE_MAP)
        obj.remove(0)
    }

    @Test(expected = UnsupportedOperationException.class)
    void testRemoveNull() {
        def obj = dpmJson(null)
        obj.remove(0)
    }

    @Test
    void testSubList() {
        def obj = dpmJson(1..10)
        def result = obj.subList(0, 5)
        assertEquals(1..5, result.collect { it.value() })

        obj = dpmJson(1..10)
        result = obj.subList(0, 1)
        assertEquals([1], result.collect { it.value() })

        obj = dpmJson("a".."g")
        result = obj.subList(0, 3)
        assertEquals("a".."c", result.collect { it.value() })
    }

    @Test(expected = UnsupportedOperationException.class)
    void testSubListNotArrayType() {
        def obj = dpmJson(TEST_PEOPLE_MAP)
        obj.subList(0, 1)
    }

    @Test(expected = UnsupportedOperationException.class)
    void testSubListNull() {
        def obj = dpmJson(null)
        obj.subList(0, 1)
    }

    @Test
    void testContainsKey() {
        def obj = dpmJson(TEST_PEOPLE_MAP)
        def result = obj.containsKey("Max")
        assertTrue(result)

        obj = dpmJson(TEST_PEOPLE_MAP)
        result = obj.containsKey(null)
        assertFalse(result)

        obj = dpmJson(TEST_PEOPLE_MAP)
        result = obj.containsKey("R")
        assertFalse(result)

        obj = dpmJson(TEST_PEOPLE_MAP)
        result = obj.Max.containsKey("carBrand")
        assertTrue(result)

        obj = dpmJson(TEST_PEOPLE_MAP)
        result = obj.Anna.containsKey("carBrand")
        assertFalse(result)
    }

    @Test(expected = UnsupportedOperationException.class)
    void testContainsKeyNotMapType() {
        def obj = dpmJson([1, 2])
        obj.containsKey("test")
    }

    @Test
    void testContainsValue() {
        def map = [test: 1, test2: 1, test3: 3, test4: 4]
        def obj = dpmJson(map)
        def result = obj.containsValue(1)
        assertTrue(result)

        obj = dpmJson(map)
        result = obj.containsValue(3)
        assertTrue(result)

        obj = dpmJson(map)
        result = obj.containsValue(5)
        assertFalse(result)

        map = [test: [test: 1], test2: [1, 2]]
        obj = dpmJson(map)
        result = obj.containsValue([1, 2])
        assertTrue(result)

        obj = dpmJson(map)
        result = obj.test.containsValue(1)
        assertTrue(result)

        obj = dpmJson(map)
        result = obj.containsValue(1)
        assertFalse(result)
    }

    @Test(expected = UnsupportedOperationException.class)
    void testContainsValueNotMapType() {
        def obj = dpmJson([1, 2])
        obj.containsValue("test")
    }

    @Test
    void testPut() {
        def map = [test: 1, test2: 2]
        def obj = dpmJson(map)
        def result = obj.put("test", 3)
        assertEquals(1, result.value().value)
        assertEquals(3, obj.test.value())

        obj = dpmJson(TEST_PEOPLE_MAP)
        result = obj.put("Steve", [hasPets: true])
        assertEquals("{}", result.toString())
        assertTrue(obj.Steve.hasPets.value())

        obj = dpmJson(TEST_PEOPLE_MAP)
        result = obj.put("Anna", [carBrand: "BMW"])
        assertEquals(TEST_PEOPLE_MAP.Anna.lastName, result.lastName.value())
        assertEquals("BMW", obj.Anna.carBrand.value())
    }

    @Test(expected = UnsupportedOperationException.class)
    void testPutNotMapType() {
        def obj = dpmJson([1, 2, 3])
        obj.put("test", 1)
    }

    @Test
    void testPutAllMapType() {
        def map = [test: 1, test2: 2]
        def obj = dpmJson(map)
        obj.putAll([new MapEntry("test", 0), new MapEntry("test3", 3)])
        assertEquals(0, obj.test.value())
        assertEquals(3, obj.test3.value())

        obj = dpmJson(TEST_PEOPLE_MAP)
        obj.putAll([new MapEntry("Steve", [hasPets: true])])
        assertTrue(obj.Steve.hasPets.value())

        obj = dpmJson(TEST_PEOPLE_MAP)
        obj.putAll([new MapEntry("Steve", [hasPets: true]), new MapEntry("Carl", [carBrand: "BMW"])])
        assertTrue(obj.Steve.hasPets.value())
        assertEquals("BMW", obj.Carl.carBrand.value())
    }

    @Test(expected = UnsupportedOperationException.class)
    void testPutAllNotMapType() {
        def obj = dpmJson([1, 2, 3])
        obj.putAll([new MapEntry("test", 1)])
    }

    @Test
    void testKeySet() {
        def map = [test: 1, test2: 2]
        def obj = dpmJson(map)
        def result = obj.keySet()
        assertEquals(2, result.size())
        assertTrue(result.containsAll(["test", "test2"]))

        obj = dpmJson(TEST_PEOPLE_MAP)
        result = obj.keySet()
        assertEquals(6, result.size())
        assertTrue(result.contains("Max"))

        obj = dpmJson(TEST_PEOPLE_MAP)
        result = obj.Max.keySet()
        assertEquals(4, result.size())
        assertTrue(result.containsAll(["lastName", "id", "hasPets", "carBrand"]))
    }


    @Test(expected = UnsupportedOperationException.class)
    void testKeySetNotMapType() {
        def obj = dpmJson([1, 2, 3])
        obj.keySet()
    }

    @Test
    void testValues() {
        def map = [test: 1, test2: 2]
        def obj = dpmJson(map)
        def result = obj.values()
        assertEquals(2, result.size())
        assertTrue(result.collect { it.value() }.containsAll([1, 2]))

        obj = dpmJson(TEST_PEOPLE_MAP)
        result = obj.values()
        assertEquals(6, result.size())
        assertEquals(TEST_PEOPLE_MAP.Max.lastName, result[0].lastName.value())

    }

    @Test(expected = UnsupportedOperationException.class)
    void testValuesNotMapType() {
        def obj = dpmJson([1, 2, 3])
        obj.values()
    }

    @Test
    void testEntrySet() {
        def map = [test: 1, test2: 2]
        def obj = dpmJson(map)
        def result = obj.entrySet()
                .collectEntries { [it.key, it.value.value()] }
        assertEquals(2, result.size())
        assertTrue(result.containsKey("test"))

        obj = dpmJson(TEST_PEOPLE_MAP)
        result = obj.entrySet()
                .collectEntries { [it.key, it.value.value()] }
        assertEquals(6, result.size())
        assertTrue(result.containsKey("Max"))

        obj = dpmJson(TEST_PEOPLE_MAP)
        result = obj.Max.entrySet()
                .collectEntries { [it.key, it.value.value()] }
        assertEquals(4, result.size())
        assertTrue(result.containsValue(false))

    }


    @Test(expected = UnsupportedOperationException.class)
    void testEntrySetNotMapType() {
        def obj = dpmJson([1, 2, 3])
        obj.entrySet()
    }

    @Test
    void testUseCase1() {
        def result = [:]
        def people = dpmJson(TEST_PEOPLE_MAP)
        people = people.findAll { it.value.carBrand.exists() }
        people.each { it ->
            def carBrand = it.value.carBrand.value()
            def firstName = it.key
            result[carBrand] = firstName
        }
        assertEquals(5, people.size())
        assertTrue(people.Max.exists())
        assertFalse(people.Anna.exists())
        assertEquals(5, result.size())
        def firstKey = TEST_PEOPLE_MAP.keySet().iterator().next()
        assertEquals(firstKey, result[TEST_PEOPLE_MAP[firstKey].carBrand])

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

    private static final TEST_PEOPLE_MAP = [
            Max   : [
                    lastName: "Muster",
                    id      : 34577,
                    hasPets : false,
                    carBrand: "VW"
            ],
            Anna  : [
                    lastName: "Beispiel",
                    id      : 12356,
                    hasPets : true
            ],
            Henry : [
                    lastName: "West",
                    id      : 99348,
                    hasPets : false,
                    carBrand: "Ferrari"
            ],
            Farid : [
                    lastName: "Al-Shami",
                    id      : 16528,
                    hasPets : false,
                    carBrand: "Mercedes"
            ],
            Chiara: [
                    lastName: "Bernasconi",
                    id      : 93678,
                    hasPets : true,
                    carBrand: "Audi"
            ],
            Celine: [
                    lastName: "Foster",
                    id      : 26735,
                    hasPets : true,
                    carBrand: "Jaguar"
            ]
    ]
}
