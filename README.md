# Banco XYZ - Arquitectura Backend for Frontend (BFF) con Seguridad Integral
### Asignatura: Desarrollo Backend III (PBY2203) - Experiencia 2 / Semana 5
**Autora:** Carolina Delgado Sapunar  
**Repositorio GitHub:** [https://github.com/Lybern/Exp2_S5_Carolina_Delgado_Sapunar](https://github.com/Lybern/Exp2_S5_Carolina_Delgado_Sapunar)

---

## 1. Descripción General del Proyecto
Este proyecto implementa y consolida la arquitectura **Backend for Frontend (BFF)** y **Seguridad Integral** para el sistema financiero del **Banco XYZ**. 

El sistema proporciona capas de servicios especializadas, adaptadas a los requisitos funcionales y restricciones técnicas de cada canal de atención (Web, Móvil, Cajeros Automáticos), incorporando:
1. **Cifrado en tránsito (HTTPS / TLS):** Certificado digital PKCS12 en puerto seguro **`8443`** con protección de claves criptográficas (`blanquito123`).
2. **Autenticación y Autorización JWT (Spring Security 6+ / Boot 4+):** Emisión de tokens firmados mediante algoritmo HMAC-SHA256 con protección de audiencia (`aud`) y roles por canal (`ROLE_WEB`, `ROLE_MOVIL`, `ROLE_ATM`, `ROLE_ADMIN`).
3. **Comunicación Segura entre BFF y Microservicios (Delegated Service Tokens):** Autenticación bidireccional y delegación de contexto mediante clave secreta compartida (`jwt.service-secret`) y cliente moderno `RestClient`.
4. **Validación Estricta y Manejo Global de Errores:** Validación declarativa con Jakarta Bean Validation (`@Valid`, `@Pattern`, `@NotNull`) y respuestas uniformes `RFC 7807` vía `ResponseEntityExceptionHandler`.
5. **Generación Concurrente Segura y Normalización Temporal:** IDs thread-safe con `AtomicLong` y fechas estandarizadas en formato ISO-8601 (`java.time.LocalDate`).

---

## 2. Matriz de Canales, Credenciales y Roles de Seguridad

| Canal / Tipo | Usuario | Contraseña | Rol Spring Security | Audiencia (`aud`) | Ruta Base |
| :--- | :--- | :--- | :--- | :--- | :--- |
| 🌐 **BFF Web** | `usuario_web` | `web123` | `ROLE_WEB` | `WEB` | `/api/v1/web/**` |
| 📱 **BFF Móvil** | `usuario_movil` | `movil123` | `ROLE_MOVIL` | `MOVIL` | `/api/v1/movil/**` |
| 🏧 **BFF Cajero ATM** | `operador_atm` | `atm123` | `ROLE_ATM` | `ATM` | `/api/v1/cajero/**` |
| 👑 **Administrador** | `admin_general` | `admin123` | `ROLE_ADMIN` (todos) | `WEB`, `MOVIL`, `ATM` | Todos los endpoints |

> **Nota de Seguridad:** Las contraseñas se almacenan cifradas con algoritmo **BCrypt** (factor de costo 10). La validación de audiencia (`aud`) previene que un token emitido para la App Móvil sea utilizado fraudulentamente en el Portal Web o en Cajeros Automáticos.

---

## 3. Arquitectura de Seguridad y Comunicación Bidireccional

```
                              ┌────────────────────────────────────────────────────────┐
                              │                 CLIENTES BANCARIOS                     │
                              └───────┬────────────────────────┬───────────────┬───────┘
                                      │ (1) Login HTTPS / TLS  │               │
                                      │     Puerto 8443        │               │
                                      ▼                        ▼               ▼
                              ┌───────────────┐        ┌───────────────┐ ┌───────────────┐
                              │   BFF Web     │        │   BFF Móvil   │ │  BFF Cajero   │
                              │ (/api/v1/web) │        │(/api/v1/movil)│ │(/api/v1/cajero)
                              └───────┬───────┘        └───────┬───────┘ └───────┬───────┘
                                      │                        │                 │
                                      └────────────────────────┼─────────────────┘
                                                               │ (2) Intercambio de Contexto:
                                                               │     Genera Service Token
                                                               │     (aud="core-bancario", iss="bff-*")
                                                               │     Firma: 'jwt.service-secret'
                                                               ▼
                                              ┌─────────────────────────────────┐
                                              │      CORE BANCARIO CLIENT       │
                                              │   (RestClient HTTPS / TLS)      │
                                              └────────────────┬────────────────┘
                                                               │ (3) Canal HTTPS Cifrado +
                                                               │     Authorization: Bearer <ServiceToken>
                                                               ▼
                                              ┌─────────────────────────────────┐
                                              │    MICROSERVICIOS / DOMINIO     │
                                              │  - Validación de firma secreta  │
                                              │  - Ejecución transaccional      │
                                              │  - Repositorio concurrente      │
                                              └─────────────────────────────────┘
```

---

## 4. Estructura del Código Fuente

```text
src/main/java/cl/duoc/bancoxyz/
├── BancoBffApplication.java              # Clase principal Spring Boot
├── auth/
│   └── dto/                             # DTOs de inicio de sesión y Service Tokens
│       ├── LoginRequestDto.java
│       ├── LoginResponseDto.java
│       ├── ServiceTokenRequestDto.java
│       └── ServiceTokenResponseDto.java
├── client/                              # Cliente de integración con microservicios
│   └── CoreBancarioClient.java          # Inyector de Service Tokens vía RestClient
├── config/                              # Configuraciones centrales
│   ├── JwtProperties.java               # Mapeo tipado de application.properties
│   ├── OpenApiConfig.java               # Configuración OpenAPI 3 con Bearer JWT
│   ├── RestClientConfig.java            # Configuración de RestClient HTTP/HTTPS
│   ├── SecurityConfig.java              # Cadena de filtros Spring Security, CORS y RBAC
│   ├── SeguridadCanalesInterceptor.java # Interceptor de auditoría por canal
│   └── WebMvcConfig.java                # Registro de interceptores Spring MVC
├── controller/
│   └── AuthController.java              # Endpoints públicos /api/auth/login y /api/auth/service-token
├── exception/                           # Gestión global de errores y validación
│   ├── ErrorRespuestaDto.java           # Formato uniforme RFC 7807
│   └── ManejadorExcepcionesGlobal.java  # ResponseEntityExceptionHandler y @ExceptionHandler
├── model/                               # Entidades de dominio bancario
│   ├── Cuenta.java
│   ├── MovimientoAnual.java
│   └── Transaccion.java
├── repository/                          # Capa de persistencia y carga de datos
│   ├── BancoRepository.java             # Almacén concurrente thread-safe con AtomicLong
│   └── CargadorDatosLegacy.java         # Parser de archivos CSV legacy
├── security/                            # Lógica criptográfica y filtros
│   ├── JwtAuthenticationFilter.java     # Filtro OncePerRequestFilter con validación de audiencia
│   └── JwtTokenUtil.java                # Generador/validador JJWT 0.12.6 y Service Tokens
├── service/                             # Capa de negocio central
│   └── BancoService.java                # Lógica transaccional de depósitos y retiros
└── bff/                                 # Módulos Backend for Frontend por canal
    ├── cajero/                          # 🏧 Módulo Cajero Automático
    │   ├── controller/CajeroController.java
    │   ├── dto/ (ConsultaSaldoCajeroDto, SolicitudRetiroCajeroDto, RespuestaRetiroDto)
    │   └── service/CajeroBffService.java
    ├── movil/                           # 📱 Módulo App Móvil
    │   ├── controller/MovilController.java
    │   ├── dto/ (ResumenCuentaMovilDto, TransaccionMovilDto, SolicitudTransferenciaMovilDto)
    │   └── service/MovilBffService.java
    └── web/                             # 🌐 Módulo Portal Web
        ├── controller/WebController.java
        ├── dto/ (DashboardWebDto, DetalleCuentaWebDto, TransaccionWebDto)
        └── service/WebBffService.java
```

---

## 5. Instrucciones de Compilación y Ejecución

### Requisitos Previos:
* Java JDK 21 o superior.
* Maven Wrapper (incluido en el repositorio).
* Certificado digital PKCS12 (`keystore.p12`) ubicado en `src/main/resources/`.

### Comandos de Ejecución:
1. **Compilar y verificar todas las pruebas automatizadas:**
   ```bash
   ./mvnw clean test
   ```
2. **Ejecutar la aplicación Spring Boot:**
   ```bash
   ./mvnw spring-boot:run
   ```
3. La aplicación iniciará en el puerto seguro **`8443`** bajo el protocolo **`HTTPS`**.

---

## 6. Documentación Interactiva OpenAPI / Swagger UI

* 🔗 **Swagger UI (HTTPS Seguro):** [https://localhost:8443/swagger-ui/index.html](https://localhost:8443/swagger-ui/index.html)
* 🔗 **OpenAPI Schema JSON:** [https://localhost:8443/v3/api-docs](https://localhost:8443/v3/api-docs)

> Para interactuar con los endpoints protegidos en Swagger UI, haga clic en el botón **Authorize 🔒**, obtenga un token mediante `/api/auth/login` e ingréselo como `Bearer <token>`.

---

## 7. Guía de Pruebas de Endpoints (cURL)

> **Nota:** Debido a que se utiliza un certificado autofirmado en entorno de desarrollo, agregue el parámetro `-k` o `--insecure` en los comandos cURL.

### 1. Autenticación y Obtención de Token JWT

* **Login para Canal Móvil:**
  ```bash
  curl -k -X POST "https://localhost:8443/api/auth/login" \
       -H "Content-Type: application/json" \
       -d '{"username": "usuario_movil", "password": "movil123", "canal": "MOVIL"}'
  ```

* **Login para Canal Web:**
  ```bash
  curl -k -X POST "https://localhost:8443/api/auth/login" \
       -H "Content-Type: application/json" \
       -d '{"username": "usuario_web", "password": "web123", "canal": "WEB"}'
  ```

* **Login para Operador de Cajero ATM:**
  ```bash
  curl -k -X POST "https://localhost:8443/api/auth/login" \
       -H "Content-Type: application/json" \
       -d '{"username": "operador_atm", "password": "atm123", "canal": "ATM"}'
  ```

### 2. Consumo de Endpoints Protegidos por Canal

* **BFF Móvil - Resumen Ligero de Cuenta:**
  ```bash
  curl -k -X GET "https://localhost:8443/api/v1/movil/cuentas/101" \
       -H "Authorization: Bearer <TOKEN_MOVIL>"
  ```

* **BFF Móvil - Transferencia con Bean Validation:**
  ```bash
  curl -k -X POST "https://localhost:8443/api/v1/movil/cuentas/101/transferencia" \
       -H "Authorization: Bearer <TOKEN_MOVIL>" \
       -H "Content-Type: application/json" \
       -d '{"cuentaDestinoId": 102, "monto": 25000, "comentario": "Pago arriendo"}'
  ```

* **BFF Web - Dashboard Financiero Consolidado:**
  ```bash
  curl -k -X GET "https://localhost:8443/api/v1/web/dashboard" \
       -H "Authorization: Bearer <TOKEN_WEB>"
  ```

* **BFF Cajero ATM - Retiro de Efectivo (Múltiplos de $5.000 y PIN de 4 dígitos):**
  ```bash
  curl -k -X POST "https://localhost:8443/api/v1/cajero/cuentas/101/retiro" \
       -H "Authorization: Bearer <TOKEN_ATM>" \
       -H "Content-Type: application/json" \
       -d '{"monto": 40000, "pin": "1234", "terminalId": "ATM-SCL-CENTRO-01"}'
  ```

### 3. Emisión de Token Delegado de Servicio (BFF ➔ Core Bancario)

* **Generación de Service Token:**
  ```bash
  curl -k -X POST "https://localhost:8443/api/auth/service-token" \
       -H "Content-Type: application/json" \
       -d '{"emisorBff": "bff-movil", "username": "usuario_movil"}'
  ```
