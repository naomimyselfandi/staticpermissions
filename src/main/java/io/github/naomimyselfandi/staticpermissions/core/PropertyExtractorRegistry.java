package io.github.naomimyselfandi.staticpermissions.core;

import io.github.naomimyselfandi.staticpermissions.PropertyExtractor;

interface PropertyExtractorRegistry {
    PropertyExtractor<?> get(Class<?> type);
}
