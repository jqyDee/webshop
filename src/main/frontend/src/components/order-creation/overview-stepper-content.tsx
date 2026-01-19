import React from "react";
import {AddressDto, OrderDto, OrderItemDto} from "../../api";
import {Address} from "../order-details/address.tsx";
import {DataTable} from "primereact/datatable";
import {Column} from "primereact/column";
import {Button} from "primereact/button";

interface OverviewStepperContentProps {
    readonly order: OrderDto;
    readonly shippingAddress: AddressDto;
    readonly paymentAddress: AddressDto;
    readonly onSubmitOrder: () => void;
    readonly onGoBackToAddress: () => void;
}

export const OverviewStepperContent: React.FC<OverviewStepperContentProps> = ({
                                                                                  shippingAddress,
                                                                                  paymentAddress,
                                                                                  order,
                                                                                  onGoBackToAddress,
                                                                                  onSubmitOrder,
                                                                              }) => {
    return (<>
        <h2>Overview</h2>
        <DataTable title={"Order Items"} value={order.products}>
            <Column
                field="name"
                header="Product"
                sortable
                headerClassName="w-10rem"
            />
            <Column
                field="quantity"
                header="Quantity"
                sortable
                headerClassName="w-8rem"
            />
            <Column
                header={"Product Price"}
                sortable
                body={(item: OrderItemDto) => <span>{(item.total / item.quantity).toFixed(2)}</span>}
                headerClassName={"w-8rem"}
            />
            <Column
                field="total"
                header="Total"
                sortable
                headerClassName="w-10rem"
            />
        </DataTable>
        <h3>Total: €{order.sum.toFixed(2)}</h3>
        <br/>
        <br/>
        <Address title={"Shipping Address"} mode={"view"} address={shippingAddress}/>
        <Address title={"Payment Address"} mode={"view"} address={paymentAddress}/>
        <div className="flex justify-content-between align-items-center pt-4">
            <Button label="Back" icon="pi pi-arrow-left" iconPos="left"
                    onClick={onGoBackToAddress}/>
            <Button label="Order now" icon="pi pi-arrow-right" iconPos="right"
                    onClick={onSubmitOrder}/>
        </div>
    </>);
}