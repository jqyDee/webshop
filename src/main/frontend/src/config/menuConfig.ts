import {UserxRole} from "../DTO/userx.types";
import {ROUTES} from "../utilities/routes.paths";

export type MenuItemConfig = {
    label: string;
    icon?: string;
    url?: string;
    roles?: UserxRole[];
    items?: MenuItemConfig[];
};

export const menuConfig: MenuItemConfig[] = [
    {
        label: 'Home', icon: 'pi pi-home', url: ROUTES.HOME
    }, {
        label: 'Admin Submenu', icon: 'pi pi-star',
        roles: [UserxRole.ADMIN],
        items: [{
            label: 'Manage Users', icon: 'pi pi-star', url: ROUTES.MANAGE_USERS, roles: [UserxRole.ADMIN]
        }]
    }, {
        label: "Logout", icon: "pi pi-sign-out", url: ROUTES.LOGOUT
    }
];