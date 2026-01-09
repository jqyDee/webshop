import {CartItemDto, ProductDto} from "../api";
import React, {useContext, useEffect, useState} from "react";
import {useUser} from "./authenticatedUserContext.tsx";
import {useMutation, useQuery, useQueryClient} from "@tanstack/react-query";
import {
    addAllToShoppingCartMutation,
    addProductToShoppingCartMutation, deleteProductFromShoppingCartMutation,
    getShoppingCartOptions
} from "../api/@tanstack/react-query.gen.ts";

interface CartContextType {
    cartItems: CartItemDto[],
    addToCart: (product: ProductDto, quantity: number) => Promise<void>,
    removeFromCart: (productId: number) => Promise<void>,
    isLoading: boolean,
}

const CartContext = React.createContext<CartContextType | null>(null);

const CART_STORAGE_KEY = "guest_cart";

export const CartContextProvider: React.FC<{ children: React.ReactNode }> = ({children}) => {
    const { currentUser } = useUser();
    const queryClient = useQueryClient();

    // GUEST
    const [localCart, setLocalCart] = useState<CartItemDto[]>(() => {
        const saved = localStorage.getItem(CART_STORAGE_KEY);
        return saved ? JSON.parse(saved) : [];
    });

    useEffect(() => {
        if (!currentUser) {
            localStorage.setItem(CART_STORAGE_KEY, JSON.stringify(localCart));
        }
    }, [localCart, currentUser]);

    // BACKEND QUERIES
    const { data: remoteCart, isLoading: isFetchingRemote } = useQuery({
        ...getShoppingCartOptions(),
        enabled: !!currentUser, // Only fetch if logged in
    });

    const addMutation = useMutation(addProductToShoppingCartMutation());
    const syncMutation = useMutation(addAllToShoppingCartMutation());
    const removeMutation = useMutation(deleteProductFromShoppingCartMutation());

    // SYNCING
    useEffect(() => {
        const syncCart = async () => {
            if (currentUser && localCart.length > 0) {
                // Backend expects a Map<Long, Integer> (productId: quantity)
                const body: Record<string, number> = {};
                localCart.forEach(item => {
                    body[item.product.id.toString()] = item.quantity || 1;
                });

                await syncMutation.mutateAsync({ body });
                setLocalCart([]); // Clear guest cart
                localStorage.removeItem(CART_STORAGE_KEY);
                await queryClient.invalidateQueries({queryKey: ['getShoppingCart']});
            }
        };
        syncCart();
    }, [currentUser]);

    // ACTIONS
    const addToCart = async (product: ProductDto, quantity: number) => {
        if (currentUser) {
            await addMutation.mutateAsync({
                path: { productId: product.id },
                query: { quantity }
            });
            await queryClient.invalidateQueries({queryKey: ['getShoppingCart']});
        } else {
            setLocalCart(prev => {
                const existing = prev.find(item => item.product.id === product.id);
                if (existing) {
                    return prev.map(item =>
                        item.product.id === product.id
                            ? { ...item, quantity: (item.quantity || 0) + quantity }
                            : item
                    );
                }
                return [...prev, { product, quantity, user: {} as any }];
            });
        }
    };

    const removeFromCart = async (productId: number) => {
        if (currentUser) {
            await removeMutation.mutateAsync({ path: { productId } });
            await queryClient.invalidateQueries({queryKey: ['getShoppingCart']});
        } else {
            setLocalCart(prev => prev.filter(item => item.product.id !== productId));
        }
    };

    return (
        <CartContext.Provider value={{
            cartItems: currentUser ? (remoteCart ?? []) : localCart,
            addToCart,
            removeFromCart,
            isLoading: isFetchingRemote
        }}>
            {children}
        </CartContext.Provider>
    );
};

export const useCart = () => {
    const context = useContext(CartContext);
    if (!context) throw new Error("useCart must be used within CartProvider");
    return context;
};