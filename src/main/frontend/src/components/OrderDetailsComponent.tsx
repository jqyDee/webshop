import {ProgressSpinner} from "primereact/progressspinner";
import React, {useRef} from "react";
import {Toast} from "primereact/toast";
import {Tag} from "primereact/tag";
import {DataTable} from "primereact/datatable";
import {Column} from "primereact/column";
import {getOrderByIdOptions, cancelOrderMutation} from "../api/@tanstack/react-query.gen.ts";
import { useNavigate, useParams } from "react-router-dom";
import { Button } from "primereact/button";
import AddressComponent from "./AddressComponent.tsx";
import { useQuery, useMutation, useQueryClient } from "@tanstack/react-query";
import { useUser } from "../contexts/authenticatedUserContext.tsx";

const OrderDetailsComponent: React.FC = () => {

    const {id} = useParams<{ id: string }>();
    const toast = useRef<Toast | null>(null);
    const navigate = useNavigate();
    const queryClient = useQueryClient();

    const { currentUser, isAdmin } = useUser();

    const cancelMutation = useMutation(cancelOrderMutation());

    const onCancel = async (orderId: number) => {
        await cancelMutation.mutateAsync({
            path: {orderId: orderId}
        }, {
            onSuccess: async () => {
                // Invalidate the query to refresh the UI
                await queryClient.invalidateQueries({
                    queryKey: getOrderByIdOptions({ path: { id: orderId } }).queryKey
                });
                toast.current?.show({ severity: 'success', summary: 'Success', detail: 'Order Cancelled' });
            },
            onError: (error) => {
                console.error("Cancel failed:", error);
                toast.current?.show({ severity: 'error', summary: 'Error', detail: 'Failed to cancel' });
            }
        });
    };

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

            {/* Header Row: Back Button and Title */}
            <div className="flex align-items-center gap-3 mb-4">
                <Button
                    icon="pi pi-arrow-left"
                    className="p-button-text p-button-rounded"
                    onClick={() => navigate('/orders')}
                />
                <h2 className="m-0">Order #{id}</h2>
            </div>

            {/* Info Row: Customer (Admin Only) and Status */}
            <div className="flex justify-content-between align-items-start mb-4">
                <div className="flex gap-4">
                    {isAdmin && order.user && (
                        <div className="flex flex-column gap-1">
                            <span className="text-500 font-bold text-xs uppercase">Customer</span>
                            <div className="flex align-items-center text-900 font-medium">
                                <i className="pi pi-user mr-2 text-primary"></i>
                                {order.user.firstName} {order.user.lastName}
                                <span className="text-500 ml-2 text-sm">({order.user.username})</span>
                            </div>
                        </div>
                    )}
                </div>

                {/* Right Side: Status Tag */}
                <div className="flex flex-column gap-1 align-items-end">
                    <span className="text-500 font-bold text-xs uppercase">Status</span>
                    <Tag value={order.status} severity={getStatusSeverity(order.status)} />
                </div>
            </div>
            <DataTable value={order.products} className="p-datatable-sm">
                <Column
                    header="Productname"
                    body={(item) => (
                        <span
                            className="text-primary font-medium cursor-pointer hover:underline"
                            onClick={() => navigate(`/product/${item.product.id}`)}
                        >
                            {item.product.name}
                        </span>
                    )}
                />
                <Column field="quantity" header="Quantity" />
                <Column
                    header="Price"
                    body={(item) => `€${(item.total).toFixed(2)}`}
                />
                <Column
                    field="total"
                    header="Sum"
                    body={(item) => <span className="font-bold text-primary">€{item.total * item.quantity}</span>}
                />
            </DataTable>

            <div className="grid">
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

            <div className="flex justify-content-end align-items-center">
                <div className="text-right">
                    <span className="text-xl font-light block mb-2">Total</span>
                    <span className="text-4xl font-bold text-900">€{order.sum}</span>
                </div>
            </div>

            {(currentUser || isAdmin) && ['PENDING', 'PENDING_PAYMENT', 'PAID'].includes(order.status) && (
                <Button
                    icon="pi pi-times"
                    label="Cancel Order"
                    onClick={() => onCancel(order.id!)}
                    loading={cancelMutation.isPending}
                    className="p-button-danger p-button-sm w-auto mt-2"
                />
            )}
        </div>
    );
}

export default OrderDetailsComponent;