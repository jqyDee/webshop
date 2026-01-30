import {forwardRef, useImperativeHandle, useState} from "react";
import {ProductDto} from "../api";
import {QueryObserverResult, RefetchOptions, useMutation, useQueryClient} from "@tanstack/react-query";
import {
    createProductMutation,
    deleteProductMutation,
    updateProductMutation
} from "../api/@tanstack/react-query.gen.ts";
import {useLocation, useNavigate} from "react-router-dom";
import {ProductsRoute} from "../routes.ts";
import {useGlobalToast} from "../contexts/toast.tsx";
import {Message} from "primereact/message";
import {Dialog} from "primereact/dialog";
import {Button} from "primereact/button";
import {ProductForm} from "./product-dialog/product-form.tsx";
import {validateFormData, ValidationResult} from "../utilities/form-data-validator.ts";

export interface ProductDialogHandle {
    open: (openProduct: ProductDto | null) => void;
}

interface ProductDialogComponentProps {
    refetch: (options?: RefetchOptions) => Promise<QueryObserverResult<any, Error>>;
}

export const ProductDialog = forwardRef<ProductDialogHandle, ProductDialogComponentProps>(
    ({ refetch }, ref) => {
        const {showToast} = useGlobalToast();
        const navigate = useNavigate();

        const [dialogVisible, setDialogVisible] = useState<boolean>(false);
        const [isNewProduct, setIsNewProduct] = useState<boolean>(false);
        const [validation, setValidation] = useState<ValidationResult<ProductDto>>({valid: true});
        const [product, setProduct] = useState<ProductDto | null>(null);
        const queryClient = useQueryClient();

        useImperativeHandle(ref, () => ({
            open: (openProduct) => {
                if (openProduct) {
                    // shallow copy. currently working but might break if nested object structure
                    setIsNewProduct(false);
                    setProduct({...openProduct});
                    setValidation({valid: true});
                    showDialog()
                } else {
                    setIsNewProduct(true);
                    setProduct(emptyProductDto());
                    setValidation({valid: true});
                    showDialog()
                }
            }
        }));

        const deleteProduct = useMutation({
            ...deleteProductMutation(),
            onSuccess: async () => await queryClient.invalidateQueries({
                predicate: (query) =>
                    Array.isArray(query.queryKey) &&
                    (query.queryKey[0])?._id === 'getProducts'
            })
        })

        const updateProduct = useMutation({
            ...updateProductMutation(),
            onSuccess: async () => await refetch()
        });

        const createProduct = useMutation({
            ...createProductMutation(),
            onSuccess: async () => await refetch()
        })

        const validateProduct = (product: ProductDto): ValidationResult<ProductDto> => {
            if (!product) return {valid: false, message: 'No product could be loaded'};

            return validateFormData(product, [
                (key) => key === "name" && product[key].length === 0 ? "Required" : undefined,
                (key) => (key === "price" || key === "stock") && !product[key] ? "Required" : undefined,
                (key) => key === "discount" && (product[key] < 0.0 || product[key] > 1) ? "discount has to be between 0 and 1" : undefined,
            ]);
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
            if (!product) return;

            const validationResult = validateProduct(product)
            if (!validationResult.valid) {
                setValidation(validationResult);
                console.error('Please fill in all required fields');
                return;
            }

            setValidation({valid: true});

            try {
                if (isNewProduct) {
                    await createProduct.mutateAsync({
                        body: product
                    })
                } else {
                    await updateProduct.mutateAsync({
                        path: {id: product.id!},
                        body: product
                    });

                }
                hideDialog()
            } catch (error) {
                console.error('Something went wrong updating product', error);
                showToast({severity: 'error', summary: 'Something went wrong updating product.', life: 3000});
            }
        }

        const handleInputChange = (event: any) => {
            if (!product) return;

            const name = event.target?.name || event.originalEvent?.target?.name;
            const value = event.target ? event.target.value : event.value;

            if (name) {
                setProduct({
                    ...product,
                    [name]: value ?? 0 // Use 0 as fallback for numeric fields if cleared
                });
            }
        };

        const location = useLocation();
        const handleDelete = async () => {
            if (product?.id && window.confirm("Do you really want to delete this Product? This cannot be undone!")) {
                try {
                    await deleteProduct.mutateAsync({
                        path: {id: product?.id}
                    });
                    if (location.pathname !== ProductsRoute.url) {
                        navigate("/products");
                    }
                    hideDialog();
                } catch (error) {
                    console.error('Something went wrong deleting product', error);
                    showToast({severity: 'error', summary: 'Something went wrong deleting product.', life: 3000});
                }
            }
        }

        const renderFooter = () => (
            <div className="flex justify-content-between">
                <Button label="Delete Product" icon="pi pi-times" onClick={handleDelete} className="p-button-danger" disabled={isNewProduct}/>
                <Button label={isNewProduct ? "Create" : "Save"} icon="pi pi-check" onClick={handleSubmit}
                        autoFocus />
            </div>
        );
        return (
            <Dialog
                header={isNewProduct ? "Create Product" : "Edit Product"}
                visible={dialogVisible}
                style={{ width: '50vw' }}
                breakpoints={{ '960px': '75vw', '641px': '90vw' }}
                onHide={hideDialog}
                footer={renderFooter}
            >
                {validation.message && (<Message severity="error" text={validation.message} className="mb-3"/>)}
                {product &&
                    <ProductForm
                        product={product}
                        fieldErrors={validation.fieldErrors}
                        onInputChange={handleInputChange}
                    />
                }
            </Dialog>
        );
    }
);

const emptyProductDto = (): ProductDto => {
    return {
        id: undefined,
        name: "",
        price: 0,
        stock: 0,
        discount: 0.0,
        discountedPrice: 0,
        shortDescription: undefined,
        description: undefined,
        rating: undefined,
        imageUrl: undefined,
        createdDate: undefined,
        updatedDate: undefined
    }
}
