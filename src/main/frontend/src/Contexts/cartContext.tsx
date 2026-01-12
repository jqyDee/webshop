import {CartItemDto, ProductDto} from "../api";
import React, {useCallback, useContext, useEffect, useMemo, useState} from "react";
import {useUser} from "./authenticatedUserContext.tsx";
import {useMutation, useQuery, useQueryClient} from "@tanstack/react-query";
import {
    addAllToShoppingCartMutation,
    deleteProductFromShoppingCartMutation,
    getShoppingCartOptions, getShoppingCartQueryKey, updateProductInShoppingCartMutation
} from "../api/@tanstack/react-query.gen.ts";

interface CartContextType {
    cartItems: CartItemDto[],
    updateCartItem: (product: ProductDto, quantity: number) => Promise<void>,
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

    const updateMutation = useMutation(updateProductInShoppingCartMutation());
    const syncMutation = useMutation(addAllToShoppingCartMutation());
    const removeMutation = useMutation(deleteProductFromShoppingCartMutation());

    // SYNCING
    useEffect(() => {
        if (!currentUser || localCart.length === 0) return;

        (async () => {
            try {
                // backend expects map
                const body: Record<string, number> = {};
                localCart.forEach(item => {
                    body[item.product.id.toString()] = item.quantity || 1;
                });

                await syncMutation.mutateAsync({ body });

                // cleanup cart
                setLocalCart([]);
                localStorage.removeItem(CART_STORAGE_KEY);

                await queryClient.invalidateQueries({queryKey: getShoppingCartQueryKey()});
            } catch (error) {
                console.error("Failed to sync cart: ", error);
            }
        })();
    }, [currentUser, setLocalCart]);

    // ACTIONS
    const updateCartItem = useCallback(async (product: ProductDto, quantity: number, add: boolean = true) => {
        if (currentUser) {
            await updateMutation.mutateAsync({
                path: { productId: product.id },
                query: { quantity, add }
            });
            await queryClient.invalidateQueries({queryKey: getShoppingCartQueryKey()});
        } else {
            setLocalCart(prev => {
                const existing = prev.find(item => item.product.id === product.id);
                if (existing) {
                    return prev.map(item =>
                        item.product.id === product.id
                            ? { ...item, quantity: item.quantity + quantity }
                            : item
                    );
                }
                return [...prev, { product, quantity, user: {} as any }];
            });
        }
    }, [currentUser, updateMutation, queryClient, setLocalCart]);

    const removeFromCart = useCallback(async (productId: number) => {
        if (currentUser) {
            await removeMutation.mutateAsync({ path: { productId } });
            await queryClient.invalidateQueries({queryKey: getShoppingCartQueryKey()});
        } else {
            setLocalCart(prev => prev.filter(item => item.product.id !== productId));
        }
    }, [currentUser, updateMutation, queryClient, setLocalCart]);

    const cartItems = currentUser ? (remoteCart ?? []) : localCart;
    const isLoading = isFetchingRemote;

    const cartValues = useMemo(() => ({
        cartItems,
        updateCartItem,
        removeFromCart,
        isLoading
    }), [cartItems, updateCartItem, removeFromCart, isLoading]);

    return (
        <CartContext.Provider value={cartValues}>
            {children}
        </CartContext.Provider>
    );
};

export const useCart = () => {
    const context = useContext(CartContext);
    if (!context) throw new Error("useCart must be used within CartProvider");
    return context;
};