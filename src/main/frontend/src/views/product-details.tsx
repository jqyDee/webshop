import React from "react";
import {Card} from "primereact/card";
import {ProductDetails as ProductDetailsComponent} from "../components/product-details.tsx";

class ProductDetails extends React.Component {
    render() {
        return (
            <div className="grid justify-content-center">
                <div className="col-12 md:col-10 lg:col-8">
                    <Card className="m-4">
                        <ProductDetailsComponent />
                    </Card>
                </div>
            </div>
        );
    }
}

export default ProductDetails;