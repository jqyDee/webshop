/**
 * This code is part of the skeleton project provided for students of the course "Software
 * Architecture" offered by Innsbruck University.
 */
import '../styles/app.css';
import "primereact/resources/themes/lara-light-cyan/theme.css";
import React from "react";
import {UserTable} from "../components/user-table.tsx";

/**
 * Component / View for managing users.
 */

class ManageUsers extends React.Component {

    render() {
        return (
            <div>
                <UserTable />
            </div>
        );
    }
}

export default ManageUsers;
