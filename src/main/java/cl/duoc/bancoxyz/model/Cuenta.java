package cl.duoc.bancoxyz.model;

public class Cuenta {

    private Long cuentaId;
    private String nombreTitular;
    private Long saldo;
    private Integer edad;
    private String tipo;
    private Long lineaSobregiro;
    private Double tasaInteres;
    private String estado;

    public Cuenta() {
    }

    public Cuenta(Long cuentaId, String nombreTitular, Long saldo, Integer edad, String tipo, Long lineaSobregiro, Double tasaInteres, String estado) {
        this.cuentaId = cuentaId;
        this.nombreTitular = nombreTitular;
        this.saldo = saldo;
        this.edad = edad;
        this.tipo = tipo;
        this.lineaSobregiro = lineaSobregiro;
        this.tasaInteres = tasaInteres;
        this.estado = estado;
    }

    public Long getCuentaId() {
        return cuentaId;
    }

    public void setCuentaId(Long cuentaId) {
        this.cuentaId = cuentaId;
    }

    public String getNombreTitular() {
        return nombreTitular;
    }

    public void setNombreTitular(String nombreTitular) {
        this.nombreTitular = nombreTitular;
    }

    public Long getSaldo() {
        return saldo;
    }

    public void setSaldo(Long saldo) {
        this.saldo = saldo;
    }

    public Integer getEdad() {
        return edad;
    }

    public void setEdad(Integer edad) {
        this.edad = edad;
    }

    public String getTipo() {
        return tipo;
    }

    public void setTipo(String tipo) {
        this.tipo = tipo;
    }

    public Long getLineaSobregiro() {
        return lineaSobregiro;
    }

    public void setLineaSobregiro(Long lineaSobregiro) {
        this.lineaSobregiro = lineaSobregiro;
    }

    public Double getTasaInteres() {
        return tasaInteres;
    }

    public void setTasaInteres(Double tasaInteres) {
        this.tasaInteres = tasaInteres;
    }

    public String getEstado() {
        return estado;
    }

    public void setEstado(String estado) {
        this.estado = estado;
    }
}
