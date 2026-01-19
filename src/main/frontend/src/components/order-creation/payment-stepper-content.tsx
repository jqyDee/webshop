import React from "react";
import {Button} from "primereact/button";

interface PaymentStepperContentProps {
    readonly onPay: () => void;
}

export const PaymentStepperContent: React.FC<PaymentStepperContentProps> = (props) => {
    return (
        <>
            <h2>Payment is stubbed</h2>
            <span>Normaly you would enter your payment credentials here!</span>
            <div className="flex pt-4 justify-content-end">
                <Button label="Next" icon="pi pi-arrow-right" iconPos="right"
                        onClick={() => props.onPay()}/>
            </div>
        </>
    );
}