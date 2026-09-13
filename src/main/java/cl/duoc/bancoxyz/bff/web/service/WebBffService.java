package cl.duoc.bancoxyz.bff.web.service;

import cl.duoc.bancoxyz.bff.web.dto.DashboardWebDto;
import cl.duoc.bancoxyz.bff.web.dto.DetalleCuentaWebDto;
import cl.duoc.bancoxyz.bff.web.dto.TransaccionWebDto;
import cl.duoc.bancoxyz.model.Cuenta;
import cl.duoc.bancoxyz.model.MovimientoAnual;
import cl.duoc.bancoxyz.model.Transaccion;
import cl.duoc.bancoxyz.service.BancoService;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class WebBffService {

    private final BancoService bancoService;

    public WebBffService(BancoService bancoService) {
        this.bancoService = bancoService;
    }

    public DetalleCuentaWebDto obtenerDetalleWeb(Long cuentaId) {
        Cuenta cuenta = bancoService.obtenerCuentaPorId(cuentaId)
                .orElseThrow(() -> new IllegalArgumentException("Cuenta no encontrada con ID: " + cuentaId));

        List<TransaccionWebDto> transacciones = listarTodasTransaccionesWeb(cuentaId);

        long sobregiro = cuenta.getLineaSobregiro() != null ? cuenta.getLineaSobregiro() : 0L;
        long saldoTotal = cuenta.getSaldo() + sobregiro;
        double tasa = cuenta.getTasaInteres() != null ? cuenta.getTasaInteres() : 0.0;
        double interesEstimado = Math.round((cuenta.getSaldo() * (tasa / 100.0) / 12.0) * 100.0) / 100.0;

        List<MovimientoAnual> anuales = bancoService.obtenerMovimientosAnuales(cuentaId);

        Map<String, Object> metadatos = new HashMap<>();
        metadatos.put("canalRecomendado", "WEB_CLIENTES");
        metadatos.put("permiteTransferenciasMasivas", true);
        metadatos.put("descargaCartolaPDF", "/api/v1/web/cuentas/" + cuentaId + "/pdf");
        metadatos.put("notificacionesPendientes", 0);

        return new DetalleCuentaWebDto(
                cuenta.getCuentaId(),
                cuenta.getNombreTitular(),
                cuenta.getEdad(),
                cuenta.getTipo(),
                cuenta.getSaldo(),
                sobregiro,
                saldoTotal,
                tasa,
                interesEstimado,
                cuenta.getEstado(),
                transacciones.size(),
                transacciones,
                anuales,
                metadatos
        );
    }

    public List<TransaccionWebDto> listarTodasTransaccionesWeb(Long cuentaId) {
        return bancoService.obtenerTransaccionesPorCuenta(cuentaId).stream()
                .map(this::convertirTransaccionWeb)
                .collect(Collectors.toList());
    }

    public DashboardWebDto obtenerDashboardWeb() {
        List<Cuenta> todas = bancoService.obtenerTodasLasCuentas();
        int totalCuentas = todas.size();
        long capitalTotal = todas.stream().mapToLong(Cuenta::getSaldo).sum();
        long promedio = totalCuentas > 0 ? capitalTotal / totalCuentas : 0L;

        Map<String, Long> distribucion = todas.stream()
                .collect(Collectors.groupingBy(c -> c.getTipo().toUpperCase(), Collectors.counting()));

        return new DashboardWebDto(
                totalCuentas,
                capitalTotal,
                promedio,
                distribucion
        );
    }

    private TransaccionWebDto convertirTransaccionWeb(Transaccion tx) {
        String categoria = clasificarCategoria(tx.getDescripcion(), tx.getTipo());
        return new TransaccionWebDto(
                tx.getId(),
                tx.getFecha(),
                tx.getMonto(),
                tx.getTipo(),
                tx.getDescripcion(),
                tx.getCanal() != null ? tx.getCanal() : "CORE_BANCARIO",
                categoria
        );
    }

    private String clasificarCategoria(String desc, String tipo) {
        if (desc == null) return "General";
        String lower = desc.toLowerCase();
        if (lower.contains("supermercado") || lower.contains("almacen") || lower.contains("restaurante")) return "Alimentación";
        if (lower.contains("sueldo") || lower.contains("remuneracion") || lower.contains("deposito")) return "Ingresos";
        if (lower.contains("farmacia") || lower.contains("salud") || lower.contains("medico")) return "Salud";
        if (lower.contains("transferencia")) return "Transferencias";
        if (lower.contains("cajero") || lower.contains("retiro")) return "Efectivo";
        return "Servicios/Otros";
    }
}
