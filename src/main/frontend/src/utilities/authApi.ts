/**
 * This code is part of the skeleton project provided for students of the course "Software
 * Architecture" offered by Innsbruck University.
 */

import {LoginRequestDTO, LoginResponseDTO} from "../DTO/api-generated.types";

import globalAxios from "axios";


/**
 * Try to log in a user
 * @param login the login data of the user (username and password)
 *
 * @returns Promise with the status and data of the response
 * @throws Error if the request fails
 */
const login = async (login: LoginRequestDTO) : Promise<LoginResponseDTO> => {

    // Send the request, await the response
    const response = await globalAxios.post<LoginResponseDTO>(
        `/authentication/login`,
        login
    );

    // Return the response
    return response.data;
}

export const AuthApi = {
    login,
}
