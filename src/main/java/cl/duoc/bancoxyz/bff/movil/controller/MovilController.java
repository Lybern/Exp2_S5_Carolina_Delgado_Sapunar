package cl.duoc.bancoxyz.bff.movil.controller;

import cl.duoc.bancoxyz.bff.movil.dto.ResumenCuentaMovilDto;
import cl.duoc.bancoxyz.bff.movil.dto.SolicitudTransferenciaMovilDto;
import cl.duoc.bancoxyz.bff.movil.dto.TransaccionMovilDto;
import cl.duoc.bancoxyz.bff.movil.service.MovilBffService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping({"/api/v1/movil", "/api/v1/mobile"})
@Tag(name = "BFF Móvil", description = "Endpoints para aplicación móvil (respuestas ligeras y bajo consumo de red)")
public class MovilController {

    private final MovilBffService movilBffService;

    public MovilController(MovilBffService movilBffService) {
        this.movilBffService = movilBffService;
    }

    @Operation(summary = "Obtener resumen de cuenta para móvil",
               description = "Entrega un payload ligero con el saldo disponible y las últimas 3 transacciones para minimizar transferencia de datos en redes móviles.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Resumen móvil entregado con éxito"),
            @ApiResponse(responseCode = "404", description = "Cuenta no encontrada")
    })
    @GetMapping("/cuentas/{cuentaId}")
    public ResponseEntity<ResumenCuentaMovilDto> obtenerResumenCuenta(
            @Parameter(description = "ID de la cuenta bancaria", example = "101")
            @PathVariable Long cuentaId) {
        return ResponseEntity.ok(movilBffService.obtenerResumenMovil(cuentaId));
    }

    @Operation(summary = "Consulta rápida de saldo móvil",
               description = "Devuelve exclusivamente el saldo disponible en formato ultracompacto.")
    @GetMapping("/cuentas/{cuentaId}/saldo")
    public ResponseEntity<Map<String, Object>> obtenerSaldoRapido(
            @Parameter(description = "ID de la cuenta bancaria", example = "101")
            @PathVariable Long cuentaId) {
        Long saldo = movilBffService.consultarSaldoMovil(cuentaId);
        return ResponseEntity.ok(Map.of(
                "cuentaId", cuentaId,
                "saldoDisponible", saldo
        ));
    }

    @Operation(summary = "Transferencia rápida móvil",
               description = "Permite enviar dinero de forma simplificada a otra cuenta desde la app móvil.")
    @PostMapping("/cuentas/{cuentaId}/transferencia")
    public ResponseEntity<TransaccionMovilDto> transferir(
            @Parameter(description = "ID de la cuenta origen", example = "101")
            @PathVariable Long cuentaId,
            @RequestBody SolicitudTransferenciaMovilDto solicitud) {
        TransaccionMovilDto respuesta = movilBffService.transferirMovil(cuentaId, solicitud);
        return ResponseEntity.ok(respuesta);
    }
}
