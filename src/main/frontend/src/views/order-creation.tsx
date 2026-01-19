import React from "react";
import {OrderCreation as OrderCreationComponent} from "../components/order-creation.tsx";


class OrderCreation extends React.Component {
    render() {
        return (
            <div>
                <OrderCreationComponent />
            </div>
        )
    }

}

export default OrderCreation;