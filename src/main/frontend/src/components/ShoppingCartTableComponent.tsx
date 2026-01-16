import React, { useMemo, useState } from "react";
import ShoppingCartListComponent from "./ShoppingCartListComponent";
import { useCart } from "../Contexts/cartContext";
import {Button} from "primereact/button";

const ShoppingCartTableComponent: React.FC = () => {
    const { cartItems, isLoading, updateCartItem, removeFromCart, removeAllFromCart } = useCart();

    const [searchTerm, setSearchTerm] = useState('');

    const filteredItems = useMemo(() => {
        if (!searchTerm) return cartItems;
        return cartItems.filter(item =>
            item.product.name.toLowerCase().includes(searchTerm.toLowerCase())
        );
    }, [cartItems, searchTerm]);

    const totalPrice = useMemo(() =>
        cartItems.reduce((sum, item) =>
            sum + ((item.product.discountedPrice ?? item.product.price) * item.quantity), 0
        ), [filteredItems]);

    const header = (
        <div className="flex justify-content-between p-3">
            <input
                type="text"
                placeholder="Search in cart..."
                value={searchTerm}
                onChange={(e) => setSearchTerm(e.target.value)}
                className="p-inputtext p-component w-full"
            />
        </div>
    );

    return (
        <div>
            {header}
            <ShoppingCartListComponent
                items={filteredItems}
                loading={isLoading}
                onQuantityChange={(product, quantity) => updateCartItem(product, quantity, false)}
                onRemove={removeFromCart}
            />
            <div className="flex align-items-center">
                <Button label="Clear all" icon="pi pi-trash" onClick={() => removeAllFromCart()}/>
                <div className="flex flex-column mt-4 text-xl font-bold ml-auto">
                <span>
                    Total
                </span>
                    €{totalPrice.toFixed(2)}
                </div>
            </div>
        </div>
    );
};

export default ShoppingCartTableComponent;
