/**
 * This code is part of the skeleton project provided for students of the course "Software
 * Architecture" offered by Innsbruck University.
 */
import {useRef} from 'react';
import {Button} from "primereact/button";
import {Card} from 'primereact/card';
import 'primeicons/primeicons.css';
import UserListComponent from "./UserListComponent";
import {UserxDto} from "../api";
import {useQuery} from "@tanstack/react-query";
import {getAllUsersOptions} from "../api/@tanstack/react-query.gen.ts";
import UserDialogComponent, {UserDialogHandle} from "./UserDialogComponent.tsx";

/**
 * Component for managing users.
 */
const UserTable = () => {
    const dialogRef = useRef<UserDialogHandle>(null);

    const {data: users, refetch, isLoading} = useQuery({
        ...getAllUsersOptions(),
    });

    const openEditDialog = (user: UserxDto | null) => {
        dialogRef.current?.open(user);
    }

    return (<Card title="User List" className="m-4">
            {/* Button that opens a new user dialog on click */}
            <Button label="Add User" icon="pi pi-plus" className="p-button-raised p-button-rounded"
                    style={{marginBottom: "10px"}} onClick={() => openEditDialog(null)}/>
            <UserListComponent users={users ?? []} loading={isLoading} onEditUser={openEditDialog}/>
            <UserDialogComponent refetch={refetch} canSetRole={true} ref={dialogRef}/>
        </Card>
    );
};

export default UserTable;

