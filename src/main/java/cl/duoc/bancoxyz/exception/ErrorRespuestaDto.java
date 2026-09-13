package cl.duoc.bancoxyz.exception;

import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDateTime;

@Schema(description = "Estructura estándar de respuesta de error")
public class ErrorRespuestaDto {

    private String fechaHora;
    private int codigoEstado;
    private String error;
    private String mensaje;
    private String ruta;

    public ErrorRespuestaDto() {
    }

    public ErrorRespuestaDto(int codigoEstado, String error, String mensaje, String ruta) {
        this.fechaHora = LocalDateTime.now().toString();
        this.codigoEstado = codigoEstado;
        this.error = error;
        this.mensaje = mensaje;
        this.ruta = ruta;
    }

    public String getFechaHora() {
        return fechaHora;
    }

    public void setFechaHora(String fechaHora) {
        this.fechaHora = fechaHora;
    }

    public int getCodigoEstado() {
        return codigoEstado;
    }

    public void setCodigoEstado(int codigoEstado) {
        this.codigoEstado = codigoEstado;
    }

    public String getError() {
        return error;
    }

    public void setError(String error) {
        this.error = error;
    }

    public String getMensaje() {
        return mensaje;
    }

    public void setMensaje(String mensaje) {
        this.mensaje = mensaje;
    }

    public String getRuta() {
        return ruta;
    }

    public void setRuta(String ruta) {
        this.ruta = ruta;
    }
}
