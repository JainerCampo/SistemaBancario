# 🏦 Sistema Bancario — Java, JDBC & DAO

Sistema bancario desarrollado en **Java**, orientado al diseño de software, modelado de dominio y persistencia de datos. El proyecto comenzó como una aplicación completamente **en memoria** y evolucionó hacia una arquitectura con **base de datos, JDBC y patrón DAO**.

El objetivo principal del proyecto no es únicamente implementar funcionalidades bancarias, sino demostrar el proceso de **diseñar, evolucionar y mantener un sistema de software**, tomando decisiones arquitectónicas y utilizando herramientas de Inteligencia Artificial como apoyo durante el desarrollo.

---

## 🎯 Objetivo del proyecto

Construir un sistema bancario que permita gestionar clientes, cuentas y movimientos financieros, aplicando principios de **Programación Orientada a Objetos, diseño de dominio, persistencia y separación de responsabilidades**.

El proyecto busca representar un escenario cercano al desarrollo de una aplicación empresarial, evitando reducir el sistema a un CRUD básico.

---

## 🏗️ Evolución del proyecto

El desarrollo se realizó de forma incremental:

### 1. Modelo de dominio

Inicialmente, el sistema funcionaba completamente en memoria.

Se construyó un modelo orientado al dominio utilizando:

* Encapsulamiento
* Herencia
* Polimorfismo
* Abstracción
* Inmutabilidad
* Validaciones de dominio
* Excepciones personalizadas
* Enumeraciones (`enum`)
* Identificadores generados de forma segura
* Control de concurrencia en operaciones críticas

Entre las entidades principales se encuentran:

```text
Persona
 └── Cliente

Cuenta
 ├── CuentaAhorros
 └── CuentaCorriente

Movimiento
```

El comportamiento de las cuentas se define mediante polimorfismo. Por ejemplo, las reglas de retiro pueden variar dependiendo del tipo de cuenta.

---

### 2. Interfaz gráfica

Se incorporó una interfaz utilizando **JavaFX**, permitiendo interactuar con el modelo de dominio mediante una aplicación de escritorio.

La interfaz se mantiene separada de las reglas de negocio para evitar acoplar el dominio con la capa de presentación.

---

### 3. Persistencia con JDBC y DAO

Posteriormente, el sistema evolucionó desde una arquitectura basada completamente en memoria hacia una arquitectura con **persistencia real**.

Se incorporó:

* Base de datos relacional
* JDBC
* Patrón DAO (Data Access Object)
* Consultas SQL parametrizadas
* Separación entre lógica de negocio y acceso a datos
* Mapeo entre objetos Java y registros de la base de datos

La arquitectura general puede representarse así:

```text
┌─────────────────────────┐
│       JavaFX / UI       │
└────────────┬────────────┘
             │
             ▼
┌─────────────────────────┐
│     Capa de aplicación  │
│      / servicios        │
└────────────┬────────────┘
             │
             ▼
┌─────────────────────────┐
│     Modelo de dominio   │
│                         │
│ Cliente                 │
│ Cuenta                  │
│ Movimiento              │
│ Enums                   │
│ Excepciones de dominio  │
└────────────┬────────────┘
             │
             ▼
┌─────────────────────────┐
│          DAO            │
│                         │
│ ClienteDAO              │
│ CuentaDAO               │
│ MovimientoDAO           │
└────────────┬────────────┘
             │
             ▼
┌─────────────────────────┐
│          JDBC           │
└────────────┬────────────┘
             │
             ▼
┌─────────────────────────┐
│      Base de datos      │
└─────────────────────────┘
```

Una de las decisiones importantes es mantener el **modelo de dominio independiente de JDBC**.

El dominio representa las reglas del negocio, mientras que DAO/JDBC se encarga de la persistencia.

---

## 🗄️ Base de datos

La incorporación de la base de datos permite que la información deje de depender exclusivamente de la memoria de la aplicación.

Los datos principales relacionados con el sistema incluyen:

* Clientes
* Cuentas
* Tipos de cuenta
* Movimientos
* Relaciones entre entidades

El DAO funciona como una capa de abstracción entre el dominio y la base de datos.

Por ejemplo:

```text
Objeto Java
     │
     ▼
 Cuenta
     │
     ▼
 CuentaDAO
     │
     ▼
   JDBC
     │
     ▼
 Base de datos
```

Esto permite que la lógica de negocio no tenga que conocer directamente los detalles de SQL o JDBC.

---

## 🤖 Uso de Inteligencia Artificial

La Inteligencia Artificial fue utilizada como **herramienta de apoyo durante el proceso de desarrollo**, no como sustituto de las decisiones de diseño.

La IA se utilizó principalmente para:

* Analizar alternativas de arquitectura.
* Revisar decisiones de diseño.
* Detectar posibles problemas de acoplamiento.
* Generar ideas para refactorizaciones.
* Analizar errores y excepciones.
* Apoyar la implementación de JDBC y DAO.
* Revisar consultas SQL.
* Proponer estructuras de persistencia.
* Documentar componentes del sistema.
* Explorar buenas prácticas de desarrollo.
* Acelerar tareas repetitivas.

La implementación y las decisiones finales fueron revisadas y comprendidas durante el proceso de desarrollo.

### 🧠 IA como herramienta, no como arquitecto

El proyecto utiliza una filosofía de desarrollo en la que la IA funciona como **copiloto técnico**.

La responsabilidad sobre decisiones como:

* qué arquitectura utilizar,
* dónde colocar cada responsabilidad,
* qué entidades pertenecen al dominio,
* cómo separar las capas,
* cómo modelar la persistencia,
* qué reglas deben protegerse,

permanece en el desarrollador.

La IA puede proponer una solución, pero el desarrollador debe ser capaz de **evaluarla, modificarla, rechazarla o justificarla**.

---

## 👨‍💻 Rol del desarrollador

En este proyecto el objetivo no es únicamente demostrar que puedo escribir código Java.

El enfoque está en desarrollar la capacidad de **pensar como desarrollador y arquitecto de software**.

Las principales responsabilidades durante el proyecto han sido:

* Analizar el problema.
* Diseñar el modelo de dominio.
* Definir responsabilidades.
* Elegir patrones de diseño.
* Diseñar la interacción entre capas.
* Diseñar la persistencia.
* Evaluar decisiones técnicas.
* Refactorizar cuando la arquitectura lo requiere.
* Utilizar IA para aumentar la productividad.
* Validar técnicamente las soluciones propuestas por IA.

La intención es evolucionar desde:

```text
"¿Cómo escribo este código?"
```

hacia:

```text
"¿Dónde debe vivir esta responsabilidad
y por qué?"
```

---

## 🛠️ Tecnologías

* **Java**
* **JavaFX**
* **JDBC**
* **SQL**
* **Base de datos relacional**
* **DAO Pattern**
* **Programación Orientada a Objetos**
* **Git / GitHub**
* **Inteligencia Artificial como herramienta de desarrollo**

---

## 📚 Conceptos aplicados

### Programación

* POO
* Herencia
* Polimorfismo
* Abstracción
* Encapsulamiento
* Inmutabilidad
* Excepciones personalizadas
* `enum`
* Concurrencia

### Arquitectura y diseño

* Separación de responsabilidades
* Separación por capas
* Modelo de dominio
* DAO
* Abstracción de persistencia
* Bajo acoplamiento
* Alta cohesión
* Evolución incremental de arquitectura

### Persistencia

* SQL
* JDBC
* `PreparedStatement`
* `ResultSet`
* Mapeo objeto-relacional manual
* CRUD mediante DAO
* Relaciones entre entidades

---

## 🚀 Próximos pasos

Algunas de las posibles evoluciones del proyecto son:

* Implementar transacciones JDBC.
* Mejorar el manejo de errores de persistencia.
* Implementar un Service Layer más definido.
* Incorporar pruebas unitarias.
* Incorporar pruebas de integración.
* Mejorar la gestión de configuración.
* Implementar logging.
* Gestionar migraciones de base de datos.
* Evolucionar hacia una API REST.
* Separar completamente frontend y backend.
* Explorar Spring Boot y Spring Data/JPA.
* Containerizar la aplicación.
* Implementar autenticación y autorización.

---

## 📌 Propósito

Este proyecto forma parte de mi proceso de formación y evolución como **desarrollador backend**, con especial interés en **Java, bases de datos, arquitectura de software y diseño de sistemas**.

Más que construir una aplicación que simplemente funcione, el objetivo es aprender a construir sistemas que puedan **evolucionar, mantenerse y escalar sin perder claridad arquitectónica**.
