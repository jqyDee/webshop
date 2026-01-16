import {ReviewDto} from "../api";

export type ReviewValidationResult = {
    valid: boolean;
    message?: string;
    fieldErrors?: Partial<Record<keyof ReviewDto, string>>;
}

export const emptyReviewDto = (): ReviewDto  => {
    return {
        id: undefined,
        rating: 0,
        title: '',
        comment: '',
        author: undefined,
        product: undefined,
        createdDate: undefined,
    }
}