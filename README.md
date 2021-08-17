# dpm-json
Json handling and manipulation tool for easy use in Camunda processes with [Groovy](https://groovy-lang.org/syntax.html).

<table border="0">
 <tr>
    <td><b style="font-size:30px">Javascript spin</b></td>
    <td><b style="font-size:30px">dpmJson</b></td>
 </tr>
 <tr>
   <td>
     <pre><code>
def myVar = dpmJson.read([1,2,3,4])
myVar.isArray() // true
myVar[2] // 3 as Spin object
myVar[2].value() // 3 as number
        </code>
      </pre>
   </td>
    <td>
      <pre><code>
def myVar = dpmJson.read([1,2,3,4])
myVar.isArray() // true
myVar[2] // 3 as Spin object
myVar[2].value() // 3 as number
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

