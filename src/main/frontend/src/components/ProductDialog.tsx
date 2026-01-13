import {ProductDto} from "../api";
import {InputMaskChangeEvent} from "primereact/inputmask";
import {Button} from "primereact/button";
import {Dialog} from "primereact/dialog";
import {ProductValidationResult} from "../utilities/productUtilities.ts";
import {Message} from "primereact/message";
import ProductForm from "./ProductForm.tsx";
import {InputNumberChangeEvent} from "primereact/inputnumber";
import React from "react";


interface ProductDialogProps {
    visible: boolean;
    product: ProductDto | null;
    isNewProduct: boolean;
    validation: ProductValidationResult;
    onHide: () => void;
    onSubmit: () => void;
    onDelete: () => void;
    onInputChange: (event: React.ChangeEvent<HTMLInputElement> | React.ChangeEvent<HTMLTextAreaElement> | InputNumberChangeEvent | InputMaskChangeEvent) => void,
}

const ProductDialog: React.FC<ProductDialogProps> = ({
    visible,
    product,
    isNewProduct,
    validation,
    onHide,
    onDelete,
    onSubmit,
    onInputChange,
}) => {
    const renderFooter = () => (
        <div className="flex justify-content-between">
            <Button label="Delete Product" icon="pi pi-times" onClick={onDelete} className="p-button-danger" />
            <Button label={isNewProduct ? "Create" : "Save"} icon="pi pi-check" onClick={onSubmit}
                    autoFocus />
        </div>
    );

    return (
        <Dialog
            header={isNewProduct ? "Create Product" : "Edit Product"}
            visible={visible}
            style={{ width: '50vw' }}
            breakpoints={{ '960px': '75vw', '641px': '90vw' }}
            onHide={onHide}
            footer={renderFooter}
        >
            {validation.message && (<Message severity="error" text={validation.message} className="mb-3"/>)}
            {product &&
                <ProductForm
                    product={product}
                    fieldErrors={validation.fieldErrors}
                    onInputChange={onInputChange}
                />
            }
        </Dialog>
    )
}

export default ProductDialog;