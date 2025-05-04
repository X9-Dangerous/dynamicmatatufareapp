# Dynamic Matatu Fare App Backend

This is a full-featured backend API for your Dynamic Matatu Fare App, built with Python (FastAPI) and PostgreSQL.

## Features
- Centralized management of fleets, matatus, fares, users, and payments
- JWT-based authentication
- RESTful API endpoints
- Ready for deployment on your school server

## Tech Stack
- Python 3.9+
- FastAPI
- SQLAlchemy (ORM)
- PostgreSQL (recommended; SQLite supported for dev)
- Alembic (migrations)
- Uvicorn (ASGI server)
- Pydantic (validation)

## Setup Instructions

### 1. Clone the repo and install dependencies
```bash
cd backend
python -m venv venv
source venv/bin/activate  # On Windows: venv\Scripts\activate
pip install -r requirements.txt
```

### 2. Configure Database
- Set your DB connection in `.env` (see `.env.example`)

### 3. Run Migrations
```bash
alembic upgrade head
```

### 4. Start the Server
```bash
uvicorn app.main:app --reload --host 0.0.0.0 --port 8000
```

## API Endpoints
See [docs](http://localhost:8000/docs) after running the server for full OpenAPI documentation.

---

## Project Structure
```
backend/
├── app/
│   ├── main.py
│   ├── models.py
│   ├── schemas.py
│   ├── crud.py
│   ├── database.py
│   ├── auth.py
│   ├── routers/
│   │   ├── users.py
│   │   ├── fleets.py
│   │   ├── matatus.py
│   │   ├── fares.py
│   │   ├── payments.py
│   │   └── auth.py
├── alembic/
├── requirements.txt
├── .env.example
└── README.md
```

---

## Deployment
- You can use Gunicorn/Uvicorn and serve behind Nginx/Apache for production.
- Make sure to set `DEBUG=0` and configure allowed hosts.

---

## License
MIT
