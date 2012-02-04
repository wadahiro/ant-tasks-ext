ant-tasks-ext
========================
https://github.com/wadahiro/ant-tasks-ext

How to use
-------------------

```
<?xml version="1.0" encoding="UTF-8"?>
<project name="project" default="default">

    <taskdef classpath="lib/ant-tasks-ext-1.0.jar;lib/ant-1.7.0.jar" resource="antlib.xml" />

    <target name="default">
        <excludejar destfile="target/result.jar"
            basefile="target/base.jar"
            excludefile="target/exclude.jar"
            autoclean="true"
            work="./work" />
    </target>
    
</project>
```

License
-------
* [Apache Licence]