import {ProductDto, ProductFilterDto} from "../api";
import {DataView, DataViewLayoutOptions, DataViewPageEvent} from "primereact/dataview";
import {Tag} from "primereact/tag";
import {Rating} from "primereact/rating";
import {Button} from "primereact/button";
import {Dropdown} from "primereact/dropdown";
import {InputNumber} from "primereact/inputnumber";
import {InputText} from "primereact/inputtext";
import {IconField} from "primereact/iconfield";
import {InputIcon} from "primereact/inputicon";
import React, {useEffect, useState} from "react";

import DefaultImage from "../assets/default.jpg"
import {useCart} from "../contexts/cartContext.tsx";
import {useUser} from "../contexts/authenticatedUserContext.tsx";
import {useNavigate} from "react-router-dom";

interface ProductListComponentProps {
    products: ProductDto[],
    totalCount: number,
    loading: boolean
    pageSize: number,
    first: number,
    onPage: (event: DataViewPageEvent) => void;
    openDialog: (editProduct: ProductDto | null) => void;
    filters: ProductFilterDto,
    onFilterChange: (field: keyof ProductFilterDto, value: any) => void;
    onNameChange: (value: string) => void;
    sortOptions: { label: string, value: string }[];
    sortKey: string;
    onSortChange: (event: any) => void;
}

const ProductListComponent: React.FC<ProductListComponentProps> = (props) => {
    const { currentUser, isAdmin, isManager } = useUser();
    const { updateCartItem } = useCart();
    const [searchTerm, setSearchTerm] = useState(props.filters.name || '');
    const navigate = useNavigate();
    const [layout, setLayout] = useState<'list' | 'grid'>('grid');

    useEffect(() => {
        setSearchTerm(props.filters.name || '');
    }, [props.filters.name]);

    const getSeverity = (product: ProductDto) => {
        if (product.stock === 0) {
            return 'danger'
        } else if (product.stock <= 4) {
            return 'warning'
        } else {
            return 'success'
        }
    };

    const handleKeyDown = (e: React.KeyboardEvent<HTMLInputElement>) => {
        if (e.key === 'Enter') {
            props.onNameChange(searchTerm);
        }
    };

    const header = () => {
        return (
            <div className="grid grid-nogutter p-2 row-gap-3">
                {/* Row 1: Search - Full width (12/12 columns) */}
                <div className="col-12">
                    <IconField iconPosition={'left'}>
                        <InputIcon className="pi pi-search"> </InputIcon>
                        <InputText
                            value={searchTerm} // Bind to local state
                            onChange={(e) => setSearchTerm(e.target.value)} // Update local state only
                            onKeyDown={handleKeyDown} // Trigger parent on Enter
                            placeholder="Search product name and press Enter..."
                            className="w-full"
                        />
                    </IconField>
                </div>

                {/* Row 2: The Filter Row */}
                <div className="col-12">
                    <div className="flex flex-wrap align-items-start gap-3">

                        {/* Min Price - Fixed container */}
                        <div className="flex flex-column gap-2" style={{ minWidth: '120px' }}>
                            <label className="text-sm font-bold">Min Price</label>
                            <InputNumber
                                value={props.filters.minPrice}
                                onValueChange={(e) => props.onFilterChange('minPrice', e.value)}
                                placeholder="0.00"
                                maxFractionDigits={2}
                                inputClassName="w-full" // Forces the input to fill the 120px div
                            />
                        </div>

                        {/* Max Price - Fixed container */}
                        <div className="flex flex-column gap-2" style={{ minWidth: '120px' }}>
                            <label className="text-sm font-bold">Max Price</label>
                            <InputNumber
                                value={props.filters.maxPrice}
                                onValueChange={(e) => props.onFilterChange('maxPrice', e.value)}
                                placeholder="999.00"
                                maxFractionDigits={2}
                                inputClassName="w-full"
                            />
                        </div>

                        {/* Min Stock - Fixed container */}
                        <div className="flex flex-column gap-2" style={{ minWidth: '120px' }}>
                            <label className="text-sm font-bold">Min Stock</label>
                            <InputNumber
                                value={props.filters.minStock}
                                onValueChange={(e) => props.onFilterChange('minStock', e.value)}
                                placeholder="10"
                                maxFractionDigits={0}
                                inputClassName="w-full"
                            />
                        </div>

                        {/* Min Rating - Fixed container */}
                        <div className="flex flex-column gap-2" style={{ minWidth: '120px' }}>
                            <label className="text-sm font-bold">Min Rating</label>
                            <Rating
                                value={props.filters.minRating || 0} // Use the value from filters prop
                                onChange={(e) => props.onFilterChange('minRating', e.value)} // Update parent state
                                cancel={true} // Allow users to clear the rating filter
                            />
                        </div>

                        {/* Sort Dropdown and Layout - Responsive width */}
                        <div className="flex flex-row align-items-end gap-2 ml-auto" style={{ minWidth: '200px' }}>
                            {canEdit &&
                                <Button
                                    icon="pi pi-plus"
                                    label="Create Product"
                                    onClick={() => props.openDialog(null)}
                                />
                            }
                            <div className="flex flex-column gap-2" >
                                <label className="text-sm font-bold">Sort By</label>
                                <Dropdown
                                    options={props.sortOptions}
                                    value={props.sortKey}
                                    optionLabel="label"
                                    placeholder="Select Order"
                                    onChange={props.onSortChange}
                                    className="w-full"
                                />
                            </div>
                            <div className="hidden md:flex">
                                <div className="flex flex-column gap-2" >
                                    <label className="text-sm font-bold">Layout</label>
                                    <DataViewLayoutOptions
                                        layout={layout}
                                        onChange={(e) => setLayout(e.value as 'list' | 'grid')}
                                        className="w-full"
                                    />
                                </div>
                            </div>
                        </div>
                    </div>
                </div>
            </div>
        );
    };

    const canEdit = currentUser && (isAdmin || isManager);

    const listItem= (product: ProductDto) => {
        return (
            <div className="col-12" key={product.id}>
                <div className="card border-round flex flex-column xl:flex-row xl:align-items-start p-4 gap-4 shadow-5 m-2">
                    <img className="w-9 sm:w-16rem xl:w-10rem shadow-2 block xl:block mx-auto border-round"
                         src={product.imageUrl || DefaultImage}
                         alt={product.name}
                         onError={(e) => {
                             (e.currentTarget.src = DefaultImage);
                         }}
                    />
                    <div className="flex flex-column sm:flex-row justify-content-between align-items-center xl:align-items-start flex-1 gap-4">
                        <div className="flex flex-column align-items-center sm:align-items-start gap-2">
                            <div className="text-2xl font-bold text-900 cursor-pointer hover:underline"
                                 onClick={() => navigate(`/product/${product.id}`)}
                            >
                                {product.name}
                            </div>
                            <div className="text-sm text-900">{product.shortDescription}</div>
                            <div className="flex align-items-center gap-2 mb-4">
                                <Rating value={product.rating} readOnly cancel={false}></Rating>
                                <span className="text-500">({product.rating?.toFixed(1) || 0})</span>
                            </div>
                            <div className="flex align-items-center gap-3">
                                <Tag value={'Stock: ' + product.stock} severity={getSeverity(product)}></Tag>
                            </div>
                        </div>
                        <div className="flex sm:flex-column align-items-center sm:align-items-end gap-3 sm:gap-2">
                            <div className="flex flex-row align-items-center gap-2">
                                {/* Original Price (strikethrough) */}
                                {product.discountedPrice && product.discount > 0.0 && (
                                    <span className="text-xl text-500 line-through">
                                        €{product.price.toFixed(2)}
                                    </span>
                                )}

                                {/* Current Price */}
                                <span className="text-2xl font-semibold text-900">
                                    €{product.discountedPrice.toFixed(2) || product.price.toFixed(2)}
                                </span>
                            </div>

                            {/* 3. Discount Percentage */}
                            {product.discount > 0.0 && (
                                <Tag severity="danger" value={`${product.discount * 100}% OFF`} />
                            )}
                            <div className="flex flex-row gap-2">
                                {canEdit &&
                                    <Button
                                        icon="pi pi-pencil"
                                        className="p-button-rounded p-button-danger p-button-text"
                                        onClick={() => props.openDialog(product)}
                                    />
                                }
                                <Button icon="pi pi-shopping-cart"
                                        className="p-button-rounded ml-auto"
                                        disabled={product.stock === 0 || (isAdmin || isManager)}
                                        onClick={async () => {
                                            await updateCartItem(product, 1);
                                        }}
                                />
                            </div>
                        </div>
                    </div>
                </div>
            </div>
        );
    };

    const gridItem = (product: ProductDto) => {
        return (
            <div className="col-12 sm:col-6 lg:col-12 xl:col-4 p-2" key={product.id}>
                <div className="p-4 card border-round flex flex-column h-full shadow-5">
                    {/* Image Section */}
                    <div className="flex flex-column align-items-center gap-3 py-5">
                        <img
                            className="w-9 shadow-2 border-round"
                            src={product.imageUrl || DefaultImage}
                            alt={product.name}
                            onError={(e) => { (e.currentTarget.src = DefaultImage); }}
                            style={{ objectFit: 'cover', width: '300px', height: '300px' }}
                        />
                        <div className="text-2xl font-bold text-900 cursor-pointer hover:underline text-center"
                             onClick={() => navigate(`/product/${product.id}`)}>
                            {product.name}
                        </div>
                        <div className="flex align-items-center gap-2">
                            <Rating value={product.rating} readOnly cancel={false}></Rating>
                            <span className="text-500">({product.rating?.toFixed(1) || 0})</span>
                        </div>
                    </div>

                    {/* Description & Tags */}
                    <div className="flex-1">
                        <div className="text-sm text-700 mb-3 line-height-3">
                            {product.shortDescription}
                        </div>
                        <div className="flex align-items-center gap-3 mb-4">
                            <Tag value={'Stock: ' + product.stock} severity={getSeverity(product)}></Tag>
                        </div>
                    </div>

                    {/* Footer Section: Price and Buttons */}
                    <div className="flex align-items-center justify-content-between">
                        <div className="flex flex-column">
                            {product.discountedPrice && product.discount > 0.0 && (
                                <span className="text-sm text-500 line-through">
                                    €{product.price.toFixed(2)}
                                </span>
                            )}
                            <div className="flex gap-2">
                                <span className="text-2xl font-semibold text-900">
                                    €{product.discountedPrice?.toFixed(2) || product.price.toFixed(2)}
                                </span>
                                {product.discount > 0.0 && (
                                    <Tag severity="danger" value={`${product.discount * 100}% OFF`} />
                                )}
                            </div>
                        </div>

                        <div className="flex gap-2">
                            {canEdit &&
                                <Button
                                    icon="pi pi-pencil"
                                    className="p-button-rounded p-button-danger p-button-text"
                                    onClick={() => props.openDialog(product)}
                                />
                            }
                            <Button
                                icon="pi pi-shopping-cart"
                                className="p-button-rounded"
                                disabled={product.stock === 0 || (isAdmin || isManager)}
                                onClick={() => updateCartItem(product, 1)}
                            />
                        </div>
                    </div>
                </div>
            </div>
        );
    };

    const itemTemplate = (product: ProductDto) => {
        if (!product) return null;
        return layout === 'list' ? listItem(product) : gridItem(product);
    }

    const listTemplate = (items: ProductDto[]) => {
        if (!items || items.length === 0) return null;

        let list = items.map((product) => {
            return itemTemplate(product);
        });

        return <div className="grid grid-nogutter">{list}</div>;
    };

    return (
        <DataView
            value={props.products}
            listTemplate={listTemplate}
            header={header()}
            layout={layout}
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
};

export default ProductListComponent;