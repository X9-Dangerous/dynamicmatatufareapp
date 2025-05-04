from fastapi import APIRouter, Depends, HTTPException
from sqlalchemy.orm import Session
from .. import crud, schemas, auth
from ..database import get_db
from typing import List

router = APIRouter(prefix="/api/payments", tags=["payments"])

@router.post("/", response_model=schemas.PaymentOut)
def create_payment(payment: schemas.PaymentCreate, db: Session = Depends(get_db)):
    return crud.create_payment(db=db, payment=payment)

@router.get("/", response_model=List[schemas.PaymentOut])
def read_payments(skip: int = 0, limit: int = 100, db: Session = Depends(get_db)):
    return crud.get_payments(db, skip=skip, limit=limit)

@router.get("/user/{user_id}", response_model=List[schemas.PaymentOut])
def read_payments_for_user(user_id: int, db: Session = Depends(get_db)):
    return crud.get_payments_for_user(db, user_id=user_id)
