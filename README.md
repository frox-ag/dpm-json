# dpm-json
Json handling and manipulation tool for easy use in Camunda scripts with [Groovy](https://groovy-lang.org/syntax.html). All dpmJson functions can be accessed after wrapping a JSON-String, a Map, a List or a Camunda-Spin object with dpmJson.read(myObject). The returned object is a wrapped Camunda-spin variable with additional functions.

Below a example of the same logic written in Javascript with Spin and with Groovy-dpmJson. Both examples return a list containing all even numbers of the following list:
```
def myList = [0, 1, 2, 3]
```

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
// returns [‘0’, ‘2’]
        </code>
      </pre>
   </td>
    <td>
      <pre><code>
dpmJson(myList).findAll{it % 2 == 0}
// returns [0, 2]
        </code>
      </pre>
   </td>
 </tr>
</table>

- [dpm-json](#dpm-json)
  * [Get started](#get-started)
  * [Usage](#usage)
    + [👉 Accessing attributes](#accessing-attributes)
    + [👉 Assigning attributes](#assigning-attributes)
    + [👉 Verifying attribute existence](#verifying-attribute-existence)
    + [👉 List operations](#list-operations)
    + [👉 Map operations](#map-operations)
  * [Roadmap](#roadmap)

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
- 👉 LinkedHashMap (or any Map, including Groovy Map)
```
  def myMap = [myKey: "myValue"]
  dpmJson(myMap)
```
- 👉 ArrayList (or any List, including Groovy List)
```
  def myList = [0,1,2,3]
  dpmJson(myList)
```
- 👉 JSON string 
```
  def myJsonString = '{ "myKey": "myValue" }'
  dpmJson(myJsonString)
```
- 👉 Spin JSON Object
```
  dpmJson(mySpinJsonObject)
```
- 👉 No parameter
```
  dpmJson() // returns {}
```
Initializing a dpmJson object will return a wrapped Spin JSON object. The wrapped spin object will automatically be stored as a spin JSON variable in the Camunda engine *without* further serialization steps.

TODO Note: depending on the architecture of your Camunda application the API call may differ...

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
dpmJson(jsonObj).myMap
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

To get the unwrapped value of this key it is necessary to add *.value()* after the expression:
```
dpmJson(jsonObj).myMap.myKey.value()
```
This will return a string containing "myValue".

It is also possible to access values with variable keys as follows:
```
def key = "myMap"
dpmJson(jsonObj)[key].value()
```

### Assigning attributes

It is poissible to assign values to keys as follows:
```
dpmJson(jsonObj).myMap.myKey.mySecondKey = "myNewValue"
```
In this case *mySecondKey* does not exist in advance and will be created. The previous value of myKey will be overwritten.
```
{
  myMap: { 
    myKey: {
      mySecondKey: "myNewValue"
    } 
  },
  myList: [0,1,2,3]
{
```

It is also possible to assign values with variable keys as follows:
```
def key = "myMap"
dpmJson(jsonObj)[key] = "myNewValue"
```

### Verifying attribute existence

It is possible to check easily for attribute existence by adding *.exists()*. If the attribute does not exist, no exception is thrown but *false* is returned. This allows fail-safe scripting.
```
dpmJson(jsonObj).myMap.myKey.myInexistingKey.exists()
```
Note: this does **not** work for array accesses like
```
dpmJson(jsonObj).myMap.myKey[2].value()
```
since myKey is not of type List.

### List operations

dpmJson allows to use many List operations. To verify if an attribute is of type List, add *.isList()* after your attribute chain. This will return a boolean. 
```
dpmJson(jsonObj).myList.isList() // true
dpmJson(jsonObj).myMap.isList() // false
```
Furthermore, it is possible to use common List functions like
```
dpmJson(jsonObj).myList.size() // 4
dpmJson(jsonObj).myList.clear() // clears the content of the list
dpmJson(jsonObj).myList.elements() // returns a SpinJson List that can be used e.g. in Camunda Loops
dpmJson(jsonObj).myList.push(someObject) // adds a new object to the list (accepts Groovy List, Groovy Map and dpmJson objects)
dpmJson(jsonObj).myList[2] // gets the third element in the list. Note: throws exception if index is out of bounds
dpmJson(jsonObj).myList[2] = "myNewValue" // assigns "myNewValue" to the third element of the list. Note: throws exception if index is out of bounds
```

It is possible to iterate in many ways over Lists. The classic for loop:
```
def list = dpmJson(jsonObj).myList
for(val in list) {
   ... your logic here ...
}
```
With Groovy's List iterators:
```
def list = dpmJson(jsonObj).myList
list.each { it -> println(it) } // Iterate over all elements in list. For multiline logic in lambda
list.each { println(it) } // static lambda declaration, where 'it' is implicitly the iteratee. Suggested for single line scripts
list.eachWithIndex { it, i -> println(it); println(i); } // iteration with additional index variable
list.collect { it.myKey.value() } // remaps the List with the defined logic. 
list.sort { it.number.value() } // Sorts list in ascending order. 
list.find { it.value() % 2 == 0 } // finds first element for which the given lambda returns true. 
list.findAll { it.value() % 2 == 0 } // finds all elements for which the given lambda returns true. 
```

### Map operations

dpmJson allows to use many Map operations. To verify if an attribute is of type Map, add *.isMap()* after your attribute chain. This will return a boolean. 
```
dpmJson(jsonObj).myMap.isMap() // true
dpmJson(jsonObj).myList.isMap() // false
```
Furthermore, it is possible to use common Map functions like
```
dpmJson(jsonObj).myMap.size() // 1
dpmJson(jsonObj).myMap.clear() // clears the content of the map
dpmJson(jsonObj).myMap.elements() // returns a SpinJson List that can be used e.g. in Camunda Loops
dpmJson(jsonObj).myMap.myKey // gets the value for the key "myKey"
dpmJson(jsonObj).myMap["myKey"] // gets the value for the key "myKey". Note: you can use variable keys with this accessor
dpmJson(jsonObj).myMap.myKey = "myNewValue" // sets the value for the key "myKey"
dpmJson(jsonObj).myMap["myKey"] = "myNewValue" // sets the value for the key "myKey". Note: you can use variable keys with this accessor
```

It is possible to iterate in many ways over Maps. The classic for each loop:
```
def map = dpmJson(jsonObj).myMap
for(val in map) {
   ... your logic here ...
}
```
With Groovy's List iterators:
```
def map = dpmJson(jsonObj).myMap
map.each { it -> println(it) } // Iterate over all elements in list. For multiline logic in lambda
map.each { println(it) } // static lambda declaration, where 'it' is implicitly the iteratee. Suggested for single line scripts
map.eachWithIndex { it, i -> println(it.value); println(it.key); println(i); } // iteration with additional index variable
map.collect { it.myKey.value() } // remaps the Map with the defined logic. 
map.sort{ it.number.value() } // Sorts map in descending order. 
map.find { it.value() % 2 == 0 } // finds first element for which the given lambda returns true. 
map.findAll { it.value() % 2 == 0 } // finds all elements for which the given lambda returns true. 
```

## Roadmap
- [ ] Support in Camunda expressions
- [x] List concat function
- [x] Groovy Iterators return dpmJson wrappers
