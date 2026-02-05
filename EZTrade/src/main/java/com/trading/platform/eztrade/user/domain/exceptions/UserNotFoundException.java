package com.trading.platform.eztrade.user.domain.exceptions;

/**
 * Excepción de dominio que indica que el usuario solicitado
 * no existe en el sistema.
 * <p>
 * Suele lanzarse desde la capa de aplicación cuando una operación
 * de búsqueda de usuario no devuelve resultados.
 */
public class UserNotFoundException extends RuntimeException {

    /**
     * Crea una nueva instancia de la excepción con el mensaje detallado.
     *
     * @param message descripción del motivo por el que no se ha encontrado el usuario
     */
    public UserNotFoundException(String message) {
        super(message);
    }
}
