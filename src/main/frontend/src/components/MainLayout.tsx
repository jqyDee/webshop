import React from 'react';
import { Outlet } from 'react-router-dom';
import NavbarComponent from './NavbarComponent';

const MainLayout: React.FC = () => {
    return (
        <div className="main-layout">
            <NavbarComponent />
            <main>
                <Outlet />
            </main>
        </div>
    );
};

export default MainLayout;