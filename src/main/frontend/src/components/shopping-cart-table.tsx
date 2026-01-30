import React, {useMemo, useState} from "react";
import {useCart} from "../contexts/cart.tsx";
import {Button} from "primereact/button";
import {ROUTES} from "../utilities/routes.paths.ts";
import {generatePath, useNavigate} from "react-router-dom";
import {useMutation, useQueryClient} from "@tanstack/react-query";
import {createOrderMutation, getShoppingCartQueryKey} from "../api/@tanstack/react-query.gen.ts";
import {ShoppingCartList} from "./shopping-cart-table/shopping-cart-list.tsx";
import {useGlobalToast} from "../contexts/toast.tsx";
import {useUser} from "../contexts/authenticated-user.tsx";
import {Total} from "./total.tsx";

export const ShoppingCartTable: React.FC = () => {
    const {cartItems, isLoading, updateCartItem, removeFromCart, removeAllFromCart} = useCart();
    const {currentUser} = useUser();
    const [searchTerm, setSearchTerm] = useState('');
    const navigate = useNavigate();

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

    const goBack = () => {
        if (window.history.length > 1) {
            navigate(-1);
        } else {
            navigate("/");
        }
    }

    const header = (
        <div className={"flex flex-wrap align-items-center justify-content-between my-4"}>
            <div className="flex align-items-center gap-2">
                <Button
                    icon="pi pi-arrow-left"
                    className="p-button-text p-button-rounded"
                    onClick={goBack}
                />
                <h2 className={"m-0"}>Shopping Cart</h2>
            </div>

            <div className="flex flex-row align-items-center gap-2">
                <Button className={"p-button-text p-button-danger w-full"} label="Clear" icon="pi pi-trash" onClick={removeAllFromCart}/>
                <input
                    type="text"
                    placeholder="Search in cart..."
                    value={searchTerm}
                    onChange={(e) => setSearchTerm(e.target.value)}
                    className="p-inputtext p-component w-full"
                    style={{minWidth: '300px'}}
                />
            </div>
        </div>
    );

    return (
        <div>
            {header}
            <ShoppingCartList
                items={filteredItems}
                loading={isLoading}
                onQuantityChange={(product, quantity) => updateCartItem(product, quantity, false)}
                onRemove={removeFromCart}
            />
            <div className="flex align-items-center justify-content-between gap-2 mt-4">
                <div className="flex align-items-center gap-2">
                    {currentUser ? <CreateOrderButton onCreateOrderFailure={() => setSearchTerm("")}/> : null}
                </div>
                <Total total={totalPrice} />
            </div>
        </div>
    );
};

interface CreateOrderButtonProps {
    readonly onCreateOrderFailure: () => void;
}

const CreateOrderButton: React.FunctionComponent<CreateOrderButtonProps> = (props) => {
    const navigate = useNavigate();
    const {showToast} = useGlobalToast();
    const queryClient = useQueryClient();

    const createOrder = useMutation({
        ...createOrderMutation(),
        onError: async (error) => {
            console.error("Error ordering shopping cart: ", error);
            showToast({
                severity: "error",
                summary: "Error",
                detail: "Error ordering your shopping cart. You cannot order more than the stock"
            });
            await queryClient.invalidateQueries({queryKey: getShoppingCartQueryKey()});
            props.onCreateOrderFailure();
        },
        onSuccess: (order) => {
            if (!order.id) {
                return;
            }
            navigate(generatePath(ROUTES.ORDER_CREATION, {id: order.id.toString()}));
        },
    });
    return <Button label="Order now" icon="pi pi-plus-circle" onClick={() => createOrder.mutate({})}/>;

}
