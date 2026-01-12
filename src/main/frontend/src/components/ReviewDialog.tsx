import {ProductDto, ReviewDto} from "../api";
import {Button} from "primereact/button";
import {Dialog} from "primereact/dialog";
import {useUser} from "../Contexts/authenticatedUserContext.tsx";
import React from "react";
import {Message} from "primereact/message";
import ReviewForm from "./ReviewForm.tsx";
import {InputMaskChangeEvent} from "primereact/inputmask";
import {ReviewValidationResult} from "../utilities/reviewUtilities.ts";

interface ReviewDialogProps {
    product: ProductDto,
    visible: boolean;
    review: ReviewDto | null;
    validation: ReviewValidationResult;
    onHide: () => void;
    onSubmit: () => void;
    onInputChange: (e: React.ChangeEvent<HTMLInputElement> | React.ChangeEvent<HTMLTextAreaElement> | InputMaskChangeEvent) => void;
}

const ReviewDialog: React.FC<ReviewDialogProps> = ({
    product,
    visible,
    review,
    onSubmit,
    onHide,
    onInputChange,
    validation
}) => {
    const {currentUser} = useUser();

    const renderFooter = () => (
        <div>
            <Button label="Create" icon="pi pi-check" onClick={onSubmit} autoFocus />
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

export default ReviewDialog;
