/**
 * This code is part of the skeleton project provided for students of the course "Software
 * Architecture" offered by Innsbruck University.
 */
import React, { useRef } from 'react';
import {Menubar} from "primereact/menubar";
import {useUser} from "../Contexts/authenticatedUserContext";
import {menuConfig, MenuItemConfig, userMenuConfig} from "../config/menuConfig";
import {MenuItem} from "primereact/menuitem";
import {Link, useNavigate} from "react-router-dom";
import {RoleEnum} from "../api";
import { TieredMenu } from "primereact/tieredmenu"; // Use TieredMenu for the dropdown
import { Avatar } from "primereact/avatar";
import { ROUTES } from "../utilities/routes.paths.ts";
import { Button } from "primereact/button";
import {useCart} from "../Contexts/cartContext.tsx";
import {Badge} from "primereact/badge";

/**
 * Navbar component.
 */
const NavbarComponent: React.FC = () => {
    const {currentUser: user, isCustomer} = useUser();
    const {cartItems} = useCart();
    const userMenuRef = useRef<TieredMenu>(null);

    const navigate = useNavigate();

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

    // we want to use navigate (react router) to ensure pure client-side navigation on menu item click
    // incidentally, we also want to fix primereact component-related aria warnings
    const buildMenubarModel = React.useCallback((items: MenuItemConfig[]): MenuItem[] => {
        return items.map(configItem => {
            const children = configItem.items ? buildMenubarModel(configItem.items) : undefined;

            const menuItem: MenuItem = {
                label: configItem.label,
                icon: configItem.icon,
                items: children,
            };

            menuItem.template = (menuItem, options) => {
                const handleClick = (e: React.MouseEvent<HTMLElement>) => {
                    options.onClick?.(e);
                };

                return (
                    <Link
                    to={configItem.route ?? "#"}
                    className={`${options.className ?? ""} p-menuitem-link`}
                    onClick={handleClick}
                >
                    {menuItem.icon && <span className={options.iconClassName} />}
                    <span className={options.labelClassName}>{menuItem.label}</span>
                </Link>
                );
            }
            return menuItem;
        });
    }, []);

    const leftModel = React.useMemo(() => buildMenubarModel(filterMenu(menuConfig)), [filterMenu, buildMenubarModel]);
    const rightModel = React.useMemo(() => buildMenubarModel(filterMenu(userMenuConfig)), [filterMenu, buildMenubarModel]);

    const canPutIntoCart = !user || (user && isCustomer);

    const endContent = () => (
        <div className="flex align-items-center gap-3 pr-3">
                <div className="flex align-items-center cursor-pointer p-menuitem-link border-round p-2 transition-colors transition-duration-150" onClick={() => {navigate("/shopping-cart")}}>
                    {canPutIntoCart &&
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
                    }
                </div>

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

export default NavbarComponent;
