package cl.duoc.bancoxyz.exception;

import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Schema(description = "Estructura estándar uniforme de respuesta de error bancario")
public class ErrorRespuestaDto {

    @Schema(description = "Marca de tiempo de la ocurrencia del error", example = "2026-09-13T21:25:00")
    private String fechaHora;

    @Schema(description = "Código de estado HTTP", example = "400")
    private int codigoEstado;

    @Schema(description = "Tipo o nombre del error HTTP", example = "Solicitud Incorrecta")
    private String error;

    @Schema(description = "Mensaje amigable descriptivo del error", example = "El PIN de seguridad debe contener exactamente 4 dígitos")
    private String mensaje;

    @Schema(description = "Ruta o endpoint donde ocurrió el error", example = "/api/v1/atm/cuentas/101/retiro")
    private String ruta;

    @Schema(description = "Lista detallada de inconsistencias o campos fallidos")
    private List<String> detalles;

    public ErrorRespuestaDto() {
        this.fechaHora = LocalDateTime.now().toString();
        this.detalles = new ArrayList<>();
    }

    public ErrorRespuestaDto(int codigoEstado, String error, String mensaje, String ruta) {
        this.fechaHora = LocalDateTime.now().toString();
        this.codigoEstado = codigoEstado;
        this.error = error;
        this.mensaje = mensaje;
        this.ruta = ruta;
        this.detalles = new ArrayList<>();
    }

    public ErrorRespuestaDto(int codigoEstado, String error, String mensaje, String ruta, List<String> detalles) {
        this.fechaHora = LocalDateTime.now().toString();
        this.codigoEstado = codigoEstado;
        this.error = error;
        this.mensaje = mensaje;
        this.ruta = ruta;
        this.detalles = detalles != null ? detalles : new ArrayList<>();
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

    public List<String> getDetalles() {
        return detalles;
    }

    public void setDetalles(List<String> detalles) {
        this.detalles = detalles;
    }
}
