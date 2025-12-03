/**
 * This code is part of the skeleton project provided for students of the course "Software
 * Architecture" offered by Innsbruck University.
 */

/**
 * User roles
 */
export enum UserxRole {
    ADMIN = 'ADMIN',
    MANAGER = 'MANAGER',
    EMPLOYEE = 'EMPLOYEE'
}

/**
 * User DTO
 */
export interface UserDTO {
    id?: number;
    username: string;
    createUserId?: number | null;
    createDate?: Date | null;
    updateUserId?: number | null;
    updateDate?: Date | null;
    password?: string;
    firstName: string;
    lastName: string;
    email: string;
    phone: string;
    enabled: boolean;
    roles: UserxRole[];
}

/**
 * User class with methods for serialization
 */
export class UserxTypes implements UserDTO {
    id?: number;
    username: string;
    createUserId?: number | null;
    createDate: Date | null;
    updateUserId?: number | null;
    updateDate: Date | null;
    password?: string;
    firstName: string;
    lastName: string;
    email: string;
    phone: string;
    enabled: boolean = true;
    roles: UserxRole[];

    /**
     * Constructor for the User class
     * @param data :UserDTO object
     */
    constructor(data: UserDTO) {
        this.id = data.id;
        this.username = data.username;
        this.createUserId = data.createUserId ?? null;
        this.createDate = data.createDate ? new Date(data.createDate) : null;
        this.updateUserId = data.updateUserId ?? null;
        this.updateDate = data.updateDate ? new Date(data.updateDate) : null;
        this.password = data.password;
        this.firstName = data.firstName;
        this.lastName = data.lastName;
        this.email = data.email;
        this.phone = data.phone;
        this.enabled = data.enabled
        this.roles = data.roles;
    }

    /**
     * Getter method for the full name of the user
     * Get the full name of the user
     * @returns Full name of the user
     */
    get fullName(): string {
        return `${this.firstName} ${this.lastName}`;
    }

    /**
     * Serialize the User instance to JSON
     * @returns JSON object with the password field omitted
     */
    toJSON(): Omit<UserDTO, 'password'> {
        return {
            id: this.id,
            username: this.username,
            createUserId: this.createUserId,
            createDate: this.createDate,
            updateUserId: this.updateUserId,
            updateDate: this.updateDate,
            firstName: this.firstName,
            lastName: this.lastName,
            email: this.email,
            phone: this.phone,
            enabled: this.enabled,
            roles: this.roles
        };
    }

    /**
     * Serialize the User instance to JSON for creating a new user
     * @returns JSON object with the fields required for creating a new user
     */
    toCreateJSON(): Pick<UserDTO, 'username' | 'password' | 'firstName' | 'lastName' | 'email' | 'phone' | 'roles' | 'enabled'> {
        return {
            username: this.username,
            password: this.password,
            firstName: this.firstName,
            lastName: this.lastName,
            email: this.email,
            phone: this.phone,
            enabled: this.enabled,
            roles: this.roles
        };
    }

    /**
     * Serialize the User instance to JSON for updating an existing user
     * @returns JSON object with the fields required for updating a user
     */
    toUpdateJSON(): Pick<UserDTO, 'id' | 'firstName' | 'lastName' | 'email' | 'phone' | 'roles' | 'enabled'> {
        return {
            id: this.id,
            firstName: this.firstName,
            lastName: this.lastName,
            email: this.email,
            phone: this.phone,
            enabled: this.enabled,
            roles: this.roles
        };
    }

    /**
     * Create an empty User instance
     * @returns User instance with empty fields
     */
    static empty(): UserxTypes {
        return new UserxTypes({
            id: undefined,
            username: '',
            createUserId: null,
            createDate: null,
            updateUserId: null,
            updateDate: null,
            password: '',
            firstName: '',
            lastName: '',
            email: '',
            phone: '',
            enabled: true,
            roles: []
        });
    }

    /**
     * Create a User instance from a JSON object
     * @param json
     * @returns User instance
     */
    static fromJSON(json: any): UserxTypes {
        if (!json || typeof json !== 'object') {
            throw new Error('Invalid JSON for User');
        }
        return new UserxTypes(json);
    }
}
