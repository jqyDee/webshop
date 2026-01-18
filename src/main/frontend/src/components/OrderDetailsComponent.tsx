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
import { useQuery, useMutation, useQueryClient } from "@tanstack/react-query";

const OrderDetailsComponent: React.FC = () => {

    const {id} = useParams<{ id: string }>();
    const toast = useRef<Toast | null>(null);
    const navigate = useNavigate();
    const queryClient = useQueryClient();

    const cancelMutation = useMutation({
        mutationFn: async (orderId: number) => {
            const response = await fetch(`/api/orders/${orderId}/cancel`, {
                method: 'POST',
            });
            if (!response.ok) throw new Error('Failed to cancel order');
            return response;
        },
        onSuccess: () => {
            queryClient.invalidateQueries({ queryKey: getOrderByIdOptions({ path: { id: Number(id) } }).queryKey });

            toast.current?.show({
                severity: 'success',
                summary: 'Cancelled',
                detail: 'The order has been successfully cancelled.',
                life: 3000
            });
        },
        onError: () => {
            toast.current?.show({
                severity: 'error',
                summary: 'Error',
                detail: 'Could not cancel the order. Please try again.',
                life: 3000
            });
        }
    });

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

    const onCancel = (orderId: number) => {
        cancelMutation.mutate(orderId);
    };

    return (
        <div className="p-2">
            <Toast ref={toast}/>

            <div className="flex align-items-center justify-content-between w-full mb-2">
                <div className="flex-1 flex justify-content-start">
                    <Button
                        icon="pi pi-arrow-left"
                        className="p-button-text p-button-rounded"
                        onClick={() => navigate('/orders')}
                    />
                </div>

                <div className="flex-grow-0 text-end">
                    <h2 className="m-0">Order id: {id}</h2>
                </div>

            </div>
            {/* 3. Right Section: Flex-1 to match the left section */}
            <div className="flex flex-column gap-2 mb-4 align-items-end">
                <span className="text-700 font-bold text-xl">Status</span>
                <Tag value={order.status} severity={getStatusSeverity(order.status)} className=""/>
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

            {(order.status === 'PENDING' || order.status === 'PENDING_PAYMENT' || order.status === 'PAID' || order.status === 'PROCESSING') && (
                <Button
                    icon="pi pi-times"
                    label="Cancel Order"
                    onClick={() => onCancel(order.id!)}
                    loading={cancelMutation.isPending} // Show a spinner on the button while working
                    className="p-button-danger w-full mt-4"
                />
            )}
        </div>
    );
}

export default OrderDetailsComponent;