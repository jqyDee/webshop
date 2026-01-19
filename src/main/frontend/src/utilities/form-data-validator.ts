export type Validator<D extends object, K extends keyof D> = (key: K) => undefined | string;
export interface ValidationResult<D extends object> {
    valid: boolean;
    message?: string;
    fieldErrors?: Partial<Record<keyof D, string>>;
}

export function validateFormData<D extends object>(
    formData: D,
    validators: Validator<D, keyof D>[],
): ValidationResult<D> {
    let result = {valid: true} as ValidationResult<D>;
    for (const key of Object.keys(formData) as unknown as ReadonlyArray<keyof D>) {
        const results = validators.map((validator) => validator(key));
        const message = results.reduce((acc, next) => next === undefined ? acc : acc + next, "");
        if (message !== undefined && message.length > 0) {
            result = {...result, valid: false, fieldErrors: {...result.fieldErrors, [key]: message}};
        }
    }
    if (!result.valid) {
        result = {...result, message: "Please fill in all required fields."}
    }
    return result;
}