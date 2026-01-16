/**
 * This code is part of the skeleton project provided for students of the course "Software
 * Architecture" offered by Innsbruck University.
 */
import './styles/App.css';
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
    OrderDetailRoute
} from "./routes";
import PrivateRoute from './components/PrivateRoute';
import {UserProvider} from "./Contexts/authenticatedUserContext";
import {QueryClient, QueryClientProvider} from "@tanstack/react-query";
import {ReactQueryDevtools} from "@tanstack/react-query-devtools";
import {CartContextProvider} from "./Contexts/cartContext.tsx";
import MainLayout from "./components/MainLayout.tsx";

const client = new QueryClient({
    defaultOptions: {
        queries: {
            staleTime: 1000 * 60 * 5,    // Data is "fresh" for 5 minutes
        },
    },
});

const App: React.FC = () => {
    return (
        <QueryClientProvider client={client}>
            {/* Wrap the application in the UserProvider, which allows to access the authenticated user*/}
            <UserProvider>
                <ReactQueryDevtools initialIsOpen={false}></ReactQueryDevtools>
                <CartContextProvider>
                    <Suspense fallback={<div>Loading...</div>}>
                        <BrowserRouter>
                            <Routes>
                                <Route element={<MainLayout/>}>
                                    <Route path={LoginsRoute.url} Component={LoginsRoute.component}/>
                                    <Route path={HomePageRoute.url} Component={HomePageRoute.component}/>
                                    <Route path={ProductRoute.url} Component={ProductRoute.component}/>
                                    <Route path={ProductsRoute.url} Component={ProductsRoute.component}/>
                                    {/* Protected Routes (authentication required) */}
                                    <Route element={<PrivateRoute/>}>
                                        <Route path={OrdersRoute.url} Component={OrdersRoute.component}/>
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
            </UserProvider>
        </QueryClientProvider>
    );
}

export default App;
