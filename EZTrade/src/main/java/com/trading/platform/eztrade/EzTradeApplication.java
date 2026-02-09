package com.trading.platform.eztrade;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Punto de entrada principal de la aplicación EzTrade.
 * <p>
 * Esta clase arranca el contexto de Spring Boot y carga todos los
 * componentes configurados de la aplicación.
 */
@SpringBootApplication
public class EzTradeApplication {

    /**
     * Método principal que inicia la aplicación Spring Boot.
     *
     * @param args argumentos de la línea de comandos
     */
    public static void main(String[] args) {
        SpringApplication.run(EzTradeApplication.class, args);
    }

}
