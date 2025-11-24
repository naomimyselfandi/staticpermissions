package io.github.naomimyselfandi.staticpermissions.core;

import java.util.Optional;

public interface ExtractorFactory {
    Optional<Extractor<?>> apply(Class<?> source, Class<?> target);
}
