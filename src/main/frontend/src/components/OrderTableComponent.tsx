import React, { useState } from "react";
import { useNavigate } from "react-router-dom";
import { useQuery } from "@tanstack/react-query";
import { DataViewPageEvent } from "primereact/dataview";
import OrderListComponent from "./OrderListComponent";
import {getOrdersOptions} from "../api/@tanstack/react-query.gen";
import { useUser } from "../Contexts/authenticatedUserContext.tsx";
import { RoleEnum } from "../api";
import {Button} from "primereact/button";

const OrderTableComponent: React.FC = () => {
    const navigate = useNavigate();
    const { currentUser } = useUser();

    const [sortField] = useState<string>('createdDate');
    const [sortOrder] = useState<number>(1);

    const [lazyState, setLazyState] = useState({
        first: 0,
        pageSize: 10,
        pageId: 0,
    });

    const { data, isLoading, isError } = useQuery({
        ...getOrdersOptions({
            query: {
                pageId: lazyState.pageId,
                pageSize: lazyState.pageSize,
                sort: [`${sortField},${sortOrder === 1 ? 'asc' : 'desc'}`] as any,
            }
        }),
    });

    // Explicitly typed event
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
        <OrderListComponent
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

export default OrderTableComponent;