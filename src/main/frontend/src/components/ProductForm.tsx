/**
 * This code is part of the skeleton project provided for students of the course "Software
 * Architecture" offered by Innsbruck University.
 */
import React from "react";
import {InputMaskChangeEvent} from "primereact/inputmask";
import {InputText} from "primereact/inputtext";
import {ProductDto} from "../api";
import {InputNumber, InputNumberChangeEvent} from "primereact/inputnumber";
import {InputTextarea} from "primereact/inputtextarea";


interface ProductFormProps {
    product: ProductDto,
    fieldErrors?: Partial<Record<keyof ProductDto, string>>,
    onInputChange: (event: React.ChangeEvent<HTMLInputElement> | React.ChangeEvent<HTMLTextAreaElement> | InputNumberChangeEvent | InputMaskChangeEvent) => void,
}

/**
 * Form for creating or editing a product.
 * @param product the product to be edited
 * @param fieldErrors field validation
 * @param onInputChange callback when the input changes
 */
const ProductForm: React.FC<ProductFormProps> =
    ({
         product,
         fieldErrors,
         onInputChange,
     }) => {
        return (
            <div>
                <h1>{product.name}</h1>
                {/* create form */}
                <form>
                    <div className="card p-fluid flex flex-column gap-3">
                        <div className="flex-auto mb-3">
                            <label htmlFor="name" className="font-bold block">Name</label>
                            <InputText id="name" name="name" value={product.name}
                                       onChange={onInputChange}
                                       placeholder="Name"
                                       autoComplete="off"
                                       className={fieldErrors?.name ? 'p-invalid' : undefined}
                            />
                            {fieldErrors?.name && <small className="p-error">{fieldErrors.name}</small>}
                        </div>
                        <div className="flex-auto mb-3">
                            <label htmlFor="price" className="font-bold block">Price</label>
                            <InputNumber id="price" name="price" value={product.price}
                                         onChange={onInputChange}
                                         placeholder="999,99"
                                         minFractionDigits={2}
                                         maxFractionDigits={2}
                                         className={fieldErrors?.price ? 'p-invalid' : undefined}
                            />
                            {fieldErrors?.price && <small className="p-error">{fieldErrors.price}</small>}
                        </div>
                        <div className="flex-auto mb-3">
                            <label htmlFor="stock" className="font-bold block">Stock</label>
                            <InputNumber id="stock" name="stock" value={product.stock}
                                         onChange={onInputChange}
                                         placeholder="Stock"
                                         minFractionDigits={0}
                                         maxFractionDigits={0}
                                         className={fieldErrors?.stock ? 'p-invalid' : undefined}
                            />
                            {fieldErrors?.stock && <small className="p-error">{fieldErrors.stock}</small>}
                        </div>
                        <div className="flex-auto mb-3">
                            <label htmlFor="discount" className="font-bold block">Discount</label>
                            <InputNumber id="discount"
                                         name="discount"
                                         value={product.discount}
                                         onChange={onInputChange}
                                         min={0}
                                         maxFractionDigits={2}
                                         minFractionDigits={2}
                                         max={1}
                                         placeholder="0.3"
                                         className={fieldErrors?.discount ? 'p-invalid' : undefined}
                            />
                            {fieldErrors?.discount && <small className="p-error">{fieldErrors.discount}</small>}
                        </div>
                        <div className="flex-auto mb-3">
                            <label htmlFor="shortDescription" className="font-bold block">Short Description</label>
                            <InputTextarea id="shortDescription"
                                           name="shortDescription"
                                           value={product.shortDescription}
                                           onChange={onInputChange}
                                           placeholder="Short Description"
                                           style={{minHeight: '10vw', maxWidth: '100%', resize: 'vertical'}}
                            />
                        </div>
                        <div className="flex-auto mb-3">
                            <label htmlFor="description" className="font-bold block">Description</label>
                            <InputTextarea id="description"
                                           name="description"
                                           value={product.description}
                                           onChange={onInputChange}
                                           placeholder="Description"
                                           style={{minHeight: '20vw', maxWidth: '100%', resize: 'vertical'}}
                            />
                        </div>
                        <div className="flex-auto mb-3">
                            <label htmlFor="imageUrl" className="font-bold block">Image URL</label>
                            <InputText id="imageUrl" name="imageUrl" value={product.imageUrl}
                                       onChange={onInputChange}
                                       placeholder="Image URL"
                                       autoComplete="off"
                            />
                        </div>
                    </div>
                </form>
            </div>
        )

    }

export default ProductForm;
