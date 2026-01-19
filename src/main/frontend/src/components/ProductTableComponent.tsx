import ProductListComponent from "./ProductListComponent.tsx";
import {useQuery} from "@tanstack/react-query";
import {getProductsOptions} from "../api/@tanstack/react-query.gen.ts";
import {useRef, useState} from "react";
import {ProductDto, ProductFilterDto} from "../api";
import {DataViewPageEvent} from "primereact/dataview";
import ProductDialogComponent, {ProductDialogHandle} from "./ProductDialogComponent.tsx";

const sortOptions = [
    { label: 'Name: A-Z', value: 'name' },
    { label: 'Price High to Low', value: '!discountedPrice' },
    { label: 'Price Low to High', value: 'discountedPrice' },
    { label: 'Rating: High to Low', value: '!rating' },
];

const ProductTableComponent = () => {
    const dialogRef = useRef<ProductDialogHandle>(null);

    // pagination state
    const [lazyState, setLazyState] = useState({
        first: 0,
        pageSize: 10,
        pageId: 0,
    });

    // sorting
    const [sortKey, setSortKey] = useState<string>('name');
    const [sortField, setSortField] = useState<string>('name');
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

    // filters
    const [filters, setFilters] = useState<ProductFilterDto>({
        name: '',
        minPrice: undefined,
        maxPrice: undefined,
        minRating: undefined,
        minStock: undefined,
    });

    const onNameChange= (value: string) => {
        setFilters(prev => ({ ...prev, name: value }));
        setLazyState(prev => ({ ...prev, first: 0, pageId: 0 }));
    };

    const onFilterChange = (field: keyof ProductFilterDto, value: any) => {
        setFilters(prev => ({ ...prev, [field]: value }));
        // reset pagination to first page
        setLazyState(prev => ({ ...prev, first: 0, pageId: 0 }));
    }

    // query
    const {data: pageData, refetch, isLoading} = useQuery({
        ...getProductsOptions({
            query: {
                pageId: lazyState.pageId,
                pageSize: lazyState.pageSize,
                // @ts-ignore // I have no idea why this is being flagged as an error
                sort: [`${sortField},${sortOrder === 1 ? 'asc' : 'desc'}`],
                ...filters,
            },
        }),
    });

    // DataView Page Event
    const onPage = (event: DataViewPageEvent) => {
        setLazyState({
            first: event.first,
            pageSize: event.rows,
            pageId: event.page ?? 0
        });
    };

    const handleOpenDialog = (editProduct: ProductDto | null) => {
        dialogRef.current?.open(editProduct);
    }

    return (
        <>
            <ProductListComponent
                products={pageData?.items ?? []}
                totalCount={pageData?.totalCount ?? 0}
                loading={isLoading}
                pageSize={lazyState.pageSize}
                first={lazyState.first}
                onPage={onPage}
                openDialog={handleOpenDialog}
                filters={filters}
                onFilterChange={onFilterChange}
                onNameChange={onNameChange}
                sortOptions={sortOptions}
                sortKey={sortKey}
                onSortChange={onSortChange}
            />
            <ProductDialogComponent ref={dialogRef} refetch={refetch} />
        </>
    )
}

export default ProductTableComponent;