package cl.duoc.bancoxyz.bff.cajero.controller;

import cl.duoc.bancoxyz.bff.cajero.dto.ConsultaSaldoCajeroDto;
import cl.duoc.bancoxyz.bff.cajero.dto.RespuestaRetiroDto;
import cl.duoc.bancoxyz.bff.cajero.dto.SolicitudRetiroCajeroDto;
import cl.duoc.bancoxyz.bff.cajero.service.CajeroBffService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping({"/api/v1/cajero", "/api/v1/atm"})
@Tag(name = "BFF Cajero Automático", description = "Endpoints para cajeros ATM (operaciones críticas de saldo y retiros)")
public class CajeroController {

    private final CajeroBffService cajeroBffService;

    public CajeroController(CajeroBffService cajeroBffService) {
        this.cajeroBffService = cajeroBffService;
    }

    @Operation(summary = "Consulta express de saldo en Cajero Automático",
               description = "Devuelve el saldo disponible y el monto máximo de retiro permitido según el hardware del cajero ($200.000).")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Saldo consultado exitosamente"),
            @ApiResponse(responseCode = "404", description = "Tarjeta o cuenta no encontrada")
    })
    @GetMapping("/cuentas/{cuentaId}/saldo")
    public ResponseEntity<ConsultaSaldoCajeroDto> consultarSaldo(
            @Parameter(description = "ID de la cuenta/tarjeta", example = "101")
            @PathVariable Long cuentaId,
            @Parameter(description = "ID del terminal físico ATM", example = "ATM-SCL-01")
            @RequestParam(required = false, defaultValue = "ATM-SCL-01") String terminalId) {
        return ResponseEntity.ok(cajeroBffService.consultarSaldoCajero(cuentaId, terminalId));
    }

    @Operation(summary = "Retiro de dinero en efectivo (Operación Crítica)",
               description = "Valida PIN (4 dígitos), saldo disponible, límite por giro ($200.000) y múltiplos de billetes ($5.000). Retorna comprobante y código de autorización.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Retiro autorizado y saldo actualizado"),
            @ApiResponse(responseCode = "400", description = "Fondos insuficientes, PIN inválido o monto fuera de límite")
    })
    @PostMapping("/cuentas/{cuentaId}/retiro")
    public ResponseEntity<RespuestaRetiroDto> procesarRetiro(
            @Parameter(description = "ID de la cuenta", example = "101")
            @PathVariable Long cuentaId,
            @RequestBody SolicitudRetiroCajeroDto solicitud) {
        return ResponseEntity.ok(cajeroBffService.procesarRetiroCajero(cuentaId, solicitud));
    }
}
