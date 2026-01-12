import React from "react";
import NavbarComponent from "../components/NavbarComponent.tsx";
import ProductTableComponent from "../components/ProductTableComponent.tsx";


class Products extends React.Component {
    render(){
        return (
            <div>
                <NavbarComponent/>
                <ProductTableComponent/>
            </div>
        )
    }
}

export default Products;