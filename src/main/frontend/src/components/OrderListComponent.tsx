import React from "react";
import { DataView, DataViewPageEvent } from "primereact/dataview";
import { Button } from "primereact/button";
import { OrderDto } from "../api";

interface OrderListComponentProps {
    orders: OrderDto[];
    loading: boolean;
    totalCount: number;
    first: number;
    pageSize: number;
    onPage: (event: DataViewPageEvent) => void;
    onView: (orderId: number) => void;
}

const OrderListComponent: React.FC<OrderListComponentProps> = ({
                                                                   orders,
                                                                   loading,
                                                                   totalCount,
                                                                   first,
                                                                   pageSize,
                                                                   onPage,
                                                                   onView,
                                                               }) => {

    const itemTemplate = (order: OrderDto) => (
        <div className="order-card p-4 border-1 surface-border border-round mb-3 flex justify-content-between">
            <div>
                <strong>Order #{order.id}</strong>
                <p>Status: {order.status}</p>
                <p>Total: {order.sum} €</p>
                {order.createdDate && (
                    <p>Date: {new Date(order.createdDate).toLocaleDateString()}</p>
                )}
            </div>

            {order.id !== undefined && (
                <Button label="View details" onClick={() => onView(order.id!)} />
            )}
        </div>
    );

    return (
        <DataView
            value={orders}
            loading={loading}
            itemTemplate={itemTemplate}
            paginator
            lazy
            totalRecords={totalCount}
            first={first}
            rows={pageSize}
            onPage={onPage}
        />
    );
};

export default OrderListComponent;
