package cl.duoc.bancoxyz.service;

import cl.duoc.bancoxyz.model.Cuenta;
import cl.duoc.bancoxyz.model.MovimientoAnual;
import cl.duoc.bancoxyz.model.Transaccion;
import cl.duoc.bancoxyz.repository.BancoRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Service
public class BancoService {

    private final BancoRepository bancoRepository;

    public BancoService(BancoRepository bancoRepository) {
        this.bancoRepository = bancoRepository;
    }

    public List<Cuenta> obtenerTodasLasCuentas() {
        return bancoRepository.obtenerTodasLasCuentas();
    }

    public Optional<Cuenta> obtenerCuentaPorId(Long cuentaId) {
        return bancoRepository.buscarCuentaPorId(cuentaId);
    }

    public List<Transaccion> obtenerTransaccionesPorCuenta(Long cuentaId) {
        return bancoRepository.buscarTransaccionesPorCuenta(cuentaId);
    }

    public List<MovimientoAnual> obtenerMovimientosAnuales(Long cuentaId) {
        return bancoRepository.buscarMovimientosAnualesPorCuenta(cuentaId);
    }

    // =========================================================================
    // CAPA DE DOMINIO BANCARIO (CORE BANKING DOMAIN):
    // Responsabilidad: Encapsular y ejecutar las reglas de negocio financieras
    // independientemente del canal de acceso (Web, Móvil o ATM).
    // =========================================================================

    /**
     * Calcula el monto máximo disponible para retiro según el saldo y las políticas del cajero.
     */
    public long calcularLimiteGiroATM(Long cuentaId, long limiteMaximoGiro) {
        Cuenta cuenta = obtenerCuentaPorId(cuentaId)
                .orElseThrow(() -> new IllegalArgumentException("Tarjeta o cuenta no válida: " + cuentaId));
        long saldo = cuenta.getSaldo() != null ? cuenta.getSaldo() : 0L;
        return Math.min(saldo, limiteMaximoGiro);
    }

    /**
     * Calcula la estimación mensual de intereses ganados según la tasa pactada de la cuenta.
     */
    public double calcularInteresMensualEstimado(Cuenta cuenta) {
        if (cuenta == null || cuenta.getSaldo() == null || cuenta.getTasaInteres() == null) {
            return 0.0;
        }
        double tasa = cuenta.getTasaInteres();
        return Math.round((cuenta.getSaldo() * (tasa / 100.0) / 12.0) * 100.0) / 100.0;
    }

    /**
     * Procesa retiros bancarios para el canal de cajeros automáticos (ATM),
     * validando reglas financieras (múltiplos de dispensación y topes de seguridad).
     */
    public synchronized Transaccion procesarRetiroATM(Long cuentaId, Long monto, String pin, String terminalId, long limiteMaximo, long multiploBillete) {
        if (monto == null || monto <= 0) {
            throw new IllegalArgumentException("El monto a retirar debe ser mayor a $0.");
        }

        // Regla bancaria: Los cajeros solo pueden dispensar denominaciones válidas
        if (multiploBillete > 0 && monto % multiploBillete != 0) {
            throw new IllegalArgumentException("El monto solicitado ($" + monto + ") debe ser múltiplo de $" + multiploBillete + " (denominaciones de billetes válidas).");
        }

        // Regla bancaria: Límite máximo operativo por giro en cajero
        if (limiteMaximo > 0 && monto > limiteMaximo) {
            throw new IllegalArgumentException("El monto solicitado ($" + monto + ") supera el límite máximo por giro en cajero ($" + limiteMaximo + ").");
        }

        return procesarRetiro(
                cuentaId,
                monto,
                "CAJERO_ATM",
                "Giro ATM Terminal " + (terminalId != null ? terminalId : "ATM-GENERIC")
        );
    }

    public synchronized Transaccion procesarRetiro(Long cuentaId, Long monto, String canal, String detalleTerminal) {
        if (monto == null || monto <= 0) {
            throw new IllegalArgumentException("El monto a retirar debe ser mayor a $0.");
        }

        Cuenta cuenta = bancoRepository.buscarCuentaPorId(cuentaId)
                .orElseThrow(() -> new IllegalArgumentException("La cuenta número " + cuentaId + " no existe."));

        long saldoDisponibleTotal = cuenta.getSaldo() + (cuenta.getLineaSobregiro() != null ? cuenta.getLineaSobregiro() : 0L);
        if (saldoDisponibleTotal < monto) {
            throw new IllegalStateException("Fondos insuficientes. Saldo actual: $" + cuenta.getSaldo());
        }

        cuenta.setSaldo(cuenta.getSaldo() - monto);

        // Generación determinista y concurrente de identificador único con AtomicLong
        Long nuevoIdTransaccion = bancoRepository.generarSiguienteIdTransaccion();

        Transaccion tx = new Transaccion(
                nuevoIdTransaccion,
                cuentaId,
                LocalDate.now(),
                monto,
                "retiro",
                detalleTerminal != null ? detalleTerminal : "Retiro de efectivo en " + canal,
                canal
        );

        bancoRepository.guardarTransaccion(tx);
        return tx;
    }

    public synchronized Transaccion procesarDeposito(Long cuentaId, Long monto, String canal) {
        if (monto == null || monto <= 0) {
            throw new IllegalArgumentException("El monto a depositar debe ser mayor a $0.");
        }

        Cuenta cuenta = bancoRepository.buscarCuentaPorId(cuentaId)
                .orElseThrow(() -> new IllegalArgumentException("La cuenta número " + cuentaId + " no existe."));

        cuenta.setSaldo(cuenta.getSaldo() + monto);

        // Generación segura de ID sin colisión por milisegundos
        Long nuevoIdTransaccion = bancoRepository.generarSiguienteIdTransaccion();

        Transaccion tx = new Transaccion(
                nuevoIdTransaccion,
                cuentaId,
                LocalDate.now(),
                monto,
                "abono",
                "Depósito de fondos vía " + canal,
                canal
        );

        bancoRepository.guardarTransaccion(tx);
        return tx;
    }

    public synchronized Transaccion procesarTransferencia(Long cuentaOrigenId, Long cuentaDestinoId, Long monto, String descripcion) {
        if (cuentaOrigenId.equals(cuentaDestinoId)) {
            throw new IllegalArgumentException("No se puede transferir a la misma cuenta de origen.");
        }

        if (monto == null || monto <= 0) {
            throw new IllegalArgumentException("El monto a transferir debe ser mayor a $0.");
        }

        Cuenta origen = bancoRepository.buscarCuentaPorId(cuentaOrigenId)
                .orElseThrow(() -> new IllegalArgumentException("Cuenta origen no encontrada: " + cuentaOrigenId));

        Cuenta destino = bancoRepository.buscarCuentaPorId(cuentaDestinoId)
                .orElseThrow(() -> new IllegalArgumentException("Cuenta destino no encontrada: " + cuentaDestinoId));

        long saldoDisponible = origen.getSaldo() + (origen.getLineaSobregiro() != null ? origen.getLineaSobregiro() : 0L);
        if (saldoDisponible < monto) {
            throw new IllegalStateException("Saldo insuficiente para transferir $" + monto);
        }

        origen.setSaldo(origen.getSaldo() - monto);
        destino.setSaldo(destino.getSaldo() + monto);

        // Generación de IDs secuenciales y atómicos para el par de transacciones contables
        Long idTxOrigen = bancoRepository.generarSiguienteIdTransaccion();
        Long idTxDestino = bancoRepository.generarSiguienteIdTransaccion();

        Transaccion txOrigen = new Transaccion(
                idTxOrigen,
                cuentaOrigenId,
                LocalDate.now(),
                monto,
                "transferencia_saliente",
                descripcion != null ? descripcion : "Transferencia a cuenta " + cuentaDestinoId,
                "MOVIL"
        );
        bancoRepository.guardarTransaccion(txOrigen);

        Transaccion txDestino = new Transaccion(
                idTxDestino,
                cuentaDestinoId,
                LocalDate.now(),
                monto,
                "transferencia_entrante",
                "Transferencia recibida desde cuenta " + cuentaOrigenId,
                "MOVIL"
        );
        bancoRepository.guardarTransaccion(txDestino);

        return txOrigen;
    }
}
