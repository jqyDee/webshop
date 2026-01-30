import React, {useCallback} from "react";
import {OrderDto, OrderItemDto} from "../../api";
import {DataTable} from "primereact/datatable";
import {Column} from "primereact/column";
import DefaultImage from "../../assets/default.jpg";
import {Button} from "primereact/button";
import {useNavigate} from "react-router-dom";
import {ROUTES} from "../../utilities/routes.paths.ts";
import {useQueryClient} from "@tanstack/react-query";

interface ConfirmationStepperContentProps {
    readonly order: OrderDto;
}

export const ConfirmationStepperContent: React.FC<ConfirmationStepperContentProps> = (props) => {
    const navigate = useNavigate();
    const queryClient = useQueryClient();
    const onContinueShopping = useCallback(async () => {
        await queryClient.invalidateQueries();
        navigate(ROUTES.HOME);
    }, [navigate, queryClient]);
    return (<>
        <h2>Your Order: {props.order.id} is right at your doorbell</h2>
        <span>Enjoy your new products: </span>
        <DataTable title={"Products"} value={props.order.products}>
            <Column body={imageTemplate}/>
            <Column field={"name"} sortable/>
        </DataTable>
        <h4>We hope you enjoyed the experience and come back soon :)</h4>
        <Button label={"Continue Shopping"} onClick={onContinueShopping} />
    </>);
}

const imageTemplate = (item: OrderItemDto) => (
    <img className="w-9 sm:w-8rem xl:w-5rem shadow-2 block xl:block mx-auto border-round"
         src={item.product.imageUrl ?? DefaultImage}
         alt={item.name}
         onError={(e) => {
             (e.currentTarget.src = DefaultImage);
         }}
    />);