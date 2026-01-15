import {useParams} from "react-router-dom";
import {useQuery} from "@tanstack/react-query";
import {getProductByIdOptions} from "../api/@tanstack/react-query.gen.ts";
import {ProgressSpinner} from "primereact/progressspinner";
import React, {useRef} from "react";
import {Toast} from "primereact/toast";

const OrderDetailsComponent: React.FC = () => {

    const {id} = useParams<{ id: string }>();
    const toast = useRef<Toast | null>(null);

    // QUERY
    const {data: order, isLoading, error} = useQuery(
        getOrderByIdOptions({
            path: {id: Number(id)}
        }),
    );

    // LAYOUT
    if (isLoading) return (
        <div className="flex justify-content-center mt-8">
            <ProgressSpinner/>
        </div>
    );

    if (error || !order) return (
        <div className="text-center mt-8">
            Order not found.
        </div>
    );

    return (
        <div className="border-none">
            <Toast ref={toast}/>


        </div>
    );
}

export default OrderDetailsComponent;