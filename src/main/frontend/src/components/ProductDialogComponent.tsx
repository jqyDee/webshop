import ProductDialog from "./ProductDialog.tsx";
import {forwardRef, useImperativeHandle, useRef, useState} from "react";
import {emptyProductDto, ProductValidationResult} from "../utilities/productUtilities.ts";
import {ProductDto} from "../api";
import {QueryObserverResult, RefetchOptions, useMutation, useQueryClient} from "@tanstack/react-query";
import {
    createProductMutation,
    deleteProductMutation,
    updateProductMutation
} from "../api/@tanstack/react-query.gen.ts";
import {useLocation, useNavigate} from "react-router-dom";
import {Toast} from "primereact/toast";
import {ProductsRoute} from "../routes.ts";

export interface ProductDialogHandle {
    open: (openProduct: ProductDto | null) => void;
}

interface ProductDialogComponentProps {
    refetch: (options?: RefetchOptions) => Promise<QueryObserverResult<any, Error>>;
}

const ProductDialogComponent = forwardRef<ProductDialogHandle, ProductDialogComponentProps>(
    ({ refetch }, ref) => {
        const toast = useRef<Toast | null>(null);
        const navigate = useNavigate();

        const [dialogVisible, setDialogVisible] = useState<boolean>(false);
        const [isNewProduct, setIsNewProduct] = useState<boolean>(false);
        const [validation, setValidation] = useState<ProductValidationResult>({valid: true});
        const [product, setEditProduct] = useState<ProductDto | null>(null);
        const queryClient = useQueryClient();

        useImperativeHandle(ref, () => ({
            open: (openProduct) => {
                if (openProduct) {
                    // shallow copy. currently working but might break if nested object structure
                    setIsNewProduct(false);
                    setEditProduct({...openProduct});
                    setValidation({valid: true});
                    showDialog()
                } else {
                    setIsNewProduct(true);
                    setEditProduct(emptyProductDto());
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
                    (query.queryKey[0] as any)?._id === 'getProducts'
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
            if (discount) {
                if (discount < 0.0 || discount > 1) {
                    fieldErrors['discount'] = 'discount has to be between 0 and 1';
                }
            }

            const valid = Object.keys(fieldErrors).length === 0;
            return valid ? {valid} : {valid, message: 'Please fill in all required fields', fieldErrors};
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
                toast.current?.show({severity: 'error', summary: 'Something went wrong updating product.', life: 3000});
            }
        }

        const handleInputChange = (event: any) => {
            if (!product) return;

            const name = event.target?.name || event.originalEvent?.target?.name;
            const value = event.target ? event.target.value : event.value;

            if (name) {
                setEditProduct({
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
                    toast.current?.show({severity: 'error', summary: 'Something went wrong deleting product.', life: 3000});
                }
            }
        }

        return (
            <ProductDialog
                visible={dialogVisible}
                product={product}
                isNewProduct={isNewProduct}
                validation={validation}
                onDelete={handleDelete}
                onHide={hideDialog}
                onSubmit={handleSubmit}
                onInputChange={handleInputChange}
            />
        );
    }
);

export default ProductDialogComponent;