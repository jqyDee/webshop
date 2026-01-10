/**
 * This code is part of the skeleton project provided for students of the course "Software
 * Architecture" offered by Innsbruck University.
 */
import '../styles/App.css';
import "primereact/resources/themes/lara-light-cyan/theme.css";
import React from "react";
import UserTableComponent from "../components/UserTableComponent";

/**
 * Component / View for managing users.
 */

class ManageUsers extends React.Component {

    render() {
        return (
            <div>
                <UserTableComponent />
            </div>
        );
    }
}

export default ManageUsers;
