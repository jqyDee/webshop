import React, {forwardRef, useImperativeHandle, useMemo, useState} from "react";
import {ItemsEnum, RoleEnum, UserxDto, UserxUpdateDto} from "../api";
import {InputMaskChangeEvent} from "primereact/inputmask";
import {CheckboxChangeEvent} from "primereact/checkbox";
import {QueryObserverResult, RefetchOptions, useMutation} from "@tanstack/react-query";
import {
    createUserMutation,
    deleteUserMutation,
    registerMutation,
    updateUserMutation
} from "../api/@tanstack/react-query.gen.ts";
import {useUser} from "../contexts/authenticated-user.tsx";
import {useNavigate} from "react-router-dom";
import {useGlobalToast} from "../contexts/toast.tsx";
import {Message} from "primereact/message";
import {Dialog} from "primereact/dialog";
import {Button} from "primereact/button";
import {UserForm} from "./user-dialog/user-form.tsx";
import {validateFormData, ValidationResult} from "../utilities/form-data-validator.ts";
import {enforceNonNull} from "../utilities/enforce-non-null.ts";

export interface UserDialogHandle {
    open: (user: UserxDto | null, register?: boolean) => void;
}

interface UserDialogComponentProps {
    refetch?: (options?: RefetchOptions) => Promise<QueryObserverResult<UserxDto[], Error>>;
    canSetRole: boolean;
}

export const UserDialog = forwardRef<UserDialogHandle, UserDialogComponentProps>(
    ({refetch, canSetRole}, ref) => {
        const [selectedUser, setSelectedUser] = useState<UserxUpdateDto | null>(null);
        const [isNewUser, setIsNewUser] = useState<boolean>(false);
        const [isRegister, setIsRegister] = useState<boolean>(false);
        const [dialogVisible, setDialogVisible] = useState<boolean>(false);
        const [validation, setValidation] = useState<ValidationResult<UserxDto>>({valid: true});
        const [submitting, setSubmitting] = useState<boolean>(false);
        const {login} = useUser();
        const navigate = useNavigate();

        const {showToast} = useGlobalToast();

        const createUser = useMutation({
            ...createUserMutation(),
            onError: (err) => {
                console.error('Error saving user:', err);
                showToast({severity: 'error', summary: 'Error', detail: 'Error saving user', life: 3000});
            },
            onSuccess: async () => {
                if (refetch) {
                    await refetch()
                }
            }
        });

        const updateUser = useMutation({
            ...updateUserMutation(),
            onError: (err) => {
                console.error('Error updating user:', err);
                showToast({severity: 'error', summary: 'Error', detail: 'Error updating user', life: 3000});
            },
            onSuccess: async () => {
                if (refetch) {
                    await refetch()
                }
            }
        });

        const registerUser = useMutation({
            ...registerMutation(),
            onError: (err) => {
                console.error('Error updating user:', err);
                showToast({severity: 'error', summary: 'Error', detail: 'Error updating user', life: 3000});
            },
            onSuccess: async () => {
                showToast({severity: "success", summary: 'Successfully registered', life: 3000});
            }
        })

        const deleteUser = useMutation({
            ...deleteUserMutation(),
            onError: (err) => {
                console.error('Error deleting user:', err);
                showToast({severity: 'error', summary: 'Error', detail: 'Error deleting user', life: 3000});
            },
            onSuccess: async () => {
                showToast({severity: "success", summary: 'Successfully deleted', life: 3000});
                hideDialog();
                if (refetch) {
                    await refetch()
                }
            }
        })

        useImperativeHandle(ref, () => ({
            open: (openUser, register) => {
                if (register) {
                    openRegisterDialog();
                }

                if (openUser) {
                    openEditDialog(openUser);
                } else {
                    openNewUserDialog();
                }
            }
        }));


        /**
         * Validate the user object.
         * @param user
         * @param opts
         */
        const validateUser = (
            user: UserxUpdateDto,
            opts: {
                requirePassword?: boolean
            } = {requirePassword: true},
        ): ValidationResult<UserxDto> => {

            if (!user) return {valid: false, message: 'No user selected'};

            const required: (keyof UserxUpdateDto)[] = ['firstName', 'lastName', 'username'];
            const {requirePassword = true} = opts; // password input on edit user not needed
            return validateFormData(user, [
                (key) => required.includes(key) && !user[key] ? "Required" : undefined,
                (key) => key === "password" && requirePassword && (user[key] ?? "").length === 0 ? "Required" : undefined,
                (key) => key === "role" && !user.role ? "Required" : undefined,
            ]);
        }

        /**
         * Handle the submit event for the user dialog.
         */
        const handleSubmit = async () => {
            setSubmitting(true);
            if (!selectedUser) return;

            const validationResult = validateUser(selectedUser, {requirePassword: isNewUser});
            if (!validationResult.valid) {
                // Display an error eventMessage or handle the validation error
                setValidation(validationResult);
                setSubmitting(false);
                console.error('Please fill in all required fields.');
                return;
            }

            setValidation({valid: true});

            if (isRegister) {
                // This has to be there, see validation
                const pw = selectedUser.password!;

                await registerUser.mutateAsync({body: selectedUser});

                await login({username: selectedUser.username, password: pw});
                navigate('/');

            } else if (isNewUser) {
                await createUser.mutateAsync({body: selectedUser});

            } else {
                // id cant be undefined because the user isnt new
                await updateUser.mutateAsync({body: selectedUser, path: {id: selectedUser.id!}});
            }
            hideDialog();
        };

        /**
         * Open the edit dialog for a user.
         * @param user
         */
        const openEditDialog = (user: UserxDto) => {
            setSubmitting(false);
            setSelectedUser(user);
            setValidation({valid: true});
            setIsNewUser(false);
            showDialog()
        };

        /**
         * Open the dialog for creating a new user.
         */
        const openNewUserDialog = () => {
            setSubmitting(false);
            setSelectedUser(emptyUserxUpdateDto);
            setValidation({valid: true});
            showDialog()
            setIsNewUser(true);
        }

        const openRegisterDialog = () => {
            setIsRegister(true);
            openNewUserDialog();
        }

        /**
         * Show the dialog.
         */
        const showDialog = () => {
            setValidation({valid: true});
            setDialogVisible(true);
        }

        /**
         * Hide the dialog.
         */
        const hideDialog = () => {
            setValidation({valid: true});
            setDialogVisible(false);
        };

        /**
         * Handle input changes for the user dialog.
         * @param event
         */
        const handleInputChange = (event: React.ChangeEvent<HTMLInputElement> | InputMaskChangeEvent) => {
            if (!selectedUser) return;

            const {name, value} = event.target;

            setSelectedUser({...selectedUser, [name]: value});
        }

        /**
         * Handle user enabled change for the user dialog.
         * @param event
         */
        const handleUserEnabledChange = (event: CheckboxChangeEvent) => {
            if (!selectedUser) return;

            const {name, checked} = event.target;

            setSelectedUser({...selectedUser, [name]: checked});
        }

        /**
         * Handle role change for the user dialog.
         * @param event
         */
        const handleRolesChange = (event: { value: string }) => {
            if (!selectedUser) return;

            const role = createUserxRoleArrayFromStrings(event.value);

            setSelectedUser({...selectedUser, role: role});
        }

        const handleNotifyOptionToggle = (option: ItemsEnum) => {
            if (!selectedUser) return;
            const currentOptions = selectedUser.notifyOptions ?? [];
            const isPresent = currentOptions.find(opt => option === opt);
            if (isPresent) {
                setSelectedUser({...selectedUser, notifyOptions: currentOptions.filter(opt => opt !== option)});
                return;
            }
            setSelectedUser({...selectedUser, notifyOptions: [...currentOptions, option]});
        }

        /**
         * Renders the footer of the dialog.
         */
        const renderFooter = () => (
            <div className={"flex flex-row justify-content-between"}>
                <Button disabled={isNewUser || isRegister} label="Delete" icon="pi pi-trash" className={"p-button-text p-button-danger"} onClick={async () => {
                    if (!selectedUser) return;
                    await deleteUser.mutateAsync({
                        path: {id: enforceNonNull(selectedUser.id)}
                    })
                }}/>
                <div>
                    <Button label="Cancel" icon="pi pi-times" onClick={hideDialog} className="p-button-text" />
                    <Button label={isNewUser ? "Create" : "Save"} icon="pi pi-check" onClick={handleSubmit}
                            autoFocus loading={submitting} disabled={submitting}/>
                </div>
            </div>
        );
        const header = useMemo(() => {
            if (isRegister) return 'Register';
            if (isNewUser) return 'Create New User';
            return "Edit User"
        }, [isRegister, isNewUser])
        return (
            <div>
                <Dialog
                    header={header}
                    visible={dialogVisible}
                    style={{ width: '50vw' }}
                    onHide={hideDialog}
                    footer={renderFooter}
                >
                    {validation.message && (<Message severity="error" text={validation.message} className="mb-3"/>)}
                    {selectedUser && (
                        <UserForm
                            user={selectedUser}
                            fieldErrors={validation.fieldErrors}
                            isRegister={isRegister}
                            canSetRole={canSetRole}
                            onInputChange={handleInputChange}
                            onRolesChange={handleRolesChange}
                            onToggleNotifyOption={handleNotifyOptionToggle}
                            onUserEnabledChange={handleUserEnabledChange}
                        />
                    )}
                </Dialog>
            </div>
        )
    }
);

/**
 * Create a UserxRole array from a string array of role
 * @param role
 *
 * @returns UserxRole[]
 * @throws Error if an invalid role is provided
 */
const createUserxRoleArrayFromStrings = (role: string): RoleEnum => {
    switch (role) {
        case RoleEnum.ADMIN.valueOf():
            return RoleEnum.ADMIN;
        case RoleEnum.MANAGER.valueOf():
            return RoleEnum.MANAGER;
        case RoleEnum.CUSTOMER.valueOf():
            return RoleEnum.CUSTOMER;
        default:
            throw new Error(`Invalid role: ${role}`);
    }
}

/**
 * Create empty UserxUpdateDto for new User Dialog
 *
 * @returns UserxUpdateDto
 */
const emptyUserxUpdateDto = (): UserxUpdateDto => {
    return {
        id: undefined,
        username: "",
        password: "",
        firstName: "",
        lastName: "",
        email: "",
        phone: "",
        enabled: true,
        role: RoleEnum.CUSTOMER,
    };
}
