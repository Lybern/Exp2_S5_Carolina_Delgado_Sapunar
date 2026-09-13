package cl.duoc.bancoxyz.model;

public class Transaccion {

    private Long id;
    private Long cuentaId;
    private String fecha;
    private Long monto;
    private String tipo;
    private String descripcion;
    private String canal;

    public Transaccion() {
    }

    public Transaccion(Long id, Long cuentaId, String fecha, Long monto, String tipo, String descripcion, String canal) {
        this.id = id;
        this.cuentaId = cuentaId;
        this.fecha = fecha;
        this.monto = monto;
        this.tipo = tipo;
        this.descripcion = descripcion;
        this.canal = canal;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getCuentaId() {
        return cuentaId;
    }

    public void setCuentaId(Long cuentaId) {
        this.cuentaId = cuentaId;
    }

    public String getFecha() {
        return fecha;
    }

    public void setFecha(String fecha) {
        this.fecha = fecha;
    }

    public Long getMonto() {
        return monto;
    }

    public void setMonto(Long monto) {
        this.monto = monto;
    }

    public String getTipo() {
        return tipo;
    }

    public void setTipo(String tipo) {
        this.tipo = tipo;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public void setDescripcion(String descripcion) {
        this.descripcion = descripcion;
    }

    public String getCanal() {
        return canal;
    }

    public void setCanal(String canal) {
        this.canal = canal;
    }
}
