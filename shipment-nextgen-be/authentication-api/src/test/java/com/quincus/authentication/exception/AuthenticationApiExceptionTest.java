package com.quincus.authentication.exception;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

class AuthenticationApiExceptionTest {

    @Test
    void constructor_Message_SuccessfullyCreated() {
        // Arrange
        String message = "Test Exception";

        // Act
        AuthenticationApiException exception = new AuthenticationApiException(message);

        // Assert
        assertThat(exception).isNotNull();
        assertThat(exception.getMessage()).isEqualTo(message);
        assertThat(exception.getCause()).isNull();
    }

    @Test
    void constructor_MessageAndCause_SuccessfullyCreated() {
        // Arrange
        String message = "Test Exception";
        Throwable cause = mock(Throwable.class);

        // Act
        AuthenticationApiException exception = new AuthenticationApiException(message, cause);

        // Assert
        assertThat(exception).isNotNull();
        assertThat(exception.getMessage()).isEqualTo(message);
        assertThat(exception.getCause()).isEqualTo(cause);
    }

}
