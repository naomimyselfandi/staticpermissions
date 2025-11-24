package io.github.naomimyselfandi.staticpermissions.core;

import org.springframework.stereotype.Component;

import java.util.function.Function;
import java.util.regex.MatchResult;
import java.util.regex.Pattern;

@Component
class NamingConventionImpl implements NamingConvention {

    private static final Pattern PREFIX = Pattern.compile("(?:get|is)([A-Z])");
    private static final Function<MatchResult, String> REMOVE_PREFIX = r -> r.group(1).toLowerCase();

    @Override
    public String normalize(String name) {
        return PREFIX.matcher(name).replaceAll(REMOVE_PREFIX);
    }

}
