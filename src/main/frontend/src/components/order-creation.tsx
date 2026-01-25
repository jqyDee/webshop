import React, {useEffect, useMemo, useState} from "react";
import {useMatch} from "react-router-dom";
import {ROUTES} from "../utilities/routes.paths.ts";
import {useMutation, useQuery} from "@tanstack/react-query";
import {confirmOrderMutation, getOrderByIdOptions} from "../api/@tanstack/react-query.gen.ts";
import {Stepper} from "primereact/stepper";
import {StepperPanel} from "primereact/stepperpanel";
import {AddressDto, StatusEnum} from "../api";
import {AddressStepperContent} from "./order-creation/address-stepper-content.tsx";
import {OverviewStepperContent} from "./order-creation/overview-stepper-content.tsx";
import {useGlobalToast} from "../contexts/toast.tsx";
import {PaymentStepperContent} from "./order-creation/payment-stepper-content.tsx";
import {ConfirmationStepperContent} from "./order-creation/confirmation-stepper-content.tsx";

enum Steps {
    Address,
    Overview,
    Payment,
    Confirmation,
}

export const OrderCreation: React.FC = () => {
    const pathId = useMatch(ROUTES.ORDER_CREATION)?.params.id;
    const {showToast} = useGlobalToast();
    const [activeStep, setActiveStep] = useState<Steps>(Steps.Address);
    const id = useMemo(() => Number(pathId), [pathId]);
    const isPathIdValid = useMemo(() => !Number.isNaN(pathId), [pathId]);
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

    const {data: order, isLoading, refetch} = useQuery({
        ...getOrderByIdOptions({path: {id}}),
        enabled: isPathIdValid,
    });
    const confirmOrder = useMutation({
        ...confirmOrderMutation(),
        onError: (error) => {
            console.error("Confirm failed:", error);
            showToast({ severity: 'error', summary: 'Error', detail: 'Failed to confirm order' });
        },
        onSuccess: async () => {
            await refetch();
            setActiveStep(Steps.Payment);
        }
    });

    useEffect(() => {
        if (
            order !== undefined
            && [StatusEnum.PAID, StatusEnum.SHIPPED, StatusEnum.DELIVERED, StatusEnum.CANCELLED, StatusEnum.PROCESSING].includes(order.status)
        ) {
            setActiveStep(Steps.Confirmation);
        }
        if (order?.shippingAddress !== undefined && order.shippingAddress !== null) {
            setShippingAddress(order.shippingAddress);
        }
        if (order?.paymentAddress !== undefined && order?.paymentAddress !== null) {
            setPaymentAddress(order.paymentAddress);
        }
    }, [order, setShippingAddress, setPaymentAddress, setActiveStep]);

    // !important do this after all hooks are called
    if (isLoading || !isPathIdValid || order === undefined) {
        return <div>Loading...</div>;
    }
    return <div className="card flex justify-content-center">
        <Stepper style={{flexBasis: "80rem"}} linear orientation={"vertical"} activeStep={activeStep}>
            <StepperPanel header={"Address"}>
                <AddressStepperContent
                    user={order.user}
                    paymentAddress={paymentAddress}
                    shippingAddress={shippingAddress}
                    onChangeShippingAddress={setShippingAddress}
                    onChangePaymentAddress={setPaymentAddress}
                    onSubmit={() => setActiveStep(Steps.Overview)}
                />
            </StepperPanel>
            <StepperPanel header={"Overview"}>
                <OverviewStepperContent
                    paymentAddress={paymentAddress}
                    shippingAddress={shippingAddress}
                    order={order}
                    onSubmitOrder={async () => {
                        await confirmOrder.mutateAsync({body: {shippingAddress, paymentAddress}, path: {orderId: order.id}});

                    }}
                    onGoBackToAddress={() => setActiveStep(Steps.Address)}
                />
            </StepperPanel>
            <StepperPanel header={"Payment"}>
                    <PaymentStepperContent onPay={() => setActiveStep(Steps.Confirmation)}/>
            </StepperPanel>
            <StepperPanel header={"Confirmation"}>
                <ConfirmationStepperContent order={order}/>
            </StepperPanel>
        </Stepper>
    </div>;
};
