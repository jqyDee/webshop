import React from "react";
import {AddressDto, OrderDto, OrderItemDto} from "../../api";
import {Address} from "../order-details/address.tsx";
import {DataTable} from "primereact/datatable";
import {Column} from "primereact/column";
import {Button} from "primereact/button";
import {Total} from "../total.tsx";

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
        <DataTable title={"Order Items"} value={order.products} className={"mb-2"}>
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
                body={priceTemplate}
                headerClassName={"w-8rem"}
            />
            <Column
                field="total"
                header="Total"
                sortable
                headerClassName="w-10rem"
            />
        </DataTable>
        <Total total={order.sum} />
        <div className="grid mt-2">
            <div className="col-12 md:col-6">
                <Address title={"Shipping Address"} mode={"view"} address={shippingAddress}/>
            </div>
            <div className="col-12 md:col-6">
                <Address title={"Payment Address"} mode={"view"} address={paymentAddress}/>
            </div>
        </div>
        <div className="flex justify-content-between align-items-center pt-4">
            <Button label="Back" icon="pi pi-arrow-left" iconPos="left"
                    onClick={onGoBackToAddress}/>
            <Button label="Order now" icon="pi pi-arrow-right" iconPos="right"
                    onClick={onSubmitOrder}/>
        </div>
    </>);
}

const priceTemplate = (item: OrderItemDto) => (<span>{(item.total / item.quantity).toFixed(2)}</span>);
