from pydantic import BaseModel, EmailStr, Field
from typing import Optional, List
import datetime

class UserBase(BaseModel):
    name: str
    email: EmailStr
    phone: Optional[str] = None
    role: Optional[str] = "user"

class UserCreate(UserBase):
    password: str

class UserOut(UserBase):
    id: int
    class Config:
        orm_mode = True

# --- FLEET MODELS ---
class FleetBase(BaseModel):
    name: str
    operator_id: str = Field(..., alias="operatorId")

class FleetCreate(FleetBase):
    pass

class FleetOut(BaseModel):
    fleetId: str
    name: str
    operatorId: str

    @classmethod
    def from_orm(cls, obj):
        return cls(
            fleetId=str(getattr(obj, 'id', None)),
            name=getattr(obj, 'name', None),
            operatorId=str(getattr(obj, 'operator_id', None)) if getattr(obj, 'operator_id', None) is not None else None,
        )
    class Config:
        orm_mode = True
        allow_population_by_field_name = True
        from_attributes = True

# --- MATATU MODELS ---
class MatatuBase(BaseModel):
    registration_number: str = Field(..., alias="registrationNumber")
    fleet_id: Optional[int] = Field(None, alias="fleetId")
    pochi_number: Optional[str] = Field(None, alias="pochiNumber")
    paybill_number: Optional[str] = Field(None, alias="paybillNumber")
    till_number: Optional[str] = Field(None, alias="tillNumber")
    account_number: Optional[str] = Field(None, alias="accountNumber")
    send_money_phone: Optional[str] = Field(None, alias="sendMoneyPhone")
    mpesa_option: Optional[str] = Field(None, alias="mpesaOption")
    route_start: Optional[str] = Field(None, alias="routeStart")
    route_end: Optional[str] = Field(None, alias="routeEnd")
    matatu_id: Optional[str] = Field(None, alias="matatuId")
    operator_id: str = Field(..., alias="operatorId")

    class Config:
        allow_population_by_field_name = True
        orm_mode = True

class MatatuCreate(MatatuBase):
    pass

class MatatuOut(MatatuBase):
    matatuId: str
    registrationNumber: str
    operatorId: str
    fleetId: Optional[str] = None
    pochiNumber: Optional[str] = None
    paybillNumber: Optional[str] = None
    tillNumber: Optional[str] = None
    accountNumber: Optional[str] = None
    sendMoneyPhone: Optional[str] = None
    mpesaOption: Optional[str] = None
    routeStart: Optional[str] = None
    routeEnd: Optional[str] = None
    stops: List[str] = []
    fleetname: Optional[str] = None

    @classmethod
    def from_orm(cls, obj):
        stops = getattr(obj, 'stops', None)
        if stops is None:
            stops = []
        fleet_id = getattr(obj, 'fleet_id', None)
        fleet_id_str = str(fleet_id) if fleet_id is not None else None
        return cls(
            matatuId=str(getattr(obj, 'id', None)),
            registrationNumber=getattr(obj, 'registration_number', None),
            operatorId=str(getattr(obj, 'operator_id', None)) if getattr(obj, 'operator_id', None) is not None else None,
            fleetId=fleet_id_str,
            pochiNumber=getattr(obj, 'pochi_number', None),
            paybillNumber=getattr(obj, 'paybill_number', None),
            tillNumber=getattr(obj, 'till_number', None),
            accountNumber=getattr(obj, 'account_number', None),
            sendMoneyPhone=getattr(obj, 'send_money_phone', None),
            mpesaOption=getattr(obj, 'mpesa_option', None),
            routeStart=getattr(obj, 'route_start', None),
            routeEnd=getattr(obj, 'route_end', None),
            stops=stops,
            fleetname=getattr(obj, 'fleetname', None),
        )
    class Config:
        orm_mode = True
        allow_population_by_field_name = True
        from_attributes = True

from pydantic import BaseModel, Field
from typing import Optional

class FareBase(BaseModel):
    matatu_id: int = Field(..., alias="matatuId")
    peak_fare: float = Field(..., alias="peakFare")
    non_peak_fare: float = Field(..., alias="nonPeakFare")
    rainy_peak_fare: float = Field(..., alias="rainyPeakFare")
    rainy_non_peak_fare: float = Field(..., alias="rainyNonPeakFare")
    disability_discount: float = Field(..., alias="disabilityDiscount")

    class Config:
        from_attributes = True
        allow_population_by_field_name = True

class FareCreate(FareBase):
    pass

class FareOut(BaseModel):
    fare_id: Optional[int] = Field(None, alias="fareId")
    matatu_id: Optional[int] = Field(None, alias="matatuId")
    peak_fare: Optional[float] = Field(None, alias="peakFare")
    non_peak_fare: Optional[float] = Field(None, alias="nonPeakFare")
    rainy_peak_fare: Optional[float] = Field(None, alias="rainyPeakFare")
    rainy_non_peak_fare: Optional[float] = Field(None, alias="rainyNonPeakFare")
    disability_discount: Optional[float] = Field(0.0, alias="disabilityDiscount")

    @classmethod
    def from_orm(cls, obj):
        return cls(
            fare_id=getattr(obj, 'id', None),
            matatu_id=getattr(obj, 'matatu_id', None),
            peak_fare=getattr(obj, 'peak_fare', None),
            non_peak_fare=getattr(obj, 'non_peak_fare', None),
            rainy_peak_fare=getattr(obj, 'rainy_peak_fare', None),
            rainy_non_peak_fare=getattr(obj, 'rainy_non_peak_fare', None),
            disability_discount=getattr(obj, 'disability_discount', None),
        )

    class Config:
        orm_mode = True
        from_attributes = True
        allow_population_by_field_name = True


# --- PAYMENT MODELS ---
class PaymentBase(BaseModel):
    user_id: int = Field(..., alias="userId")
    matatu_id: int = Field(..., alias="matatuId")
    amount: float
    route: Optional[str] = None
    status: Optional[str] = "pending"
    start_location: Optional[str] = Field(None, alias="startLocation")
    end_location: Optional[str] = Field(None, alias="endLocation")
    mpesa_receipt_number: Optional[str] = Field(None, alias="mpesaReceiptNumber")
    payment_method: Optional[str] = Field(None, alias="paymentMethod")
    phone_number: Optional[str] = Field(None, alias="phoneNumber")
    fleet_id: Optional[int] = Field(None, alias="fleetId")

class PaymentCreate(PaymentBase):
    pass

class PaymentOut(PaymentBase):
    id: int
    timestamp: datetime.datetime
    class Config:
        orm_mode = True
        from_attributes = True