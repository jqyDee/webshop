import {ProductDto} from "../api";

export type ProductValidationResult = {
    valid: boolean;
    message?: string;
    fieldErrors?: Partial<Record<keyof ProductDto, string>>
};

export const emptyProductDto = (): ProductDto => {
    return {
        id: undefined,
        name: "",
        price: 0,
        stock: 0,
        discount: 0.0,
        discountedPrice: 0,
        shortDescription: undefined,
        description: undefined,
        rating: undefined,
        imageUrl: undefined,
        createdDate: undefined,
        updatedDate: undefined
    }
}
