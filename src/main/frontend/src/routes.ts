/**
 * This code is part of the skeleton project provided for students of the course "Software
 * Architecture" offered by Innsbruck University.
 */

import HomePage from "./views/HomePage";
import ManageUsers from "./views/ManageUsers";
import Login from "./views/Login";
import Logout from "./views/Logout";
import {ROUTES} from "./utilities/routes.paths";
import Products from "./views/Products.tsx";
import Orders from "./views/Orders.tsx";

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

export const ProductsRoute = {
    url: ROUTES.PRODUCTS,
    component: Products
}

export const OrdersRoute = {
    url: ROUTES.ORDERS,
    component: Orders
}

