from fastapi import APIRouter, Depends, HTTPException, Response
from sqlalchemy.orm import Session
from .. import crud, schemas, auth, models
from ..database import get_db
from typing import List

router = APIRouter(prefix="/api/matatus", tags=["matatus"])

@router.post("/", response_model=schemas.MatatuOut)
def create_matatu(matatu: schemas.MatatuCreate, db: Session = Depends(get_db)):
    result = crud.create_matatu(db=db, matatu=matatu)
    if result is None:
        raise HTTPException(status_code=409, detail="Matatu already exists or registration failed. Check backend logs for details.")
    # Always return a Pydantic schema, not the ORM model
    return schemas.MatatuOut.from_orm(result)

@router.get("/", response_model=List[schemas.MatatuOut])
def read_matatus(skip: int = 0, limit: int = 100, db: Session = Depends(get_db)):
    return [schemas.MatatuOut.from_orm(m) for m in crud.get_matatus(db, skip=skip, limit=limit)]

@router.get("/{matatu_id}", response_model=schemas.MatatuOut)
def read_matatu(matatu_id: int, db: Session = Depends(get_db)):
    db_matatu = crud.get_matatu(db, matatu_id=matatu_id)
    if db_matatu is None:
        raise HTTPException(status_code=404, detail="Matatu not found")
    return schemas.MatatuOut.from_orm(db_matatu)

@router.get("/registration/{registration_number}", response_model=schemas.MatatuOut)
def read_matatu_by_registration(registration_number: str, db: Session = Depends(get_db)):
    db_matatu = crud.get_matatu_by_registration(db, registration_number=registration_number)
    if db_matatu is None:
        raise HTTPException(status_code=404, detail="Matatu not found")
    return schemas.MatatuOut.from_orm(db_matatu)

@router.get("/operator/{operator_id}", response_model=List[schemas.MatatuOut])
def get_matatus_for_operator(operator_id: str, db: Session = Depends(get_db)):
    matatus = db.query(models.Matatu).filter(models.Matatu.operator_id == operator_id).all()
    return [schemas.MatatuOut.from_orm(m) for m in matatus]

@router.delete("/{matatu_id}", status_code=204)
def delete_matatu(matatu_id: str, db: Session = Depends(get_db)):
    db_matatu = crud.get_matatu(db, matatu_id=matatu_id)
    if db_matatu is None:
        raise HTTPException(status_code=404, detail="Matatu not found")
    db.delete(db_matatu)
    db.commit()
    return Response(status_code=204)
