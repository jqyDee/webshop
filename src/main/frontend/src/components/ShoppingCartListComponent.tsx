import { DataTable } from "primereact/datatable";
import { Column } from "primereact/column";
import { Button } from "primereact/button";
import { InputNumber } from "primereact/inputnumber";
import React from "react";
import { CartItemDto } from "../api";

interface ShoppingCartListComponentProps {
    items: CartItemDto[];
    loading: boolean;
    onQuantityChange: (product: any, qty: number) => void;
    onRemove: (productId: number) => void;
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

    const quantityTemplate = (item: CartItemDto) => (
        <InputNumber
            value={item.quantity}
            min={1}
            max={item.product.stock}
            onValueChange={(e) => onQuantityChange(item.product, e.value ?? 1)}
            showButtons
        />
    );

    const subtotalTemplate = (item: CartItemDto) => (
        <strong>€{((item.product.discountedPrice ?? item.product.price) * item.quantity).toFixed(2)}</strong>
    );

    const removeTemplate = (item: CartItemDto) => (
        <Button
            icon="pi pi-trash"
            severity="danger"
            text
            onClick={() => onRemove(item.product.id)}
        />
    );

    return (
        <DataTable value={items} loading={loading} responsiveLayout="scroll">
            <Column field="product.name" header="Product" />
            <Column header="Price" body={priceTemplate} />
            <Column header="Quantity" body={quantityTemplate} />
            <Column header="Subtotal" body={subtotalTemplate} />
            <Column body={removeTemplate} />
        </DataTable>
    );
};

export default ShoppingCartListComponent;
