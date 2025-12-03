/* tslint:disable */
/* eslint-disable */
// Generated using typescript-generator version 3.2.1263 on 2025-12-03 18:05:19.

export interface LoginRequestDTO {
    username: string;
    password: string;
}

export interface LoginResponseDTO {
    bearerToken: string;
}

export interface UserxDTO {
    id?: number;
    createdBy?: number;
    createDate?: Date;
    updatedBy?: number;
    updateDate?: Date;
    username: string;
    firstName: string;
    lastName: string;
    email?: string;
    phone?: string;
    enabled: boolean;
    roles: UserxRole[];
}

export interface UserxUpdateDTO {
    id?: number;
    username: string;
    password?: string;
    firstName?: string;
    lastName?: string;
    email?: string;
    phone?: string;
    enabled?: boolean;
    roles: UserxRole[];
}

export enum UserxRole {
    ADMIN = "ADMIN",
    MANAGER = "MANAGER",
    EMPLOYEE = "EMPLOYEE",
}
