# ReadyUp

ReadyUp es una aplicación social para gestionar y reseñar videojuegos. Permite a los usuarios llevar un registro de los juegos que están jugando, han completado, tienen pendientes o han abandonado, así como escribir reseñas y seguir a otros usuarios para ver sus opiniones en un feed social.

---

## Tecnologías utilizadas

**Backend**
- Python 3 + Django 6
- SQLite
- JWT (autenticación manual con PyJWT)

**Frontend Android**
- Kotlin + Jetpack Compose
- Arquitectura MVVM
- Retrofit + OkHttp + Moshi
- Coil (carga de imágenes)

---

## Estructura del repositorio

```
ReadyUp/
├── api/                  # Modelos, vistas y lógica del backend
├── ReadyUp/              # Configuración Django (settings, urls)
├── android/ReadyUp/      # Proyecto Android completo
├── import_games.py       # Script de importación de juegos desde RAWG API
├── manage.py
└── requirements.txt
```

---

## Requisitos previos

- Python 3.10 o superior
- Android Studio Narwhal 2025.1.3 o superior
- Dispositivo Android o emulador con API 24 o superior

---

## Instalación y ejecución

### 1. Clonar el repositorio

```bash
git clone https://github.com/olopezd24/ReadyUp.git
cd ReadyUp
```

### 2. Configurar el backend

Crear y activar el entorno virtual:

```bash
python -m venv venv

# Windows
venv\Scripts\activate

# Mac / Linux
source venv/bin/activate
```

Instalar dependencias:

```bash
pip install -r requirements.txt
```

Aplicar migraciones y arrancar el servidor:

```bash
python manage.py migrate
python manage.py runserver
```

El servidor quedará disponible en `http://localhost:8000`.

### 3. Importar juegos (opcional)

Para poblar la base de datos con juegos reales desde la API de [RAWG](https://rawg.io/apidocs):

1. Obtén una API key gratuita en rawg.io/apidocs
2. Edita `import_games.py` y pon tu clave en `RAWG_API_KEY`
3. Ejecuta el script con el servidor parado:

```bash
python import_games.py
```

### 4. Configurar y ejecutar el frontend Android

1. Abre Android Studio
2. Selecciona **Open** y navega hasta la carpeta `android/ReadyUp`
3. Espera a que Gradle sincronice las dependencias
4. Crea un emulador en **Device Manager** o conecta un dispositivo físico
5. Asegúrate de que el backend Django está corriendo
6. Pulsa ▶ para ejecutar la app

> El emulador accede al servidor local mediante la IP `10.0.2.2:8000`, que ya está configurada en el proyecto.

---

## Funcionalidades

- Registro e inicio de sesión con autenticación JWT
- Explorar catálogo de juegos con búsqueda y filtros
- Añadir juegos a la biblioteca con estados: **Jugando**, **Pendiente**, **Completado** o **Abandonado**
- Escribir, editar y eliminar reseñas con puntuación del 1 al 10
- Feed social con reseñas de usuarios seguidos
- Perfil con estadísticas personales

---

## Autor

Óscar López — [@olopezd24](https://github.com/olopezd24)