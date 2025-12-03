/**
 * This code is part of the skeleton project provided for students of the course "Software
 * Architecture" offered by Innsbruck University.
 */

export type LoginDTO = { username: string; password: string; };

export type LoginResponse = { bearerToken: string; };