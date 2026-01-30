/**
 * This code is part of the skeleton project provided for students of the course "Software
 * Architecture" offered by Innsbruck University.
 */
import './styles/app.css';
import "primereact/resources/themes/lara-light-cyan/theme.css";
import "primeflex/primeflex.css"
import React, {Suspense} from "react";
import {BrowserRouter, Route, Routes} from "react-router-dom";
import {
    HomePageRoute,
    LoginsRoute,
    LogoutsRoute,
    ManageUsersRoute,
    ProductRoute,
    OrdersRoute,
    ProductsRoute,
    ShoppingCartRoute,
    OrderDetailRoute,
    OrderCreationRoute,
} from "./routes";
import {UserProvider} from "./contexts/authenticated-user.tsx";
import {QueryClient, QueryClientProvider} from "@tanstack/react-query";
import {ReactQueryDevtools} from "@tanstack/react-query-devtools";
import {CartContextProvider} from "./contexts/cart.tsx";
import {ToastProvider} from "./contexts/toast.tsx";
import {MainLayout} from "./components/main-layout.tsx";
import {PrivateRoute} from "./components/private-route.tsx";

const client = new QueryClient({
    defaultOptions: {
        queries: {
            staleTime: 1000 * 60 * 5,    // Data is "fresh" for 5 minutes
        },
    },
});

const App: React.FC = () => {
    const dev = import.meta.env.MODE === "development";
    return (
        <QueryClientProvider client={client}>
            { /* Wrap the application in the UserProvider, which allows to access the authenticated user */ }
            <UserProvider>
                <ToastProvider>
                    {dev ? <ReactQueryDevtools initialIsOpen={false}></ReactQueryDevtools>: null}
                    <CartContextProvider>
                        <Suspense fallback={<div>Loading...</div>}>
                            <BrowserRouter>
                                <Routes>
                                    <Route element={<MainLayout/>}>
                                        <Route path={LoginsRoute.url} Component={LoginsRoute.component}/>
                                        <Route path={HomePageRoute.url} Component={HomePageRoute.component}/>
                                        <Route path={ProductRoute.url} Component={ProductRoute.component}/>
                                        <Route path={ProductsRoute.url} Component={ProductsRoute.component}/>
                                        <Route path={ShoppingCartRoute.url} Component={ShoppingCartRoute.component}/>
                                        {/* Protected Routes (authentication required) */}
                                        <Route element={<PrivateRoute/>}>
                                            <Route path={OrdersRoute.url} Component={OrdersRoute.component}/>
                                            <Route path={OrderDetailRoute.url} Component={OrderDetailRoute.component}/>
                                            <Route path={OrderCreationRoute.url} Component={OrderCreationRoute.component}/>
                                            <Route path={ManageUsersRoute.url}
                                                   Component={ManageUsersRoute.component}/>
                                            <Route path={LogoutsRoute.url} Component={LogoutsRoute.component}/>
                                        </Route>
                                        {/* end of protected routes */}
                                    </Route>
                                </Routes>
                            </BrowserRouter>
                        </Suspense>
                    </CartContextProvider>
                </ToastProvider>
            </UserProvider>
        </QueryClientProvider>
    );
}

export default App;
