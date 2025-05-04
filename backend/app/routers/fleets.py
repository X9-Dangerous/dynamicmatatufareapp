from fastapi import APIRouter, Depends, HTTPException, Response
from sqlalchemy.orm import Session
from .. import crud, schemas, auth, models
from ..database import get_db
from typing import List

router = APIRouter(prefix="/api/fleets", tags=["fleets"])

@router.post("/", response_model=schemas.FleetOut)
def create_fleet(fleet: schemas.FleetCreate, db: Session = Depends(get_db)):
    result = crud.create_fleet(db=db, fleet=fleet)
    return schemas.FleetOut.from_orm(result)

@router.get("/", response_model=List[schemas.FleetOut])
def read_fleets(skip: int = 0, limit: int = 100, db: Session = Depends(get_db)):
    return [schemas.FleetOut.from_orm(f) for f in crud.get_fleets(db, skip=skip, limit=limit)]

@router.get("/{fleet_id}", response_model=schemas.FleetOut)
def read_fleet(fleet_id: int, db: Session = Depends(get_db)):
    db_fleet = crud.get_fleet(db, fleet_id=fleet_id)
    if db_fleet is None:
        raise HTTPException(status_code=404, detail="Fleet not found")
    return schemas.FleetOut.from_orm(db_fleet)

@router.get("/operator/{operator_id}", response_model=List[schemas.FleetOut])
def get_fleets_for_operator(operator_id: str, db: Session = Depends(get_db)):
    fleets = db.query(models.Fleet).filter(models.Fleet.operator_id == operator_id).all()
    return [schemas.FleetOut.from_orm(f) for f in fleets]

@router.delete("/{fleet_id}", status_code=204)
def delete_fleet(fleet_id: str, db: Session = Depends(get_db)):
    db_fleet = crud.get_fleet(db, fleet_id=fleet_id)
    if db_fleet is None:
        raise HTTPException(status_code=404, detail="Fleet not found")
    # Check if fleet has any matatus
    matatus_count = db.query(models.Matatu).filter(models.Matatu.fleet_id == fleet_id).count()
    if matatus_count > 0:
        raise HTTPException(status_code=400, detail="Cannot delete fleet with matatus assigned")
    db.delete(db_fleet)
    db.commit()
    return Response(status_code=204)
