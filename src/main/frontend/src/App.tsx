/**
 * This code is part of the skeleton project provided for students of the course "Software
 * Architecture" offered by Innsbruck University.
 */
import './styles/App.css';
import "primereact/resources/themes/lara-light-cyan/theme.css";
import "primeflex/primeflex.css"
import React, {Suspense} from "react";
import {BrowserRouter, Route, Routes} from "react-router-dom";
import {HomePageRoute, LoginsRoute, LogoutsRoute, ManageUsersRoute} from "./routes";
import PrivateRoute from './components/PrivateRoute';
import {UserProvider} from "./Contexts/authenticatedUserContext";
import {QueryClient, QueryClientProvider} from "@tanstack/react-query";
import MainLayout from "./components/MainLayout.tsx";

const client = new QueryClient();

const App: React.FC = () => {
    return (
        // Wrap the application in the UserProvider, which allows to access the authenticated user
        <UserProvider>
            <QueryClientProvider client={client}>
                <Suspense fallback={<div>Loading...</div>}>
                    <BrowserRouter>
                        <Routes>
                            <Route element={<MainLayout/>}>
                                <Route path={LoginsRoute.url} Component={LoginsRoute.component}/>
                                <Route path={HomePageRoute.url} Component={HomePageRoute.component}/>
                                {/* Protected Routes (authentication required) */}
                                <Route element={<PrivateRoute/>}>
                                    <Route path={ManageUsersRoute.url}
                                           Component={ManageUsersRoute.component}/>
                                    <Route path={LogoutsRoute.url} Component={LogoutsRoute.component}/>
                                </Route>
                                {/* end of protected routes */}
                            </Route>
                        </Routes>
                    </BrowserRouter>
                </Suspense>
            </QueryClientProvider>
        </UserProvider>
    );
}

export default App;
