# A Maven Extension for Docker Maven Plugin
[![Build Status](https://github.com/sparsick/maven-docker-extension/workflows/MavenBuild/badge.svg)

## Overview
This Maven extension is a PoC for an [issue #1266](https://github.com/fabric8io/docker-maven-plugin/issues/1266) of [docker-maven-plugin](https://github.com/fabric8io/docker-maven-plugin). 
The concept of this extension is based on [Maven Deployer Extension](https://github.com/khmarbaise/maven-deployer-extension).
It checks if Docker Maven Plugin's goal push is configured in the project. 
If it is so, then this goal is deactivated and all Docker images, that are configured in Docker Maven Plugin, are pushed via this extension after a Maven build runs successfully.

## Usage

If you want to use this extension, you need Maven 3.3.1+ and you have to define `.mvn/extensions.xml` file in your Maven project:

```xml
<extensions xmlns="http://maven.apache.org/EXTENSIONS/1.0.0" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
  xsi:schemaLocation="http://maven.apache.org/EXTENSIONS/1.0.0 http://maven.apache.org/xsd/core-extensions-1.0.0.xsd">
  <extension>
   <groupId>com.github.sparsick.maven.docker.extension</groupId>
   <artifactId>maven-docker-extension</artifactId>
   <version>0.0.1</version>
  </extension>
</extensions>
```
## License

Apache License, Version 2.0, January 2004
