/**
 * This code is part of the skeleton project provided for students of the course "Software
 * Architecture" offered by Innsbruck University.
 */
import React, { useRef } from 'react';
import {Menubar} from "primereact/menubar";
import {useUser} from "../contexts/authenticated-user.tsx";
import {menu, MenuItemConfig, userMenuConfig} from "../config/menu.ts";
import {MenuItem, MenuItemOptions} from "primereact/menuitem";
import {Link} from "react-router-dom";
import {RoleEnum} from "../api";
import { TieredMenu } from "primereact/tieredmenu"; // Use TieredMenu for the dropdown
import { Avatar } from "primereact/avatar";
import { ROUTES } from "../utilities/routes.paths.ts";
import { Button } from "primereact/button";
import {useCart} from "../contexts/cart.tsx";
import {Badge} from "primereact/badge";

/**
 * Navbar component.
 */
export const Navbar: React.FC = () => {
    const {currentUser: user, isCustomer} = useUser();
    const {cartItems} = useCart();
    const userMenuRef = useRef<TieredMenu>(null);

    const totalCartItems = React.useMemo(() => {
        return cartItems.reduce((acc, item) => acc + (item.quantity || 0), 0);
    }, [cartItems]);

    const filterMenu = React.useCallback((items: MenuItemConfig[]): MenuItemConfig[] => {
        const hasRole = (required?: RoleEnum[]) => {
            if (!required || required.length === 0) return true;
            if (!user) return false;
            return required.some(r => user.role === r);
        }

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
    const leftModel = React.useMemo(() => buildMenuBarModel(filterMenu(menu)), [filterMenu]);
    const rightModel = React.useMemo(() => buildMenuBarModel(filterMenu(userMenuConfig)), [filterMenu]);
    const canPutIntoCart = !user || (user && isCustomer);

    const endContent = () => (
        <div className="flex align-items-center gap-3 pr-3">
                    {canPutIntoCart &&
                        <Link to="/shopping-cart" className="no-underline text-color">
                            <Avatar
                                icon="pi pi-shopping-cart"
                                className="p-overlay-badge"
                            >
                                {totalCartItems > 0 && (
                                    <Badge value={totalCartItems} severity="danger" className="text-xs" style={{
                                        transform: 'scale(0.75)',
                                        transformOrigin: 'top right',
                                        top: '-8px',
                                        right: '-8px',
                                    }}/>
                                )}
                            </Avatar>
                        </Link>
                    }

            {user ? (
                <div className="flex align-items-center gap-2 cursor-pointer" onClick={(e) => userMenuRef.current?.toggle(e)}>
                    <div className="flex flex-column align-items-end">
                        <span className="font-bold text-sm text-900">{user.firstName} {user.lastName}</span>
                        <small className="text-500">{user.role}</small>
                    </div>
                    <Avatar icon="pi pi-user" shape="circle"/>
                    <TieredMenu model={rightModel} popup ref={userMenuRef} />
                </div>
            ) : (
                <Link to={ROUTES.LOGIN} style={{ textDecoration: 'none' }}>
                    <Button label="Login" icon="pi pi-sign-in" className="p-button-text p-button-sm" />
                </Link>
            )}
        </div>
    );

    return (
        <div className="card">
            <Menubar model={leftModel} end={endContent} />
        </div>
    );
}

function buildMenuBarModel(items: MenuItemConfig[]): MenuItem[] {
    return items.map(configItem => {
        const children = configItem.items ? buildMenuBarModel(configItem.items) : undefined;
        return {
            label: configItem.label,
            icon: configItem.icon,
            items: children,
            template: (item, options) => <MenuItemTemplate item={item} options={options} config={configItem}/>,
        };
    });
}

interface MenuItemTemplateProps {
    readonly item: MenuItem;
    readonly options: MenuItemOptions;
    readonly config: MenuItemConfig;
}

const MenuItemTemplate: React.FC<MenuItemTemplateProps> = ({item, options, config}) => {
    const handleClick = (e: React.MouseEvent<HTMLElement>) => {
        options.onClick?.(e);
    };

    return (
        <Link
            to={config.route ?? "#"}
            className={`${options.className ?? ""} p-menuitem-link`}
            onClick={handleClick}
        >
            {item.icon && <span className={options.iconClassName}/>}
            <span className={options.labelClassName}>{item.label}</span>
        </Link>
    );
};
