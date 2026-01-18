import React from "react";
import OrderDetailsComponent from "../components/OrderDetailsComponent.tsx";
import {Card} from "primereact/card";

class OrderDetails extends React.Component {
    render() {
        return (
            <div className="grid justify-content-center">
                <div className="col-12 md:col-10 lg:col-8">
                    <Card className="m-4">
                        <OrderDetailsComponent />
                    </Card>
                </div>
            </div>
        );
    }
}

export default OrderDetails;