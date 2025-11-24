package io.github.naomimyselfandi.staticpermissions.core;

import java.util.Map;

interface Extractor<S> {

    Map<String, Object> extract(S source);

}
