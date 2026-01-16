import {CartItemDto, ProductDto} from "../api";
import React, {useCallback, useContext, useEffect, useMemo, useState} from "react";
import {useUser} from "./authenticatedUserContext.tsx";
import {useMutation, useQuery, useQueryClient} from "@tanstack/react-query";
import {
    addAllToShoppingCartMutation, clearShoppingCartMutation,
    deleteProductFromShoppingCartMutation,
    getShoppingCartOptions, getShoppingCartQueryKey, updateProductInShoppingCartMutation
} from "../api/@tanstack/react-query.gen.ts";

interface CartContextType {
    cartItems: CartItemDto[],
    updateCartItem: (product: ProductDto, quantity: number, add?: boolean) => Promise<void>,
    removeFromCart: (productId: number) => Promise<void>,
    removeAllFromCart: () => Promise<void>,
    isLoading: boolean,
}

const CartContext = React.createContext<CartContextType | null>(null);

const CART_STORAGE_KEY = "guest_cart";

export const CartContextProvider: React.FC<{ children: React.ReactNode }> = ({children}) => {
    const { currentUser, isCustomer } = useUser();
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
    const hasShoppingCart = currentUser && isCustomer;
    const { data: remoteCart, isLoading: isFetchingRemote } = useQuery({
        ...getShoppingCartOptions(),
        enabled: !!hasShoppingCart, // Only fetch if logged in and Customer
    });

    const updateMutation = useMutation(updateProductInShoppingCartMutation());
    const syncMutation = useMutation(addAllToShoppingCartMutation());
    const removeMutation = useMutation(deleteProductFromShoppingCartMutation());
    const clearMutation = useMutation(clearShoppingCartMutation());

    // SYNCING
    useEffect(() => {
        if (!currentUser || localCart.length === 0) return;

        (async () => {
            try {
                // backend expects map
                const body: Record<string, number> = {};
                localCart.forEach(item => {
                    if (item.product.id){
                        body[item.product.id.toString()] = item.quantity || 1;
                    }
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
        if (!product.id) return;

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
                            ? { ...item, quantity: item.quantity + quantity}
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
    }, [currentUser, queryClient, setLocalCart]);

    const removeAllFromCart = useCallback(async () => {
        if (currentUser) {
            await clearMutation.mutateAsync({})
            await queryClient.invalidateQueries({queryKey: getShoppingCartQueryKey()});

        } else {
            setLocalCart([]);
            localStorage.removeItem(CART_STORAGE_KEY);
        }
    },[currentUser, queryClient, setLocalCart]);

    const cartItems = currentUser ? (remoteCart ?? []) : localCart;
    const isLoading = isFetchingRemote;

    const cartValues = useMemo(() => ({
        cartItems,
        updateCartItem,
        removeFromCart,
        isLoading,
        removeAllFromCart,
    }), [cartItems, updateCartItem, removeFromCart, isLoading, removeAllFromCart]);

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