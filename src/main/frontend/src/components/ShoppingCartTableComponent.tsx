import React, {useMemo, useState} from "react";
import ShoppingCartListComponent from "./ShoppingCartListComponent";
import { useCart } from "../contexts/cartContext";
import {Button} from "primereact/button";
import {ROUTES} from "../utilities/routes.paths.ts";
import {generatePath, useNavigate} from "react-router-dom";
import {useMutation} from "@tanstack/react-query";
import {createOrderMutation} from "../api/@tanstack/react-query.gen.ts";

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
                <Button label="Clear all" icon="pi pi-trash" onClick={removeAllFromCart}/>
                <CreateOrderButton/>
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

const CreateOrderButton: React.FunctionComponent = () => {
    const navigate = useNavigate();

    const createOrder = useMutation({
        ...createOrderMutation(),
        onSuccess: (order) => {
            if (!order.id) {
                return;
            }
            navigate(generatePath(ROUTES.ORDER_CREATION, {id: order.id.toString()}));
        },
    });
    return <Button label="order now" icon="pi pi-plus-circle" onClick={() => createOrder.mutate({})}/>;

}

export default ShoppingCartTableComponent;
