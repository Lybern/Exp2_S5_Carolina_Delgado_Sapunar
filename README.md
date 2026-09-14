# Banco XYZ - Arquitectura Backend for Frontend (BFF) Multi-Módulo con Seguridad Integral
### Asignatura: Desarrollo Backend III (PBY2203) - Experiencia 2 / Semana 5
**Autora:** Carolina Delgado Sapunar  
**Repositorio GitHub:** [https://github.com/Lybern/Exp2_S5_Carolina_Delgado_Sapunar](https://github.com/Lybern/Exp2_S5_Carolina_Delgado_Sapunar)

---

## 1. Descripción de la Solución

Este proyecto implementa una arquitectura **Backend for Frontend (BFF) Multi-Módulo y Distribuida** para el **Banco XYZ**, desacoplando los canales de atención en microservicios independientes y altamente especializados, comunicados de forma cifrada con el **Core Bancario**:

```
                                    +-----------------------+
                                    |   CLIENTES BANCARIOS  |
                                    +-----------+-----------+
                                                |
                 +------------------------------+------------------------------+
                 | (HTTPS / TLS Puerto 8443)    | (HTTPS / TLS Puerto 8444)    | (HTTPS / TLS Puerto 8445)
                 v                              v                              v
        +------------------+           +------------------+           +------------------+
        |    BFF MOVIL     |           |     BFF WEB      |           |    BFF CAJERO    |
        |  (Puerto: 8443)  |           |  (Puerto: 8444)  |           |  (Puerto: 8445)  |
        | * Top 3 Movim.   |           | * Dashboard Web  |           | * Giros $5.000   |
        | * Payload Ligero |           | * Tasas y Anual  |           | * Valida PIN 4D  |
        | * aud: "MOVIL"   |           | * aud: "WEB"/CSP |           | * aud: "ATM"     |
        +--------+---------+           +--------+---------+           +--------+---------+
                 |                              |                              |
                 +------------------------------+------------------------------+
                                                |
                                                | Token Exchange: Delegated Service Token
                                                | (Firma: 'jwt.service-secret' | aud: 'core-bancario')
                                                v
                                    +-----------------------+
                                    |     CORE SERVICE      |
                                    |    (Puerto: 8080)     |
                                    | * BancoRepository     |
                                    |   (AtomicLong / Mem)  |
                                    | * Datos Legacy CSV    |
                                    | * ServiceTokenFilter  |
                                    +-----------------------+
```

---

## 2. Microservicios del Sistema

| Microservicio | Puerto | Protocolo | Responsabilidad Principal |
| :--- | :---: | :---: | :--- |
| **`core-service`** | `8080` | HTTP / ServiceToken | Core transaccional y persistencia legacy (`intereses.csv`, `transacciones.csv`, `cuentas_anuales.csv`). Protegido por `ServiceTokenFilter` con clave secreta compartida. |
| **`bff-movil`** | `8443` | HTTPS / PKCS12 | BFF exclusivo para smartphones. Respuestas ultraligeras (Top 3 transacciones, transferencias). Protegido por Spring Security con audiencia `MOVIL`. |
| **`bff-web`** | `8444` | HTTPS / PKCS12 | BFF para navegadores web. Dashboard consolidado de administración, desglose anual con tasas y cabeceras OWASP (CSP, HSTS, `X-Frame-Options: DENY`). Audiencia `WEB`. |
| **`bff-cajero`** | `8445` | HTTPS / PKCS12 | BFF para cajeros automáticos (ATM). Giros en múltiplos de $5.000, validación regex de PIN de 4 dígitos (`^\d{4}$`) y límites de dispensación. Audiencia `ATM`. |

---

## 3. Matriz de Credenciales y Roles de Seguridad

| Microservicio / Canal | Usuario | Contraseña | Rol Spring Security | Audiencia JWT (`aud`) | URL Swagger UI |
| :--- | :--- | :--- | :--- | :--- | :--- |
| 📱 **BFF Móvil** | `usuario_movil` | `movil123` | `ROLE_MOVIL` | `MOVIL` | `https://localhost:8443/swagger-ui/index.html` |
| 🌐 **BFF Web** | `usuario_web` | `web123` | `ROLE_WEB` | `WEB` | `https://localhost:8444/swagger-ui/index.html` |
| 🏧 **BFF Cajero ATM** | `operador_atm` | `atm123` | `ROLE_ATM` | `ATM` | `https://localhost:8445/swagger-ui/index.html` |
| 🏢 **Core Service** | Interno | - | `SERVICE_TOKEN` | `core-bancario` | `http://localhost:8080/swagger-ui/index.html` |

---

## 4. Instrucciones de Compilación y Ejecución

### 1. Compilar y Ejecutar Pruebas Automatizadas:
```bash
./mvnw clean test
```
> Ejecuta la suite de pruebas automatizadas en los 4 microservicios con **`BUILD SUCCESS`**.

### 2. Ejecutar los Microservicios:
Puedes iniciar cada microservicio en terminales independientes:

* **Terminal 1 - Core Service (Puerto 8080):**
  ```bash
  ./mvnw spring-boot:run -pl core-service
  ```

* **Terminal 2 - BFF Móvil (Puerto HTTPS 8443):**
  ```bash
  ./mvnw spring-boot:run -pl bff-movil
  ```

* **Terminal 3 - BFF Web (Puerto HTTPS 8444):**
  ```bash
  ./mvnw spring-boot:run -pl bff-web
  ```

* **Terminal 4 - BFF Cajero ATM (Puerto HTTPS 8445):**
  ```bash
  ./mvnw spring-boot:run -pl bff-cajero
  ```

---

## 5. Ejemplos de Pruebas con cURL

### A. Canal Móvil (HTTPS 8443)
1. **Login Móvil:**
   ```bash
   curl -k -X POST "https://localhost:8443/api/auth/login" \
        -H "Content-Type: application/json" \
        -d '{"username": "usuario_movil", "password": "movil123"}'
   ```
2. **Consultar Resumen de Cuenta (Top 3 Movimientos):**
   ```bash
   curl -k -X GET "https://localhost:8443/api/v1/movil/cuentas/101" \
        -H "Authorization: Bearer <TOKEN_MOVIL>"
   ```

### B. Canal Web (HTTPS 8444)
1. **Login Web:**
   ```bash
   curl -k -X POST "https://localhost:8444/api/auth/login" \
        -H "Content-Type: application/json" \
        -d '{"username": "usuario_web", "password": "web123"}'
   ```
2. **Consultar Dashboard Global con Métricas:**
   ```bash
   curl -k -X GET "https://localhost:8444/api/v1/web/dashboard" \
        -H "Authorization: Bearer <TOKEN_WEB>"
   ```

### C. Canal Cajero ATM (HTTPS 8445)
1. **Login Cajero:**
   ```bash
   curl -k -X POST "https://localhost:8445/api/auth/login" \
        -H "Content-Type: application/json" \
        -d '{"username": "operador_atm", "password": "atm123"}'
   ```
2. **Retiro de Efectivo (PIN 4 dígitos y múltiplos de $5.000):**
   ```bash
   curl -k -X POST "https://localhost:8445/api/v1/cajero/cuentas/101/retiro" \
        -H "Authorization: Bearer <TOKEN_ATM>" \
        -H "Content-Type: application/json" \
        -d '{"monto": 40000, "pin": "1234", "terminalId": "ATM-SCL-CENTRO-01"}'
   ```
