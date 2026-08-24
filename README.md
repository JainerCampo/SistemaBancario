# 🏦 DaniBanca — Sistema de Gestión Bancaria (EN DESARROLLO)

> Modelo de dominio bancario construido con Java 17, JavaFX y principios de diseño orientado a objetos. Proyecto académico/profesional con arquitectura limpia, thread-safe y preparado para escalar.

---

## 📋 Tabla de contenidos

- [Descripción](#-descripción)
- [Características principales](#-características-principales)
- [Arquitectura y diseño](#-arquitectura-y-diseño)
- [Stack tecnológico](#-stack-tecnológico)
- [Estructura del proyecto](#-estructura-del-proyecto)
- [Cómo ejecutar](#-cómo-ejecutar)
- [Uso de IA en el desarrollo](#-uso-de-ia-en-el-desarrollo)
- [Autor](#-autor)
- [Licencia](#-licencia)

---

## 📝 Descripción

**DaniBanca** es un sistema de gestión bancaria desarrollado en Java que permite administrar clientes, cuentas de ahorro, cuentas corrientes y movimientos financieros. El proyecto pone énfasis en un **modelo de dominio robusto** con validaciones estrictas, manejo de concurrencia y una interfaz gráfica construida con JavaFX.

El núcleo del sistema está diseñado con **Programación Orientada a Objetos avanzada**: herencia, polimorfismo, encapsulamiento, inmutabilidad y excepciones de dominio personalizadas.

---

## ✨ Características principales

| Módulo | Descripción |
|--------|-------------|
| 👤 **Gestión de clientes** | Registro, búsqueda y edición de clientes con validación de documento, nombres, correo, teléfono y dirección. |
| 💳 **Cuentas bancarias** | Creación de cuentas de ahorro (con intereses) y cuentas corrientes (con límite de sobregiro). |
| 💰 **Operaciones financieras** | Consignaciones, retiros, transferencias entre cuentas y aplicación de intereses. |
| 📜 **Movimientos** | Registro inmutable de cada transacción con fecha, hora, canal y trazabilidad de cuentas origen/destino. |
| 🔒 **Thread-safety** | Sincronización en operaciones de saldo, generación atómica de IDs y prevención de deadlock en transferencias. |
| ⚠️ **Excepciones de dominio** | `SaldoInsuficienteException`, `CuentaInactivaException`, `ClienteDuplicadoException`, etc. |
| 🖥️ **Interfaz gráfica** | JavaFX con navegación por vistas (FXML), sidebar, header dinámico y paneles de resultados. |

---

## 🏗️ Arquitectura y diseño

### Principios aplicados

- **Responsabilidad única (SRP):** cada clase tiene un propósito claro (`Cliente`, `Cuenta`, `Movimiento`, `Banco`).
- **Inmutabilidad:** `Movimiento` es inmutable (`final` en todos sus campos) con *factory methods* estáticos.
- **Polimorfismo:** `Cuenta` es abstracta; `validarRetiro()` se comporta diferente en `CuentaAhorros` vs `CuentaCorriente`.
- **Encapsulamiento:** getters defensivos, campos privados, métodos protegidos para modificación interna.
- **Inyección de dependencias:** el mismo objeto `Banco` se comparte entre controllers mediante la interfaz `BancoAware`.

---

## 🛠️ Stack tecnológico

| Tecnología | Versión | Uso |
|------------|---------|-----|
| Java | 17 | Lenguaje principal |
| JavaFX | 21+ | Interfaz gráfica y navegación |
| FXML | — | Declaración de vistas |
| Maven / Gradle | — | Gestión de dependencias *(recomendado)* |

> **Nota:** el proyecto no utiliza frameworks externos (Spring, Lombok, Hibernate) para mantener el enfoque en el dominio y la lógica de negocio pura.

---

## 📁 Estructura del proyecto

```
src/
├── Model/
│   ├── Persona.java
│   ├── Cliente.java
│   ├── Cuenta.java
│   ├── CuentaAhorros.java
│   ├── CuentaCorriente.java
│   ├── Movimiento.java
│   ├── Banco.java
│   ├── Direccion.java
│   ├── TipoIdentificacion.java
│   ├── EstadoCuenta.java
│   ├── TipoMovimiento.java
│   ├── CanalMovimiento.java
│   └── Exceptions/
│       ├── DominioException.java
│       ├── ClienteDuplicadoException.java
│       ├── CuentaDuplicadaException.java
│       ├── CuentaInactivaException.java
│       └── SaldoInsuficienteException.java
│
├── Controller/
│   ├── BancoAware.java
│   ├── VentanaPrincipalController.java
│   ├── ClientesController.java
│   ├── EditarClienteController.java
│   └── CrearClienteController.java
│
└── View/
    ├── VentanaPrincipal.fxml
    ├── clientes.fxml
    ├── editarCliente.fxml
    └── crearCliente.fxml
```

---

## 🚀 Cómo ejecutar

### Requisitos previos

- [JDK 17](https://adoptium.net/) o superior instalado.
- JavaFX SDK configurado en tu IDE (IntelliJ IDEA, Eclipse, VS Code).

### Pasos

1. Clona el repositorio:
   ```bash
   git clone https://github.com/JainerCampo/SistemaBancario.git
   cd danibanca
   ```

2. Abre el proyecto en tu IDE favorito.

3. Configura el *module path* de JavaFX si es necesario:
   ```bash
   --module-path /ruta/a/javafx-sdk/lib --add-modules javafx.controls,javafx.fxml
   ```

4. Ejecuta la clase principal `Main.java` (o el launcher de tu aplicación JavaFX).

---

## 🤖 Uso de IA en el desarrollo

Este proyecto fue desarrollado con un enfoque híbrido: **arquitectura humana + aceleración con IA**.

- **Diseño de arquitectura:** herencia, enums, reglas de negocio, flujo de navegación y decisiones de concurrencia fueron definidos manualmente.
- **GitHub Copilot** (modelo GPT-4o) dentro de VS Code se utilizó como *pair programmer* técnico mediante **prompts estructurados** para generar refactorizaciones, sincronización de hilos, excepciones personalizadas y vistas FXML.
- **Revisión humana:** cada salida de la IA fue depurada, validada y ajustada antes de integrarla. La coherencia del modelo y las reglas de negocio siguen siendo 100% humanas.

> La IA acelera iteraciones; el criterio técnico y el dominio son responsabilidad del desarrollador.

---

## 👤 Autor

**[JAINER CAMPO]**
- 💼 [LinkedIn](https://www.linkedin.com/in/jainer-campo/)
- 🐙 [GitHub](https://github.com/JainerCampo/)

---

## 📄 Licencia

Este proyecto está bajo la licencia **MIT**. Consulta el archivo [LICENSE](LICENSE) para más detalles.

```
MIT License

Copyright (c) 2026 [JAINER CAMPO]

Permission is hereby granted, free of charge, to any person obtaining a copy
of this software and associated documentation files (the "Software"), to deal
in the Software without restriction, including without limitation the rights
to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
copies of the Software, and to permit persons to whom the Software is
furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in all
copies or substantial portions of the Software.
```

---

<p align="center">
  <strong>⭐ Si te sirvió o te gustó el proyecto, déjale una estrella.</strong>
</p>
