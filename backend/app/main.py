from fastapi import FastAPI
from .database import engine, Base
from .routers import users, fleets, matatus, fares, payments, auth

Base.metadata.create_all(bind=engine)

app = FastAPI(title="Dynamic Matatu Fare API")

app.include_router(auth.router)
app.include_router(users.router)
app.include_router(fleets.router)
app.include_router(matatus.router)
app.include_router(fares.router)
app.include_router(payments.router)
