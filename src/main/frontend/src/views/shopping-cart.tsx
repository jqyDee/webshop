import React from "react";
import {Card} from "primereact/card";
import {ShoppingCartTable} from "../components/shopping-cart-table.tsx";


class ShoppingCart extends React.Component {
    render(){
        return (
            <div className="grid justify-content-center">
                <div className="col-12 md:col-10 lg:col-8">
                    <Card className="m-4">
                        <ShoppingCartTable />
                    </Card>
                </div>
            </div>
        )
    }
}

export default ShoppingCart;