package com.trading.platform.eztrade;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.modulith.core.ApplicationModules;

class ModulithStructureTest {

    @Test
    @DisplayName("la estructura de modulos de Spring Modulith es valida")
    void verify_module_structure() {
        ApplicationModules.of(EzTradeApplication.class).verify();
    }
}

