from sqlalchemy import Column, Integer, String, Float, ForeignKey, DateTime
from sqlalchemy.orm import relationship
from .database import Base
import datetime

class User(Base):
    __tablename__ = "users"
    id = Column(Integer, primary_key=True, index=True)
    name = Column(String, nullable=False)
    email = Column(String, unique=True, index=True, nullable=False)
    hashed_password = Column(String, nullable=False)
    phone = Column(String, nullable=True)
    role = Column(String, default="user")
    payments = relationship("Payment", back_populates="user")

class Fleet(Base):
    __tablename__ = "fleets"
    id = Column(Integer, primary_key=True, index=True)
    name = Column(String, nullable=False)
    operator_id = Column(String, nullable=False, index=True)
    matatus = relationship("Matatu", back_populates="fleet")

class Matatu(Base):
    __tablename__ = "matatus"
    id = Column(Integer, primary_key=True, index=True, autoincrement=True)
    registration_number = Column(String, unique=True, index=True, nullable=False)
    fleet_id = Column(Integer, ForeignKey("fleets.id"))
    pochi_number = Column(String, nullable=True)
    paybill_number = Column(String, nullable=True)
    till_number = Column(String, nullable=True)
    account_number = Column(String, nullable=True)
    send_money_phone = Column(String, nullable=True)
    mpesa_option = Column(String, nullable=True)
    route_start = Column(String, nullable=True)
    route_end = Column(String, nullable=True)
    matatu_id = Column(String, nullable=True)
    operator_id = Column(String, nullable=False, index=True)
    fares = relationship("Fare", back_populates="matatu")
    fleet = relationship("Fleet", back_populates="matatus")
    payments = relationship("Payment", back_populates="matatu")

class Fare(Base):
    __tablename__ = "fares"
    id = Column(Integer, primary_key=True, index=True)
    matatu_id = Column(Integer, ForeignKey("matatus.id"))
    peak_fare = Column(Float, nullable=False)
    non_peak_fare = Column(Float, nullable=False)
    rainy_peak_fare = Column(Float, nullable=False)
    rainy_non_peak_fare = Column(Float, nullable=False)
    disability_discount = Column(Float, default=0.0)
    matatu = relationship("Matatu", back_populates="fares")

class Payment(Base):
    __tablename__ = "payments"
    id = Column(Integer, primary_key=True, index=True)
    user_id = Column(Integer, ForeignKey("users.id"))
    matatu_id = Column(Integer, ForeignKey("matatus.id"))
    amount = Column(Float, nullable=False)
    route = Column(String, nullable=True)
    timestamp = Column(DateTime, default=datetime.datetime.utcnow)
    status = Column(String, default="pending")
    start_location = Column(String, nullable=True)
    end_location = Column(String, nullable=True)
    mpesa_receipt_number = Column(String, nullable=True)
    payment_method = Column(String, nullable=True)
    phone_number = Column(String, nullable=True)
    fleet_id = Column(Integer, ForeignKey("fleets.id"), nullable=True)
    user = relationship("User", back_populates="payments")
    matatu = relationship("Matatu", back_populates="payments")
