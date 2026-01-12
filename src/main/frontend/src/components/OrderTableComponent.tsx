import React, { useState } from "react";
import { useNavigate } from "react-router-dom";
import { useQuery } from "@tanstack/react-query";
import OrderListComponent from "./OrderListComponent";
import { getOrdersOptions } from "../api/@tanstack/react-query.gen";
import {useUser} from "../Contexts/authenticatedUserContext.tsx";
import {RoleEnum} from "../api";

const OrderTableComponent: React.FC = () => {
    const navigate = useNavigate();
    const { currentUser } = useUser();

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
                sort: { sort: ['createdDate,desc'] }, // default: newest orders first
            },
        }),
    });

    const onPage = (event: any) => {
        setLazyState({
            first: event.first,
            pageSize: event.rows,
            pageId: event.page ?? 0,
        });
    };

    if (isError) {
        return <p className="text-center mt-5">Failed to load orders.</p>;
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
