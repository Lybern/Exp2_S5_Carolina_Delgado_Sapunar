package cl.duoc.bancoxyz.bff.cajero.service;

import cl.duoc.bancoxyz.bff.cajero.dto.ConsultaSaldoCajeroDto;
import cl.duoc.bancoxyz.bff.cajero.dto.RespuestaRetiroDto;
import cl.duoc.bancoxyz.bff.cajero.dto.SolicitudRetiroCajeroDto;
import cl.duoc.bancoxyz.model.Cuenta;
import cl.duoc.bancoxyz.model.Transaccion;
import cl.duoc.bancoxyz.service.BancoService;
import org.springframework.stereotype.Service;

import java.text.NumberFormat;
import java.util.Locale;
import java.util.UUID;

@Service
public class CajeroBffService {

    private static final long LIMITE_GIRO_ATM = 200000L;
    private static final long MULTIPLO_BILLETE = 5000L;

    private final BancoService bancoService;

    public CajeroBffService(BancoService bancoService) {
        this.bancoService = bancoService;
    }

    public ConsultaSaldoCajeroDto consultarSaldoCajero(Long cuentaId, String terminalId) {
        Cuenta cuenta = bancoService.obtenerCuentaPorId(cuentaId)
                .orElseThrow(() -> new IllegalArgumentException("Tarjeta o cuenta no válida: " + cuentaId));

        long saldo = cuenta.getSaldo() != null ? cuenta.getSaldo() : 0L;
        long limiteDisponible = Math.min(saldo, LIMITE_GIRO_ATM);

        return new ConsultaSaldoCajeroDto(
                cuenta.getCuentaId(),
                cuenta.getNombreTitular(),
                saldo,
                limiteDisponible,
                terminalId != null ? terminalId : "ATM-DEFAULT",
                "ACTIVA".equalsIgnoreCase(cuenta.getEstado()),
                "Seleccione el monto que desea retirar. Límite máximo por giro: $" + LIMITE_GIRO_ATM
        );
    }

    public RespuestaRetiroDto procesarRetiroCajero(Long cuentaId, SolicitudRetiroCajeroDto solicitud) {
        if (solicitud.getPin() == null || solicitud.getPin().trim().length() != 4) {
            throw new IllegalArgumentException("PIN de seguridad inválido. Debe contener 4 dígitos.");
        }

        if (solicitud.getMonto() == null || solicitud.getMonto() <= 0) {
            throw new IllegalArgumentException("El monto a retirar debe ser mayor a $0.");
        }

        if (solicitud.getMonto() % MULTIPLO_BILLETE != 0) {
            throw new IllegalArgumentException("El monto solicitado ($" + solicitud.getMonto() + ") debe ser múltiplo de $" + MULTIPLO_BILLETE + " (billetes disponibles de $5.000, $10.000 y $20.000).");
        }

        if (solicitud.getMonto() > LIMITE_GIRO_ATM) {
            throw new IllegalArgumentException("El monto solicitado ($" + solicitud.getMonto() + ") supera el límite máximo por giro en cajero ($" + LIMITE_GIRO_ATM + ").");
        }

        Transaccion tx = bancoService.procesarRetiro(
                cuentaId,
                solicitud.getMonto(),
                "CAJERO_ATM",
                "Giro ATM Terminal " + (solicitud.getTerminalId() != null ? solicitud.getTerminalId() : "ATM-GENERIC")
        );

        Cuenta cuentaActualizada = bancoService.obtenerCuentaPorId(cuentaId)
                .orElseThrow(() -> new IllegalStateException("Error al consultar cuenta post-giro"));

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
