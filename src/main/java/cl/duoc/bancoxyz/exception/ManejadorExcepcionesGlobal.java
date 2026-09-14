package cl.duoc.bancoxyz.exception;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.ServletWebRequest;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import java.util.List;

// =========================================================================
// TRATAMIENTO GLOBAL DE EXCEPCIONES:
// Extiende de ResponseEntityExceptionHandler para interceptar de forma estándar
// todos los errores nativos de Spring (validaciones @Valid, parseo JSON, etc.)
// y proveer respuestas HTTP uniformes, coherentes y predecibles en formato JSON.
// =========================================================================
@RestControllerAdvice
public class ManejadorExcepcionesGlobal extends ResponseEntityExceptionHandler {

    /**
     * Intercepta fallos de validación producidos por anotaciones Bean Validation (@Valid, @NotNull, @Pattern, @Positive).
     * Retorna HTTP 400 BAD REQUEST con la lista detallada de campos que no cumplieron la regla.
     */
    @Override
    protected ResponseEntity<Object> handleMethodArgumentNotValid(
            MethodArgumentNotValidException ex,
            HttpHeaders headers,
            HttpStatusCode status,
            WebRequest request) {

        List<String> detalles = ex.getBindingResult()
                .getFieldErrors()
                .stream()
                .map(error -> error.getField() + ": " + error.getDefaultMessage())
                .toList();

        String ruta = request instanceof ServletWebRequest swr ? swr.getRequest().getRequestURI() : "";

        ErrorRespuestaDto errorDto = new ErrorRespuestaDto(
                HttpStatus.BAD_REQUEST.value(),
                "Error de Validación de Datos",
                "Uno o más campos de la solicitud no cumplen con las reglas requeridas.",
                ruta,
                detalles
        );

        return new ResponseEntity<>(errorDto, headers, HttpStatus.BAD_REQUEST);
    }

    /**
     * Intercepta cuerpos de solicitud vacíos o JSON con formato incorrecto.
     * Retorna HTTP 400 BAD REQUEST.
     */
    @Override
    protected ResponseEntity<Object> handleHttpMessageNotReadable(
            HttpMessageNotReadableException ex,
            HttpHeaders headers,
            HttpStatusCode status,
            WebRequest request) {

        String ruta = request instanceof ServletWebRequest swr ? swr.getRequest().getRequestURI() : "";

        ErrorRespuestaDto errorDto = new ErrorRespuestaDto(
                HttpStatus.BAD_REQUEST.value(),
                "Cuerpo de Solicitud Inválido",
                "El cuerpo JSON es obligatorio o contiene un formato no deserializable.",
                ruta,
                List.of(ex.getMessage() != null ? ex.getMessage() : "JSON malformado")
        );

        return new ResponseEntity<>(errorDto, headers, HttpStatus.BAD_REQUEST);
    }

    /**
     * Captura argumentos ilegales (parámetros no válidos o recursos no encontrados en consultas).
     * Retorna HTTP 400 BAD REQUEST.
     */
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ErrorRespuestaDto> manejarArgumentoInvalido(IllegalArgumentException ex, HttpServletRequest request) {
        ErrorRespuestaDto error = new ErrorRespuestaDto(
                HttpStatus.BAD_REQUEST.value(),
                "Solicitud Incorrecta / Parámetro Inválido",
                ex.getMessage(),
                request.getRequestURI()
        );
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
    }

    /**
     * Captura estados ilegales (fondos insuficientes, cuentas inactivas o conflictos operacionales).
     * Retorna HTTP 409 CONFLICT.
     */
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

    /**
     * Manejador de respaldo para cualquier error no controlado o inesperado.
     * Retorna HTTP 500 INTERNAL SERVER ERROR.
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorRespuestaDto> manejarErrorGeneral(Exception ex, HttpServletRequest request) {
        ErrorRespuestaDto error = new ErrorRespuestaDto(
                HttpStatus.INTERNAL_SERVER_ERROR.value(),
                "Error Interno del Servidor",
                ex.getMessage() != null ? ex.getMessage() : "Ocurrió un error inesperado al procesar la solicitud.",
                request.getRequestURI()
        );
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
    }
}
