import {Button} from "primereact/button";
import {Dialog} from "primereact/dialog";
import React from "react";
import {Message} from "primereact/message";
import {InputMaskChangeEvent} from "primereact/inputmask";
import {useUser} from "../../contexts/authenticated-user.tsx";
import {ProductDto, ReviewDto} from "../../api";
import {ReviewForm} from "./review-dialog/review-form.tsx";
import {ValidationResult} from "../../utilities/form-data-validator.ts";

interface ReviewDialogProps {
    product: ProductDto,
    visible: boolean;
    review: ReviewDto | null;
    validation: ValidationResult<ReviewDto>;
    onHide: () => void;
    onSubmit: () => void;
    onInputChange: (e: React.ChangeEvent<HTMLInputElement> | React.ChangeEvent<HTMLTextAreaElement> | InputMaskChangeEvent) => void;
    submitting: boolean;
}

export const ReviewDialog: React.FC<ReviewDialogProps> = ({
    product,
    visible,
    review,
    onSubmit,
    onHide,
    onInputChange,
    validation,
    submitting,
}) => {
    const {currentUser} = useUser();

    const renderFooter = () => (
        <div>
            <Button label="Create" icon="pi pi-check" onClick={onSubmit} autoFocus loading={submitting} disabled={submitting} />
        </div>
    );

    if (!currentUser) return null;

    return (
        <Dialog
            header={`Create Review for ${product.name}`}
            visible={visible}
            style={{width: '50vw'}}
            breakpoints={{ '960px': '75vw', '641px': '90vw' }}
            onHide={onHide}
            footer={renderFooter()}
        >
            {validation.message && (<Message severity="error" text={validation.message} className="mb-3"/>)}
            {review && <ReviewForm
                review={review}
                fieldErrors={validation.fieldErrors}
                onInputChange={onInputChange}
            />}
        </Dialog>
    );
};
