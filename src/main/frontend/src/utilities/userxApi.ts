/**
 * This code is part of the skeleton project provided for students of the course "Software
 * Architecture" offered by Innsbruck University.
 */
import globalAxios from "axios";
import {UserxDTO, UserxUpdateDTO} from "../DTO/api-generated.types";
import {userxDTOfromJson} from "./userxUtilities";

/**
 * This file provides utility functions for CRUD operations on users.
 */

/**
 * Fetch all users from the backend
 * @returns Promise<UserxDTO[]> a promise that resolves with an array of UserxDTO objects
 * @throws Error if the request fails
 */
const fetchAllUsers = async (): Promise<UserxDTO[]> => {
    try {
        const response = await globalAxios.get("/api/admin/users");
        return response.data;
    } catch (err: any) {
        throw new Error(`Error fetching users: ${err?.message ?? String(err)}`);
    }
}

/**
 * Create a new user
 * @param selectedUser the user to create
 * @returns Promise<UserxDTO> a promise that resolves with the created user
 * @throws Error if the request fails
 */
const createUser = async (selectedUser: UserxUpdateDTO): Promise<UserxDTO> => {
    try {
        const response = await globalAxios.post("/api/admin/createUser", selectedUser);
        return userxDTOfromJson(response.data);
    } catch (err: any) {
        throw new Error(`Error saving user: ${err?.message ?? String(err)}`);
    }
}

/**
 * Update an existing user
 * @param selectedUser the user to update
 * @returns Promise<UserxDTO> a promise that resolves with the updated user
 * @throws Error if the request fails
 */
const updateUser = async (selectedUser: UserxUpdateDTO): Promise<UserxDTO> => {
    try {
        const response = await globalAxios.patch(`/api/admin/users/${selectedUser.id}`, selectedUser);
        return userxDTOfromJson(response.data);
    } catch (err: any) {
        throw new Error(`Error updating user: ${err?.message ?? String(err)}`);
    }
}

/**
 * Delete an existing user
 * @param selectedUser the user to delete
 * @returns Promise<any> a promise that resolves with the response data
 * @throws Error if the request fails
 */
const deleteUser = async (selectedUser: UserxUpdateDTO) => {
    try {
        return await globalAxios.delete(`/api/admin/users/${selectedUser.id}`);
    } catch (err: any) {
        throw new Error(`Error deleting user: ${err?.message ?? String(err)}`);
    }
}

/**
 * Return currently logged-in user or throw Error
 * @returns Promise<UserxDTO> a promise that resolves with the response data
 * @throws Error if the request fails (e.g. no user currently logged in)
 */
const getCurrentUser = async (): Promise<UserxDTO> => {
    try {
        const response = await globalAxios.get<string>("/api/users/me");
        return userxDTOfromJson(response.data);
    } catch (err: any) {
        throw new Error(`Error determining current user: ${err?.message ?? String(err)}`);
    }
}

/**
 * Return true if user is authenticated
 * @throws otherwise
 */
const isAuthenticated = async (): Promise<boolean> => {
    try {
        const res = await globalAxios.get("/api/users/authenticated");
        return res.status >= 200 && res.status < 300; // make sure you stay in this range for user is authenticated or modify accordingly
    } catch (err: any) {
        // axios throws for 4xx/5xx; treat all as not authenticated
        console.log("Catching error on trying isAuthenticated: ", err);
        return false;
    }
};

export const UserxApi = {
    createUser,
    updateUser,
    deleteUser,
    fetchAllUsers,
    getCurrentUser,
    isAuthenticated,
}
