package de.et.cucumberconvert;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * @author Erik
 */
public class TestStepConverter {

    private static File baseDir;
    private final String fileEnding;

    public static void main(String[] args) {
        if (args.length < 2) {
            System.out.println("Please provide file ending and base path of maven project");
            return;
        }
        TestStepConverter converter = new TestStepConverter(args[0], args[1]);
        converter.convert();
    }

    private TestStepConverter(String fileEnding, String basePath) {
        this.fileEnding = fileEnding;
        baseDir = new File(basePath);
    }

    private void convert() {
        List<File> javaFiles = scanRecursivelyForJavaFiles(baseDir);
        if (javaFiles.size() == 0) {
            System.out.println("There are no source files in " + baseDir.getAbsolutePath());
            return;
        }
        System.out.println("The following files have been found:");
        for (File javaFile : javaFiles) {
            System.out.println(javaFile.getAbsoluteFile());
        }
        System.out.println("\nConverted to Java 7 Notation:");
        javaFiles.forEach(this::changeToJava7Notation);
    }

    private List<File> scanRecursivelyForJavaFiles(File dir) {
        List<File> files = new LinkedList<>();
        File[] filesInBaseDir = dir.listFiles();
        if (filesInBaseDir == null || filesInBaseDir.length == 0) {
            return files;
        }
        for (File file : filesInBaseDir) {
            if (file.isDirectory()) {
                files.addAll(scanRecursivelyForJavaFiles(file));
            } else if (file.isFile() && file.getName().endsWith(fileEnding)) {
                files.add(file);
            }
        }
        return files;
    }

    private void changeToJava7Notation(File file) {
        String fileName = file.getName();
        String className = fileName.substring(0, fileName.indexOf('.'));
        String content = getFileContent(file);

        String contentWithNewSteps = replaceConstructorWithStepDefinitions(className, content);

        if (contentWithNewSteps != null) {
            String java7Notation = replaceImports(contentWithNewSteps);
            try (FileWriter fw = new FileWriter(file, false)) {
                fw.write(java7Notation);
                System.out.println(className);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }

    private String getFileContent(File file) {
        try {
            return new String(Files.readAllBytes(file.toPath()));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private String replaceConstructorWithStepDefinitions(String className, String content) {
        Pattern constructorPattern = Pattern.compile("public " + className + "\\(\\) \\{");
        Matcher constructorMatcher = constructorPattern.matcher(content);
        if (!constructorMatcher.find()) {
            System.out.println("Didn't find constructor with the name " + className + " in the file " + className +
                    ".java");
            return null;
        }
        int beginOfConstructorBody = constructorMatcher.end() - 1;
        int endOfConstructorBody = BracketUtil.matchingBracketPos(content, beginOfConstructorBody);
        String constructorBody = content.substring(beginOfConstructorBody + 1, endOfConstructorBody).trim();
        List<String> methods = extractMethodCalls(constructorBody);
        List<StepDefinition> stepDefinitions = StepDefinition.fromStrings(methods);
        List<String> stepDefinitionsInJava7 = stepDefinitions.stream().map(StepDefinition::java7Notation).collect
                (Collectors.toList());
        String allMethods = String.join("\n\n", stepDefinitionsInJava7);
        return content.substring(0, constructorMatcher.start() - 4) + allMethods + content.substring
                (endOfConstructorBody + 1);
    }

    private List<String> extractMethodCalls(String constructorBody) {
        List<String> methodCalls = new ArrayList<>();
        int startPos = 0;
        String statements = constructorBody.substring(startPos);
        extractMethodCall(statements, methodCalls);
        return methodCalls;
    }

    private static void extractMethodCall(String statements, List<String> methodCalls) {
        Pattern pattern = Pattern.compile("(?:Given|When|Then|And|But)\\(");
        Matcher matcher = pattern.matcher(statements);
        if (matcher.find()) {
            int matchingBracketPos = BracketUtil.matchingBracketPos(statements, matcher.end() - 1);
            // after the closing bracket the semicolon needs to be taken into account
            int endOfStatementPos = matchingBracketPos + 1;
            methodCalls.add(statements.substring(matcher.start(), endOfStatementPos + 1));
            int firstCharPosAfterStatement = endOfStatementPos + 1;
            if (firstCharPosAfterStatement < statements.length() - 1) {
                extractMethodCall(statements.substring(firstCharPosAfterStatement), methodCalls);
            }
        }
    }

    private String replaceImports(String contentWithNewSteps) {
        String updatedImport = contentWithNewSteps.replace("import cucumber.api.java8.En;", "import cucumber.api.java.en.*;");
        return updatedImport.replace("implements En ", "");
    }
}