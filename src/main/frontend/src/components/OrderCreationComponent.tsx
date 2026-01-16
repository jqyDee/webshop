import React, {useCallback, useEffect, useMemo, useRef, useState} from "react";
import {useMatch} from "react-router-dom";
import {ROUTES} from "../utilities/routes.paths.ts";
import {useQuery} from "@tanstack/react-query";
import {getOrderByIdOptions} from "../api/@tanstack/react-query.gen.ts";
import {Stepper} from "primereact/stepper";
import {StepperPanel} from "primereact/stepperpanel";
import {Button} from "primereact/button";
import {InputText} from "primereact/inputtext";
import {AddressDto, UserxDto} from "../api";
import {InputMaskChangeEvent} from "primereact/inputmask";
import {useUser} from "../contexts/authenticatedUserContext.tsx";

type AddressValidationResult = { valid: boolean } & Partial<Record<keyof AddressDto, string>>;

const OrderCreationComponent: React.FC = () => {
    const pathId = useMatch(ROUTES.ORDER_CREATION)?.params.id;
    const id = useMemo(() => Number(pathId), [pathId]);
    const {currentUser} = useUser();

    if (!pathId || Number.isNaN(id)) {
        return <div>Path id not valid...</div>;
    }

    const stepperRef = useRef<Stepper>(null);

    const [shippingAddress, setShippingAddress] = useState<AddressDto>({
        city: "",
        country: "",
        number: "",
        postalCode: "",
        street: "",
    });
    const [paymentAddress, setPaymentAddress] = useState<AddressDto>({
        city: "",
        country: "",
        number: "",
        postalCode: "",
        street: "",
    });

    const {data: order, isLoading} = useQuery({...getOrderByIdOptions({path: {id}})});

    useEffect(() => {
        if (order?.shippingAddress !== undefined && order.shippingAddress !== null) {
            setShippingAddress(order.shippingAddress);
        }
        if (order?.paymentAddress !== undefined && order?.paymentAddress !== null) {
            setPaymentAddress(order.paymentAddress);
        }
    }, [order]);
    const [shippingAddressFieldErrors, setShippingAddressFieldErrors] = useState<AddressValidationResult>({valid: false});
    const [paymentAddressFieldErrors, setPaymentAddressFieldErrors] = useState<AddressValidationResult>({valid: false});

    const onNavigateToOverview = useCallback(() => {
        const shippingAddressErrors: AddressValidationResult = {...shippingAddressFieldErrors, valid: true};
        const shippingAddressKeys: (keyof AddressDto)[] = Object.keys(shippingAddress) as unknown as (keyof AddressDto)[];
        for (const key of shippingAddressKeys) {
            if (key === "id") {
                continue;
            }
            if (shippingAddress[key].length > 0) {
                shippingAddressErrors[key] = undefined;
                continue;
            }
            shippingAddressErrors[key] = `${key} is required.`;
            shippingAddressErrors.valid = false;
        }
        setShippingAddressFieldErrors(shippingAddressErrors);
        const paymentAddressErrors: AddressValidationResult = {...paymentAddressFieldErrors, valid: true};
        const paymentAddressKeys: (keyof AddressDto)[] = Object.keys(paymentAddress) as unknown as (keyof AddressDto)[];
        for (const key of paymentAddressKeys) {
            if (key === "id") {
                continue;
            }
            if (paymentAddress[key].length > 0) {
                paymentAddressErrors[key] = undefined;
                continue;
            }
            paymentAddressErrors[key] = `${key} is required.`;
            paymentAddressErrors.valid = false;
        }
        setPaymentAddressFieldErrors(paymentAddressErrors);
        if (shippingAddressErrors.valid && paymentAddressErrors.valid) {
            stepperRef.current?.nextCallback();
        }
    }, [
        stepperRef,
        shippingAddressFieldErrors,
        paymentAddressFieldErrors,
        shippingAddress,
        paymentAddress,
        setShippingAddressFieldErrors,
        setPaymentAddressFieldErrors,
    ]);

    if (isLoading || order === undefined || currentUser === null) {
        return <div>Loading...</div>;
    }
    return <div className="card flex justify-content-center">
        <Stepper ref={stepperRef} style={{flexBasis: "80rem"}}>
            <StepperPanel header={"Address"}>
                <h2>Shipping Address</h2>
                <AddressForm
                    fieldErrors={shippingAddressFieldErrors ?? {}}
                    onInputChange={({target}) => setShippingAddress({...shippingAddress, [target.name]: target.value})}
                    user={currentUser}
                    address={shippingAddress}
                />
                <h2>Payment Address</h2>
                <AddressForm
                    fieldErrors={paymentAddressFieldErrors ?? {}}
                    onInputChange={({target}) => setPaymentAddress({...paymentAddress, [target.name]: target.value})}
                    user={currentUser}
                    address={paymentAddress}
                />
                <div className="flex pt-4 justify-content-end">
                    <Button label="Next" icon="pi pi-arrow-right" iconPos="right" onClick={onNavigateToOverview}/>
                </div>
            </StepperPanel>
            <StepperPanel header={"Overview"}>
                <div className="flex flex-column h-12rem">
                    <div
                        className="border-2 border-dashed surface-border border-round surface-ground flex-auto flex justify-content-center align-items-center font-medium">Content
                        I
                    </div>
                </div>
                <div className="flex pt-4 justify-content-end">
                    <Button label="Next" icon="pi pi-arrow-right" iconPos="right"
                            onClick={() => stepperRef.current?.nextCallback()}/>
                </div>
            </StepperPanel>
            <StepperPanel header={"Payment"}>
                <div className="flex flex-column h-12rem">
                    <div
                        className="border-2 border-dashed surface-border border-round surface-ground flex-auto flex justify-content-center align-items-center font-medium">Content
                        I
                    </div>
                </div>
                <div className="flex pt-4 justify-content-end">
                    <Button label="Next" icon="pi pi-arrow-right" iconPos="right"
                            onClick={() => stepperRef.current?.nextCallback()}/>
                </div>
            </StepperPanel>
            <StepperPanel header={"Confirmation"}>
                <div className="flex flex-column h-12rem">
                    <div
                        className="border-2 border-dashed surface-border border-round surface-ground flex-auto flex justify-content-center align-items-center font-medium">Content
                        I
                    </div>
                </div>
                <div className="flex pt-4 justify-content-start">
                    <Button label="Next" icon="pi pi-arrow-right" iconPos="right"
                            onClick={() => stepperRef.current?.nextCallback()}/>
                </div>
            </StepperPanel>
        </Stepper>
    </div>;
};

interface AddressFormProps {
    readonly address: AddressDto;
    readonly user: UserxDto;
    readonly onInputChange: (event: React.ChangeEvent<HTMLInputElement> | InputMaskChangeEvent) => void,
    readonly fieldErrors: Partial<Record<keyof AddressDto, string>>;
}

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

export default OrderCreationComponent;
