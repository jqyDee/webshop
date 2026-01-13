import React from "react";
import NavbarComponent from "../components/NavbarComponent.tsx";
import OrderTableComponent from "../components/OrderTableComponent.tsx";

const Orders: React.FC = () => {
    return (
        <div className="min-h-screen surface-ground">
            <NavbarComponent />
            <div className="container mx-auto p-4">
                <h2 className="text-2xl font-bold mb-4">Your Orders</h2>
                <OrderTableComponent />
            </div>
        </div>
    );
};

export default Orders;