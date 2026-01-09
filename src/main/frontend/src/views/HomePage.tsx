/**
 * This code is part of the skeleton project provided for students of the course "Software
 * Architecture" offered by Innsbruck University.
 */
import '../styles/App.css';
import "primereact/resources/themes/lara-light-cyan/theme.css";
import React from "react";
import NavbarComponent from "../components/NavbarComponent";
import ProductTableComponent from "../components/ProductTableComponent.tsx";

/**
 * The home page of the application.
 */
class HomePage extends React.Component {
    render() {
        return (
            <div>
                <NavbarComponent />
                <ProductTableComponent />
            </div>
        );
    }
}

export default HomePage;
