import {useParams} from "react-router-dom";
import {useQuery} from "@tanstack/react-query";
import {ProgressSpinner} from "primereact/progressspinner";
import React, {useRef} from "react";
import {Toast} from "primereact/toast";
import {Tag} from "primereact/tag";
import {DataTable} from "primereact/datatable";
import {Column} from "primereact/column";

const OrderDetailsComponent: React.FC = () => {

    const {id} = useParams<{ id: string }>();
    const toast = useRef<Toast | null>(null);

    // QUERY
    const {data: order, isLoading, error} = useQuery(
        getOrderByIdOptions({
            path: {id: Number(id)}
        }),
    );

    // LAYOUT
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
        <div className="surface-card p-4 shadow-2 border-round">
            <div className="flex justify-content-between align-items-center mb-5">
                <h2 className="m-0">Orderdetails #{id}</h2>
                <Tag value={order.status} severity={getStatusSeverity(order.status)} />
            </div>

            <DataTable value={order.poducts} responsiveLayout="stack" breakpoint="960px">
                <Column field="name" header="Productname" fontStyle="bold" />
                <Column field="quantity" header="Quantity" />
                <Column
                    header="Price"
                    body={(item) => `${(item.total).toFixed(2)} €`}
                />
                <Column
                    field="total"
                    header="Summe"
                    body={(item) => <span className="font-bold text-primary">{item.total * item.quantity} €</span>}
                />
            </DataTable>

            <div className="flex justify-content-end mt-4">
                <div className="text-right">
                    <span className="text-xl font-light block mb-2">Gesamtsumme</span>
                    <span className="text-4xl font-bold text-900">{order.sum} €</span>
                </div>
            </div>
        </div>
    );
}

export default OrderDetailsComponent;