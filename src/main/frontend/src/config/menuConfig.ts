import {ROUTES} from "../utilities/routes.paths";
import {RoleEnum} from "../api";

export type MenuItemConfig = {
    label: string;
    icon?: string;
    route?: string;
    roles?: RoleEnum[];
    items?: MenuItemConfig[];
};

export const menuConfig: MenuItemConfig[] = [
    {
        label: 'Home', icon: 'pi pi-home', route: ROUTES.HOME
    },
    {
        label: 'Products', icon: 'pi pi-list', route: ROUTES.PRODUCTS
    }
];

export const userMenuConfig: MenuItemConfig[] = [
    {
        label: 'Admin Panel',
        icon: 'pi pi-lock',
        route: ROUTES.MANAGE_USERS,
        roles: [RoleEnum.ADMIN]
    }, {
        label: "Orders",
        icon: 'pi pi-star',
        route: ROUTES.ORDERS,
        roles: [RoleEnum.CUSTOMER, RoleEnum.ADMIN],
    }, {
        label: "Logout",
        icon: "pi pi-sign-out",
        route: ROUTES.LOGOUT,
        roles: [RoleEnum.ADMIN, RoleEnum.MANAGER, RoleEnum.CUSTOMER]
    }
];