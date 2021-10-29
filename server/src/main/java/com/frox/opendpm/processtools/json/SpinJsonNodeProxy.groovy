package com.frox.opendpm.processtools.json

import org.camunda.spin.DataFormats
import org.camunda.spin.Spin
import org.camunda.spin.SpinList
import org.camunda.spin.impl.SpinListImpl
import org.camunda.spin.impl.json.jackson.JacksonJsonLogger
import org.camunda.spin.impl.json.jackson.JacksonJsonNode
import org.camunda.spin.json.SpinJsonNode
import org.codehaus.groovy.reflection.CachedField
import spinjar.com.fasterxml.jackson.annotation.JsonIgnore
import spinjar.com.fasterxml.jackson.databind.JsonNode

/**
 * Wrapper, which provides null-safe access to properties and field of the input object. Functionality based on Groovy script metaprogramming.
 * Extends {@link org.camunda.spin.impl.json.jackson.JacksonJsonNode} as the easiest way of using as variable inside Camunda processes.
 * Overrides most of methods of {@link org.codehaus.groovy.runtime.DefaultGroovyMethodsSupport} supported for {@link java.util.List} and {@link java.util.Map}
 *
 * @author <a href="mailto:opendpm@frox.ch">Open Dpm</a>
 */
class SpinJsonNodeProxy extends JacksonJsonNode implements Iterable, Comparable<SpinJsonNodeProxy> {

    JacksonJsonLogger LOG = JacksonJsonLogger.JSON_TREE_LOGGER

    SpinJsonNode root
    SpinJsonNode context = null
    String contextPath = null

    SpinJsonNodeProxy(SpinJsonNode root) {
        super(root.unwrap(), DataFormats.json())
        this.root = root
        this.context = root
    }

    SpinJsonNodeProxy(SpinJsonNode root, SpinJsonNode context, String contextPath) {
        super(context.unwrap(), DataFormats.json())
        this.root = root
        this.context = context
        this.contextPath = contextPath
    }

    SpinJsonNodeProxy methodMissing(String name, def args) {
        return this;
    }

    def getProperty(String name) {
        def metaProperty = metaClass.getMetaProperty(name)
        if (metaProperty && metaProperty instanceof MetaBeanProperty && metaProperty.getField() != null ||
                metaProperty instanceof CachedField) {
            return metaClass.getProperty(this, name)
        }

        if (isList() && context.elements().size() > 0) {
            context = elements().get(0)
        }

        SpinJsonNode newContext = Spin.JSON("null")
        if (context instanceof SpinJsonNode && context.hasProp(name)) {
            newContext = context.prop(name)
        }

        return new SpinJsonNodeProxy(root, newContext, contextPath != null ? contextPath + "." + name : name)
    }

    void setProperty(String name, Object value) {
        def metaProperty = metaClass.getMetaProperty(name)
        if (metaProperty && metaProperty instanceof MetaBeanProperty && metaProperty.getField() != null) {
            metaClass.setProperty(this, name, value)
            return
        }

        context = JsonProxyUtils.setValue(root, contextPath != null ? contextPath + "." + name : name, value)
    }

    @Override
    SpinList<SpinJsonNode> elements() {
        if (isList() || isMap()) {
            Iterator<JsonNode> iterator = context.unwrap().elements()
            SpinList<SpinJsonNode> list = new SpinListImpl<SpinJsonNode>()
            while (iterator.hasNext()) {
                list.add(createWrapperInstance(iterator.next()))
            }

            return list
        }
        throw LOG.unableToParseValue(SpinList.class.getSimpleName(), jsonNode.getNodeType())
    }

    private JsonNode createJsonNode(Object property) {
        return context.dataFormat.createJsonNode(property)
    }

    private List<JsonNode> createJsonNodes(Iterable properties) {
        if (properties == null) {
            return Collections.emptyList()
        }
        return properties.collect { createJsonNode(it) }
    }

    private SpinJsonNode createWrapperInstance(Object parameter) {
        return context.dataFormat.createWrapperInstance(parameter)
    }

    protected List getWrappedList() {
        return new ArrayList<SpinJsonNode>(elements())
                .collect { JsonProxyUtils.wrap(it) }
    }

    protected void setWrappedList(List<SpinJsonNodeProxy> elements) {
        clear()
        elements.each { append(it.value()) }
    }

    protected Map<String, SpinJsonNodeProxy> getWrappedMap() {
        return context.unwrap().fields()
                .collectEntries { [it.key, context.dataFormat.createWrapperInstance(it.value)] }
                .collectEntries { [it.key, JsonProxyUtils.wrap(it.value)] }
    }

    protected void setWrappedMap(Map<String, SpinJsonNodeProxy> elements) {
        clear()
        elements.each { prop(it.key, it.value.value()) }
    }

    protected Object getWrappedElements() {
        if (isList()) {
            return getWrappedList()
        }
        if (isMap()) {
            return getWrappedMap()
        }
        throw new UnsupportedOperationException("Not supported for non iterable types")
    }

    protected void setWrappedElements(Object obj) {
        if (isList()) {
            setWrappedList(obj as List<SpinJsonNodeProxy>)
        } else if (isMap()) {
            setWrappedMap(obj as Map<String, SpinJsonNodeProxy>)
        } else {
            throw new UnsupportedOperationException("Not supported for non iterable types")
        }
    }

    void push(value) {
        if (isList()) {
            context.append(value)
        } else {
            throw new UnsupportedOperationException("Operation 'push' not supported for non array types")
        }
    }

    void clear() {
        if (isList() || isMap()) {
            context.unwrap().removeAll()
        } else {
            throw new UnsupportedOperationException("Operation 'clean' not supported for non iterable types")
        }
    }

    boolean exists() {
        return JsonProxyUtils.exists(context)
    }

    boolean isMap() {
        return JsonProxyUtils.isMap(context)
    }

    boolean isList() {
        return JsonProxyUtils.isList(context)
    }


    Object value() {
        if (context == null) {
            return null
        }
        if (context.isValue()) {
            return context.value()
        }
        if (isList()) {
            return new ArrayList<SpinJsonNode>(elements())
                    .collect { it.isValue() ? it.value() : it }
        }
        return context
    }

    String toString() {
        def value = value()
        if (value != null) {
            return value.toString()
        }
        return null
    }

    boolean equals(Object otherValue) {
        if (otherValue instanceof SpinJsonNodeProxy) {
            otherValue = otherValue.value()
        }
        def thisValue = value()
        if (thisValue != null) {
            return thisValue == otherValue
        } else {
            return otherValue == null
        }
    }

    @Override
    int compareTo(SpinJsonNodeProxy o) {
        if (o == null) {
            return -1
        }
        return value() <=> o.value()
    }

    SpinJsonNodeProxy plus(Object other) {
        def value = this.exists() ? this.value() : 0
        other = other == null ? 0 : other
        if (other instanceof SpinJsonNodeProxy) {
            other = other.exists() ? other.value() : 0
        }
        try {
            return JsonProxyUtils.wrap(value + other)
        } catch (Exception e) {
            throw new UnsupportedOperationException("Not supported for specified types")
        }
    }

    SpinJsonNodeProxy plus(Iterable right) {
        if (right instanceof SpinJsonNodeProxy && !right.isList()) {
            return plus((Object) right)
        }
        if (isList() && right != null) {
            return JsonProxyUtils.wrap(getWrappedList() + right)
        } else {
            throw new UnsupportedOperationException("Not supported for non array types")
        }
    }

    SpinJsonNodeProxy minus(Object other) {
        def value = this.exists() ? this.value() : 0
        other = other == null ? 0 : other
        if (other instanceof SpinJsonNodeProxy) {
            other = other.exists() ? other.value() : 0
        }
        try {
            return JsonProxyUtils.wrap(value - other)
        } catch (Exception e) {
            throw new UnsupportedOperationException("Not supported for specified types")
        }
    }

    SpinJsonNodeProxy minus(Iterable right) {
        if (right instanceof SpinJsonNodeProxy && !right.isList()) {
            return minus((Object) right)
        }
        if (isList() && right != null) {
            return JsonProxyUtils.wrap(getWrappedList() + right)
        } else {
            throw new UnsupportedOperationException("Not supported for non array types")
        }
    }

    SpinJsonNodeProxy multiply(Object other) {
        def value = this.exists() ? this.value() : 0
        other = other == null ? 0 : other
        if (other instanceof SpinJsonNodeProxy) {
            other = other.exists() ? other.value() : 0
        }
        try {
            return JsonProxyUtils.wrap(value * other)
        } catch (Exception e) {
            throw new UnsupportedOperationException("Not supported for specified types")
        }
    }

    SpinJsonNodeProxy multiply(Number factor) {
        if (isList()) {
            try {
                return JsonProxyUtils.wrap(getWrappedList().multiply(factor))
            } catch (Exception e) {
                throw new UnsupportedOperationException("Not supported for specified types")
            }
        }
        return multiply((Object) factor)
    }

    SpinJsonNodeProxy div(Object other) {
        def value = this.exists() ? this.value() : 0
        other = other == null ? 0 : other
        if (other instanceof SpinJsonNodeProxy) {
            other = other.exists() ? other.value() : 0
        }
        try {
            return JsonProxyUtils.wrap(value / other)
        } catch (Exception e) {
            throw new UnsupportedOperationException("Not supported for specified types")
        }
    }

    // --------- supported default groovy methods --------- //
    @Override
    Iterator iterator() {
        return getWrappedElements().iterator()
    }

    def getAt(int i) {
        def index = "[" + i + "]"
        return new SpinJsonNodeProxy(
                root,
                JsonProxyUtils.getValueAt(context, i),
                contextPath != null ? contextPath + index : index
        )
    }

    def getAt(Object property) {
        if (property == null) {
            property = "null"
        }
        return getProperty(property.toString())
    }

    void putAt(int i, value) {
        if (isList()) {
            def elements = elements()
            if (i >= 0 && i < elements.size()) {
                context.removeAt(i)
                context.insertAt(i, value)
            } else {
                throw new ArrayIndexOutOfBoundsException(i)
            }
        } else if (isMap()) {
            setProperty(i.toString(), value)
        } else {
            throw new UnsupportedOperationException("Operation 'putAt' not supported for non array types")
        }
    }

    void putAt(Object property, Object newValue) {
        if (property == null) {
            property = "null"
        }
        setProperty(property.toString(), newValue)
    }

    SpinJsonNodeProxy unique() {
        if (isList()) {
            return JsonProxyUtils.wrap(getWrappedList().unique())
        } else {
            throw new UnsupportedOperationException("Not supported for non array types")
        }
    }

    SpinJsonNodeProxy unique(boolean mutate) {
        if (isList()) {
            def elements = getWrappedList()
            def result = elements.unique(mutate)
            if (mutate) {
                setWrappedList(elements)
            }
            return JsonProxyUtils.wrap(result)
        } else {
            throw new UnsupportedOperationException("Not supported for non array types")
        }
    }

    SpinJsonNodeProxy unique(Comparator comparator) {
        if (isList()) {
            return JsonProxyUtils.wrap(getWrappedList().unique(comparator))
        } else {
            throw new UnsupportedOperationException("Not supported for non array types")
        }
    }

    SpinJsonNodeProxy unique(boolean mutate, Comparator comparator) {
        if (isList()) {
            def elements = getWrappedList()
            def result = elements.unique(mutate, comparator)
            if (mutate) {
                setWrappedList(elements)
            }
            return JsonProxyUtils.wrap(result)
        } else {
            throw new UnsupportedOperationException("Not supported for non array types")
        }
    }

    SpinJsonNodeProxy unique(Closure closure) {
        if (isList()) {
            def result = getWrappedList().unique closure
            return JsonProxyUtils.wrap(result)
        } else {
            throw new UnsupportedOperationException("Not supported for non array types")
        }
    }

    SpinJsonNodeProxy unique(boolean mutate, Closure closure) {
        if (isList()) {
            def elements = getWrappedList()
            def result = elements.unique(mutate, closure)
            if (mutate) {
                setWrappedList(elements)
            }
            return JsonProxyUtils.wrap(result)
        } else {
            throw new UnsupportedOperationException("Not supported for non array types")
        }
    }

    SpinJsonNodeProxy toUnique() {
        if (isList()) {
            return JsonProxyUtils.wrap(getWrappedList().toUnique())
        } else {
            throw new UnsupportedOperationException("Not supported for non array types")
        }
    }

    SpinJsonNodeProxy toUnique(Closure condition) {
        if (isList()) {
            def result = getWrappedList().toUnique condition
            return JsonProxyUtils.wrap(result)
        } else {
            throw new UnsupportedOperationException("Not supported for non array types")
        }
    }

    SpinJsonNodeProxy toUnique(Comparator comparator) {
        if (isList()) {
            def result = getWrappedList().toUnique(comparator)
            return JsonProxyUtils.wrap(result)
        } else {
            throw new UnsupportedOperationException("Not supported for non array types")
        }
    }

    SpinJsonNodeProxy reverseEach(Closure closure) {
        def result = getWrappedElements().reverseEach closure
        return JsonProxyUtils.wrap(result)
    }

    SpinJsonNodeProxy grep(Object filter) {
        return JsonProxyUtils.wrap(value().grep(filter))
    }

    List<SpinJsonNodeProxy> toList() {
        if (isList()) {
            return getWrappedList()
        } else {
            throw new UnsupportedOperationException("Not supported for non array types")
        }
    }

    SpinJsonNodeProxy collate(int size) {
        if (isList()) {
            def result = getWrappedList().collate(size)
            result = result.collect { JsonProxyUtils.wrap(it) }
            return JsonProxyUtils.wrap(result)
        } else {
            throw new UnsupportedOperationException("Not supported for non array types")
        }
    }

    SpinJsonNodeProxy collate(int size, boolean keepRemainder) {
        if (isList()) {
            def result = getWrappedList().collate(size, keepRemainder)
            result = result.collect { JsonProxyUtils.wrap(it) }
            return JsonProxyUtils.wrap(result)
        } else {
            throw new UnsupportedOperationException("Not supported for non array types")
        }
    }

    SpinJsonNodeProxy collate(int size, int step) {
        if (isList()) {
            def result = getWrappedList().collate(size, step)
            result = result.collect { JsonProxyUtils.wrap(it) }
            return JsonProxyUtils.wrap(result)
        } else {
            throw new UnsupportedOperationException("Not supported for non array types")
        }
    }

    SpinJsonNodeProxy collate(int size, int step, boolean keepRemainder) {
        if (isList()) {
            def result = getWrappedList().collate(size, step, keepRemainder)
            result = result.collect { JsonProxyUtils.wrap(it) }
            return JsonProxyUtils.wrap(result)
        } else {
            throw new UnsupportedOperationException("Not supported for non array types")
        }
    }

    SpinJsonNodeProxy collect() {
        return JsonProxyUtils.wrap(getWrappedElements().collect())
    }

    SpinJsonNodeProxy collect(Closure transform) {
        def result = getWrappedElements().collect transform
        return JsonProxyUtils.wrap(result)
    }

    SpinJsonNodeProxy collect(Collection collector, Closure transform) {
        return JsonProxyUtils.wrap(getWrappedElements().collect(collector, transform))
    }

    SpinJsonNodeProxy collectNested(Closure transform) {
        throw new UnsupportedOperationException("Not supported yet")
        /*if (isList()) {
            def result = getWrappedList().collectNested transform
            return JsonProxyUtils.wrap(result)
        } else {
            throw new UnsupportedOperationException("Not supported for non array types")
        }*/
    }

    SpinJsonNodeProxy collectNested(Collection collector, Closure transform) {
        throw new UnsupportedOperationException("Not supported yet")
        /*if (isList()) {
            return JsonProxyUtils.wrap(getWrappedList().collectNested(collector, transform))
        } else {
            throw new UnsupportedOperationException("Not supported for non array types")
        }*/
    }

    SpinJsonNodeProxy collectMany(Closure projection) {
        def result = getWrappedElements().collectMany projection
        return JsonProxyUtils.wrap(result)
    }

    SpinJsonNodeProxy collectMany(Collection collector, Closure projection) {
        def result = getWrappedElements().collectMany(collector, projection)
        return JsonProxyUtils.wrap(result)
    }

    SpinJsonNodeProxy collectEntries() {
        throw new UnsupportedOperationException("Not supported yet")
        /*if (isList()) {
            return JsonProxyUtils.wrap(getWrappedList().collectEntries())
        } else {
            throw new UnsupportedOperationException("Not supported for non array types")
        }*/
    }

    SpinJsonNodeProxy collectEntries(Closure transform) {
        throw new UnsupportedOperationException("Not supported yet")
        /*def result = getWrappedElements().collectEntries transform
        return JsonProxyUtils.wrap(result)*/
    }

    SpinJsonNodeProxy collectEntries(Map collector) {
        throw new UnsupportedOperationException("Not supported yet")
        /*if (isList()) {
            return JsonProxyUtils.wrap(getWrappedList().collectEntries(collector))
        } else {
            throw new UnsupportedOperationException("Not supported for non array types")
        }*/
    }

    SpinJsonNodeProxy collectEntries(Map collector, Closure transform) {
        throw new UnsupportedOperationException("Not supported yet")
        /*def result = getWrappedElements().collectEntries(collector, transform)
        return JsonProxyUtils.wrap(result)*/
    }

    SpinJsonNodeProxy findResults(Closure filteringTransform) {
        def result = getWrappedElements().findResults filteringTransform
        return JsonProxyUtils.wrap(result)
    }

    SpinJsonNodeProxy findAll() {
        def result = getWrappedElements().findAll()
        return JsonProxyUtils.wrap(result)
    }

    SpinJsonNodeProxy findAll(Closure closure) {
        def result = getWrappedElements().findAll closure
        return JsonProxyUtils.wrap(result)
    }

    boolean removeAll(Iterable items) {
        if (items == null) {
            return false
        }
        if (isList()) {
            def elements = getWrappedList()
            def result = elements.removeAll(JsonProxyUtils.wrap(items).getWrappedElements())
            if (result) {
                setWrappedList(elements)
            }
            return result
        } else {
            throw new UnsupportedOperationException("Not supported for non array types")
        }
    }

    boolean removeAll(Object[] items) {
        return removeAll(Arrays.asList(items))
    }

    boolean removeAll(Closure condition) {
        if (isList()) {
            def elements = getWrappedList()
            def result = elements.removeAll condition
            if (result) {
                setWrappedList(elements)
            }
            return result
        } else {
            throw new UnsupportedOperationException("Not supported for non array types")
        }
    }

    boolean retainAll(Iterable items) {
        if (items == null) {
            return false
        }
        if (isList()) {
            def elements = getWrappedList()
            def result = elements.retainAll(JsonProxyUtils.wrap(items).getWrappedElements())
            if (result) {
                setWrappedList(elements)
            }
            return result
        } else {
            throw new UnsupportedOperationException("Not supported for non array types")
        }
    }

    boolean retainAll(Object[] items) {
        return retainAll(Arrays.asList(items))
    }

    boolean retainAll(Closure condition) {
        if (isList()) {
            def elements = getWrappedList()
            def result = elements.retainAll condition
            if (result) {
                setWrappedList(elements)
            }
            return result
        } else {
            throw new UnsupportedOperationException("Not supported for non array types")
        }
    }

    boolean addAll(Iterable items) {
        if (items == null) {
            return false
        }
        if (isList()) {
            def elements = getWrappedList()
            def result = elements.addAll(JsonProxyUtils.wrap(items).getWrappedElements())
            if (result) {
                setWrappedList(elements)
            }
            return result
        } else {
            throw new UnsupportedOperationException("Not supported for non array types")
        }
    }

    boolean addAll(int index, Iterable items) {
        if (items == null) {
            return false
        }
        if (isList()) {
            def elements = getWrappedList()
            def result = elements.addAll(index, JsonProxyUtils.wrap(items).getWrappedElements())
            if (result) {
                setWrappedList(elements)
            }
            return result
        } else {
            throw new UnsupportedOperationException("Not supported for non array types")
        }
    }

    boolean addAll(Object[] items) {
        return addAll(Arrays.asList(items))
    }

    boolean addAll(int index, Object[] items) {
        return addAll(index, Arrays.asList(items))
    }

    SpinJsonNodeProxy split(Closure closure) {
        def result = getWrappedElements().split closure
        result = result.collect { JsonProxyUtils.wrap(it) }
        return JsonProxyUtils.wrap(result)
    }

    SpinJsonNodeProxy combinations() {
        if (isList()) {
            def result = getWrappedList().collect { it.value() }.combinations()
            return JsonProxyUtils.wrap(result.collect { JsonProxyUtils.wrap(it) })
        } else {
            throw new UnsupportedOperationException("Not supported for non array types")
        }
    }

    SpinJsonNodeProxy combinations(Closure closure) {
        if (isList()) {
            def result = getWrappedList().collect { it.value() }.combinations closure
            return JsonProxyUtils.wrap(result.collect { JsonProxyUtils.wrap(it) })
        } else {
            throw new UnsupportedOperationException("Not supported for non array types")
        }
    }

    SpinJsonNodeProxy subsequences() {
        if (isList()) {
            def result = getWrappedList().subsequences()
            result = result.collect { JsonProxyUtils.wrap(it) }
            return JsonProxyUtils.wrap(result)
        } else {
            throw new UnsupportedOperationException("Not supported for non array types")
        }
    }

    SpinJsonNodeProxy permutations() {
        if (isList()) {
            def result = getWrappedList().permutations()
            result = result.collect { JsonProxyUtils.wrap(it) }
            return JsonProxyUtils.wrap(result)
        } else {
            throw new UnsupportedOperationException("Not supported for non array types")
        }
    }

    SpinJsonNodeProxy permutations(Closure closure) {
        if (isList()) {
            def result = getWrappedList().permutations closure
            result = result.collect { JsonProxyUtils.wrap(it) }
            return JsonProxyUtils.wrap(result)
        } else {
            throw new UnsupportedOperationException("Not supported for non array types")
        }
    }

    SpinJsonNodeProxy eachPermutation(Closure closure) {
        if (isList()) {
            def result = getWrappedList().eachPermutation closure
            return JsonProxyUtils.wrap(result)
        } else {
            throw new UnsupportedOperationException("Not supported for non array types")
        }
    }

    SpinJsonNodeProxy transpose() {
        if (isList()) {
            def result = getWrappedList().collect { it.value() }.transpose()
            return JsonProxyUtils.wrap(result)
        } else {
            throw new UnsupportedOperationException("Not supported for non array types")
        }
    }

    SpinJsonNodeProxy groupBy(Closure closure) {
        def result = getWrappedElements().groupBy closure
        result = result.collectEntries { [it.key, JsonProxyUtils.wrap(it.value)] }
        return JsonProxyUtils.wrap(result)
    }

    SpinJsonNodeProxy groupBy(Object... closures) {
        def result = getWrappedElements().groupBy(closures)
        return groupByClosuresWrapper(result)
    }

    SpinJsonNodeProxy groupBy(List<Closure> closures) {
        def result = getWrappedElements().groupBy(closures)
        return groupByClosuresWrapper(result)
    }

    private SpinJsonNodeProxy groupByClosuresWrapper(Map map) {
        def result =
                map.collectEntries {
                    [it.key,
                     it.value instanceof Map ?
                             groupByClosuresWrapper(it.value as Map) :
                             JsonProxyUtils.wrap(it.value)]
                }
        return JsonProxyUtils.wrap(result)
    }

    SpinJsonNodeProxy countBy(Closure closure) {
        def result = getWrappedElements().countBy closure
        return JsonProxyUtils.wrap(result)
    }

    SpinJsonNodeProxy groupEntriesBy(Closure closure) {
        if (isMap()) {
            def result = getWrappedMap().groupEntriesBy closure
            return JsonProxyUtils.wrap(result)
        } else {
            throw new UnsupportedOperationException("Not supported for non map types")
        }
    }

    SpinJsonNodeProxy inject(Closure closure) {
        def result = getWrappedElements().inject closure
        return JsonProxyUtils.wrap(result)
    }

    SpinJsonNodeProxy inject(Object initialValue, Closure closure) {
        def result = getWrappedElements().inject(JsonProxyUtils.wrap(initialValue), closure)
        return JsonProxyUtils.wrap(result)
    }

    Object sum() {
        if (isList()) {
            def result = getWrappedList().collect { it.isValue() ? it.value() : new SpinJsonNodeProxy(it) }
            return result.sum()
        } else {
            throw new UnsupportedOperationException("Not supported for non array types")
        }
    }

    Object sum(Object initialValue) {
        if (isList()) {
            def result = getWrappedList().collect { it.isValue() ? it.value() : new SpinJsonNodeProxy(it) }
            return result.sum(initialValue)
        } else {
            throw new UnsupportedOperationException("Not supported for non array types")
        }
    }

    Object sum(Closure closure) {
        if (isList()) {
            def result = getWrappedList().collect { it.isValue() ? it.value() : new SpinJsonNodeProxy(it) }
            return result.sum(closure)
        } else {
            throw new UnsupportedOperationException("Not supported for non array types")
        }
    }

    Object sum(Object initialValue, Closure closure) {
        if (isList()) {
            def result = getWrappedList().collect { it.isValue() ? it.value() : new SpinJsonNodeProxy(it) }
            return result.sum(initialValue, closure)
        } else {
            throw new UnsupportedOperationException("Not supported for non array types")
        }
    }

    String join(String separator) {
        if (isList()) {
            def result = getWrappedList().collect { it.isValue() ? it.value() : new SpinJsonNodeProxy(it) }
            return result.join(separator)
        } else {
            throw new UnsupportedOperationException("Not supported for non array types")
        }
    }

    SpinJsonNodeProxy min() {
        if (isList()) {
            return getWrappedList().min()
        } else {
            throw new UnsupportedOperationException("Not supported for non array types")
        }
    }

    SpinJsonNodeProxy min(Comparator comparator) {
        if (isList()) {
            return getWrappedList().min(comparator)
        } else {
            throw new UnsupportedOperationException("Not supported for non array types")
        }
    }

    SpinJsonNodeProxy min(Closure closure) {
        def result = getWrappedElements().min closure
        return JsonProxyUtils.wrap(result)
    }

    SpinJsonNodeProxy max() {
        if (isList()) {
            return getWrappedList().max()
        } else {
            throw new UnsupportedOperationException("Not supported for non array types")
        }
    }

    SpinJsonNodeProxy max(Comparator comparator) {
        if (isList()) {
            return getWrappedList().max(comparator)
        } else {
            throw new UnsupportedOperationException("Not supported for non array types")
        }
    }

    SpinJsonNodeProxy max(Closure closure) {
        def result = getWrappedElements().max closure
        return JsonProxyUtils.wrap(result)
    }

    @JsonIgnore
    IntRange getIndices() {
        if (isList()) {
            return getWrappedList().getIndices()
        } else {
            throw new UnsupportedOperationException("Not supported for non array types")
        }
    }

    int size() {
        return isList() || isMap() ? elements().size() : 0
    }

    SpinJsonNodeProxy subMap(Collection keys) {
        if (isMap()) {
            return JsonProxyUtils.wrap(getWrappedMap().subMap(keys))
        } else {
            throw new UnsupportedOperationException("Not supported for non map types")
        }
    }

    SpinJsonNodeProxy subMap(Object[] keys) {
        if (isMap()) {
            return JsonProxyUtils.wrap(getWrappedMap().subMap(keys))
        } else {
            throw new UnsupportedOperationException("Not supported for non map types")
        }
    }

    SpinJsonNodeProxy get(Object key, Object defaultValue) {
        if (isMap()) {
            return getWrappedMap().get(key, JsonProxyUtils.wrap(defaultValue))
        } else {
            throw new UnsupportedOperationException("Not supported for non map types")
        }
    }

    SpinJsonNodeProxy sort() {
        return JsonProxyUtils.wrap(getWrappedElements().sort())
    }

    SpinJsonNodeProxy sort(Closure closure) {
        def result = getWrappedElements().sort closure
        return JsonProxyUtils.wrap(result)
    }

    SpinJsonNodeProxy sort(Comparator comparator) {
        return JsonProxyUtils.wrap(getWrappedElements().sort(comparator))
    }

    SpinJsonNodeProxy sort(boolean mutate) {
        if (isList()) {
            def list = getWrappedList()
            def result = list.sort(mutate)
            if (mutate) {
                setWrappedList(list)
            }
            return JsonProxyUtils.wrap(result)
        } else {
            throw new UnsupportedOperationException("Not supported for non array types")
        }
    }

    SpinJsonNodeProxy sort(boolean mutate, Closure closure) {
        if (isList()) {
            def list = getWrappedList()
            def result = list.sort(mutate, closure)
            if (mutate) {
                setWrappedList(list)
            }
            return JsonProxyUtils.wrap(result)
        } else {
            throw new UnsupportedOperationException("Not supported for non array types")
        }
    }

    SpinJsonNodeProxy sort(boolean mutate, Comparator comparator) {
        if (isList()) {
            def list = getWrappedList()
            def result = list.sort(mutate, comparator)
            if (mutate) {
                setWrappedList(list)
            }
            return JsonProxyUtils.wrap(result)
        } else {
            throw new UnsupportedOperationException("Not supported for non array types")
        }
    }

    SpinJsonNodeProxy toSorted() {
        return JsonProxyUtils.wrap(getWrappedElements().toSorted())
    }

    SpinJsonNodeProxy toSorted(Comparator comparator) {
        return JsonProxyUtils.wrap(getWrappedElements().toSorted(comparator))
    }

    SpinJsonNodeProxy toSorted(Closure condition) {
        def result = getWrappedElements().toSorted condition
        return JsonProxyUtils.wrap(result)
    }

    SpinJsonNodeProxy pop() {
        if (isList()) {
            def elements = getWrappedList()
            def result = elements.pop()
            setWrappedList(elements)
            return result
        } else {
            throw new UnsupportedOperationException("Not supported for non array types")
        }
    }

    SpinJsonNodeProxy last() {
        if (isList()) {
            return getWrappedList().last()
        } else {
            throw new UnsupportedOperationException("Not supported for non array types")
        }
    }

    SpinJsonNodeProxy first() {
        if (isList()) {
            return getWrappedList().first()
        } else {
            throw new UnsupportedOperationException("Not supported for non array types")
        }
    }

    SpinJsonNodeProxy head() {
        if (isList()) {
            return getWrappedList().head()
        } else {
            throw new UnsupportedOperationException("Not supported for non array types")
        }
    }

    SpinJsonNodeProxy tail() {
        if (isList()) {
            return JsonProxyUtils.wrap(getWrappedList().tail())
        } else {
            throw new UnsupportedOperationException("Not supported for non array types")
        }
    }

    SpinJsonNodeProxy init() {
        if (isList()) {
            return JsonProxyUtils.wrap(getWrappedList().init())
        } else {
            throw new UnsupportedOperationException("Not supported for non array types")
        }
    }

    SpinJsonNodeProxy take(int num) {
        return JsonProxyUtils.wrap(getWrappedElements().take(num))
    }

    SpinJsonNodeProxy takeRight(int num) {
        if (isList()) {
            return JsonProxyUtils.wrap(getWrappedList().takeRight(num))
        } else {
            throw new UnsupportedOperationException("Not supported for non array types")
        }
    }

    SpinJsonNodeProxy takeWhile(Closure condition) {
        def result = getWrappedElements().takeWhile condition
        return JsonProxyUtils.wrap(result)
    }

    SpinJsonNodeProxy drop(int num) {
        return JsonProxyUtils.wrap(getWrappedElements().drop(num))
    }

    SpinJsonNodeProxy dropRight(int num) {
        if (isList()) {
            return JsonProxyUtils.wrap(getWrappedList().dropRight(num))
        } else {
            throw new UnsupportedOperationException("Not supported for non array types")
        }
    }

    SpinJsonNodeProxy dropWhile(Closure condition) {
        def result = getWrappedElements().dropWhile condition
        return JsonProxyUtils.wrap(result)
    }

    SpinJsonNodeProxy reverse() {
        if (isList()) {
            return JsonProxyUtils.wrap(getWrappedList().reverse())
        } else {
            throw new UnsupportedOperationException("Not supported for non array types")
        }
    }

    SpinJsonNodeProxy reverse(boolean mutate) {
        if (isList()) {
            def elements = getWrappedList()
            def result = elements.reverse(mutate)
            if (mutate) {
                setWrappedList(elements)
            }
            return JsonProxyUtils.wrap(result)
        } else {
            throw new UnsupportedOperationException("Not supported for non array types")
        }
    }

    SpinJsonNodeProxy intersect(Iterable items) {
        if (items == null) {
            return this
        }
        def result = getWrappedElements().intersect(JsonProxyUtils.wrap(items).getWrappedElements())
        return JsonProxyUtils.wrap(result)
    }

    SpinJsonNodeProxy intersect(Object obj) {
        if (obj == null) {
            return this
        }
        def result = getWrappedElements().intersect(JsonProxyUtils.wrap(obj).getWrappedElements())
        return JsonProxyUtils.wrap(result)
    }

    boolean disjoint(Iterable items) {
        if (items == null) {
            return false
        }
        if (isList()) {
            return getWrappedList().disjoint(items.asList().collect { JsonProxyUtils.wrap(it) })
        } else {
            throw new UnsupportedOperationException("Not supported for non array types")
        }
    }

    SpinJsonNodeProxy flatten(Closure condition) {
        throw new UnsupportedOperationException("Not supported yet")
        //TODO should implement Collection
        /*if (isList()) {
            def result = getWrappedList().flatten condition
            return JsonProxyUtils.wrap(result)
        } else {
            throw new UnsupportedOperationException("Not supported for non array types")
        }*/
    }

    SpinJsonNodeProxy flatten() {
        throw new UnsupportedOperationException("Not supported yet")
        //TODO should implement Collection
        /*if (isList()) {
            def result = getWrappedList().flatten()
            return JsonProxyUtils.wrap(result)
        } else {
            throw new UnsupportedOperationException("Not supported for non array types")
        }*/
    }

    SpinJsonNodeProxy leftShift(Object value) {
        if (value == null) {
            return this
        }
        if (isList()) {
            value = JsonProxyUtils.wrap(value)
        } else if (isMap()) {
            value = value.collectEntries { [it.key, JsonProxyUtils.wrap(it.value)] }
        } else {
            throw new UnsupportedOperationException("Not supported for non iterable types")
        }
        def elements = getWrappedElements()
        elements.leftShift(value)
        setWrappedElements(elements)
        return this
    }

    boolean contains(Object obj) {
        if (isList()) {
            return getWrappedList().contains(JsonProxyUtils.wrap(obj))
        } else {
            throw new UnsupportedOperationException("Not supported for non array types")
        }
    }

    SpinJsonNodeProxy swap(int i, int j) {
        if (isList()) {
            def elements = getWrappedList()
            def result = elements.swap(i, j)
            setWrappedList(elements)
            return JsonProxyUtils.wrap(result)
        } else {
            throw new UnsupportedOperationException("Not supported for non array types")
        }
    }

    SpinJsonNodeProxy removeAt(int index) {
        if (isList()) {
            def elements = getWrappedList()
            def result = elements.removeAt(index)
            setWrappedList(elements)
            return JsonProxyUtils.wrap(result)
        } else {
            throw new UnsupportedOperationException("Not supported for non array types")
        }
    }

    boolean removeElement(Object obj) {
        if (isList()) {
            def elements = getWrappedList()
            def result = elements.removeElement(JsonProxyUtils.wrap(obj))
            if (result) {
                setWrappedList(elements)
            }
            return result
        } else {
            throw new UnsupportedOperationException("Not supported for non array types")
        }
    }

    boolean isEmpty() {
        return size() == 0
    }

    // --------- supported List methods --------- //
    boolean add(Object item) {
        if (item == null) {
            return false
        }
        if (isList()) {
            def elements = getWrappedList()
            def result = elements.add(JsonProxyUtils.wrap(item))
            if (result) {
                setWrappedList(elements)
            }
            return result
        } else {
            throw new UnsupportedOperationException("Not supported for non array types")
        }
    }

    boolean containsAll(Object[] items) {
        if (isList()) {
            return getWrappedList().containsAll(JsonProxyUtils.wrap(items).getWrappedElements())
        } else {
            throw new UnsupportedOperationException("Not supported for non array types")
        }
    }

    boolean containsAll(Collection<Object> coll) {
        if (isList()) {
            return getWrappedList().containsAll(JsonProxyUtils.wrap(coll).getWrappedElements())
        } else {
            throw new UnsupportedOperationException("Not supported for non array types")
        }
    }

    SpinJsonNodeProxy get(Object key) {
        if (!exists()) {
            return JsonProxyUtils.wrap(null)
        }
        if (isList()) {
            if (!(key instanceof Integer)) {
                throw new IllegalArgumentException("Not supported index type")
            }
            return getWrappedList().get(key)
        }
        if (isMap()) {
            return getWrappedMap().get(key)
        }
        throw new UnsupportedOperationException("Not supported for non iterable types")
    }

    SpinJsonNodeProxy set(int index, Object item) {
        if (isList()) {
            def elements = getWrappedList()
            def result = elements.set(index, JsonProxyUtils.wrap(item))
            if (result) {
                setWrappedList(elements)
            }
            return result
        } else {
            throw new UnsupportedOperationException("Not supported for non array types")
        }
    }

    void add(int index, Object item) {
        if (isList()) {
            def elements = getWrappedList()
            elements.add(index, JsonProxyUtils.wrap(item))
            setWrappedList(elements)
        } else {
            throw new UnsupportedOperationException("Not supported for non array types")
        }
    }

    SpinJsonNodeProxy remove(int index) {
        return removeAt(index)
    }

    List<SpinJsonNodeProxy> subList(int fromIndex, int toIndex) {
        if (isList()) {
            return getWrappedList().subList(fromIndex, toIndex)
        } else {
            throw new UnsupportedOperationException("Not supported for non array types")
        }
    }

    // --------- support Map methods --------- //
    boolean containsKey(String key) {
        if (isMap()) {
            return getWrappedMap().containsKey(key)
        } else {
            throw new UnsupportedOperationException("Not supported for non map types")
        }
    }

    boolean containsValue(Object value) {
        if (isMap()) {
            return getWrappedMap().containsValue(JsonProxyUtils.wrap(value))
        } else {
            throw new UnsupportedOperationException("Not supported for non map types")
        }
    }

    SpinJsonNodeProxy put(String key, Object value) {
        if (isMap()) {
            def elements = getWrappedMap()
            def result = elements.put(key, JsonProxyUtils.wrap(value))
            setWrappedMap(elements)
            return JsonProxyUtils.wrap(result)
        } else {
            throw new UnsupportedOperationException("Not supported for non map types")
        }
    }

    void putAll(Collection<? extends Map.Entry> entries) {
        if (isMap()) {
            def elements = getWrappedMap()
            elements.putAll(entries.collectEntries { [it.key, JsonProxyUtils.wrap(it.value)] })
            setWrappedMap(elements)
        } else {
            throw new UnsupportedOperationException("Not supported for non map types")
        }
    }

    Set<String> keySet() {
        if (isMap()) {
            return context.unwrap().fields().collect { it.key }.toSet()
        } else {
            throw new UnsupportedOperationException("Not supported for non map types")
        }
    }

    Collection<SpinJsonNodeProxy> values() {
        if (isMap()) {
            return getWrappedMap().values()
        } else {
            throw new UnsupportedOperationException("Not supported for non map types")
        }
    }

    Set<Map.Entry<String, SpinJsonNodeProxy>> entrySet() {
        if (isMap()) {
            return getWrappedMap().entrySet()
        } else {
            throw new UnsupportedOperationException("Not supported for non map types")
        }
    }
}
