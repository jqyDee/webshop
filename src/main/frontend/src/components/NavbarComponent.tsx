/**
 * This code is part of the skeleton project provided for students of the course "Software
 * Architecture" offered by Innsbruck University.
 */
import React from 'react';
import {Menubar} from "primereact/menubar";

import {useUser} from "../Contexts/authenticatedUserContext";
import {menuConfig, MenuItemConfig} from "../config/menuConfig";
import {UserxRole} from "../DTO/userx.types";

/**
 * Navbar component.
 */
const NavbarComponent: React.FC = () => {
    const {currentUser: user} = useUser();
    const filterMenu = React.useCallback((items: MenuItemConfig[]): MenuItemConfig[] => {
        if (!user) return [];

        const hasRole = (required?: UserxRole[]) =>
            !required || required.some(r => user.roles.includes(r));

        return items
            .map(item => {
                const visibleChildren = item.items ? filterMenu(item.items) : undefined;
                return { ...item, items: visibleChildren };
            })
            .filter(item => {
                const visible = hasRole(item.roles);
                const hasChildren = !!item.items?.length;
                // Keep if user can see it, or it has visible children
                return visible || hasChildren;
            });
    }, [user]);

    const filteredItems = React.useMemo(() => filterMenu(menuConfig), [filterMenu, menuConfig]);

    return (
        <div className="card">
            <Menubar model={filteredItems} />
        </div>
    );
}

export default NavbarComponent;
