import {InputText} from "primereact/inputtext";
import React, {useEffect, useState} from "react";
import {Rating} from "primereact/rating";
import {InputTextarea} from "primereact/inputtextarea";
import {InputMaskChangeEvent} from "primereact/inputmask";
import {ReviewDto} from "../../../api";
import {ValidationResult} from "../../../utilities/form-data-validator.ts";


interface UserFormProps {
    review: ReviewDto,
    fieldErrors: ValidationResult<ReviewDto>["fieldErrors"],
    onInputChange: (event: React.ChangeEvent<HTMLInputElement> | React.ChangeEvent<HTMLTextAreaElement> | InputMaskChangeEvent) => void,
}

export const ReviewForm: React.FC<UserFormProps> = ({review, fieldErrors, onInputChange}) => {
    const [rating, setRating] = useState<number>(0);

    useEffect(() =>{
        review.rating = rating;
    }, [rating, setRating]);

    return (
        <div>
            <form>
                <div className="card p-fluid flex flex-column gap-3">
                    <div className="flex-auto mb-3">
                        <label htmlFor="rating" className="font-bold block">Rating</label>
                        <Rating id="rating" value={rating} cancel={false} onChange={(e) => setRating(e.value ?? 0)} />
                        {fieldErrors?.rating && <small className="p-error">{fieldErrors.rating}</small>}
                    </div>
                    <div className="flex-auto mb-3">
                        <label htmlFor="title" className="font-bold block">Title</label>
                        <InputText id="title" name="title" value={review.title}
                                   onChange={onInputChange}
                                   placeholder="Title"
                                   autoComplete="off"
                                   className={fieldErrors?.title ? 'p-invalid' : undefined}
                        />
                        {fieldErrors?.title && <small className="p-error">{fieldErrors.title}</small>}
                    </div>
                    <div className="flex-auto mb-3">
                        <label htmlFor="comment" className="font-bold block">Comment</label>
                        <InputTextarea id="comment" name="comment" value={review.comment}
                                       onChange={onInputChange}
                                       placeholder="Last Name"
                                       autoComplete="off"
                                       className={fieldErrors?.comment ? 'p-invalid' : undefined}
                                       autoResize={true}
                                       style={{minHeight: '20vw', maxWidth: '100%', resize: 'vertical'}}
                        />
                        {fieldErrors?.comment && <small className="p-error">{fieldErrors.comment}</small>}
                    </div>
                </div>
            </form>
        </div>
    )
}
