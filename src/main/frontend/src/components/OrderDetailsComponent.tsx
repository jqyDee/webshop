import {useQuery} from "@tanstack/react-query";
import {ProgressSpinner} from "primereact/progressspinner";
import React, {useRef} from "react";
import {Toast} from "primereact/toast";
import {Tag} from "primereact/tag";
import {DataTable} from "primereact/datatable";
import {Column} from "primereact/column";
import {getOrderByIdOptions} from "../api/@tanstack/react-query.gen.ts";
import { useNavigate, useParams } from "react-router-dom";
import { Button } from "primereact/button";
import AddressComponent from "./AddressComponent.tsx";

const OrderDetailsComponent: React.FC = () => {

    const {id} = useParams<{ id: string }>();
    const toast = useRef<Toast | null>(null);
    const navigate = useNavigate();

    // QUERY
    const {data: order, isLoading, error} = useQuery(
        getOrderByIdOptions({
            path: {id: Number(id)}
        }),
    );

    // LAYOUT
    const getStatusSeverity = (status: string) => {
        switch (status) {
            case 'DELIVERED': return 'success';
            case 'PENDING': return 'warning';
            case 'CANCELLED': return 'danger';
            case 'SHIPPED': return 'info';
            default: return null;
        }
    };

    if (isLoading) return (
        <div className="flex justify-content-center mt-8">
            <ProgressSpinner/>
        </div>
    );

    if (error || !order) return (
        <div className="text-center mt-8">
            Order not found.
        </div>
    );

    return (
        <div className="p-2">
            <Toast ref={toast}/>
            <div className="flex justify-content-between align-items-center mb-5">
                <h2 className="m-0">Details of Order with id: {id}</h2>
                <Tag value={order.status} severity={getStatusSeverity(order.status)} />
            </div>

            <DataTable value={order.products} className="p-datatable-sm">
                <Column
                    header="Productname"
                    body={(item) => (
                        <span
                            className="text-primary font-medium cursor-pointer hover:underline"
                            onClick={() => navigate(`/product/${item.product.id}`)}
                        >
                            {item.name}
                        </span>
                    )}
                />
                <Column field="quantity" header="Quantity" />
                <Column
                    header="Price"
                    body={(item) => `${(item.total).toFixed(2)} €`}
                />
                <Column
                    field="total"
                    header="sum"
                    body={(item) => <span className="font-bold text-primary">{item.total * item.quantity} €</span>}
                />
            </DataTable>

            <div className="grid mb-5">
                <div className="col-12 md:col-6">
                    <AddressComponent
                        title="Shipping Address"
                        address={order.shippingAddress}
                        mode="view"
                    />
                </div>
                <div className="col-12 md:col-6">
                    <AddressComponent
                        title="Billing Address"
                        address={order.paymentAddress}
                        mode="view"
                    />
                </div>
            </div>

            <div className="flex justify-content-between align-items-center mt-6">
                <Button
                    label="Go Back"
                    icon="pi pi-arrow-left"
                    className="p-button-primary"
                    onClick={() => navigate('/orders')}
                />
                <div className="text-right">
                    <span className="text-xl font-light block mb-2">Total</span>
                    <span className="text-4xl font-bold text-900">{order.sum} €</span>
                </div>
            </div>
        </div>
    );
}

export default OrderDetailsComponent;