JaCoCo Java Code Coverage Library reviewed for Maven multi-module aggregation
=============================================================================

# Why
As discussed [here](https://github.com/jacoco/jacoco/issues/974), creating an aggregation of Jacoco reports can be painful when dealing with a multi-modules Maven project, since it requires to create a module dedicated to it and add reported modules as a dependency of it. Although [a proposition](https://github.com/jacoco/jacoco/pull/1251) was made, it is not in Jacoco Team roadmap. So this project is made to fix it.

# How to use
Replace your jacoco-maven-plugin report-aggregate execution...
```xml
<executions>
    <execution>
        <id>report-aggregate</id>
        <goals>
            <goal>report-aggregate</goal>
        </goals>
        <phase>verify</phase>
    </execution>
</executions>
```
... by the one of this project
```xml
<plugin>
    <groupId>org.codefilarete</groupId>
    <artifactId>jacoco-aggregate-submodule</artifactId>
    <version>${codefilarete.jacoco.version}</version>
    <executions>
        <execution>
            <id>report-aggregate</id>
            <goals>
                <goal>report-aggregate</goal>
            </goals>
            <phase>verify</phase>
        </execution>
    </executions>
</plugin>
```

This module doesn't handle all features made by Jacoco plugin : it handles only aggregation for submodules so you have to keep all other goals you have in your pom.xml which may finally look like :
```xml
<plugin>
    <groupId>org.jacoco</groupId>
    <artifactId>jacoco-maven-plugin</artifactId>
    <version>${jacoco.version}</version>
    <executions>
        <execution>
            <id>prepare-agent</id>
            <goals>
                <goal>prepare-agent</goal>
            </goals>
        </execution>
        <execution>
            <id>report</id>
            <goals>
                <goal>report</goal>
            </goals>
        </execution>
        <!-- the following is replaced by Codefilarete plugin below -->
        <!--<execution>-->
        <!--<id>report-aggregate</id>-->
        <!--<goals>-->
        <!--<goal>report-aggregate</goal>-->
        <!--</goals>-->
        <!--<phase>verify</phase>-->
        <!--</execution>-->
    </executions>
</plugin>
<plugin>
    <groupId>org.codefilarete</groupId>
    <artifactId>jacoco-aggregate-submodule</artifactId>
    <version>${codefilarete.jacoco.version}</version>
    <executions>
        <execution>
            <id>report-aggregate</id>
            <goals>
                <goal>report-aggregate</goal>
            </goals>
            <phase>verify</phase>
        </execution>
    </executions>
</plugin>
```

# Disclaimer
- I didn't find out how final aggregated report was saved, I've just kept original Mojo philosophy and ... it works ! see [ReportAggregateMojo.java](src/main/java/org/codefilarete/maven/jacoco/ReportAggregateMojo.java)
- There are no tests for this plugin since it's a hard work
- It is build with Java 8. Hope it will work with all superior version.

# Reference
- Original file making the aggregation : https://github.com/jacoco/jacoco/blob/master/jacoco-maven-plugin/src/org/jacoco/maven/ReportAggregateMojo.java
- Sonar way scanner expected to read and publish Jacoco files : https://github.com/SonarSource/sonar-scanner-commons/blob/master/api/src/main/java/org/sonarsource/scanner/api/EmbeddedScanner.java 
- A merging goal from Jacoco : https://github.com/jacoco/jacoco/blob/master/jacoco-maven-plugin/src/org/jacoco/maven/MergeMojo.java