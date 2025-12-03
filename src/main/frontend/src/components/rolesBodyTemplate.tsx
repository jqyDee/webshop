import {UserDTO} from "../DTO/userx.types";
import {Tag} from "primereact/tag";
import React from "react";

/**
 * Renders the roles of a user as tags (such beautiful).
 * @param rowData
 */
export const rolesBodyTemplate = (rowData: UserDTO) => {
    return <>
        {rowData.roles.map(role => {
            return <Tag key={role} value={role} severity="info" style={{marginRight: '.5em'}}/>
        })}
    </>;
};