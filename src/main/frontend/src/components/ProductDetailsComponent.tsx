import {useParams} from "react-router-dom";
import {useQuery} from "@tanstack/react-query";
import {getProductByIdOptions} from "../api/@tanstack/react-query.gen.ts";
import {ProgressSpinner} from "primereact/progressspinner";
import {Button} from "primereact/button";
import {Tag} from "primereact/tag";
import {Rating} from "primereact/rating";
import DefaultImage from "../assets/default.jpg"
import {Accordion, AccordionTab} from "primereact/accordion";
import {useUser} from "../Contexts/authenticatedUserContext.tsx";
import React, {useRef} from "react";
import {Toast} from "primereact/toast";
import {useCart} from "../Contexts/cartContext.tsx";
import ProductDialogComponent, {ProductDialogHandle} from "./ProductDialogComponent.tsx";

const ProductDetailsComponent: React.FC = () => {
    const { updateCartItem } = useCart();

    const {id} = useParams<{ id: string }>();
    const {currentUser, isManager, isAdmin} = useUser();
    const toast = useRef<Toast | null>(null);
    const dialogRef = useRef<ProductDialogHandle>(null);

    // QUERY
    const {data: product, isLoading, refetch, error} = useQuery(
        getProductByIdOptions({
            path: {id: Number(id)}
        }),
    );

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
            <Toast ref={toast}/>
            <div className="grid">
                <div className="col-12 md:col-6 flex justify-content-center">
                    <img
                        src={product.imageUrl || DefaultImage}
                        alt={product.name}
                        className="shadow-4 w-full"
                        style={{ maxWidth: '500px', objectFit: 'contain' }}
                        onError={(e) => {
                            (e.currentTarget.src = DefaultImage);
                        }}
                    />
                </div>

                <div className="col-12 md:col-6 px-4 md:text-right md:flex xl:text-right xl:flex flex-column align-items-end">
                    <h1 className="text-4xl font-bold text-900 mb-2">{product.name}</h1>

                    <div className="flex align-items-center gap-2 mb-2">
                        <Rating value={product.rating || 0} readOnly cancel={false} />
                        <span className="text-500">({product.rating?.toFixed(1) || 0})</span>
                    </div>

                    <hr className="my-4 border-top-1 border-300 w-full" />

                    <div className="mb-4">
                        {hasDiscount ? (
                            <>
                                <div className="flex align-items-baseline gap-2">
                                    <span className="text-xl text-500 line-through">${product.price.toFixed(2)}</span>
                                    <span className="text-3xl font-bold text-red-600">${product.discountedPrice.toFixed(2)}</span>
                                </div>
                                <Tag severity="danger" value={`-${product.discount * 100}% OFF`} />
                            </>
                        ) : (
                            <span className="text-3xl font-bold text-900">${product.price.toFixed(2)}</span>
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
                <Accordion activeIndex={0}>
                    <AccordionTab header="Description">
                        <p>{product.description}</p>
                    </AccordionTab>
                    <AccordionTab header="Private Details">
                        <p style={{ wordBreak: 'break-all' }}>{JSON.stringify(product)}</p>
                    </AccordionTab>
                </Accordion>
            </div>
            <ProductDialogComponent
                ref={dialogRef}
                refetch={refetch}
            />
        </div>
    );
}

export default ProductDetailsComponent;