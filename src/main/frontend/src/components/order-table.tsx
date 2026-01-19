import React, { useState } from "react";
import { useNavigate } from "react-router-dom";
import { useQuery } from "@tanstack/react-query";
import { DataViewPageEvent } from "primereact/dataview";
import { useUser } from "../contexts/authenticated-user.tsx";
import {getOrdersOptions, getAllOrdersOptions} from "../api/@tanstack/react-query.gen";
import { RoleEnum } from "../api";
import {Button} from "primereact/button";
import {OrderList} from "./order-table/order-list.tsx";

export const OrderTable: React.FC = () => {
    const navigate = useNavigate();
    const { currentUser } = useUser();
    const isAdmin = currentUser?.role === RoleEnum.ADMIN;

    const [sortField] = useState<string>('createdDate');
    const [sortOrder] = useState<number>(1);

    const [lazyState, setLazyState] = useState({
        first: 0,
        pageSize: 10,
        pageId: 0,
    });

    const queryParams = {
        query: {
            pageId: lazyState.pageId,
            pageSize: lazyState.pageSize,
            sort: [`${sortField},${sortOrder === 1 ? 'asc' : 'desc'}`] as any,
        }
    };

    const queryOptions = isAdmin
        ? getAllOrdersOptions(queryParams)
        : getOrdersOptions(queryParams);

    const { data, isLoading, isError } = useQuery(queryOptions);

    const onPage = (event: DataViewPageEvent) => {
        setLazyState({
            first: event.first,
            pageSize: event.rows,
            pageId: event.page ?? 0,
        });
    };

    if (isError) {
        return (
            <div className="p-5 text-center border-round surface-card border-1 border-red-200">
                <p className="text-red-500 font-bold">Failed to load orders.</p>
                <Button label="Retry" className="p-button-text" onClick={() => window.location.reload()} />
            </div>
        );
    }

    return (
        <OrderList
            orders={data?.items ?? []}
            loading={isLoading}
            totalCount={data?.totalCount ?? 0}
            first={lazyState.first}
            pageSize={lazyState.pageSize}
            onPage={onPage}
            onView={(orderId) => navigate(`/orders/${orderId}`)}
            showUserColumn={currentUser?.role === RoleEnum.ADMIN}
        />
    );
};
