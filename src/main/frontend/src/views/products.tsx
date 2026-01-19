import React from "react";
import {ProductTable} from "../components/product-table.tsx";


class Products extends React.Component {
    render(){
        return (
            <div>
                <ProductTable/>
            </div>
        )
    }
}

export default Products;