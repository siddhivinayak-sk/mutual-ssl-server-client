# Getting Started

## Install Java and Maven
This project has been written in Java so need to install Java (JDK) 8 or later. After JDK installation set JAVA_HOME path so that maven can pick JDK appropriately. 

The Apache Maven build tool used in this project, so install Maven (Apache Maven installation documentation).

Once ready check Maven is working properly. Run below command on your command line:
mvn -version  

## Project build
1. Checkout the project on your local directory

2. Run below command to clean, compile and package
```
mvn clean package
```

3. Once compiled, a jar must be created into target directory. to run invoke jar
If encrypted property value not used:
```
java -jar <jar file name>
```

If encrypted property value  used:
```
java -jar <jar file name> [secretCode=<jasypt_key>]
```

Note: The sample private key (self-generated), certificate (self-generated) and configuration placed in project for testing purpose only. It should not be used for actual product.