import {useNavigate, useParams} from "react-router-dom";
import {useMutation, useQuery} from "@tanstack/react-query";
import {deleteProductMutation, getProductByIdOptions, updateProductMutation} from "../api/@tanstack/react-query.gen.ts";
import {ProgressSpinner} from "primereact/progressspinner";
import {Button} from "primereact/button";
import {Tag} from "primereact/tag";
import {Rating} from "primereact/rating";
import DefaultImage from "../assets/default.jpg"
import {Accordion, AccordionTab} from "primereact/accordion";
import {useUser} from "../Contexts/authenticatedUserContext.tsx";
import ProductDialog from "./ProductDialog.tsx";
import React, {useRef, useState} from "react";
import {ProductValidationResult} from "../utilities/productUtilities.ts";
import {ProductDto} from "../api";
import {Toast} from "primereact/toast";
import {useCart} from "../Contexts/cartContext.tsx";

const ProductDetailsComponent: React.FC = () => {
    const { updateCartItem } = useCart();

    const {id} = useParams<{ id: string }>();
    const {currentUser, isManager, isAdmin} = useUser();
    const toast = useRef<Toast | null>(null);
    const navigate = useNavigate();

    const [dialogVisible, setDialogVisible] = useState<boolean>(false);
    const [validation, setValidation] = useState<ProductValidationResult>({valid: true});
    const [editProduct, setEditProduct] = useState<ProductDto | null>(null);

    // QUERY
    const {data: product, isLoading, refetch, error} = useQuery(
        getProductByIdOptions({
            path: {id: Number(id)}
        }),
    );

    const updateProduct = useMutation({
        ...updateProductMutation(),
        onSuccess: async () => await refetch()
    });

    const deleteProduct = useMutation({
        ...deleteProductMutation(),
        onSuccess: () => navigate('/')
    })

    // DIALOG
    const validateProduct = (product: ProductDto): ProductValidationResult => {
        if (!product) return {valid: false, message: 'No product could be loaded'};

        const required: (keyof ProductDto)[] = ['name'];
        const fieldErrors: Partial<Record<keyof ProductDto, string>> = {};

        required.forEach((k) => {
            const v = (product[k] as unknown as string) ?? '';
            if (!v.trim()) fieldErrors[k] = 'Required';
        })

        // 'name', 'price', 'stock', 'discount'
        // price
        const price = product.price;
        if (!price) fieldErrors['price'] = 'Required';

        // stock
        const stock = product.stock;
        if (!stock) fieldErrors['stock'] = 'Required';

        // discount
        const discount = product.discount;
        if (discount){
            if (discount < 0.0 || discount > 1) {
                fieldErrors['discount'] = 'discount has to be between 0 and 1';
            }
        }

        const valid = Object.keys(fieldErrors).length === 0;
        return valid ? {valid} : {valid, message: 'Please fill in all required fields', fieldErrors};
    }

    const openEditDialog = () => {
        if (!product) return;

        // shallow copy. currently working but might break if nested object structure
        setEditProduct({...product});
        setValidation({valid: true});
        setDialogVisible(true);
        showDialog()
    }

    const hideDialog = () => {
        setDialogVisible(false);
        setValidation({valid: true});
    }

    const showDialog = () => {
        setValidation({valid: true});
        setDialogVisible(true);
    }

    const handleSubmit = async () => {
        if (!editProduct) return;

        const validationResult = validateProduct(editProduct)
        if (!validationResult.valid) {
            setValidation(validationResult);
            console.error('Please fill in all required fields');
            return;
        }

        setValidation({valid: true});

        try {
            await updateProduct.mutateAsync({
                path: {id: editProduct.id },
                body: editProduct
            });

            hideDialog()
        } catch (error) {
            console.error('Something went wrong updating product', error);
            toast.current?.show({severity: 'error', summary: 'Something went wrong updating product.', life: 3000});
        }
    }

    const handleInputChange = (event: any) => {
        if (!editProduct) return;

        const name = event.target?.name || event.originalEvent?.target?.name;
        const value = event.target ? event.target.value : event.value;

        if (name) {
            setEditProduct({
                ...editProduct,
                [name]: value ?? 0 // Use 0 as fallback for numeric fields if cleared
            });
        }
    };

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
    const discountedPrice = 9999;
    const canEdit = currentUser && (isAdmin || isManager);

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

                    <div className="flex align-items-center gap-2 mb-4">
                        <Rating value={product.rating || 0} readOnly cancel={false} />
                        <span className="text-500">({product.rating?.toFixed(1)})</span>
                    </div>

                    <hr className="my-4 border-top-1 border-300 w-full" />

                    <div className="mb-4">
                        {hasDiscount ? (
                            <div className="flex align-items-baseline gap-2">
                                <span className="text-3xl font-bold text-red-600">${discountedPrice.toFixed(2)}</span>
                                <span className="text-xl text-500 line-through">${product.price.toFixed(2)}</span>
                                <Tag severity="danger" value={`-${product.discount * 100}%`} />
                            </div>
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
                                onClick={openEditDialog}
                            />
                        }
                        <Button
                            label="Add to Cart"
                            icon="pi pi-shopping-cart"
                            className="p-button-lg xl:w-15rem md:w-10rem w-full"
                            disabled={product.stock === 0}
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
            <ProductDialog
                visible={dialogVisible}
                product={editProduct}
                isNewProduct={false}
                validation={validation}
                onDelete={() => {
                    if (window.confirm("Do you really want to delete this Product? This cannot be undone!")) {
                        deleteProduct.mutateAsync({
                            path: {id: product?.id}
                        })
                    }
                }}
                onHide={hideDialog}
                onSubmit={handleSubmit}
                onInputChange={handleInputChange}
            />
        </div>
    );
}

export default ProductDetailsComponent;