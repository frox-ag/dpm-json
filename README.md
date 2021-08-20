# dpm-json
Json handling and manipulation tool for easy use in Camunda scripts with [Groovy](https://groovy-lang.org/syntax.html). All dpmJson functions can be accessed after wrapping a JSON-String, a Map, a List or a Camunda-Spin object with dpmJson.read(myObject). The returned object is a wrapped Camunda-spin variable with additional functions.

Below a example of the same logic written in Javascript with Spin and with Groovy-dpmJson:

<table border="0">
 <tr>
    <td><b style="font-size:30px">Javascript spin</b></td>
    <td><b style="font-size:30px">Groovy dpmJson</b></td>
 </tr>
 <tr>
   <td>
     <pre><code>
var parsedList = JSON.parse(myList)
var filteredList = []
for(var it in parsedList) {
   if(it % 2 === 0) {
       filteredList.push(it)
   }
}
S(JSON.stringify(filteredList))
        </code>
      </pre>
   </td>
    <td>
      <pre><code>
def list = dpmJson.read(myList).findAll{it % 2 == 0}
dpmJson.read(list)
        </code>
      </pre>
   </td>
 </tr>
</table>

## Get started
Add to the pom.xml file of your Java-based Camunda application the following settings:

```
<build>
    <plugins>
        <plugin>
            <groupId>org.apache.maven.plugins</groupId>
            <artifactId>maven-compiler-plugin</artifactId>
            <configuration>
                <compilerId>groovy-eclipse-compiler</compilerId>
                <source>${java.version}</source>
                <target>${java.version}</target>
                <verbose>true</verbose>
                <fork>true</fork>
                <compilerArguments>
                    <javaAgentClass>lombok.launch.Agent</javaAgentClass>
                </compilerArguments>
                <annotationProcessorPaths>
                    <path>
                        <groupId>org.projectlombok</groupId>
                        <artifactId>lombok</artifactId>
                        <version>${lombok.version}</version>
                    </path>
                </annotationProcessorPaths>
            </configuration>
            <dependencies>
                <dependency>
                    <groupId>org.codehaus.groovy</groupId>
                    <artifactId>groovy-eclipse-compiler</artifactId>
                    <version>3.6.0-03</version>
                </dependency>
                <dependency>
                    <groupId>org.codehaus.groovy</groupId>
                    <artifactId>groovy-eclipse-batch</artifactId>
                    <version>${groovy.version}-01</version>
                </dependency>
                <dependency>
                    <groupId>org.projectlombok</groupId>
                    <artifactId>lombok</artifactId>
                    <version>${lombok.version}</version>
                </dependency>
                <dependency>
                    <groupId>org.camunda.bpm</groupId>
                    <artifactId>camunda-engine-plugin-spin</artifactId>
                    <version>7.14.0</version>
                </dependency>
                <dependency>
                    <groupId>org.camunda.spin</groupId>
                    <artifactId>camunda-spin-core</artifactId>
                    <version>1.7.5</version>
                </dependency>
                <dependency>
                    <groupId>org.camunda.spin</groupId>
                    <artifactId>camunda-spin-dataformat-all</artifactId>
                    <version>1.7.5</version>
                </dependency>
                <dependency>
                    <groupId>org.frox.dpm</groupId>
                    <artifactId>dpm-json</artifactId>
                    <version>1.0.0</version>
                </dependency>
            </dependencies>
        </plugin>
    </plugins>
</build>
```

## Usage
dpmJson is available for every script section in Camunda that supports Groovy, be it a Script task, or a script in the Input/Output section of any task type. Entry point for the usage of its features is to wrap a supported input object with *dpmJson.read(myObject)*.
Supported data types are:
- LinkedHashMap (or any Map, including Groovy Map)
```
  def myMap = [myKey: "myValue"]
  dpmJson.read(myMap)
```
- ArrayList (or any List, including Groovy List)
```
  def myList = [0,1,2,3]
  dpmJson.read(myList)
```
- JSON string 
```
  def myJsonString = "{ \"myKey\": \"myValue\" }"
  dpmJson.read(myJsonString)
```
- Spin JSON Object
```
  dpmJson.read(mySpinJsonObject)
```
Initializing a dpmJson object will return a wrapped Spin Json object. The wrapped spin object will automatically be stored as a spin json variable in the Camunda engine *without* further serialization steps.

### Accessing attributes

For a JSON Object with the following structure
```
def jsonObj = [
                myMap: [ myKey: "myValue" ],
                myList: [0,1,2,3]
              ]
```
it is possible to access attributes as follows:
```
dpmJson.read(jsonObj).myMap
```
This will return another wrapped Spin-JSON object with the key *myObj* as root.
Note: the definition of the jsonObj might be unintuitive for experienced JSON users. In Groovy a Map is not declared as e.g. in javascript:
```
{ myKey: "myValue" }
```
but
```
[ myKey: "myValue" ]
```

To get the deserialized value of this key it is necessary to add *.value()* after the expression:
```
dpmJson.read(jsonObj).myMap.myKey.value()
```
This will return a string containing "myValue".

### Verifying attribute existence

It is possible to check easily for attribute existence by adding *.exists()*. If the attribute does not exist, no exception is thrown but *false* is returned. This allows fail-safe scripting.
```
dpmJson.read(jsonObj).myMap.myKey.myInexistingKey.exists()
```
Note: this does **not** work for array accesses like
```
dpmJson.read(jsonObj).myMap.myKey[2].value()
```
since myKey is not of type List.

### List operations

dpmJson allows to use many List operations. To verify if a attribute is of type List, add *.isList()* after your attribute chain. This will return a boolean. 
```
dpmJson.read(jsonObj).myList.isList() // true
dpmJson.read(jsonObj).myMap.isList() // false
```
Furthermore, it is possible to use common List functions like
```
dpmJson.read(jsonObj).myList.size() // 4
dpmJson.read(jsonObj).myList.clear() // clears the content of the list
dpmJson.read(jsonObj).myList.elements() // returns a SpinJson List that can be used e.g. in Camunda Loops
dpmJson.read(jsonObj).myList.push(someObject) // adds a new object to the list (accepts Groovy List, Groovy Map and dpmJson objects)
dpmJson.read(jsonObj).myList[2] // gets the third element in the list. Note: throws exception if index is out of bounds
dpmJson.read(jsonObj).myList[2] = "myNewValue" // assigns "myNewValue" to the third element of the list. Note: throws exception if index is out of bounds
```

It is possible to iterate in many ways over Lists. The classic for loop:
```
def list = dpmJson.read(jsonObj).myList
for(val in list) {
   ... your logic here ...
}
```
With Groovy's List iterators:
```
def list = dpmJson.read(jsonObj).myList
list.each { it -> println(it) } // for multiline logic
list.each { println(it) } // static lambda declaration, where *it* is implicitly the iteratee
list.eachWithIndex { it, i -> println(it); println(i); } // iteration with additional index variable
list.collect { it.myKey } // remaps the List with the defined logic. Note: returns ArrayList
list.sort { a, b -> a - b } // Sorts list in ascending order. Note: returns ArrayList
list.find { it % 2 == 0 } // finds first element for which the given lambda returns true. Note: returns deserialized Data value
list.findAll { it % 2 == 0 } // finds all elements for which the given lambda returns true. Note: returns ArrayList containing deserialized Data values
```

**TODO: list.concat**

### Map operations

dpmJson allows to use many Map operations. To verify if a attribute is of type Map, add *.isMap()* after your attribute chain. This will return a boolean. 
```
dpmJson.read(jsonObj).myMap.isMap() // true
dpmJson.read(jsonObj).myList.isMap() // false
```
Furthermore, it is possible to use common Map functions like
```
dpmJson.read(jsonObj).myMap.size() // 1
dpmJson.read(jsonObj).myMap.clear() // clears the content of the map
dpmJson.read(jsonObj).myMap.elements() // returns a SpinJson List that can be used e.g. in Camunda Loops
dpmJson.read(jsonObj).myMap.myKey // gets the value for the key "myKey"
dpmJson.read(jsonObj).myMap["myKey"] // gets the value for the key "myKey". Note: you can use variable keys with this accessor
dpmJson.read(jsonObj).myMap.myKey = "myNewValue" // sets the value for the key "myKey"
dpmJson.read(jsonObj).myMap["myKey"] = "myNewValue" // sets the value for the key "myKey". Note: you can use variable keys with this accessor
```

It is possible to iterate in many ways over Maps. Note that the iteratee is a LinkedHashMap that contains the key and the value. It is possible to access the key by adding *.key* and the value by adding *.value*.
The classic for loop:
```
def map = dpmJson.read(jsonObj).myMap
for(val in map) {
   println(val.key)
   println(val.value)
   ... your logic here ...
}
```
With Groovy's List iterators:
```
def map = dpmJson.read(jsonObj).myMap
map.each { it -> println(it) } // for multiline logic
map.each { println(it) } // static lambda declaration, where *it* is implicitly the iteratee
map.eachWithIndex { it, i -> println(it.value); println(it.key); println(i); } // iteration with additional index variable
map.collect { it.myKey } // remaps the Map with the defined logic. Note: returns ArrayList
map.sort{ a, b -> b.value - a.value } // Sorts map in descending order. Note: returns LinkedHashMap
map.find { it % 2 == 0 } // finds first element for which the given lambda returns true. Note: returns LinkedHashMap with key and deserialized value
map.findAll { it % 2 == 0 } // finds all elements for which the given lambda returns true. Note: returns ArrayList containing LinkedHashMaps with key and deserialized values
```

### Additional functions
It is possible to deep-stringify all dpmJson wrapped objects:
```
def map = dpmJson.read(jsonObj).myMap.toString() // "{ \"myKey\": \"myValue\" }"
```

## Roadmap
- [] Support in Camunda expressions
- [] List concat function
- [] Iterators return dpmJson wrappers
