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
import {client as apiClient} from "./api/client.gen.ts";

// 1. Configure the API client globally to handle Spring-style sorting and filtering
apiClient.setConfig({
    baseUrl: 'http://localhost:8080',
    querySerializer: (query) => {
        const params = new URLSearchParams();
        Object.entries(query).forEach(([key, value]) => {
            if (value !== undefined && value !== null) {
                if (Array.isArray(value)) {
                    // This handles sort=['name,asc', 'id,desc'] -> ?sort=name,asc&sort=id,desc
                    value.forEach((v) => params.append(key, v));
                } else {
                    params.append(key, value.toString());
                }
            }
        });
        return params.toString();
    },
});

const client = new QueryClient();

const App: React.FC = () => {
    return (
        // Wrap the application in the UserProvider, which allows to access the authenticated user
        <UserProvider>
            <QueryClientProvider client={client}>
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
            </QueryClientProvider>
        </UserProvider>
    );
}

export default App;
