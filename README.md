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
Add to the pom.xml file of your Camunda application the following dependencies:

```
<dependencies>
            ...
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
                <groupId>org.codehaus.groovy</groupId>
                <artifactId>groovy-all</artifactId>
                <version>2.4.15</version>
            </dependency>
            <dependency>
                <groupId>org.frox.dpm</groupId>
                <artifactId>dpm-json</artifactId>
                <version>1.0.0</version>
            </dependency>
<dependencies>
```

## Usage
dpmJson is available for every script section in Camunda that supports Groovy, be it a Script task, or a script in the Input/Output section of any task type. Entry point for the usage of its features is to wrap a 
- Spin JSON Object
- LinkedHashMap (or any Map)
- ArrayList (or any List)
- JSON string 
with the command
```
dpmJson.read(myJson)
```
which will return a wrapped Spin Json object. The wrapped spin object will automatically be stored as a spin json variable without further serialization steps.

### Accessing attributes
