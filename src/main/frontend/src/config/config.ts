/**
 * This code is part of the skeleton project provided for students of the course "Software
 * Architecture" offered by Innsbruck University.
 */
import {client} from "../api/client.gen.ts";

client.interceptors.request.use((request) => {
    const accessToken = sessionStorage.getItem(BEARER_TOKEN_LOCAL_STORAGE_KEY);
    if (accessToken) {
        request.headers.set('Authorization', `Bearer ${accessToken}`);
    }
    return request;
})

export const BEARER_TOKEN_LOCAL_STORAGE_KEY: string = 'bearerToken';
