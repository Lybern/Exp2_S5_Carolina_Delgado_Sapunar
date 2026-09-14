package cl.duoc.bancoxyz.bff.cajero.service;

import cl.duoc.bancoxyz.bff.cajero.dto.ConsultaSaldoCajeroDto;
import cl.duoc.bancoxyz.bff.cajero.dto.RespuestaRetiroDto;
import cl.duoc.bancoxyz.bff.cajero.dto.SolicitudRetiroCajeroDto;
import cl.duoc.bancoxyz.model.Cuenta;
import cl.duoc.bancoxyz.model.Transaccion;
import cl.duoc.bancoxyz.service.BancoService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.text.NumberFormat;
import java.util.Locale;
import java.util.UUID;

// =========================================================================
// PATRÓN BACKEND FOR FRONTEND (BFF) - CANAL CAJERO AUTOMÁTICO (ATM):
// Responsabilidad exclusiva: Adaptar las respuestas, formatear textos y comprobantes
// para la interfaz física del cajero. No contiene reglas de negocio contables.
// =========================================================================
@Service
public class CajeroBffService {

    // =========================================================================
    // EXTERNALIZACIÓN DE LÍMITES OPERATIVOS (application.properties):
    // Se inyectan con @Value permitiendo cambiar políticas de giro o denominación
    // de billetes en producción sin recompilar el código fuente Java.
    // =========================================================================
    @Value("${banco.atm.limite-giro:200000}")
    private long limiteGiroAtm;

    @Value("${banco.atm.multiplo-billete:5000}")
    private long multiploBillete;

    private final BancoService bancoService;

    public CajeroBffService(BancoService bancoService) {
        this.bancoService = bancoService;
    }

    /**
     * Adapta la consulta de saldo para el display del cajero automático.
     * Delega el cálculo de disponible al Core Bancario (BancoService).
     */
    public ConsultaSaldoCajeroDto consultarSaldoCajero(Long cuentaId, String terminalId) {
        Cuenta cuenta = bancoService.obtenerCuentaPorId(cuentaId)
                .orElseThrow(() -> new IllegalArgumentException("Tarjeta o cuenta no válida: " + cuentaId));

        // Delegación de cálculo de límite de giro al servicio de dominio usando límite configurable
        long limiteDisponible = bancoService.calcularLimiteGiroATM(cuentaId, limiteGiroAtm);

        return new ConsultaSaldoCajeroDto(
                cuenta.getCuentaId(),
                cuenta.getNombreTitular(),
                cuenta.getSaldo() != null ? cuenta.getSaldo() : 0L,
                limiteDisponible,
                terminalId != null ? terminalId : "ATM-DEFAULT",
                "ACTIVA".equalsIgnoreCase(cuenta.getEstado()),
                "Seleccione el monto que desea retirar. Límite máximo por giro: $" + limiteGiroAtm
        );
    }

    public RespuestaRetiroDto procesarRetiroCajero(Long cuentaId, SolicitudRetiroCajeroDto solicitud) {
        // Delegamos el procesamiento financiero y las reglas bancarias al dominio usando límites configurables
        Transaccion tx = bancoService.procesarRetiroATM(
                cuentaId,
                solicitud.getMonto(),
                solicitud.getPin(),
                solicitud.getTerminalId(),
                limiteGiroAtm,
                multiploBillete
        );

        Cuenta cuentaActualizada = bancoService.obtenerCuentaPorId(cuentaId)
                .orElseThrow(() -> new IllegalStateException("Error al consultar cuenta post-giro"));

        // El BFF se encarga de formatear la salida y comprobante para la pantalla del cajero
        String codAuth = "AUTH-ATM-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
        NumberFormat formatoChileno = NumberFormat.getCurrencyInstance(Locale.forLanguageTag("es-CL"));
        String montoFormateado = formatoChileno.format(solicitud.getMonto());

        return new RespuestaRetiroDto(
                cuentaId,
                solicitud.getMonto(),
                cuentaActualizada.getSaldo(),
                codAuth,
                tx.getId(),
                true,
                "Retiro exitoso por " + montoFormateado + ". Por favor retire su dinero del dispensador y su comprobante."
        );
    }
}
