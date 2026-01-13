import {ProductDto} from "../api";

export type ProductValidationResult = {
    valid: boolean;
    message?: string;
    fieldErrors?: Partial<Record<keyof ProductDto, string>>
};
