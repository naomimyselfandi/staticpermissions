package io.github.naomimyselfandi.staticpermissions.web;

import com.fasterxml.jackson.databind.JsonNode;
import org.springframework.lang.Nullable;

interface JsonNodeHelper {
    JsonNode infer(@Nullable String input);
}
