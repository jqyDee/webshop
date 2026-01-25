import {AddressDto, UserxDto} from "../../api";
import React, {useCallback, useState} from "react";
import {InputMaskChangeEvent} from "primereact/inputmask";
import {InputText} from "primereact/inputtext";
import {Button} from "primereact/button";
import {Checkbox} from "primereact/checkbox";
import {validateFormData, ValidationResult} from "../../utilities/form-data-validator.ts";

interface AddressFormProps {
    readonly address: AddressDto;
    readonly user: UserxDto;
    readonly onInputChange: (event: React.ChangeEvent<HTMLInputElement> | InputMaskChangeEvent) => void,
    readonly fieldErrors: ValidationResult<AddressDto>["fieldErrors"];
}

interface AddressStepperPanelProps {
    readonly onSubmit: () => void;
    readonly shippingAddress: AddressDto;
    readonly paymentAddress: AddressDto;
    readonly onChangePaymentAddress: (paymentAddress: AddressDto) => void;
    readonly onChangeShippingAddress: (shippingAddress: AddressDto) => void;
    readonly user: UserxDto;
}


export const AddressStepperContent: React.FC<AddressStepperPanelProps> = (props) => {
    const {shippingAddress, paymentAddress, onSubmit, user, onChangePaymentAddress, onChangeShippingAddress} = props;
    const [shippingAddressValidation, setShippingAddressValidation] = useState<ValidationResult<AddressDto>>({valid: false});
    const [paymentAddressValidation, setPaymentAddressValidation] = useState<ValidationResult<AddressDto>>({valid: false});
    const [isIdenticalToShipping, setIsIdenticalToShipping] = useState(true);

    const onNavigateToOverview = useCallback(() => {
        const shippingAddressErrors = validateFormData(
            shippingAddress,
            [(key) => key !== "id" && shippingAddress[key].length === 0 ? `${key} is required` : undefined]
            );
        setShippingAddressValidation(shippingAddressErrors);
        let _paymentAddress = paymentAddress;
        if (isIdenticalToShipping) {
            _paymentAddress = shippingAddress;
            onChangePaymentAddress(shippingAddress);
        }
        const paymentAddressErrors = validateFormData(
            _paymentAddress,
            [(key) => key !== "id" && _paymentAddress[key].length === 0 ? `${key} is required` : undefined]
        );
        setPaymentAddressValidation(paymentAddressErrors);

        if (shippingAddressErrors.valid && paymentAddressErrors.valid) {
            onSubmit();
        }
    }, [
        isIdenticalToShipping,
        shippingAddressValidation,
        paymentAddressValidation,
        shippingAddress,
        paymentAddress,
        setShippingAddressValidation,
        setPaymentAddressValidation,
    ]);
    return (<>
            <h2>Shipping Address</h2>
            <AddressForm
                fieldErrors={shippingAddressValidation.fieldErrors}
                onInputChange={({target}) => onChangeShippingAddress({...shippingAddress, [target.name]: target.value})}
                user={user}
                address={shippingAddress}
            />
            <h2>Payment Address</h2>
            <Checkbox
                checked={isIdenticalToShipping}
                onChange={({checked}) => setIsIdenticalToShipping(checked ?? isIdenticalToShipping)}
                name={"Same as Shipping Address"}
            />
            <label htmlFor="Same as Shipping Address" className="ml-2">Same as Shipping Address</label>
            {!isIdenticalToShipping
                ? <AddressForm
                    fieldErrors={paymentAddressValidation.fieldErrors}
                    onInputChange={({target}) => onChangePaymentAddress({
                        ...paymentAddress,
                        [target.name]: target.value
                    })}
                    user={user}
                    address={paymentAddress}
                />
                : null}

            <div className="flex pt-4 justify-content-end">
                <Button label="Next" icon="pi pi-arrow-right" iconPos="right" onClick={onNavigateToOverview}/>
            </div>
        </>
    );
};

const AddressForm: React.FC<AddressFormProps> = ({address, user: _, onInputChange, fieldErrors}) => {
    return <form>
        <div className="card p-fluid flex flex-column gap-3">
            <div className="flex-auto mb-3">
                <label htmlFor="street" className="font-bold block">Street</label>
                <InputText id="street" name="street" value={address.street}
                           onChange={onInputChange} required={true}
                           placeholder="Street"
                           autoComplete="off"
                           className={fieldErrors?.street ? 'p-invalid' : undefined}
                />
                {fieldErrors?.street && <small className="p-error">{fieldErrors.street}</small>}
            </div>
            <div className="flex-auto mb-3">
                <label htmlFor="number" className="font-bold block">Number</label>
                <InputText id="number" name="number" value={address.number}
                           onChange={onInputChange}
                           placeholder="First Name"
                           autoComplete="off"
                           className={fieldErrors?.number ? 'p-invalid' : undefined}
                />
                {fieldErrors?.number && <small className="p-error">{fieldErrors.number}</small>}
            </div>
            <div className="flex-auto mb-3">
                <label htmlFor="postalCode" className="font-bold block">Postal Code</label>
                <InputText id="postalCode" name="postalCode" value={address.postalCode}
                           onChange={onInputChange}
                           placeholder="Postal Code"
                           autoComplete="off"
                           className={fieldErrors?.postalCode ? 'p-invalid' : undefined}
                />
                {fieldErrors?.postalCode && <small className="p-error">{fieldErrors.postalCode}</small>}
            </div>
            <div className="flex-auto mb-3">
                <label htmlFor="city" className="font-bold block">City</label>
                <InputText id="city" className={fieldErrors?.city ? 'p-invalid' : undefined} name="city"
                           value={address.city}
                           onChange={onInputChange} placeholder="City" autoComplete="off"
                />
                {fieldErrors?.city && <small className="p-error">{fieldErrors.city}</small>}
            </div>
            <div className="flex-auto mb-3">
                <label htmlFor="country" className="font-bold block">Country</label>
                <InputText id="country" className={fieldErrors?.country ? 'p-invalid' : undefined} name="country"
                           value={address.country}
                           onChange={onInputChange} placeholder="Country" autoComplete="off"
                />
                {fieldErrors?.country && <small className="p-error">{fieldErrors.country}</small>}
            </div>
        </div>
    </form>;
};
