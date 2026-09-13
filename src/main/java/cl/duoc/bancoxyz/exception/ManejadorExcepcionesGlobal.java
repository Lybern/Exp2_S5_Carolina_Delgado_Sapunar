package cl.duoc.bancoxyz.exception;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class ManejadorExcepcionesGlobal {

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ErrorRespuestaDto> manejarArgumentoInvalido(IllegalArgumentException ex, HttpServletRequest request) {
        ErrorRespuestaDto error = new ErrorRespuestaDto(
                HttpStatus.BAD_REQUEST.value(),
                "Solicitud Incorrecta",
                ex.getMessage(),
                request.getRequestURI()
        );
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
    }

    @ExceptionHandler(IllegalStateException.class)
    public ResponseEntity<ErrorRespuestaDto> manejarEstadoInvalido(IllegalStateException ex, HttpServletRequest request) {
        ErrorRespuestaDto error = new ErrorRespuestaDto(
                HttpStatus.CONFLICT.value(),
                "Conflicto Operacional / Fondos Insuficientes",
                ex.getMessage(),
                request.getRequestURI()
        );
        return ResponseEntity.status(HttpStatus.CONFLICT).body(error);
    }

    @ExceptionHandler(org.springframework.http.converter.HttpMessageNotReadableException.class)
    public ResponseEntity<ErrorRespuestaDto> manejarCuerpoFaltante(org.springframework.http.converter.HttpMessageNotReadableException ex, HttpServletRequest request) {
        ErrorRespuestaDto error = new ErrorRespuestaDto(
                HttpStatus.BAD_REQUEST.value(),
                "Cuerpo de Solicitud Requerido",
                "Debe enviar un objeto JSON en el Body con los datos requeridos (monto, pin, terminalId).",
                request.getRequestURI()
        );
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorRespuestaDto> manejarErrorGeneral(Exception ex, HttpServletRequest request) {
        ErrorRespuestaDto error = new ErrorRespuestaDto(
                HttpStatus.INTERNAL_SERVER_ERROR.value(),
                "Error Interno del Servidor",
                ex.getMessage(),
                request.getRequestURI()
        );
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
    }
}
