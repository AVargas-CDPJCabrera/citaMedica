# 🏥 Gestión de Citas Médicas

Aplicación de escritorio en **Java Swing** para la gestión de citas médicas en un hospital. Permite dar de alta, modificar, cancelar y consultar citas organizadas por especialidad médica.

---

## 📐 Arquitectura

El proyecto sigue el patrón **MVC (Modelo–Vista–Controlador)**:

```
src/
├── controlador/
│   └── CitasController.java   # Lógica de negocio / servicio
├── dao/
│   ├── CitasDao.java          # Acceso a datos (CRUD)
│   └── DbConnection.java      # Conexión a MySQL (Singleton)
├── excepciones/
│   ├── AforoMaximoException.java
│   ├── CamposVaciosException.java
│   └── HistoriaClinicaException.java
├── modelo/
│   └── CitaPaciente.java      # Entidad con validaciones de negocio
└── vista/
    ├── frmCitas.java          # Ventana principal (Swing)
    └── Main.java              # Punto de entrada
```

---

## 🚀 Requisitos

| Requisito | Versión mínima |
|-----------|---------------|
| Java      | 11+           |
| MySQL     | 8.0+          |
| Driver    | mysql-connector-j |

---

## ⚙️ Configuración

1. Crea la base de datos en MySQL:

```sql
CREATE DATABASE hospital2;
USE hospital2;

CREATE TABLE CITAS (
    idCita         INT AUTO_INCREMENT PRIMARY KEY,
    historiaClinica VARCHAR(13)  NOT NULL,
    nombre         VARCHAR(100) NOT NULL,
    apellidos      VARCHAR(150) NOT NULL,
    numTelefono    VARCHAR(15),
    especialidad   VARCHAR(50)  NOT NULL,
    fechaCita      DATE         NOT NULL,
    numCita        INT          NOT NULL
);
```

2. Crea el archivo `src/db.properties` (no incluido en el repositorio por seguridad):

```properties
db.url=jdbc:mysql://localhost:3306/hospital2?useSSL=false&serverTimezone=UTC
db.user=root
db.password=TU_CONTRASEÑA
```

> ⚠️ **Nunca subas `db.properties` a GitHub.** Está incluido en `.gitignore`.

---

## ▶️ Ejecución

Compila y ejecuta desde tu IDE (Eclipse, IntelliJ) o desde la línea de comandos:

```bash
javac -cp ".;mysql-connector-j-*.jar" src/**/*.java -d bin/
java  -cp "bin;mysql-connector-j-*.jar" vista.Main
```

---

## ✨ Mejoras implementadas (v2)

### 🐛 Bugs corregidos
- **Bug crítico en `guardar()`**: el bloque `if (esModificacion)` se ejecutaba siempre (doble `if` en lugar de `else if`), causando que al guardar una nueva cita se lanzara también el flujo de modificación.
- **Columna duplicada en la tabla**: se añadía `numCita` dos veces al `DefaultTableModel`.
- **Validación de especialidades rota**: la condición `&&...&& especialidad.equals("PEDIATRIA")` siempre era `false`, bloqueando todas las altas.
- **Modificación sin `else`**: el `guardar` ejecutaba ambas ramas (alta y modificación) en la misma pulsación.

### 🏗️ Mejoras de diseño
| Área | Mejora |
|------|--------|
| `DbConnection` | Patrón **Singleton**: evita abrir una conexión nueva en cada operación |
| `DbConnection` | Lee credenciales desde `db.properties` en lugar de hardcodearlas |
| `CitasDao` | **`try-with-resources`** en todas las operaciones: cierre automático garantizado |
| `CitasDao` | Método `mapRow()` elimina duplicación de código al construir `CitaPaciente` |
| `CitasDao` | `primerTurnoLibre()` y `contarCitas()` extraídos como métodos privados |
| `CitasController` | Método `modificar()` atómico (eliminar + insertar en un solo paso) |
| `CitaPaciente` | Array `ESPECIALIDADES_VALIDAS` centraliza las especialidades permitidas |
| `CitaPaciente` | `validarHistoriaClinica` acepta solo dígitos (`matches("\\d{13}")`) |
| `CitaPaciente` | Algoritmo de dígito de control simplificado con pesos por posición par/impar |
| `frmCitas` | Métodos `construirPanel*` y helpers `crearBoton`, `agregarCampo` eliminan 200 líneas duplicadas |
| `frmCitas` | Clic en fila de tabla sincroniza automáticamente el formulario |
| `frmCitas` | Filtro "Por especialidad" e "Historial paciente" implementados y funcionales |
| `frmCitas` | `mostrarError()` centraliza los mensajes de error |
| `Main` | La UI se lanza en el **EDT** (`SwingUtilities.invokeLater`) |
| `Main` | Se aplica el **Look & Feel nativo** del sistema operativo |
| General | `System.out.println` de depuración reemplazados por `java.util.logging.Logger` |

---

## 📋 Especialidades disponibles

- CARDIOLOGIA
- TRAUMATOLOGIA
- PEDIATRIA
- DERMATOLOGIA
- NEUROLOGIA
- ONCOLOGIA

> El aforo máximo por especialidad y día es de **10 citas**.

---

## 👤 Autor

Proyecto académico — Java Swing + MySQL · Patrón MVC
