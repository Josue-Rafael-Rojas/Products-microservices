#  Microservicios de productos

Este proyecto consiste en dos microservicios construidos con Spring Boot que interactúan entre sí utilizando el estándar JSON API para la comunicación. Los microservicios gestionan productos y su inventario en una arquitectura por capas.

## Descripción General de la Arquitectura

El sistema está compuesto por dos microservicios principales:

1. **Servicio de Productos (Puerto 8080)**
   - Gestiona la información de productos (operaciones CRUD)
   - Implementa la especificación JSON API para todas las respuestas
   - Maneja la validación de productos y gestión de errores
   - Proporciona listado de productos con paginación

2. **Servicio de Inventario (Puerto 8081)**
   - Gestiona el inventario de productos
   - Se comunica con el Servicio de Productos para obtener información
   - Implementa emisión de eventos para cambios en el inventario
   - Maneja la validación de inventario y gestión de errores

### Stack Tecnológico

- **Lenguaje:** Java con Spring Boot
- **Base de Datos:** PostgreSQL
- **Especificación API:** JSON API
- **Comunicación entre Servicios:** HTTP/REST con Feign Client
- **Autenticación:** Seguridad basada en API Key
- **Pruebas:** JUnit con pruebas de integración

### Base de Datos

Se eligió PostgreSQL como solución de base de datos por las siguientes razones:
- Fuerte cumplimiento ACID para la integridad de datos
- Rico conjunto de características para consultas complejas y relaciones de datos
- Excelente soporte para tipos de datos JSON
- Gestión robusta de transacciones
- Fuerte soporte de la comunidad y fiabilidad empresarial

## Prerrequisitos
- Java 17 o superior
- Gradle

## Instalación y Configuración

1. Clonar el repositorio:
```bash
git clone <url-repositorio>
cd springMicroservices
```

### Configuración del Entorno


 **Configuración de Base de Datos:**
   - Crear una base de datos llamada: `microservices`
   - Usuario por defecto: `postgres`
   - Contraseña por defecto: `123456789`
   - Puerto: `5432`

### Instrucciones de Ejecución

 **Clonar el repositorio:**
   ```bash
   git clone <url-del-repositorio>
   cd springMicroservices
   ```

 **Servicio de Productos (Puerto 8080):**
   ```bash
   cd products
   ./gradlew bootRun
   ```
   - La API estará disponible en: `http://localhost:8080`
   - Token de seguridad configurado en `application.yml`

 **Servicio de Inventario (Puerto 8081):**
   ```bash
   cd inventory/inventory
   ./gradlew bootRun
   ```
   - La API estará disponible en: `http://localhost:8081`
   - Requiere que el Servicio de Productos esté en ejecución

## Seguridad

Los servicios implementan autenticación basada en API key:
- Las API keys se configuran a través de archivo yaml.
- Cada servicio valida las solicitudes entrantes usando tokens de seguridad
- La comunicación entre servicios está asegurada usando tokens predefinidos

## Manejo de Errores

Ambos servicios implementan un manejo integral de errores:
- Respuestas de error compatibles con JSON API
- Errores de validación con mensajes detallados
- Manejo de errores de comunicación entre servicios
- Mecanismos de timeout y reintento

## Pruebas

El proyecto incluye pruebas unitarias y de integración:
- Pruebas unitarias para la lógica de la capa de servicio
- Pruebas de integración para endpoints de API
- Pruebas basadas en mocks para comunicación entre servicios
- Pruebas de interacción con base de datos

Para ejecutar las pruebas:
se requiere el IDE Intellij IDEA
```

## Documentación de la API

### Endpoints del Servicio de Productos

- `POST /api/products` - Crear un nuevo producto
- `GET /api/products/{id}` - Obtener un producto por ID
- `PUT /api/products/{id}` - Actualizar un producto
- `DELETE /api/products/{id}` - Eliminar un producto
- `GET /api/products` - Listar todos los productos (paginado)

### Endpoints del Servicio de Inventario

- `GET /api/inventory/{productId}` - Obtener inventario de un producto específico
- `PUT /api/inventory/{productId}` - Actualizar inventario de producto
- `PATCH /api/inventory/{productId}/purchase` - Procesar una compra y actualizar inventario
