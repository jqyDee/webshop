import React, { useMemo, useState } from "react";
import ShoppingCartListComponent from "./ShoppingCartListComponent";
import { useCart } from "../Contexts/cartContext";

const ShoppingCartTableComponent: React.FC = () => {
    const { cartItems, isLoading, updateCartItem, removeFromCart } = useCart();

    // Optional: local search/filter like Product component
    const [searchTerm, setSearchTerm] = useState('');

    const filteredItems = useMemo(() => {
        if (!searchTerm) return cartItems;
        return cartItems.filter(item =>
            item.product.name.toLowerCase().includes(searchTerm.toLowerCase())
        );
    }, [cartItems, searchTerm]);

    const totalPrice = useMemo(() =>
        filteredItems.reduce((sum, item) =>
            sum + ((item.product.discountedPrice ?? item.product.price) * item.quantity), 0
        ), [filteredItems]);

    // Optional: header for search/filter, similar to ProductListComponent
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
        <div className="card">
            {header}
            <ShoppingCartListComponent
                items={cartItems}
                loading={isLoading}
                onQuantityChange={(product, quantity) => updateCartItem(product, quantity, false)}
                onRemove={removeFromCart}
            />
            <div className="flex justify-content-end mt-4 text-xl font-bold">
                Total: €{totalPrice.toFixed(2)}
            </div>
        </div>
    );
};

export default ShoppingCartTableComponent;
