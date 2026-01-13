import {ReviewDto} from "../api";
import {DataView, DataViewPageEvent} from "primereact/dataview";
import React from "react";
import {classNames} from "primereact/utils";
import {Avatar} from "primereact/avatar";
import {Rating} from "primereact/rating";
import {Dropdown} from "primereact/dropdown";
import {Button} from "primereact/button";
import {useUser} from "../Contexts/authenticatedUserContext.tsx";

interface ReviewListComponentProps {
    reviews: ReviewDto[],
    totalCount: number,
    loading: boolean
    pageSize: number,
    first: number,
    onPage: (event: DataViewPageEvent) => void;
    sortOptions: { label: string, value: string }[];
    sortKey: string;
    onSortChange: (event: any) => void;
    onReviewDelete: (review: ReviewDto) => void;
    onCreateReview: () => void;
}

const ReviewListComponent: React.FC<ReviewListComponentProps> = (props) => {

    const { currentUser, isAdmin } = useUser();

    // Render sorting controls in the header
    const renderHeader = () => {
        return (
            <div className={classNames("flex align-items-center gap-3", {
                "justify-content-between": currentUser,
                "justify-content-end": !currentUser
            })}>
                {currentUser && <Button label="Review" icon="pi pi-plus" onClick={props.onCreateReview} />}
                <Dropdown
                    options={props.sortOptions}
                    value={props.sortKey}
                    onChange={props.onSortChange}
                    placeholder="Sort By"
                    className="xl:20rem md:w-10rem sm:w-5rem"
                />
            </div>
        );
    };

    const itemTemplate = (review: ReviewDto, index: number) => {
        const isAuthor = currentUser && review.author && currentUser.username === review.author.username;
        const canDelete = isAdmin || isAuthor;
        const reviewIsManagerOrAdmin = review.author?.role === 'MANAGER' || review.author?.role === 'ADMIN';

        return (
            <div className="col-12" key={review.id}>
                <div className={classNames('flex flex-column md:flex-row align-items-start p-4 gap-4', { 'border-top-1 surface-border': index !== 0 }) }>
                    <div className="flex flex-row md:flex-column align-items-center md:align-items-start gap-3 w-full md:w-2 xl:w-1">
                        <Avatar icon="pi pi-user" shape="circle" size="large" />
                        <div className="flex flex-column">
                            <span className="font-bold text-900">{review.author?.username}</span>
                            { reviewIsManagerOrAdmin &&
                                <small className="text" style={{color: 'var(--green-700)'}}>
                                    {review.author?.role}
                                </small>
                            }
                            <small className="text-500" >
                                {review.createdDate ? new Date(review.createdDate).toLocaleDateString() : 'Recently'}
                            </small>
                        </div>
                    </div>

                    {/* Content Section: Expands to fill remaining space */}
                    <div className="flex flex-column gap-2 flex-1 w-full">
                        <div className="flex align-items-center justify-content-between">
                            <div className="flex align-items-center gap-2">
                                <Rating value={review.rating || 0} readOnly cancel={false} />
                                <span className="font-semibold text-xl text-900 ml-2">{review.title}</span>
                            </div>

                            {canDelete && (
                                <Button
                                    icon="pi pi-trash"
                                    className="p-button-rounded p-button-danger p-button-text"
                                    aria-label="Delete Review"
                                    onClick={() => props.onReviewDelete(review)}
                                />
                            )}
                        </div>
                        <p className="text-700 m-0 line-height-3">
                            {review.comment}
                        </p>
                    </div>
                </div>
            </div>
        );
    }

    const listTemplate = (items: ReviewDto[]) => {
        if (!items || items.length === 0) return null;

        let list = items.map((review, index) => {
            return itemTemplate(review, index);
        });

        return <div className="grid grid-nogutter">{list}</div>;
    }

    return (
        <DataView
            value={props.reviews}
            header={renderHeader()}
            listTemplate={listTemplate}
            layout="list"
            paginator
            rows={props.pageSize}
            first={props.first}
            totalRecords={props.totalCount}
            lazy
            onPage={props.onPage}
            loading={props.loading}
            rowsPerPageOptions={[10, 25, 50]}
        />
    );
}

export default ReviewListComponent;
