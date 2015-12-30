package de.et.cucumberconvert;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author Erik
 */
public class StepDefinition {

    private String type;
    private String regEx;
    private String parameterGroup;
    private String body;

    private StepDefinition() {
        // use factory methods instead
    }

    public static List<StepDefinition> fromStrings(List<String> methods) {
        List<StepDefinition> stepDefinitions = new ArrayList<>();
        for (String method : methods) {
            stepDefinitions.add(fromString(method));
        }
        return stepDefinitions;
    }

    /**
     * for each method extract: method name, RegEx-String, List of Parameter names and types, and the body
     */
    public static StepDefinition fromString(String methodString) {
        StepDefinition stepDefinition = new StepDefinition();
        Pattern pattern = Pattern.compile("(?:Given|When|Then|And|But)");
        Matcher matcher = pattern.matcher(methodString);
        if (!matcher.find()) {
            return null;
        }
        stepDefinition.type = matcher.group();
        int startOfFirstArgument = methodString.indexOf('(') + 1;
        Pattern endOfFirstArgumentPattern = Pattern.compile("[^\\\\]\",");
        Matcher endOfFirstArgumentMatcher = endOfFirstArgumentPattern.matcher(methodString.substring
                (startOfFirstArgument));
        if (!endOfFirstArgumentMatcher.find()) {
            return null;
        }
        stepDefinition.regEx = methodString.substring(startOfFirstArgument, endOfFirstArgumentMatcher.end() + startOfFirstArgument - 1);
        int posAfterFirstArgument = endOfFirstArgumentMatcher.end() + startOfFirstArgument + 1;
        String remainingMethodStringAfterRegEx = methodString.substring(posAfterFirstArgument);
        int startOfParameterGroup = remainingMethodStringAfterRegEx.indexOf('(');
        int endOfParameterGroup = BracketUtil.matchingBracketPos(remainingMethodStringAfterRegEx, startOfParameterGroup);
        stepDefinition.parameterGroup = remainingMethodStringAfterRegEx.substring(startOfParameterGroup, endOfParameterGroup + 1);
        String remainingMethodStringAfterParameterGroup = remainingMethodStringAfterRegEx.substring
                (endOfParameterGroup + 1);
        int startOfBody = remainingMethodStringAfterParameterGroup.indexOf('{');
        int endOfBody = BracketUtil.matchingBracketPos(remainingMethodStringAfterParameterGroup, startOfBody);
        stepDefinition.body = remainingMethodStringAfterParameterGroup.substring(startOfBody, endOfBody + 1);
        return stepDefinition;
    }

    public String java7Notation() {
        String methodName = generateMethodName(regEx);
        return "    @" + type + "(" + regEx + ")\n    public void " + methodName + parameterGroup + " " + body;
    }

    private String generateMethodName(String regEx) {
        String simpleText = removeRegExFeatures(regEx);
        String normalizedText = removeIllegalCharacters(simpleText.toLowerCase());
        String [] words = normalizedText.trim().split(" ");
        return concatenateInCamelCase(words);
    }

    private String removeRegExFeatures(String regEx) {
        String trimmed = removeSurroundingQuotesAndAnchors(regEx);
        return removeCaptureGroups(trimmed);
    }

    private String removeSurroundingQuotesAndAnchors(String regEx) {
        String result;
        int starPos = 0;
        while (regEx.charAt(starPos) == '"' || regEx.charAt(starPos) == '^') {
            starPos++;
        }
        int endPos = regEx.length() - 1;
        while (regEx.charAt(endPos) == '"' || regEx.charAt(endPos) == '$') {
            endPos--;
        }
        result = regEx.substring(starPos, endPos + 1);
        return result;
    }

    private String removeCaptureGroups(String text) {
        return text.replaceAll("\\(.*?\\)", "");
    }

    private String removeIllegalCharacters(String text) {
        int startPos = 0;
        char firstSymbol = text.charAt(startPos);
        while (!Character.isJavaIdentifierStart(firstSymbol) && startPos < text.length()) {
            startPos++;
            firstSymbol = text.charAt(startPos);
        }
        if (startPos >= text.length()) {
            throw new RuntimeException("Couldn't generate method name from text: " + text);
        }
        StringBuilder result = new StringBuilder();
        result.append(text.charAt(startPos));
        for(int i = startPos + 1; i < text.length(); i++) {
            char currentChar = text.charAt(i);
            if(Character.isJavaIdentifierPart(currentChar)) {
                result.append(currentChar);
            } else if (result.charAt(result.length() - 1) != ' ') {
                result.append(' ');
            }
        }
        return result.toString();
    }

    private String concatenateInCamelCase(String[] words) {
        StringBuilder methodName = new StringBuilder();
        boolean firstWord = true;
        for (String word : words) {
            if (firstWord) {
                methodName.append(word);
                firstWord = false;
            } else {
                methodName.append(capitalize(word));
            }
        }
        return methodName.toString();
    }

    private String capitalize(String line) {
        return Character.toUpperCase(line.charAt(0)) + line.substring(1);
    }

    @Override
    public String toString() {
        return "StepDefinition{" +
                "type='" + type + '\'' +
                ", regEx='" + regEx + '\'' +
                ", parameterGroup='" + parameterGroup + '\'' +
                ", body='" + body + '\'' +
                '}';
    }
}
