package io.github.naomimyselfandi.staticpermissions.web;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.*;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Component;

import java.util.regex.Pattern;

@Component
final class JsonNodeHelperImpl implements JsonNodeHelper {

    private static final Pattern DBL, INT;

    static {
        var digits    = "(\\d+)";
        var hexDigits = "(\\p{XDigit}+)";
        var exponent  = "[eE][+-]?"+digits;
        var fpRegex   = ("[\\x00-\\x20]*" +
                        "[+-]?(NaN|Infinity|"  +
                        "((("+digits+"(\\.)?("+digits+"?)("+exponent+")?)|"+
                        "(\\."+digits+"("+exponent+")?)|"+
                        "((" +
                        "(0[xX]" + hexDigits + "(\\.)?)|" +
                        "(0[xX]" + hexDigits + "?(\\.)" + hexDigits + ")" +
                        ")[pP][+-]?" + digits + "))" +
                        "[fFdD]?))" +
                        "[\\x00-\\x20]*");
        DBL = Pattern.compile(fpRegex);
        INT = Pattern.compile("[+-]?\\d+");
    }

    @Override
    public JsonNode infer(@Nullable String input) {
        if (input == null) {
            return NullNode.getInstance();
        } else if ("true".equals(input)) {
            return BooleanNode.TRUE;
        } else if ("false".equals(input)) {
            return BooleanNode.FALSE;
        } else if (INT.matcher(input).matches()) {
            var value = Long.parseLong(input, 10);
            if (value >= Integer.MIN_VALUE && value <= Integer.MAX_VALUE) {
                return new IntNode((int) value);
            } else {
                return new LongNode(value);
            }
        } else if (DBL.matcher(input).matches()) {
            return new DoubleNode(Double.parseDouble(input));
        } else {
            return new TextNode(input);
        }
    }

}
