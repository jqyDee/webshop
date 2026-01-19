import React, {forwardRef, useImperativeHandle, useState} from "react";
import {UserxDto, UserxUpdateDto} from "../api";
import {
    createUserxRoleArrayFromStrings,
    emptyUserxUpdateDto,
    fromUserxDtoToUserxUpdateDto,
    UserxValidationResult
} from "../utilities/userx-utilities.ts";
import {InputMaskChangeEvent} from "primereact/inputmask";
import {CheckboxChangeEvent} from "primereact/checkbox";
import {QueryObserverResult, RefetchOptions, useMutation} from "@tanstack/react-query";
import {createUserMutation, registerMutation, updateUserMutation} from "../api/@tanstack/react-query.gen.ts";
import {useUser} from "../contexts/authenticated-user.tsx";
import {useNavigate} from "react-router-dom";
import {useGlobalToast} from "../contexts/toast.tsx";
import {Message} from "primereact/message";
import {Dialog} from "primereact/dialog";
import {Button} from "primereact/button";
import {UserForm} from "./user-dialog/user-form.tsx";

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
        const [validation, setValidation] = useState<UserxValidationResult>({valid: true});
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
        ): UserxValidationResult => {

            if (!user) return {valid: false, message: 'No user selected'};

            const required: (keyof UserxUpdateDto)[] = ['firstName', 'lastName', 'username'];
            const {requirePassword = true} = opts; // password input on edit user not needed
            const fieldErrors: Partial<Record<keyof UserxUpdateDto, string>> = {};

            required.forEach((k) => {
                const v = (user[k] as unknown as string) ?? '';
                if (!v.trim()) fieldErrors[k] = 'Required';
            });

            // check for password required
            const pwd = (user.password as unknown as string) ?? '';
            if (requirePassword && !pwd.trim()) fieldErrors.password = 'Required';

            // at least one role required (see also UserxCreateDTO in backend
            if (!user.role) {
                fieldErrors.role = 'Required';
            }

            const valid = Object.keys(fieldErrors).length === 0;
            return valid
                ? {valid}
                : {valid, message: 'Please fill in all required fields', fieldErrors};
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
                console.log('here')
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
            setSelectedUser(fromUserxDtoToUserxUpdateDto(user));
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

        /**
         * Renders the footer of the dialog.
         */
        const renderFooter = () => (
            <div>
                <Button label="Cancel" icon="pi pi-times" onClick={hideDialog} className="p-button-text" />
                <Button label={isNewUser ? "Create" : "Save"} icon="pi pi-check" onClick={handleSubmit}
                        autoFocus loading={submitting} disabled={submitting}/>
            </div>
        );
        return (
            <div>
                <Dialog
                    header={isRegister ? "Register" : isNewUser ? "Create New User" : "Edit User"}
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
                            onUserEnabledChange={handleUserEnabledChange}
                        />
                    )}
                </Dialog>
            </div>
        )
    }
);
