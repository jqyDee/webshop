import {useParams} from "react-router-dom";
import {useMutation, useQuery} from "@tanstack/react-query";
import {getProductByIdOptions, updateSubscriptionMutation} from "../api/@tanstack/react-query.gen.ts";
import {ProgressSpinner} from "primereact/progressspinner";
import {Button} from "primereact/button";
import {Tag} from "primereact/tag";
import {Rating} from "primereact/rating";
import DefaultImage from "../assets/default.jpg"
import {Accordion, AccordionTab} from "primereact/accordion";
import {useUser} from "../contexts/authenticated-user.tsx";
import React, {useEffect, useRef, useState} from "react";
import {useCart} from "../contexts/cart.tsx";
import {ProductDialog, ProductDialogHandle} from "./product-dialog.tsx";
import {ReviewTable} from "./review-table.tsx";
import {Checkbox} from "primereact/checkbox";
import {SchemaEnum} from "../api";
import {useGlobalToast} from "../contexts/toast.tsx";

export const ProductDetails: React.FC = () => {
    const {updateCartItem} = useCart();
    const {showToast} = useGlobalToast();

    const {id} = useParams<{ id: string }>();
    const {currentUser, isManager, isAdmin} = useUser();
    const dialogRef = useRef<ProductDialogHandle>(null);

    // QUERY
    const {data: product, isLoading, refetch, error} = useQuery(
        getProductByIdOptions({
            path: {id: Number(id)}
        }),
    );
    const [subscriptions, setSubscriptions] = useState<Record<SchemaEnum, boolean>>(assertProductSubscriptionsRecord(
        product?.subscriptions ?? {
            [SchemaEnum.BACK_IN_STOCK]: false, [SchemaEnum.FOR_SALE]: false,
        }));

    useEffect(() => product?.subscriptions !== undefined ? setSubscriptions(assertProductSubscriptionsRecord(product.subscriptions)) : void(0) , [product]);

    const subscribe = useMutation({
        ...updateSubscriptionMutation(),
        onSuccess: async () => await refetch(),
        onError: (_: Error) => {
            showToast({ severity: 'error', summary: 'Error', detail: 'Failed to subscribe'});
        }
    });

    // LAYOUT
    if (isLoading) return (
        <div className="flex justify-content-center mt-8">
            <ProgressSpinner/>
        </div>
    );

    if (error || !product) return (
        <div className="text-center mt-8">
            Product not found.
        </div>
    );

    const hasDiscount = product.discount > 0;
    const canEdit = currentUser && (isAdmin || isManager);
    const cannotPutIntoCart = isAdmin || isManager;

    return (
        <div className="border-none">
            <div className="grid">
                <div className="col-12 md:col-6 flex justify-content-center align-items-center">
                    <div className="inline-block relative shadow-2 border-round overflow-hidden">
                        <img
                            src={product.imageUrl || DefaultImage}
                            alt={product.name}
                            className="block h-auto"
                            style={{
                                maxWidth: '100%',
                                maxHeight: '500px',
                                width: 'auto',
                                objectFit: 'contain'
                            }}
                            onError={(e) => {
                                (e.currentTarget.src = DefaultImage);
                            }}
                        />
                    </div>
                </div>

                <div
                    className="col-12 md:col-6 px-4 md:text-right md:flex xl:text-right xl:flex flex-column align-items-end">
                    <h1 className="text-4xl font-bold text-900 mb-2">{product.name}</h1>

                    <div className="flex align-items-center gap-2 mb-2">
                        <Rating value={product.rating || 0} readOnly cancel={false}/>
                        <span className="text-500">({product.rating?.toFixed(1) || 0})</span>
                    </div>

                    <hr className="my-4 border-top-1 border-300 w-full"/>

                    <div className="mb-4">
                        {hasDiscount ? (
                            <>
                                <div className="flex align-items-baseline gap-2">
                                    <span className="text-xl text-500 line-through">€{product.price.toFixed(2)}</span>
                                    <span
                                        className="text-3xl font-bold text-red-600">€{product.discountedPrice.toFixed(2)}</span>
                                </div>
                                <Tag severity="danger" value={`-${product.discount * 100}% OFF`}/>
                            </>
                        ) : (
                            <span className="text-3xl font-bold text-900">€{product.price.toFixed(2)}</span>
                        )}
                    </div>

                    <div className="mb-4">
                        <Tag
                            severity={product.stock > 0 ? 'success' : 'danger'}
                            value={product.stock > 0 ? 'In Stock' : 'Out of Stock'}
                        />
                        {product.stock > 0 && <span className="ml-2 text-600">Only {product.stock} left!</span>}
                    </div>

                    <p className="text-700 line-height-3 mb-5 text-lg">
                        {product.shortDescription}
                    </p>

                    <div className="flex flex-column md:flex-row gap-2">
                        {canEdit &&
                            <Button
                                icon="pi pi-pencil"
                                className="p-button-rounded p-button-danger p-button-text"
                                label="Edit"
                                onClick={() => dialogRef.current?.open(product)}
                            />
                        }
                        <Button
                            label="Add to Cart"
                            icon="pi pi-shopping-cart"
                            className="p-button-lg xl:w-15rem md:w-10rem w-full"
                            disabled={product.stock === 0 || cannotPutIntoCart}
                            onClick={() => updateCartItem(product, 1)}
                        />
                    </div>
                </div>
            </div>

            <div className="col-12">
                <Accordion activeIndex={[2]} multiple>
                    <AccordionTab header="Description">
                        <p>{product.description}</p>
                    </AccordionTab>
                    <AccordionTab header="Private Details">
                        <p style={{wordBreak: 'break-all'}}>{JSON.stringify(product)}</p>
                    </AccordionTab>
                    <AccordionTab
                        header="Customer Reviews"
                        pt={{
                            content: {className: 'p-0'} // This removes padding from the internal content div
                        }}
                    >
                        <ReviewTable product={product}/>
                    </AccordionTab>
                    <AccordionTab header="Subscriptions" disabled={isAdmin || isManager}>
                        <div className={"flex flex-column gap-2"}>
                            <label>
                                    <Checkbox
                                        checked={subscriptions.BACK_IN_STOCK}
                                        onClick={async ()=> await subscribe.mutateAsync({
                                            path: {id: Number(id)},
                                            query: {flip: SchemaEnum.BACK_IN_STOCK},
                                        })}
                                        title={getCheckBoxLabel(SchemaEnum.BACK_IN_STOCK)}
                                        className={"mr-2"}
                                    />
                                    {getCheckBoxLabel(SchemaEnum.BACK_IN_STOCK)}
                            </label>
                            <label>
                                    <Checkbox
                                        checked={subscriptions.FOR_SALE}
                                        onClick={async ()=> await subscribe.mutateAsync({
                                            path: {id: Number(id)},
                                            query: {flip: SchemaEnum.FOR_SALE},
                                        })}
                                        title={getCheckBoxLabel(SchemaEnum.FOR_SALE)}
                                        className={"mr-2"}
                                    />
                                    {getCheckBoxLabel(SchemaEnum.FOR_SALE)}
                            </label>
                        </div>
                    </AccordionTab>
                </Accordion>
            </div>
            <ProductDialog
                ref={dialogRef}
                refetch={refetch}
            />
        </div>
    );
}

function getCheckBoxLabel(key: string): string {
    switch (key) {
        case SchemaEnum.BACK_IN_STOCK:
            return "Product is back in Stock";
        case SchemaEnum.FOR_SALE:
            return "Product is for sale";
        default:
            return key;
    }
}

function assertProductSubscriptionsRecord(obj: { [key: string]: boolean }): Record<SchemaEnum, boolean> {
    if (
        Object.keys(obj).every((key) => key === SchemaEnum.FOR_SALE || key === SchemaEnum.BACK_IN_STOCK)
        && Object.keys(obj).length === 2
    ) {
        return obj as Record<SchemaEnum, boolean>;
    }
    throw new Error("unsupported subscription type");
}
