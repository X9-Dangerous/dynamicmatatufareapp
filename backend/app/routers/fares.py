from fastapi import APIRouter, Depends, HTTPException
from sqlalchemy.orm import Session
from .. import crud, schemas, auth
from ..database import get_db
from typing import List

router = APIRouter(prefix="/api/fares", tags=["fares"])

@router.post("/", response_model=schemas.FareOut)
def create_fare(fare: schemas.FareCreate, db: Session = Depends(get_db)):
    db_fare = crud.create_fare(db=db, fare=fare)
    return schemas.FareOut.from_orm(db_fare)

@router.get("/", response_model=List[schemas.FareOut])
def read_fares(skip: int = 0, limit: int = 100, db: Session = Depends(get_db)):
    fares = crud.get_fares(db, skip=skip, limit=limit)
    return [schemas.FareOut.from_orm(f) for f in fares]

@router.get("/matatu/{matatu_id}", response_model=List[schemas.FareOut])
def read_fares_for_matatu(matatu_id: int, db: Session = Depends(get_db)):
    fares = crud.get_fares_for_matatu(db, matatu_id=matatu_id)
    print("FARES FROM DB:", fares)
    for f in fares:
        print("FARE FIELDS:", f.id, f.matatu_id, f.peak_fare, f.non_peak_fare, f.rainy_peak_fare, f.rainy_non_peak_fare, f.disability_discount)
    return [{
        "fareId": f.id,
        "matatuId": f.matatu_id,
        "peakFare": f.peak_fare,
        "nonPeakFare": f.non_peak_fare,
        "rainyPeakFare": f.rainy_peak_fare,
        "rainyNonPeakFare": f.rainy_non_peak_fare,
        "disabilityDiscount": f.disability_discount,
    } for f in fares]

# NOTE: Send disability_discount as a decimal (e.g., 0.02 for 2%)
@router.put("/{fare_id}", response_model=schemas.FareOut)
def update_fare(fare_id: int, fare: schemas.FareCreate, db: Session = Depends(get_db)):
    updated = crud.update_fare(db, fare_id, fare)
    if not updated:
        raise HTTPException(status_code=404, detail="Fare not found")
    return schemas.FareOut.from_orm(updated)
