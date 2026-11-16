# Productos: API con Spring Boot, JPA y DTO

Ejemplo de DSY1104 para conectar React con Axios. Es la versión Spring Boot de la [API con Node.js y Express](https://github.com/ProfeMike-sudo/dsy1104-productos-api-express): mismo contrato, mismo puerto y mismos errores, así que el frontend funciona sin cambios con cualquiera de las dos. Implementa el CRUD completo de productos. El frontend de la clase permite guardar y listar.

Frontend: https://github.com/ProfeMike-sudo/dsy1104-productos-react-axios

## Requisitos

Java 21 o superior. No hace falta instalar Maven (el proyecto trae `mvnw`) ni una base de datos. Comprueba la versión con `java -version`.

## Ejecutar en tu computador

```bash
git clone https://github.com/ProfeMike-sudo/dsy1104-productos-api-springboot.git
cd dsy1104-productos-api-springboot
./mvnw spring-boot:run
```

La API escucha en `http://localhost:3000/api`. La primera ejecución descarga dependencias. En Windows usa `mvnw.cmd`.

## Ejecutar en Ubuntu EC2

Entra a la instancia con Visual Studio Code, instala Java 21 (`sudo apt install -y openjdk-21-jdk`), clona el repositorio y define el origen del frontend antes de iniciar:

```bash
export CORS_ORIGINS=http://IP_PUBLICA_FRONTEND:5173
./mvnw spring-boot:run
```

Reemplaza `IP_PUBLICA_FRONTEND`. El origen no lleva `/api` ni barra final. Para varios orígenes exactos, sepáralos con comas. En el grupo de seguridad del backend permite TCP 3000 desde la IP pública de salida del computador que abre el navegador: Axios corre en ese navegador. Conserva SSH 22 según la configuración de clase. Reinicia el backend si cambias una variable.

Prueba primero en la terminal de EC2:

```bash
curl http://localhost:3000/api/health
curl http://localhost:3000/api/productos
```

Variables disponibles: `PORT` (3000), `HOST` (0.0.0.0), `CORS_ORIGINS` (`http://localhost:5173`) y `DATA_URL` (`jdbc:h2:file:./data/productos`). Ctrl+C detiene el proceso; no se configura arranque automático.

## Contrato y DTO

Producto de entrada para POST y PUT:

```json
{ "nombre": "Cuaderno", "precio": 2500, "stock": 8 }
```

- `nombre`: texto, entre 2 y 80 caracteres después de quitar espacios laterales.
- `precio`: número entre 0 y 100000000; los ejemplos usan CLP.
- `stock`: entero entre 0 y 1000000.
- Los tres campos son obligatorios. `ProductoEntradaDTO` solo declara esos tres: cualquier otro dato, como un `id` impuesto por el cliente, se ignora. El id lo genera el servidor.
- Las anotaciones `@Valid`, `@Size`, `@DecimalMin`, `@Min` y similares validan en el servidor aunque React ya haya validado.

El DTO de salida (`ProductoSalidaDTO`) contiene `id`, `nombre`, `precio` y `stock`. La entidad `Producto` guarda además `fechaCreacion`, que no se devuelve. Un DTO describe los datos que cruzan la API; no es una tabla de base de datos.

| Método | Ruta | Resultado |
| --- | --- | --- |
| GET | `/api/health` | 200, estado de la API |
| GET | `/api/productos` | 200, arreglo (inicialmente vacío) |
| GET | `/api/productos/{id}` | 200, un producto; 404 si no existe |
| POST | `/api/productos` | 201, producto creado y encabezado Location |
| PUT | `/api/productos/{id}` | 200, reemplaza nombre, precio y stock; 404 si no existe |
| DELETE | `/api/productos/{id}` | 204, sin cuerpo; 404 si no existe |

Entrada inválida: 400 con `{ "mensaje": "Producto inválido.", "detalles": [...] }`. JSON mal formado o con tipos incorrectos: 400. Error interno: 500 sin exponer detalles del servidor. Para PUT envía todos los campos, no solo el que cambió.

Importa `postman/productos.postman_collection.json`. Ajusta `baseUrl` a la dirección del backend y ejecuta las solicitudes en orden. Crear guarda automáticamente `productoId` para consultar, actualizar y eliminar.

## Organización

```text
src/main/java/com/duoc/productos/
  controller/   Métodos HTTP y códigos de respuesta
  dto/          Datos de entrada con validación y datos de salida
  service/      Operaciones del producto
  repository/   Interfaz JpaRepository: Spring genera el SQL
  model/        Entidad Producto, la fila de la base de datos
  exception/    Respuestas de error con la forma { mensaje, detalles }
  config/       CORS
```

## Persistencia y alcance de la demostración

Los productos quedan en `data/productos.mv.db`, una base H2 en archivo creada automáticamente y excluida de Git. Reiniciar el proceso conserva los datos. Usa un único proceso sobre ese archivo. H2 en archivo sirve para la clase, no para varios servidores ni producción; el proyecto semestral debe usar su propio motor de base de datos (por ejemplo MySQL, cambiando `DATA_URL` y las credenciales).

Esta práctica no incorpora login ni JWT. CORS permite al navegador leer respuestas del origen configurado; no autentica ni bloquea solicitudes de otros clientes. Usa productos ficticios y limita los puertos al entorno de clase.

## Pruebas

```bash
./mvnw test
```

Las pruebas usan una base H2 en memoria y cubren CRUD, DTO de salida, validación, JSON inválido, recursos inexistentes y CORS. No modifican los productos de la demostración.
