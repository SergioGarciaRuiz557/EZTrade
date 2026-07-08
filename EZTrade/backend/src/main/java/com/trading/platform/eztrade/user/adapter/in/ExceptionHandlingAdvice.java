package com.trading.platform.eztrade.user.adapter.in;

import com.trading.platform.eztrade.user.domain.exceptions.UserExistsException;
import com.trading.platform.eztrade.user.domain.exceptions.UserNotFoundException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * Global exception handler for the web context.
 * <p>
 * Intercepts domain-specific exceptions and turns them into standardized HTTP
 * responses using {@link ProblemDetail}.
 */
@ControllerAdvice
public class ExceptionHandlingAdvice {

    /**
     * Handles {@link UserExistsException} when attempting to register a user
     * that already exists in the system.
     * <p>
     * Returns an HTTP response with <strong>409 CONFLICT</strong> status and a
     * {@link ProblemDetail} body containing a descriptive title and the exception
     * message as the detail.
     *
     * @param ex exception thrown when a user with the same data already exists
     * @return {@link ProblemDetail} object with the error information
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

    /**
     * Handles {@link UserNotFoundException} when trying to access a user that
     * does not exist in the system.
     * <p>
     * Returns an HTTP response with <strong>404 NOT FOUND</strong> status and a
     * {@link ProblemDetail} body containing a descriptive title and the exception
     * message as the detail.
     *
     * @param ex exception thrown when no user is found with the provided data
     * @return {@link ProblemDetail} object with the error information
     */
    @ExceptionHandler(UserNotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    @ResponseBody
    public ProblemDetail handleUserNotFoundException(UserNotFoundException ex) {
        ProblemDetail pd = ProblemDetail.forStatus(HttpStatus.NOT_FOUND);
        pd.setTitle("User not found");
        pd.setDetail(ex.getMessage());
        return pd;
    }
}
