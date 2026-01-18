import React, { useState } from "react";
import { DataView, DataViewLayoutOptions, DataViewPageEvent } from "primereact/dataview";
import { Button } from "primereact/button";
import { Tag } from "primereact/tag";
import { OrderDto } from "../api";
import {useUser} from "../Contexts/authenticatedUserContext.tsx";
import {Link} from "react-router-dom";
import {OrderDetailRoute} from "../routes.ts";

interface OrderListComponentProps {
    orders: OrderDto[];
    loading: boolean;
    totalCount: number;
    first: number;
    pageSize: number;
    onPage: (event: DataViewPageEvent) => void;
    onView: (orderId: number) => void;
    showUserColumn?: boolean;
}

const OrderListComponent: React.FC<OrderListComponentProps> = ({
                                                                   orders,
                                                                   loading,
                                                                   totalCount,
                                                                   first,
                                                                   pageSize,
                                                                   onPage,
                                                                   onView,
                                                                   showUserColumn,
                                                               }) => {
    const [layout, setLayout] = useState<'list' | 'grid'>('list');
    const {currentUser, isAdmin} = useUser();

    const getStatusSeverity = (status: string) => {
        switch (status?.toUpperCase()) {
            case 'DELIVERED': return 'success';
            case 'PENDING': return 'warning';
            case 'PENDING_PAYMENT': return 'warning';
            case 'PROCESSING': return 'warning';
            case 'SHIPPED': return 'warning';
            case 'CANCELLED': return 'danger';
            default: return 'info';
        }
    };

    const listItem = (order: OrderDto) => (
        <div className="col-12">
            <div className="card border-round flex flex-column xl:flex-row xl:align-items-start p-4 gap-4 shadow-5 m-2">
                <div className="flex-1 flex flex-column gap-2">
                    <div className="flex align-items-center gap-2">
                        <Link className="nostyle no-underline font-bold text-xl text-color hover:underline" to={OrderDetailRoute.url.replace(':id', order.id!.toString())}>Order #{order.id}</Link>
                        <Tag value={order.status} severity={getStatusSeverity(order.status!)} />
                    </div>
                    {showUserColumn && <span className="text-500 text-sm">Customer ID: {order.user?.id}</span>}
                    <div className="text-900 font-semibold">{order.sum} €</div>
                </div>
                <div className="flex flex-row md:flex-column align-items-center md:align-items-end gap-2">
                    <span className="text-500">{new Date(order.createdDate!).toLocaleDateString()}</span>
                    <Button icon="pi pi-search" label="View" onClick={() => onView(order.id!)} className="p-button-sm" />
                </div>
            </div>
        </div>
    );

    const gridItem = (order: OrderDto) => (
        <div className="col-12 sm:col-6 lg:col-4 p-2">
            <div className="p-4 card border-round flex flex-column shadow-5">
                <div className="flex justify-content-between align-items-center">
                    <Link className="nostyle no-underline font-bold text-xl text-color hover:underline" to={OrderDetailRoute.url.replace(':id', order.id!.toString())}>Order #{order.id}</Link>
                    <Tag value={order.status} severity={getStatusSeverity(order.status!)} />
                </div>
                <div className="flex flex-column align-items-center py-3">
                    <i className="pi pi-package text-5xl text-primary mb-2"></i>
                    <div className="text-2xl font-bold">{order.sum} €</div>
                    <div className="text-500 text-sm">{new Date(order.createdDate!).toLocaleDateString()}</div>
                </div>
                <Button icon="pi pi-search" label="View Details" onClick={() => onView(order.id!)} className="w-full" />
            </div>
        </div>
    );

    const itemTemplate = (order: OrderDto, layoutType: 'list' | 'grid') => {
        if (!order) return null;
        return layoutType === 'list' ? listItem(order) : gridItem(order);
    };

    const header = () => (
        <div className="flex justify-content-between align-items-center">
            <h4 className="m-0">{!isAdmin ? currentUser?.username + "'s" : 'All'} Order History</h4>
            <DataViewLayoutOptions layout={layout} onChange={(e) => setLayout(e.value as 'list' | 'grid')} />
        </div>
    );

    return (
            <DataView
                value={orders}
                layout={layout}
                itemTemplate={itemTemplate}
                paginator
                rows={pageSize}
                totalRecords={totalCount}
                lazy
                first={first}
                onPage={onPage}
                loading={loading}
                header={header()}
                emptyMessage="No orders found."
            />
    );
};

export default OrderListComponent;