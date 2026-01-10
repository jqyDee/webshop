/**
 * This code is part of the skeleton project provided for students of the course "Software
 * Architecture" offered by Innsbruck University.
 */
import './styles/App.css';
import "primereact/resources/themes/lara-light-cyan/theme.css";
import React, {Suspense} from "react";
import {BrowserRouter, Route, Routes} from "react-router-dom";
import {HomePageRoute, LoginsRoute, LogoutsRoute, ManageUsersRoute} from "./routes";
import PrivateRoute from './components/PrivateRoute';
import {UserProvider} from "./Contexts/authenticatedUserContext";
import {QueryClient, QueryClientProvider} from "@tanstack/react-query";
import {ReactQueryDevtools} from "@tanstack/react-query-devtools";
import {CartContextProvider} from "./Contexts/cartContext.tsx";

const client = new QueryClient({
    defaultOptions: {
        queries: {
            staleTime: 1000 * 60 * 5,    // Data is "fresh" for 5 minutes
            refetchOnWindowFocus: false, // Stop refetching on window click
        },
    },
});

const App: React.FC = () => {
    return (
        // Wrap the application in the UserProvider, which allows to access the authenticated user
        <UserProvider>
            <QueryClientProvider client={client}>
                <ReactQueryDevtools initialIsOpen={false}></ReactQueryDevtools>
                <CartContextProvider>
                    <Suspense fallback={<div>Loading...</div>}>
                        <BrowserRouter>
                            <Routes>
                                <Route path={LoginsRoute.url} Component={LoginsRoute.component}/>
                                {/* Protected Routes (authentication required) */}
                                <Route element={<PrivateRoute/>}>
                                    <Route path={HomePageRoute.url} Component={HomePageRoute.component}/>
                                    <Route path={ManageUsersRoute.url}
                                           Component={ManageUsersRoute.component}/>
                                    <Route path={LogoutsRoute.url} Component={LogoutsRoute.component}/>
                                </Route>
                                {/* end of protected routes */}
                            </Routes>
                        </BrowserRouter>
                    </Suspense>
                </CartContextProvider>
            </QueryClientProvider>
        </UserProvider>
    );
}

export default App;
