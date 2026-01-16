import {useParams} from "react-router-dom";
import {useMutation, useQuery} from "@tanstack/react-query";
import {createReviewMutation, deleteReviewMutation, getReviewsOptions} from "../api/@tanstack/react-query.gen.ts";
import React, {useState} from "react";
import {DataViewPageEvent} from "primereact/dataview";
import ReviewListComponent from "./ReviewListComponent.tsx";
import ReviewDialog from "./ReviewDialog.tsx";
import {ProductDto, ReviewDto} from "../api";
import {InputMaskChangeEvent} from "primereact/inputmask";
import {emptyReviewDto, ReviewValidationResult} from "../utilities/reviewUtilities.ts";

interface ReviewTableProps {
    product: ProductDto;
}

const sortOptions = [
    { label: 'Newest First', value: '!createdDate' },
    { label: 'Oldest First', value: 'createdDate' },
    { label: 'Rating: High to Low', value: '!rating' },
    { label: 'Rating: Low to High', value: 'rating' },
];

const ReviewTableComponent: React.FC<ReviewTableProps> = (props) => {
    const {id} = useParams<{ id: string }>();
    const [createReviewDto, setCreateReviewDto] = useState<ReviewDto | null>(null);
    const [dialogVisible, setDialogVisible] = useState<boolean>(false);
    const [validation, setValidation] = useState<ReviewValidationResult>({valid: true});
    const [submitting, setSubmitting] = useState<boolean>(false);

    // pagination state
    const [lazyState, setLazyState] = useState({
        first: 0,
        pageSize: 10,
        pageId: 0,
    });

    // sorting
    const [sortKey, setSortKey] = useState<string>('title');
    const [sortField, setSortField] = useState<string>('title');
    const [sortOrder, setSortOrder] = useState<number>(1); // 1 = Asc, -1 = Desc

    const onSortChange = (event: any) => {
        const value = event.value;

        if (value.indexOf('!') === 0) {
            setSortOrder(-1);
            setSortField(value.substring(1));
            setSortKey(value);
        } else {
            setSortOrder(1);
            setSortField(value);
            setSortKey(value);
        }

        // reset pagination to first page
        setLazyState(prev => ({ ...prev, first: 0, pageId: 0 }));
    }

    // Queries
    const {data: pageData, refetch, isLoading} = useQuery(
        getReviewsOptions({
            path: { id: Number(id) },
            query: {
                pageId: lazyState.pageId,
                pageSize: lazyState.pageSize,
                sort: [`${sortField},${sortOrder === 1 ? 'asc' : 'desc'}`] as any
            }
        })
    );

    const deleteReview = useMutation({
        ...deleteReviewMutation(),
        onSuccess: () => refetch()
    });

    const createReview = useMutation({
        ...createReviewMutation(),
        onSuccess: () => refetch()
    })

    // DataView Page Event
    const onPage = (event: DataViewPageEvent) => {
        setLazyState({
            first: event.first,
            pageSize: event.rows,
            pageId: event.page ?? 0
        });
    };

    // Reviews and Dialog
    const validateReview = (review: ReviewDto) => {
        if (!review) return {valid: false, message: 'Something went wrong. Please try again.'};

        const required: (keyof ReviewDto)[] = ["title", "comment"];
        const fieldErrors: Partial<Record<keyof ReviewDto, string>> = {};

        required.forEach((k) => {
            const v = (review[k] as unknown as string) ?? '';
            if (!v.trim()) fieldErrors[k] = "Required";
        })

        if (!review.rating) fieldErrors["rating"] = "Required";

        const valid = Object.keys(fieldErrors).length === 0;
        return valid ? {valid} : {valid, message: 'Please fill in all required fields', fieldErrors};
    };

    const handleSubmit = async () => {
        setSubmitting(true);
        if (!createReviewDto) return;

        const validationResult = validateReview(createReviewDto);
        if (!validationResult.valid) {
            setValidation(validationResult);
            setSubmitting(false);
            console.error('Please fill in all required fields');
            return;
        }

        setValidation({valid: true});

        await createReview.mutateAsync({
            path: {id: Number(id)},
            body: createReviewDto
        });

        hideDialog();
    };

    const handleInputChange = (e: React.ChangeEvent<HTMLInputElement> | React.ChangeEvent<HTMLTextAreaElement> | InputMaskChangeEvent) => {
        if (!createReviewDto) return;

        const {name, value} = e.target;

        setCreateReviewDto({...createReviewDto, [name]: value});
    };

    const showDialog = () => {
        setSubmitting(false);
        setValidation({valid: true})
        setDialogVisible(true);
    }

    const hideDialog = () => {
        setSubmitting(false);
        setValidation({valid: true})
        setDialogVisible(false);
    };

    const openCreateReviewDialog = () => {
        setCreateReviewDto(emptyReviewDto());
        setValidation({valid: true})
        showDialog();
    }

    return (
        <>
            <ReviewListComponent
                reviews={pageData?.items ?? []}
                totalCount={pageData?.totalCount ?? 0}
                loading={isLoading}
                pageSize={lazyState.pageSize}
                first={lazyState.first}
                onPage={onPage}
                sortOptions={sortOptions}
                sortKey={sortKey}
                onSortChange={onSortChange}
                onReviewDelete={(review) => {
                    if (window.confirm("Are you sure you want to delete this review?")) {
                        deleteReview.mutate({
                            path: {
                                productId: Number(id),
                                reviewId: review.id!
                            }
                        })
                    }
                }}
                onCreateReview={openCreateReviewDialog}
            />
            <ReviewDialog
                visible={dialogVisible}
                submitting={submitting}
                review={createReviewDto}
                product={props.product}
                validation={validation}
                onHide={hideDialog}
                onSubmit={handleSubmit}
                onInputChange={handleInputChange}
            />
        </>
    );
}

export default ReviewTableComponent;