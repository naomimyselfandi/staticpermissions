package io.github.naomimyselfandi.staticpermissions_integration;

import io.github.naomimyselfandi.staticpermissions.Intent;

public interface FooIntent extends Intent {

    int getEggs();

    default boolean isSpam() {
        return false;
    }

}
