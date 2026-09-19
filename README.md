# OftApp — Sistema Móvil de Consulta y Trazabilidad de Exámenes Oftalmológicos

## 1. Nombre y Propósito del Proyecto

**OftApp** es una aplicación móvil desarrollada para Android orientada a centralizar, consultar y organizar el historial de resultados de exámenes oftalmológicos.

---

## 2. Identidad Visual

* **Nombre de la App:** OftApp
* **Logotipo:** Ojo estilizado integrado con un documento digital y un tilde de verificación (check), que simboliza claridad visual, trazabilidad digital de exámenes y confianza clínica. Imagen ubicada en `docs/diseno/logo.png`.

### Paleta de Colores (Material Design 3)
* **Principal (`#006689`):** Azul Clínico — Utilizado en TopAppBar, botones de acción primaria, FAB y pestañas activas.
* **Secundario (`#4F616E`):** Gris Azulado — Utilizado en iconos secundarios, bordes de tarjetas y estados neutros.
* **Fondo (`#F8FDFF`):** Blanco Clínico Limpio — Fondo general de las pantallas y contenedores.
* **Texto (`#191C1E`):** Gris Oscuro — Títulos y textos principales para un alto contraste y legibilidad[cite: 3].
* **Éxito / Validado (`#006E36`):** Verde Salud — Indicador de estado "Validado" o "Entregado"[cite: 3].
* **Alerta / Pendiente (`#B02F00`):** Naranja/Rojo Clínico — Indicador de estado "Pendiente" u "Observado"[cite: 3].

---

## 3. Flujo de Usuario (Diagrama de Actividad UML)

El recorrido principal del usuario dentro de la aplicación está representado visualmente en la siguiente estructura:

```mermaid
stateDiagram-v2
    [*] --> Login
    Login --> ValidarCredenciales: Ingresar Usuario y Rol
    
    state check_valid <<choice>>
    ValidarCredenciales --> check_valid
    check_valid --> Login: Credenciales Inválidas
    check_valid --> Dashboard: Credenciales Válidas

    Dashboard --> RegistroAtencion: Registrar Nuevo Examen (Tecnólogo/Admin)
    Dashboard --> ListadoBusqueda: Consultar Exámenes (Todos)
    Dashboard --> HistorialClinico: Ver Historial por Paciente (Médico/Paciente)

    RegistroAtencion --> CargaDocumentos: Adjuntar Reporte (PDF/Imagen)
    CargaDocumentos --> DetalleExamen: Guardar y Generar Ficha

    ListadoBusqueda --> DetalleExamen: Seleccionar Examen de la Lista
    DetalleExamen --> HistorialClinico: Consultar Exámenes Anteriores del Paciente

    DetalleExamen --> [*]
    HistorialClinico --> [*]

```
---

4. Pantallas Principales del MVP

Las propuestas de interfaz y maquetas visuales creadas se organizan en la carpeta docs/diseno/interfaces/:

 login_screen.png — Login / Autenticación: Validación de usuarios ficticios con asignación de permisos según el rol elegido (Médico, Tecnólogo, Administrativo, Paciente, Admin).

 dashboard_screen.png — Inicio / Dashboard: Panel central con accesos directos, resumen de estados de atenciones y botón de acción flotante (FAB).

 registro_atencion.png — Registro de Atención: Formulario de captura para datos básicos del paciente, fecha, sucursal, tipo de examen e indicación de ojo evaluado.

 carga_documentos.png — Carga de Documentos: Vinculación visual y asociación simulada de reportes en PDF, imágenes o planillas.

 listado_busqueda.png — Listado y Búsqueda: Búsqueda rápida por paciente y filtrado mediante Chips interactivos según estado y sucursal.

 detalle_examen.png — Detalle del Examen: Ficha completa de la atención, visualización del documento adjunto, observaciones y cambio de estado de entrega.

 historial_clinico.png — Historial Clínico: Vista cronológica del historial de exámenes previos por paciente para apoyo en la revisión médica.

---

5. Integrantes del Equipo

Nombre del Equipo: Vitoco lover’s

Sección: 001D

Integrantes:

 Sebastian De La Paz — Líder / Desarrollador Backend/Frontend

 Diego Barria — Desarrollador Backend/Frontend

 Fernando Castillo — Desarrollador Backend/Frontend

--- 

6. Tecnologías Utilizadas

 IDE de Desarrollo: Android Studio

 Plataforma Target: Android

 Lenguaje: Kotlin

 UI Framework: Jetpack Compose + Material Design 3

 Arquitectura: MVVM (Model - ViewModel - UI - Repository)
