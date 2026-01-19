import {Tag} from "primereact/tag";
import {UserxDto} from "../api";

/**
 * Renders the role of a user as tags (such beautiful).
 * @param rowData
 */
export const rolesBodyTemplate = (rowData: UserxDto) => {
    return <Tag value={rowData.role} severity="info" style={{marginRight: '.5em'}}/>
};