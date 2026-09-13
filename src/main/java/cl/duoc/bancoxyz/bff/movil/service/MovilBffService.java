package cl.duoc.bancoxyz.bff.movil.service;

import cl.duoc.bancoxyz.bff.movil.dto.ResumenCuentaMovilDto;
import cl.duoc.bancoxyz.bff.movil.dto.SolicitudTransferenciaMovilDto;
import cl.duoc.bancoxyz.bff.movil.dto.TransaccionMovilDto;
import cl.duoc.bancoxyz.model.Cuenta;
import cl.duoc.bancoxyz.model.Transaccion;
import cl.duoc.bancoxyz.service.BancoService;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class MovilBffService {

    private final BancoService bancoService;

    public MovilBffService(BancoService bancoService) {
        this.bancoService = bancoService;
    }

    public ResumenCuentaMovilDto obtenerResumenMovil(Long cuentaId) {
        Cuenta cuenta = bancoService.obtenerCuentaPorId(cuentaId)
                .orElseThrow(() -> new IllegalArgumentException("Cuenta no encontrada con ID: " + cuentaId));

        List<TransaccionMovilDto> ultimosMovimientos = bancoService.obtenerTransaccionesPorCuenta(cuentaId).stream()
                .limit(3)
                .map(this::convertirTransaccionMovil)
                .collect(Collectors.toList());

        String titularAbreviado = cuenta.getNombreTitular();
        if (titularAbreviado != null && titularAbreviado.contains(" ")) {
            String[] partes = titularAbreviado.split(" ");
            titularAbreviado = partes[0] + " " + partes[partes.length - 1];
        }

        return new ResumenCuentaMovilDto(
                cuenta.getCuentaId(),
                titularAbreviado,
                cuenta.getSaldo(),
                cuenta.getTipo(),
                ultimosMovimientos,
                "Bienvenido(a) a tu Banca Móvil. Datos al día."
        );
    }

    public Long consultarSaldoMovil(Long cuentaId) {
        return bancoService.obtenerCuentaPorId(cuentaId)
                .map(Cuenta::getSaldo)
                .orElseThrow(() -> new IllegalArgumentException("Cuenta no encontrada con ID: " + cuentaId));
    }

    public TransaccionMovilDto transferirMovil(Long cuentaOrigenId, SolicitudTransferenciaMovilDto solicitud) {
        Transaccion tx = bancoService.procesarTransferencia(
                cuentaOrigenId,
                solicitud.getCuentaDestinoId(),
                solicitud.getMonto(),
                "TRANSFERENCIA MOVIL: " + (solicitud.getComentario() != null ? solicitud.getComentario() : "Sin glosa")
        );
        return convertirTransaccionMovil(tx);
    }

    private TransaccionMovilDto convertirTransaccionMovil(Transaccion tx) {
        boolean esCargo = "retiro".equalsIgnoreCase(tx.getTipo()) || "debito".equalsIgnoreCase(tx.getTipo()) || "transferencia_saliente".equalsIgnoreCase(tx.getTipo());
        long montoFinal = esCargo ? -Math.abs(tx.getMonto()) : Math.abs(tx.getMonto());

        return new TransaccionMovilDto(
                tx.getId(),
                tx.getFecha(),
                montoFinal,
                tx.getTipo(),
                tx.getDescripcion()
        );
    }
}
