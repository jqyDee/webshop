import React, {useEffect, useMemo, useRef, useState} from "react";
import {useMatch} from "react-router-dom";
import {ROUTES} from "../utilities/routes.paths.ts";
import {useQuery} from "@tanstack/react-query";
import {getOrderByIdOptions} from "../api/@tanstack/react-query.gen.ts";
import {Stepper} from "primereact/stepper";
import {StepperPanel} from "primereact/stepperpanel";
import {Button} from "primereact/button";
import {AddressDto} from "../api";
import {useUser} from "../contexts/authenticatedUserContext.tsx";
import AddressStepperContent from "./order-creation-component/AddressStepperContent.tsx";

const OrderCreationComponent: React.FC = () => {
    const pathId = useMatch(ROUTES.ORDER_CREATION)?.params.id;
    const id = useMemo(() => Number(pathId), [pathId]);
    const isPathIdValid = useMemo(() => !Number.isNaN(pathId), [pathId]);
    const {currentUser} = useUser();
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

    const {data: order, isLoading} = useQuery({...getOrderByIdOptions({path: {id}}), enabled: isPathIdValid});

    useEffect(() => {
        if (order?.shippingAddress !== undefined && order.shippingAddress !== null) {
            setShippingAddress(order.shippingAddress);
        }
        if (order?.paymentAddress !== undefined && order?.paymentAddress !== null) {
            setPaymentAddress(order.paymentAddress);
        }
    }, [order, setShippingAddress, setPaymentAddress]);

    // !important do this after all hooks are called
    if (isLoading || !isPathIdValid || order === undefined || currentUser === null) {
        return <div>Loading...</div>;
    }
    return <div className="card flex justify-content-center">
        <Stepper ref={stepperRef} style={{flexBasis: "80rem"}}>
            <StepperPanel header={"Address"}>
                <AddressStepperContent
                    user={currentUser}
                    paymentAddress={paymentAddress}
                    shippingAddress={shippingAddress}
                    onChangeShippingAddress={setShippingAddress}
                    onChangePaymentAddress={setPaymentAddress}
                    onSubmit={() => stepperRef.current?.nextCallback()}
                />
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

export default OrderCreationComponent;
