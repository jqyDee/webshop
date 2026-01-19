import {DataTable} from "primereact/datatable";
import {Column} from "primereact/column";
import {Button} from "primereact/button";
import {InputNumber} from "primereact/inputnumber";
import React from "react";
import {CartItemDto, ProductDto} from "../../api";
import {useGlobalToast} from "../../contexts/toast.tsx";

interface ShoppingCartListComponentProps {
    items: CartItemDto[];
    loading: boolean;
    onQuantityChange: (product: ProductDto, qty: number) => void;
    onRemove: (productId: number) => Promise<void>;
}

export const ShoppingCartList: React.FC<ShoppingCartListComponentProps> = ({
                                                                               items,
                                                                               loading,
                                                                               onQuantityChange,
                                                                               onRemove
                                                                           }) => {

    const priceTemplate = (item: CartItemDto) => (
        <span>€{item.product.discountedPrice ?? item.product.price}</span>
    );

    const {showToast} = useGlobalToast();

    const quantityTemplate = (item: CartItemDto) => {
        return (<div className={"flex gap-2 align-items-center"}>
            <InputNumber
                value={item.quantity}
                min={1}
                onValueChange={(e) => onQuantityChange(item.product, e.value ?? 1)}
                showButtons
            />
            <span className={"font-bold"}>(Stock: {item.product.stock})</span>
        </div>);
    };


    const subtotal = (item: CartItemDto) => {
        return ((item.product.discountedPrice ?? item.product.price) * item.quantity).toFixed(2);
    }

    const subtotalTemplate = (item: CartItemDto) => (
        <strong>€{subtotal(item)}</strong>
    );

    const removeTemplate = (item: CartItemDto) => {
        return (<div className={"flex gap-2 align-items-center"}>
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
                <Button
                    label={"Fix"}
                    disabled={item.quantity <= item.product.stock}
                    onClick={() => item.product.stock === 0 ? onRemove(item.product.id!) : onQuantityChange(item.product, item.product.stock)}
                    className={item.quantity > item.product.stock ? "p-button-danger" : ""}
                />
            </div>
        )
    };

    return (
        <DataTable
            value={items}
            loading={loading}
            dataKey="product.id"
        >
            <Column
                field="product.name"
                header="Product"
                sortable
                headerClassName="w-10rem"
            />
            <Column
                field="product.price"
                header="Price"
                body={priceTemplate}
                sortable
                headerClassName="w-10rem"
            />
            <Column
                field="quantity"
                header="Quantity"
                body={quantityTemplate}
                sortable
                headerClassName="w-8rem"
            />
            <Column
                header="Subtotal"
                body={subtotalTemplate}
                headerClassName="w-10rem"
            />
            <Column
                body={removeTemplate}
                headerClassName="w-2rem"
            />
        </DataTable>);
};
