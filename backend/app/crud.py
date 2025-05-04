from sqlalchemy.orm import Session
from . import models, schemas
from passlib.context import CryptContext
import logging

pwd_context = CryptContext(schemes=["bcrypt"], deprecated="auto")

logger = logging.getLogger("matatu_registration")
logger.setLevel(logging.DEBUG)

def get_password_hash(password):
    return pwd_context.hash(password)

def verify_password(plain_password, hashed_password):
    return pwd_context.verify(plain_password, hashed_password)

# Users
def create_user(db: Session, user: schemas.UserCreate):
    hashed_password = get_password_hash(user.password)
    db_user = models.User(
        name=user.name,
        email=user.email,
        hashed_password=hashed_password,
        phone=user.phone,
        role=user.role
    )
    db.add(db_user)
    db.commit()
    db.refresh(db_user)
    return db_user

def get_user_by_email(db: Session, email: str):
    return db.query(models.User).filter(models.User.email == email).first()

def get_user(db: Session, user_id: int):
    return db.query(models.User).filter(models.User.id == user_id).first()

def authenticate_user(db: Session, email: str, password: str):
    user = get_user_by_email(db, email)
    if not user:
        return False
    if not verify_password(password, user.hashed_password):
        return False
    return user

def get_users(db: Session, skip: int = 0, limit: int = 100):
    return db.query(models.User).offset(skip).limit(limit).all()

# Fleets
def create_fleet(db: Session, fleet: schemas.FleetCreate):
    db_fleet = models.Fleet(
        name=fleet.name,
        operator_id=fleet.operator_id  # <-- This line is required!
    )
    db.add(db_fleet)
    db.commit()
    db.refresh(db_fleet)
    return db_fleet

def get_fleets(db: Session, skip: int = 0, limit: int = 100):
    return db.query(models.Fleet).offset(skip).limit(limit).all()

def get_fleet(db: Session, fleet_id: int):
    return db.query(models.Fleet).filter(models.Fleet.id == fleet_id).first()

# Matatus
def create_matatu(db: Session, matatu: schemas.MatatuCreate):
    logger.debug(f"Received matatu registration request: {matatu}")
    existing = db.query(models.Matatu).filter(models.Matatu.registration_number == matatu.registration_number).first()
    if existing:
        logger.warning(f"Matatu with registration number {matatu.registration_number} already exists: {existing}")
        return None
    try:
        db_matatu = models.Matatu(**matatu.dict(by_alias=False))
        db.add(db_matatu)
        db.commit()
        db.refresh(db_matatu)
        logger.info(f"Matatu registered successfully: {db_matatu}")
        return db_matatu
    except Exception as e:
        db.rollback()
        logger.error(f"Matatu registration failed for {matatu.registration_number}: {e}")
        return None

def get_matatus(db: Session, skip: int = 0, limit: int = 100):
    return db.query(models.Matatu).offset(skip).limit(limit).all()

def get_matatu(db: Session, matatu_id: int):
    return db.query(models.Matatu).filter(models.Matatu.id == matatu_id).first()

def get_matatu_by_registration(db: Session, registration_number: str):
    return db.query(models.Matatu).filter(models.Matatu.registration_number == registration_number).first()

# Fares
def create_fare(db: Session, fare: schemas.FareCreate):
    db_fare = models.Fare(**fare.dict(by_alias=False))
    db.add(db_fare)
    db.commit()
    db.refresh(db_fare)
    return db_fare

def get_fares_for_matatu(db: Session, matatu_id: int):
    return db.query(models.Fare).filter(models.Fare.matatu_id == matatu_id).all()

def update_fare(db: Session, fare_id: int, fare_update: schemas.FareCreate):
    db_fare = db.query(models.Fare).filter(models.Fare.id == fare_id).first()
    if db_fare:
        for field, value in fare_update.dict().items():
            setattr(db_fare, field, value)
        db.commit()
        db.refresh(db_fare)
    return db_fare

def get_fares(db: Session, skip: int = 0, limit: int = 100):
    return db.query(models.Fare).offset(skip).limit(limit).all()

# Payments
def create_payment(db: Session, payment: schemas.PaymentCreate):
    db_payment = models.Payment(**payment.dict())
    db.add(db_payment)
    db.commit()
    db.refresh(db_payment)
    return db_payment

def get_payments_for_user(db: Session, user_id: int):
    return db.query(models.Payment).filter(models.Payment.user_id == user_id).all()

def get_payments(db: Session, skip: int = 0, limit: int = 100):
    return db.query(models.Payment).offset(skip).limit(limit).all()