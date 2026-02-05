package com.trading.platform.eztrade.user.adapter.in;

import com.trading.platform.eztrade.user.domain.exceptions.UserExistsException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * Manejador global de excepciones para el contexto web.
 * <p>
 * Intercepta las excepciones específicas de la capa de dominio y
 * las transforma en respuestas HTTP estandarizadas utilizando
 * {@link ProblemDetail}.
 */
@ControllerAdvice
public class ExceptionHandlingAdvice {

    /**
     * Maneja la excepción {@link UserExistsException} cuando se intenta registrar
     * un usuario que ya existe en el sistema.
     * <p>
     * Devuelve una respuesta HTTP con estado <strong>409 CONFLICT</strong> y un cuerpo
     * de tipo {@link ProblemDetail} que contiene un título descriptivo y
     * el mensaje de la excepción como detalle.
     *
     * @param ex excepción lanzada cuando ya existe un usuario con los mismos datos
     * @return objeto {@link ProblemDetail} con la información del error
     */
    @ExceptionHandler(UserExistsException.class)
    @ResponseStatus(HttpStatus.CONFLICT)
    @ResponseBody
    public ProblemDetail UserExistsException(UserExistsException ex) {
        ProblemDetail pd = ProblemDetail.forStatus(HttpStatus.CONFLICT);
        pd.setTitle("User already exists");
        pd.setDetail(ex.getMessage());
        return pd;
    }
}

