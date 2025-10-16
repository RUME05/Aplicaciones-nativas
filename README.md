# App de Pasos Nativa para Android 🏃‍♂️💨

Esta es una aplicación nativa para Android. La aplicación funciona como un rastreador de actividad física básico, utilizando los sensores y recursos nativos del dispositivo para monitorear los pasos y la ubicación del usuario.



---

## 📋 Descripción

Es una herramienta sencilla para monitorear tu actividad diaria. Sus principales características son:

* **Contador de Pasos:** Utiliza el sensor de pasos del dispositivo para contar los pasos del usuario en tiempo real.
* **Acumulación Diaria:** Los pasos se guardan en el dispositivo y se acumulan a lo largo del día, reiniciándose automáticamente a la medianoche.
* **Rastreo GPS:** Registra la ubicación (latitud y longitud) del usuario mientras el seguimiento está activo.
* **Servicio en Segundo Plano:** El conteo de pasos y el rastreo de ubicación continúan funcionando de manera confiable incluso si la aplicación está cerrada, gracias a un `ForegroundService`.
* **Notificación Persistente:** Muestra una notificación que informa al usuario que el seguimiento está activo, mostrando los datos actuales.
* **Personalización de Tema:** Permite al usuario cambiar entre un modo claro y un modo oscuro, guardando su preferencia para futuras sesiones.

---

## 🛠️ Requisitos del Sistema

* **Android Studio:** `Iguana | 2023.2.1` o superior.
* **Android Gradle Plugin:** `8.4.0` o superior.
* **Minimum SDK:** `API 26 (Android 8.0 - Oreo)`.

---

## 🚀 Instrucciones de Instalación

1.  **Clonar el Repositorio:**
    ```bash
    git clone [https://github.com/tu-usuario/tu-repositorio.git](https://github.com/tu-usuario/tu-repositorio.git)
    ```

2.  **Abrir en Android Studio:**
    * Abrir Android Studio.
    * Seleccionar **"Open an Existing Project"**.
    * Navegar a la carpeta donde se clono el repositorio y seleccionarla.

3.  **Sincronizar Gradle:**
    * Android Studio debería sincronizar el proyecto automáticamente. Si no, **File > Sync Project with Gradle Files**.

4.  **Ejecutar la Aplicación:**
    * Conectar un dispositivo físico (recomendado para probar los sensores) o iniciar en un emulador.
    * Presionar el botón de **"Run 'app'"** (▶️).

---

## 🔒 Permisos Requeridos

La aplicación solicita los siguientes permisos para funcionar correctamente:

| Permiso                                     | Justificación                                                                                             |
| ------------------------------------------- | --------------------------------------------------------------------------------------------------------- |
| `ACCESS_FINE_LOCATION`                      | **Necesario** para acceder a la ubicación precisa del GPS y rastrear la ruta del usuario.                 |
| `ACCESS_BACKGROUND_LOCATION`                | **Opcional pero recomendado.** Permite que el rastreo de ubicación continúe si la app pasa a segundo plano. |
| `ACTIVITY_RECOGNITION`                      | **Necesario** para acceder al sensor de contador de pasos del dispositivo.                                  |
| `POST_NOTIFICATIONS`                        | **Necesario** (en Android 13+) para mostrar la notificación persistente del servicio en primer plano.     |
| `FOREGROUND_SERVICE`                        | Permiso general para ejecutar un servicio en primer plano que no sea interrumpido por el sistema.         |
| `FOREGROUND_SERVICE_HEALTH`                 | **Necesario** (en Android 14+) para especificar que el servicio accederá a sensores de salud (pasos).       |
| `FOREGROUND_SERVICE_LOCATION`               | **Necesario** (en Android 14+) para especificar que el servicio accederá a la ubicación (GPS).            |

---

## 📸 Capturas de Pantalla

A continuación se muestran algunas capturas de la interfaz de la aplicación en sus diferentes estados y temas.

**Tema Claro**


**Tema Oscuro**


**Diálogo de Permisos**
