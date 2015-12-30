package de.et.cucumberconvert;

/**
 * @author Erik
 */
public class BracketUtil {

    static int matchingBracketPos(String content, int firstBracketPos) {
        int nestedBracketCount = 0;
        char openingBracket = content.charAt(firstBracketPos);
        char closingBracket;
        switch (openingBracket) {
            case '{':
                closingBracket = '}';
                break;
            case '(':
                closingBracket = ')';
                break;
            default:
                throw new RuntimeException("FirstBracketPos doesn't indicate a bracket position!");
        }
        for (int pos = firstBracketPos + 1; pos < content.length(); pos++) {
            if (content.charAt(pos) == closingBracket) {
                if (nestedBracketCount == 0) {
                    return pos;
                } else {
                    nestedBracketCount--;
                }
            }
            if (content.charAt(pos) == openingBracket) {
                nestedBracketCount++;
            }
        }
        return 0;
    }
}
