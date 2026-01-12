import React from "react";
import NavbarComponent from "../components/NavbarComponent.tsx";
import OrderTableComponent from "../components/OrderTableComponent.tsx";


class Orders extends React.Component {
    render(){
        return (
            <div>
                <NavbarComponent/>
                <OrderTableComponent/>
            </div>
        )
    }
}

export default Orders;