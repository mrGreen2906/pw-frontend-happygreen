# HappyGreen 🌱

Un [**ecosistema completo dedicato alla sostenibilità**](https://evanescent-seat-39c.notion.site/HappyGreen-1fb5ac9e665a80c095d8e2290e8f8602?pvs=4) ambientale e all'educazione ecologica, composto da un'applicazione Android innovativa e un robusto backend API.
Qui il [**mock-up**](https://www.canva.com/design/DAGoRb8NNhQ/1EtbcskeAjI8b5nkf87Xmg/edit?utm_content=DAGoRb8NNhQ&utm_campaign=designshare&utm_medium=link2&utm_source=sharebutton) del progetto.

## 🚀 Installazione e Setup

### 📋 Prerequisiti

**Frontend Android:**
- Android Studio Hedgehog | 2023.1.1 o superiore
- Android SDK 21+ (Android 5.0+)
- Kotlin 1.9.0+
- Gradle 8.0+

**Backend Django:**
- Python 3.12+
- MySQL 8.0+
- pip (Python package manager)
- Virtualenv (raccomandato)

### 🖥️ Setup Backend

#### 1. Clonazione e Setup Ambiente

```bash
# Clona il repository
git clone https://github.com/tuo-username/happygreen-backend.git
cd happygreen-backend

# Crea ambiente virtuale
python -m venv .venv

# Attiva ambiente virtuale
# Windows:
.venv\Scripts\activate
# macOS/Linux:
source .venv/bin/activate

# Installa dipendenze
pip install -r requirements.txt
```

#### 2. Configurazione Database MySQL

```sql
-- Accedi a MySQL e crea il database
CREATE DATABASE pwhappygreen_db CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
CREATE USER 'happygreen_user'@'localhost' IDENTIFIED BY 'your_secure_password';
GRANT ALL PRIVILEGES ON pwhappygreen_db.* TO 'happygreen_user'@'localhost';
FLUSH PRIVILEGES;
```

#### 3. Configurazione Django

Aggiorna `happygreen_backend/settings.py`:

```python
# Database Configuration
DATABASES = {
    'default': {
        'ENGINE': 'django.db.backends.mysql',
        'NAME': 'pwhappygreen_db',
        'USER': 'happygreen_user',  # Il tuo username MySQL
        'PASSWORD': 'your_secure_password',  # La tua password MySQL
        'HOST': 'localhost',
        'PORT': '3306',
    }
}

# Email Configuration
EMAIL_HOST_USER = 'your-email@gmail.com'  # La tua email
EMAIL_HOST_PASSWORD = 'your-app-password'  # Password app Gmail
DEFAULT_FROM_EMAIL = 'HappyGreen <your-email@gmail.com>'
```

#### 4. Migrazioni Database

```bash
# Applica migrazioni
python manage.py makemigrations
python manage.py migrate

# Crea superuser (opzionale)
python manage.py createsuperuser
```

#### 5. Avvio Server Django

```bash
python manage.py runserver 127.0.0.1:8000
```

### 🌐 Setup Ngrok per Tunnel HTTPS

Ngrok permette di esporre il server locale Django su internet con HTTPS, necessario per l'app Android.

#### 1. Installazione Ngrok

```bash
# Scarica ngrok da https://ngrok.com/download
# Oppure usando package manager:

# macOS (Homebrew)
brew install ngrok/ngrok/ngrok

# Windows (Chocolatey)
choco install ngrok

# Linux (Snap)
snap install ngrok
```

#### 2. Configurazione Account Ngrok

```bash
# Registrati su https://ngrok.com e ottieni il tuo authtoken
ngrok config add-authtoken YOUR_AUTHTOKEN
```

#### 3. Creazione Tunnel con Dominio Personalizzato

```bash
# Tunnel semplice (dominio casuale)
ngrok http 8000

# Tunnel con dominio personalizzato (account Pro/Plus)
ngrok http 8000 --hostname=happygreen-api.ngrok.io

# Tunnel con configurazione avanzata
ngrok http 8000 --log=stdout --log-level=debug
```

#### 4. Configurazione Django per Ngrok

Aggiorna `settings.py` con il tuo dominio ngrok:

```python
# happygreen_backend/settings.py
ALLOWED_HOSTS = [
    'localhost',
    '127.0.0.1',
    'your-ngrok-domain.ngrok-free.app',  # Il tuo dominio ngrok
    'happygreen-api.ngrok.io',  # Dominio personalizzato (se disponibile)
]

# CORS per permettere richieste dall'app Android
CORS_ALLOW_ALL_ORIGINS = True  # Solo per sviluppo
# In produzione, specifica domini specifici:
# CORS_ALLOWED_ORIGINS = [
#     "https://your-app-domain.com",
# ]
```

#### 5. Script di Avvio Automatico

Crea `start_backend.sh` (Linux/macOS) o `start_backend.bat` (Windows):

```bash
#!/bin/bash
# start_backend.sh

echo "🚀 Avvio HappyGreen Backend..."

# Attiva ambiente virtuale
source .venv/bin/activate

# Avvia Django in background
echo "📡 Avvio server Django..."
python manage.py runserver 127.0.0.1:8000 &
DJANGO_PID=$!

# Attendi che Django si avvii
sleep 3

# Avvia ngrok
echo "🌐 Avvio tunnel ngrok..."
ngrok http 8000 --log=stdout

# Cleanup quando lo script viene terminato
trap "kill $DJANGO_PID" EXIT
```

### 📱 Setup Frontend Android

#### 1. Clonazione e Apertura Progetto

```bash
# Clona il repository frontend
git clone https://github.com/tuo-username/happygreen-android.git
cd happygreen-android

# Apri con Android Studio
# File → Open → Seleziona la cartella del progetto
```

#### 2. Configurazione API Endpoint

Modifica `RetrofitClient.kt` con il tuo dominio ngrok:

```kotlin
// app/src/main/java/com/example/frontend_happygreen/api/RetrofitClient.kt
private const val BASE_URL = "https://your-ngrok-domain.ngrok-free.app/api/"

// Esempio:
// private const val BASE_URL = "https://worm-shining-accurately.ngrok-free.app/api/"
```

#### 3. Build e Run

```bash
# Pulisci e builda il progetto
./gradlew clean build

# Collega dispositivo Android o avvia emulatore
# Clicca "Run" in Android Studio o:
./gradlew installDebug
```

---

## 🎯 Configurazione Avanzata

### 📧 Setup Email Gmail

1. **Abilita 2FA** sul tuo account Gmail
2. **Genera Password App**:
   - Vai su Google Account Settings
   - Security → 2-Step Verification → App passwords
   - Genera password per "Mail"
3. **Usa la password app** in `settings.py`

### 🔧 Variabili d'Ambiente

Crea `.env` nella root del backend:

```env
# .env
SECRET_KEY=your-secret-key-here
DEBUG=True
DATABASE_URL=mysql://user:password@localhost:3306/pwhappygreen_db
EMAIL_HOST_USER=your-email@gmail.com
EMAIL_HOST_PASSWORD=your-app-password
NGROK_DOMAIN=your-ngrok-domain.ngrok-free.app
```

### 🐳 Docker Setup (Opzionale)

```yaml
# docker-compose.yml
version: '3.8'
services:
  web:
    build: .
    ports:
      - "8000:8000"
    depends_on:
      - db
    environment:
      - DATABASE_URL=mysql://root:password@db:3306/pwhappygreen_db
  
  db:
    image: mysql:8.0
    environment:
      MYSQL_ROOT_PASSWORD: password
      MYSQL_DATABASE: pwhappygreen_db
    ports:
      - "3306:3306"
```

---

## 📡 API Endpoints

### 🔐 Autenticazione

| Endpoint | Metodo | Descrizione |
|----------|--------|-------------|
| `/api/auth/register/` | POST | Registrazione utente |
| `/api/auth/login/` | POST | Login utente |
| `/api/auth/verify-otp/{userId}/` | POST | Verifica OTP email |
| `/api/auth/resend-verification/` | POST | Reinvia codice verifica |

### 👥 Utenti e Profili

| Endpoint | Metodo | Descrizione |
|----------|--------|-------------|
| `/api/users/me/` | GET | Profilo utente corrente |
| `/api/users/update-avatar/` | POST | Aggiorna avatar |
| `/api/users/update-profile/` | PUT | Aggiorna profilo |
| `/api/leaderboard/` | GET | Classifica globale |

### 🏠 Gruppi Sociali

| Endpoint | Metodo | Descrizione |
|----------|--------|-------------|
| `/api/groups/` | GET, POST | Lista/Crea gruppi |
| `/api/groups/{id}/` | GET, PUT, DELETE | Gestione gruppo specifico |
| `/api/groups/{id}/join/` | POST | Unisciti al gruppo |
| `/api/groups/{id}/add_member/` | POST | Aggiungi membro |
| `/api/groups/my_groups/` | GET | I miei gruppi |

### 📝 Post e Interazioni

| Endpoint | Metodo | Descrizione |
|----------|--------|-------------|
| `/api/posts/?group={id}` | GET | Post del gruppo |
| `/api/posts/` | POST | Crea nuovo post |
| `/api/posts/{id}/toggle_like/` | POST | Like/Unlike post |
| `/api/posts/{id}/add_reaction/` | POST | Aggiungi reaction |
| `/api/comments/` | GET, POST | Gestione commenti |

### 🎮 Gaming e Punteggi

| Endpoint | Metodo | Descrizione |
|----------|--------|-------------|
| `/api/user/update-points/` | POST | Aggiorna punteggio gioco |
| `/api/leaderboard/?game_id={id}` | GET | Classifica per gioco |

---

## 🧪 Testing

### Backend Testing

```bash
# Test completi Django
python manage.py test

# Test specifici
python manage.py test core.tests.AuthenticationTests

# Coverage testing
pip install coverage
coverage run --source='.' manage.py test
coverage report
```

### Frontend Testing

```bash
# Unit tests Android
./gradlew test

# Integration tests
./gradlew connectedAndroidTest

# UI tests
./gradlew connectedDebugAndroidTest
```

---

## 🚀 Deploy in Produzione

### 🌐 Backend Deploy

**Opzioni consigliate:**
- **Heroku**: Deploy facile con database PostgreSQL
- **DigitalOcean App Platform**: Scalabile e affidabile
- **AWS EC2**: Controllo completo
- **Railway**: Deploy moderno e semplice

### 📱 Frontend Deploy

**Google Play Store:**
1. Genera keystore per signing
2. Build APK/AAB firmato
3. Upload su Google Play Console
4. Configura listing e privacy policy

---

## 🤝 Contribuire

### Processo di Contribuzione

1. **Fork** il progetto
2. **Crea branch** per la feature (`git checkout -b feature/AmazingFeature`)
3. **Commit** modifiche (`git commit -m 'Add AmazingFeature'`)
4. **Push** al branch (`git push origin feature/AmazingFeature`)
5. **Apri Pull Request**

### Guidelines Sviluppo

**Backend:**
- Segui PEP 8 per Python
- Scrivi test per nuove API
- Documenta endpoint con docstring
- Usa migrations per modifiche database

**Frontend:**
- Segui convenzioni Kotlin
- Usa Jetpack Compose best practices
- Includi test per UI components
- Mantieni codice leggibile e modulare

---

## 🐛 Troubleshooting

### ❌ Problemi Comuni Backend

**Errore Database Connection:**
```bash
# Verifica stato MySQL
sudo systemctl status mysql
# Riavvia MySQL se necessario
sudo systemctl restart mysql
```

**Errore Ngrok:**
```bash
# Verifica token ngrok
ngrok config check
# Riavvia tunnel
ngrok http 8000 --log=stdout
```

**Errore Email SMTP:**
- Verifica password app Gmail
- Controlla impostazioni 2FA
- Testa connessione SMTP

### ❌ Problemi Comuni Frontend

**Errore Network/API:**
- Verifica URL backend in `RetrofitClient.kt`
- Controlla stato tunnel ngrok
- Verifica permessi INTERNET in AndroidManifest

**Errore Build Android:**
```bash
# Pulisci cache Gradle
./gradlew clean
# Invalida cache Android Studio
# File → Invalidate Caches and Restart
```

---

## 📄 Licenza

Questo progetto è distribuito sotto la licenza MIT. Vedi il file `LICENSE` per maggiori dettagli.

---

## 👥 Team di Sviluppo

- **Lead Developer**: Manuel Rancan
- **UI/UX Designer**: Da definire
- **Backend Developer**: Manuel Rancan

---

## 🙏 Riconoscimenti

- **OpenStreetMap** per i dati cartografici
- **Material Design** per le linee guida UI
- **Jotform** per l'integrazione moduli
- **Django Community** per supporto e risorse
- **Android Community** per supporto e risorse

---

## 📞 Supporto

- **Email**: support@happygreen.app
- **GitHub Issues**: [Repository Issues](https://github.com/tuo-username/happygreen/issues)
- **Documentazione**: [Wiki Progetto](https://github.com/tuo-username/happygreen/wiki)

---

**HappyGreen - Rendere il mondo più verde, un'API e un'app alla volta 🌍💚**

---

## 📊 Status del Progetto

![Build Status](https://img.shields.io/badge/build-passing-brightgreen)
![Django Version](https://img.shields.io/badge/django-5.2-blue)
![Android API](https://img.shields.io/badge/android-API21+-green)
![License](https://img.shields.io/badge/license-MIT-blue)

**Versione Corrente**: 1.0.0-beta
**Ultimo Update**: Maggio 2025
