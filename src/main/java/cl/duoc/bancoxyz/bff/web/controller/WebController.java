package cl.duoc.bancoxyz.bff.web.controller;

import cl.duoc.bancoxyz.bff.web.dto.DashboardWebDto;
import cl.duoc.bancoxyz.bff.web.dto.DetalleCuentaWebDto;
import cl.duoc.bancoxyz.bff.web.dto.TransaccionWebDto;
import cl.duoc.bancoxyz.bff.web.service.WebBffService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/web")
@Tag(name = "BFF Web", description = "Endpoints para portal web (datos completos, historial y dashboard)")
public class WebController {

    private final WebBffService webBffService;

    public WebController(WebBffService webBffService) {
        this.webBffService = webBffService;
    }

    @Operation(summary = "Obtener detalle completo de cuenta para Web",
               description = "Entrega la información exhaustiva de la cuenta bancaria: titular, edad, saldos, líneas de sobregiro, tasa de interés, desglose anual y lista completa de transacciones.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Detalle web entregado con éxito"),
            @ApiResponse(responseCode = "404", description = "Cuenta no encontrada")
    })
    @GetMapping("/cuentas/{cuentaId}")
    public ResponseEntity<DetalleCuentaWebDto> obtenerDetalleCuenta(
            @Parameter(description = "ID de la cuenta bancaria", example = "101")
            @PathVariable Long cuentaId) {
        return ResponseEntity.ok(webBffService.obtenerDetalleWeb(cuentaId));
    }

    @Operation(summary = "Listar todas las transacciones históricas de una cuenta",
               description = "Obtiene el historial íntegro de movimientos con categorías financieras y canal de origen para renderizar en tablas web con filtros.")
    @GetMapping("/cuentas/{cuentaId}/transacciones")
    public ResponseEntity<List<TransaccionWebDto>> listarTransacciones(
            @Parameter(description = "ID de la cuenta bancaria", example = "101")
            @PathVariable Long cuentaId) {
        return ResponseEntity.ok(webBffService.listarTodasTransaccionesWeb(cuentaId));
    }

    @Operation(summary = "Dashboard global consolidado para administradores Web",
               description = "Provee métricas globales del banco (total de cuentas, capital en custodia, saldo promedio y distribución por tipo de producto).")
    @GetMapping("/dashboard")
    public ResponseEntity<DashboardWebDto> obtenerDashboard() {
        return ResponseEntity.ok(webBffService.obtenerDashboardWeb());
    }
}
