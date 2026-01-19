/**
 * This code is part of the skeleton project provided for students of the course "Software
 * Architecture" offered by Innsbruck University.
 */
import {RoleEnum, UserxDto, UserxUpdateDto} from "../api";

export type UserxValidationResult = {
    valid: boolean;
    message?: string;
    fieldErrors?: Partial<Record<keyof UserxUpdateDto, string>>
};

/**
 * Create a UserxRole array from a string array of role
 * @param role
 *
 * @returns UserxRole[]
 * @throws Error if an invalid role is provided
 */
export const createUserxRoleArrayFromStrings = (role: string): RoleEnum => {
    switch (role) {
        case RoleEnum.ADMIN.valueOf():
            return RoleEnum.ADMIN;
        case RoleEnum.MANAGER.valueOf():
            return RoleEnum.MANAGER;
        case RoleEnum.CUSTOMER.valueOf():
            return RoleEnum.CUSTOMER;
        default:
            throw new Error(`Invalid role: ${role}`);
    }
}

/**
 * Create UserxUpdateDto from UserxDto
 * @param user UserxDto to map from
 *
 * @returns UserxUpdateDto
 */
export const fromUserxDtoToUserxUpdateDto = (user: UserxDto): UserxUpdateDto => {
    return {
        id: user.id,
        username: user.username,
        password: "",
        firstName: user.firstName,
        lastName: user.lastName,
        email: user.email,
        phone: user.phone,
        enabled: user.enabled,
        role: user.role,
    };
}

/**
 * Create empty UserxUpdateDto for new User Dialog
 *
 * @returns UserxUpdateDto
 */
export const emptyUserxUpdateDto = (): UserxUpdateDto => {
    return {
        id: undefined,
        username: "",
        password: "",
        firstName: "",
        lastName: "",
        email: "",
        phone: "",
        enabled: true,
        role: RoleEnum.CUSTOMER,
    };
}
