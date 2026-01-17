import { DataTable } from "primereact/datatable";
import { Column } from "primereact/column";
import { Button } from "primereact/button";
import { InputNumber } from "primereact/inputnumber";
import React from "react";
import { CartItemDto } from "../api";
import {useGlobalToast} from "../Contexts/toastContext.tsx";

interface ShoppingCartListComponentProps {
    items: CartItemDto[];
    loading: boolean;
    onQuantityChange: (product: any, qty: number) => void;
    onRemove: (productId: number) => Promise<void>;
}

const ShoppingCartListComponent: React.FC<ShoppingCartListComponentProps> = ({
                                                                                 items,
                                                                                 loading,
                                                                                 onQuantityChange,
                                                                                 onRemove
                                                                             }) => {

    const priceTemplate = (item: CartItemDto) => (
        <span>€{item.product.discountedPrice ?? item.product.price}</span>
    );

    const {showToast} = useGlobalToast();

    const quantityTemplate = (item: CartItemDto) => (
        <InputNumber
            value={item.quantity}
            min={1}
            onValueChange={(e) => onQuantityChange(item.product, e.value ?? 1)}
            showButtons
        />
    );

    const subtotal = (item: CartItemDto) => {
        return ((item.product.discountedPrice ?? item.product.price) * item.quantity).toFixed(2);
    }

    const subtotalTemplate = (item: CartItemDto) => (
        <strong>€{subtotal(item)}</strong>
    );

    const removeTemplate = (item: CartItemDto) => {
        return (
            <Button
                icon="pi pi-trash"
                severity="danger"
                text
                onClick={() => {
                    item.product.id ? onRemove(item.product.id) : showToast({
                        severity: 'error',
                        summary: 'Error loading Product',
                        detail: 'The current product could not be loaded. Please try refreshing the page!',
                        life: 3000
                    })
                }}
            />
        )
    };

    return (
        <DataTable value={items} loading={loading} dataKey="product.id">
            <Column field="product.name" header="Product" sortable/>
            <Column field="product.price" header="Price" body={priceTemplate} sortable/>
            <Column field="quantity" header="Quantity" body={quantityTemplate} sortable/>
            <Column header="Subtotal" body={subtotalTemplate}/>
            <Column body={removeTemplate} />
        </DataTable>
    );
};

export default ShoppingCartListComponent;
