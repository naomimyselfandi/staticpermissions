package io.github.naomimyselfandi.staticpermissions.core;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import static org.assertj.core.api.Assertions.*;

class NamingConventionImplTest {

    private NamingConvention fixture;

    @BeforeEach
    void setup() {
        fixture = new NamingConventionImpl();
    }

    @ParameterizedTest
    @CsvSource(textBlock = """
            foo,foo
            getFoo,foo
            isFoo,foo
            bar,bar
            getBar,bar
            isBar,bar
            get,get
            is,is
            getaway,getaway
            island,island
            """)
    void normalize(String name, String expected) {
        assertThat(fixture.normalize(name)).isEqualTo(expected);
    }

}
