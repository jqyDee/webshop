/**
 * This code is part of the skeleton project provided for students of the course "Software
 * Architecture" offered by Innsbruck University.
 */
import {useRef} from 'react';
import {Button} from "primereact/button";
import {Card} from 'primereact/card';
import 'primeicons/primeicons.css';
import {UserxDto} from "../api";
import {useQuery} from "@tanstack/react-query";
import {getAllUsersOptions} from "../api/@tanstack/react-query.gen.ts";
import {UserDialog, UserDialogHandle} from "./user-dialog.tsx";
import {UserList} from "./user-table/user-list.tsx";
import {useNavigate} from "react-router-dom";

/**
 * Component for managing users.
 */
export const UserTable = () => {
    const dialogRef = useRef<UserDialogHandle>(null);

    const {data: users, refetch, isLoading} = useQuery({
        ...getAllUsersOptions(),
    });

    const openEditDialog = (user: UserxDto | null) => {
        dialogRef.current?.open(user);
    }

    const navigate = useNavigate();

    const goBack = () => {
        if (window.history.length > 1) {
            navigate(-1);
        } else {
            navigate("/");
        }
    }

    return (<Card className="m-4">
            {/* Button that opens a new user dialog on click */}
            <div className={"flex flex-wrap align-items-center justify-content-between mb-4"}>
                <div className="flex align-items-center gap-2">
                    <Button
                        icon="pi pi-arrow-left"
                        className="p-button-text p-button-rounded"
                        onClick={goBack}
                    />
                    <h2 className={"m-0"}>User List</h2>
                </div>
                <Button label="Add User" icon="pi pi-plus" onClick={() => openEditDialog(null)}/>
            </div>
            <UserList users={users ?? []} loading={isLoading} onEditUser={openEditDialog}/>
            <UserDialog refetch={refetch} canSetRole={true} ref={dialogRef}/>
        </Card>
    );
};

