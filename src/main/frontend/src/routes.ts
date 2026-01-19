/**
 * This code is part of the skeleton project provided for students of the course "Software
 * Architecture" offered by Innsbruck University.
 */

import HomePage from "./views/home-page.tsx";
import ManageUsers from "./views/manage-users.tsx";
import Login from "./views/login.tsx";
import Logout from "./views/logout.tsx";
import {ROUTES} from "./utilities/routes.paths";
import ProductDetails from "./views/product-details.tsx";
import Products from "./views/products.tsx";
import Orders from "./views/orders.tsx";
import OrderDetails from "./views/order-details.tsx";
import ShoppingCart from "./views/shopping-cart.tsx";
import OrderCreation from "./views/order-creation.tsx";

/**
 * Define the routes of the application.
 */

export const HomePageRoute = {
    url: ROUTES.HOME,
    component: HomePage
}

export const ManageUsersRoute = {
    url: ROUTES.MANAGE_USERS,
    component: ManageUsers
}
export const LoginsRoute = {
    url: ROUTES.LOGIN,
    component: Login
}
export const LogoutsRoute = {
    url: ROUTES.LOGOUT,
    component: Logout
}
export const ProductRoute = {
    url: ROUTES.PRODUCT,
    component: ProductDetails
}

export const ProductsRoute = {
    url: ROUTES.PRODUCTS,
    component: Products
}

export const OrdersRoute = {
    url: ROUTES.ORDERS,
    component: Orders
}

export const ShoppingCartRoute = {
    url: ROUTES.SHOPPING_CART,
    component: ShoppingCart
}

export const OrderDetailRoute = {
    url: ROUTES.ORDER,
    component: OrderDetails
}
export const OrderCreationRoute = {
    url: ROUTES.ORDER_CREATION,
    component: OrderCreation
}

