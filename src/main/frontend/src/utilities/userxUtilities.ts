/**
 * This code is part of the skeleton project provided for students of the course "Software
 * Architecture" offered by Innsbruck University.
 */
import {UserxDTO, UserxRole, UserxUpdateDTO} from "../DTO/api-generated.types";

export type UserxValidationResult = {
    valid: boolean;
    message?: string;
    fieldErrors?: Partial<Record<keyof UserxUpdateDTO, string>>
};

/**
 * Create a UserxRole array from a string array of roles
 * @param roles
 *
 * @returns UserxRole[]
 * @throws Error if an invalid role is provided
 */
export const createUserxRoleArrayFromStrings = (roles: string[]): UserxRole[] => {
    return roles.map(role => {
        switch (role) {
            case UserxRole.ADMIN.valueOf():
                return UserxRole.ADMIN;
            case UserxRole.MANAGER.valueOf():
                return UserxRole.MANAGER;
            case UserxRole.CUSTOMER.valueOf():
                return UserxRole.CUSTOMER;
            default:
                throw new Error(`Invalid role: ${role}`);
        }
    });
}

/**
 * Create a UserxDTO from raw json,TODO: proper checking if json is correct
 * @param json raw json including UserxDTO
 *
 * @returns UserxDTO on success
 * @throws Error if json could not be parsed to valid json
 */
export const userxDTOfromJson = (json: any): UserxDTO =>  {
    if (!json || typeof json !== 'object') {
        throw new Error('Invalid JSON for User');
    }
    return json;
}

/**
 * Create UserxUpdateDTO from UserxDTO
 * @param user UserxDTO to map from
 *
 * @returns UserxUpdateDTO
 */
export const fromUserxDTOtoUserxUpdateDTO = (user: UserxDTO): UserxUpdateDTO => {
    return {
        id: user.id,
        username: user.username,
        password: "",
        firstName: user.firstName,
        lastName: user.lastName,
        email: user.email,
        phone: user.phone,
        enabled: user.enabled,
        roles: user.roles,
    };
}

/**
 * Create empty UserxUpdateDTO for new User Dialog
 *
 * @returns UserxUpdateDTO
 */
export const emptyUserxUpdateDTO = (): UserxUpdateDTO => {
    return {
        id: undefined,
        username: "",
        password: "",
        firstName: "",
        lastName: "",
        email: "",
        phone: "",
        enabled: true,
        roles: [],
    };
}
