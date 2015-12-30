# TestStepConverter
Converts Cucumber Test Steps between from Java 8 back to Java 7 notation.

##Build the project

`mvn clean install`

##Run the program
The program expects two arguments.
1. The file ending of all files that should be converted
2. The path that will recursively be searched for files with that ending
 
`java -cp "target/*" de.et.cucumberconvert.TestStepConverter IT.java C:/dev/proj/teststepconverter/exampleMvnProject`
