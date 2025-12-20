/* tslint:disable */
/* eslint-disable */
// Generated using typescript-generator version 3.2.1263 on 2025-12-14 14:18:53.

export interface AddressDTO {
    street: string;
    number: number;
    postalCode: string;
    city: string;
    country: string;
}

export interface LoginRequestDTO {
    username: string;
    password: string;
}

export interface LoginResponseDTO {
    bearerToken: string;
}

export interface OrderDTO {
    id?: number;
    userId?: number;
    status?: OrderStatus;
    shippingAddress?: AddressDTO;
    paymentAddress?: AddressDTO;
    sum?: number;
    products?: { [index: string]: number };
    createdDate?: Date;
}

export interface OrderResponseDTO {
    failed: boolean;
    orderId?: number;
    order?: OrderDTO;
    productsInStock?: { [index: string]: number };
}

export interface PageableListDTO<T> {
    pageSize?: number;
    pageIdAfter?: number;
    pageCount?: number;
    totalCount: number;
    items?: T[];
}

export interface ProductDTO {
    id: number;
    name: string;
    price: number;
    stock: number;
    discount: number;
    shortDescription?: string;
    description?: string;
    rating?: number;
    imageUrl?: string;
    createdDate?: Date;
    updatedDate?: Date;
}

export interface ProductFilterDTO {
    name?: string;
    minRating?: number;
    minPrice?: number;
    maxPrice?: number;
    minStock?: number;
}

export interface ReviewDTO {
    id?: number;
    product?: ProductDTO;
    author?: UserxDTO;
    rating?: number;
    title: string;
    comment: string;
    createdAt?: Date;
}

export interface ShoppingCartItemDTO {
    productId: number;
    quantity: number;
}

export interface UserxDTO {
    id?: number;
    createdBy?: number;
    createDate?: Date;
    updatedBy?: number;
    updateDate?: Date;
    username: string;
    firstName: string;
    lastName: string;
    email?: string;
    phone?: string;
    enabled: boolean;
    roles: UserxRole[];
}

export interface UserxUpdateDTO {
    id?: number;
    username: string;
    password?: string;
    firstName?: string;
    lastName?: string;
    email?: string;
    phone?: string;
    enabled?: boolean;
    roles: UserxRole[];
}

export enum OrderStatus {
    PENDING = "PENDING",
    PENDING_PAYMENT = "PENDING_PAYMENT",
    PROCESSING = "PROCESSING",
    SHIPPED = "SHIPPED",
    DELIVERED = "DELIVERED",
    CANCELLED = "CANCELLED",
}

export enum UserxRole {
    ADMIN = "ADMIN",
    MANAGER = "MANAGER",
    CUSTOMER = "CUSTOMER",
}
