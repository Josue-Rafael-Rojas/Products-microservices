# Prueba Técnica de Microservicios

Este proyecto consiste en dos microservicios construidos con Spring Boot que interactúan entre sí utilizando el estándar JSON API para la comunicación. Los microservicios gestionan productos y su inventario en una arquitectura de sistema distribuido.

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
- **Plataforma de Contenedores:** Docker
- **Comunicación entre Servicios:** HTTP/REST con Feign Client
- **Autenticación:** Seguridad basada en API Key
- **Pruebas:** JUnit con pruebas de integración

### Justificación de la Base de Datos

Se eligió PostgreSQL como solución de base de datos por las siguientes razones:
- Fuerte cumplimiento ACID para la integridad de datos
- Rico conjunto de características para consultas complejas y relaciones de datos
- Excelente soporte para tipos de datos JSON
- Gestión robusta de transacciones
- Fuerte soporte de la comunidad y fiabilidad empresarial

## Prerrequisitos

- Docker y Docker Compose
- Java 17 o superior
- Gradle

## Instalación y Configuración

1. Clonar el repositorio:
```bash
git clone <url-repositorio>
cd springMicroservices
```

2. Construir y ejecutar los servicios usando Docker Compose:
```bash
docker-compose up --build
```

Esto iniciará:
- Base de datos PostgreSQL
- Servicio de Productos en el puerto 8080
- Servicio de Inventario en el puerto 8081

## Configuración de Servicios

### Servicio de Productos
- URL Base: `http://localhost:8080`
- Base de Datos: PostgreSQL
- Token de Seguridad: Configurado a través de variables de entorno

### Servicio de Inventario
- URL Base: `http://localhost:8081`
- Base de Datos: PostgreSQL
- Depende del Servicio de Productos
- Configurado con mecanismos de reintento y timeout

## Seguridad

Los servicios implementan autenticación basada en API key:
- Las API keys se configuran a través de variables de entorno
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
```bash
# Para el Servicio de Productos
cd products
./gradlew test

# Para el Servicio de Inventario
cd inventory/inventory
./gradlew test
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
- `POST /api/inventory/purchase` - Procesar una compra y actualizar inventario

## Mejoras Futuras

1. Implementar caché para datos frecuentemente accedidos
2. Agregar métricas y monitoreo
3. Implementar circuit breakers para la comunicación entre servicios
4. Agregar versionado de API
5. Mejorar las capacidades de logging y trazabilidad

## Cómo Contribuir

1. Hacer fork del repositorio
2. Crear tu rama de características (`git checkout -b feature/CaracteristicaIncreible`)
3. Hacer commit de tus cambios (`git commit -m 'Agregar alguna CaracteristicaIncreible'`)
4. Hacer push a la rama (`git push origin feature/CaracteristicaIncreible`)
5. Abrir un Pull Request

## Licencia

Este proyecto está licenciado bajo la Licencia MIT - ver el archivo LICENSE para más detalles