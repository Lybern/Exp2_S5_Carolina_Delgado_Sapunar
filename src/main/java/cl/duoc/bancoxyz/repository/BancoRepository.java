package cl.duoc.bancoxyz.repository;

import cl.duoc.bancoxyz.model.Cuenta;
import cl.duoc.bancoxyz.model.MovimientoAnual;
import cl.duoc.bancoxyz.model.Transaccion;
import org.springframework.stereotype.Repository;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;

@Repository
public class BancoRepository {

    private final Map<Long, Cuenta> cuentas = new ConcurrentHashMap<>();
    private final List<Transaccion> transacciones = Collections.synchronizedList(new ArrayList<>());
    private final List<MovimientoAnual> movimientosAnuales = Collections.synchronizedList(new ArrayList<>());

    // =========================================================================
    // GENERACIÓN CONCURRENTE Y DETERMINISTA DE IDENTIFICADORES:
    // AtomicLong garantiza atomicidad e incrementos seguros libres de condición
    // de carrera (thread-safe), evitando colisiones de IDs bancarios en memoria.
    // =========================================================================
    private final AtomicLong correlativoTransaccion = new AtomicLong(50000L);

    public Long generarSiguienteIdTransaccion() {
        return correlativoTransaccion.incrementAndGet();
    }

    public void guardarCuenta(Cuenta cuenta) {
        cuentas.put(cuenta.getCuentaId(), cuenta);
    }

    public Optional<Cuenta> buscarCuentaPorId(Long cuentaId) {
        return Optional.ofNullable(cuentas.get(cuentaId));
    }

    public List<Cuenta> obtenerTodasLasCuentas() {
        return new ArrayList<>(cuentas.values());
    }

    public void guardarTransaccion(Transaccion transaccion) {
        if (transaccion.getId() == null) {
            transaccion.setId(correlativoTransaccion.incrementAndGet());
        } else {
            // Asegura que el contador secuencial se mantenga por encima de los IDs importados del CSV
            correlativoTransaccion.updateAndGet(actual -> Math.max(actual, transaccion.getId()));
        }
        transacciones.add(transaccion);
    }

    public List<Transaccion> buscarTransaccionesPorCuenta(Long cuentaId) {
        return transacciones.stream()
                .filter(t -> t.getCuentaId() != null && t.getCuentaId().equals(cuentaId))
                .sorted(Comparator.comparing(Transaccion::getId, Comparator.nullsLast(Comparator.reverseOrder())))
                .collect(Collectors.toList());
    }

    public void guardarMovimientoAnual(MovimientoAnual movimiento) {
        movimientosAnuales.add(movimiento);
    }

    public List<MovimientoAnual> buscarMovimientosAnualesPorCuenta(Long cuentaId) {
        return movimientosAnuales.stream()
                .filter(m -> m.getCuentaId() != null && m.getCuentaId().equals(cuentaId))
                .collect(Collectors.toList());
    }
}
