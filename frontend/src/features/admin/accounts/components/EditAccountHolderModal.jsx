import { useState } from "react";

import Button from "../../../../shared/components/ui/Button";
import Input from "../../../../shared/components/ui/Input";
import Modal from "../../../../shared/components/ui/Modal";

const emailPattern = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;

function validateHolder(values) {
    const errors = {};
    const name = values.name.trim();
    const email = values.email.trim();
    const address = values.address.trim();

    if (!name) {
        errors.name = "Name is required.";
    } else if (name.length > 100) {
        errors.name = "Name cannot exceed 100 characters.";
    }

    if (!email) {
        errors.email = "Email is required.";
    } else if (!emailPattern.test(email)) {
        errors.email = "Enter a valid email address.";
    } else if (email.length > 150) {
        errors.email = "Email cannot exceed 150 characters.";
    }

    if (!address) {
        errors.address = "Address is required.";
    } else if (address.length > 255) {
        errors.address = "Address cannot exceed 255 characters.";
    }

    return errors;
}

function EditAccountHolderModal({ account, onClose, onSave }) {
    const [values, setValues] = useState(() => ({
        name: account.holderName,
        email: account.holderEmail,
        address: account.holderAddress,
    }));
    const [errors, setErrors] = useState({});

    function handleChange(event) {
        const { name, value } = event.target;

        setValues((currentValues) => ({
            ...currentValues,
            [name]: value,
        }));
        setErrors((currentErrors) => ({
            ...currentErrors,
            [name]: undefined,
        }));
    }

    function handleSubmit(event) {
        event.preventDefault();

        const nextErrors = validateHolder(values);

        if (Object.keys(nextErrors).length > 0) {
            setErrors(nextErrors);
            return;
        }

        onSave({
            name: values.name.trim(),
            email: values.email.trim().toLowerCase(),
            address: values.address.trim(),
        });
    }

    return (
        <Modal
            isOpen
            onClose={onClose}
            title="Edit account holder"
            footer={(
                <>
                    <Button
                        variant="secondary"
                        onClick={onClose}
                        className="w-full sm:w-auto"
                    >
                        Cancel
                    </Button>
                    <Button
                        type="submit"
                        form="edit-account-holder-form"
                        className="w-full sm:w-auto"
                    >
                        Save changes
                    </Button>
                </>
            )}
        >
            <p className="text-sm leading-6 text-brand-muted">
                Update the holder information linked to this bank account.
            </p>

            <form
                id="edit-account-holder-form"
                className="mt-5 space-y-5"
                onSubmit={handleSubmit}
                noValidate
            >
                <Input
                    name="name"
                    label="Full name"
                    autoFocus
                    value={values.name}
                    onChange={handleChange}
                    error={errors.name}
                    maxLength={100}
                    autoComplete="name"
                />
                <Input
                    name="email"
                    label="Email address"
                    type="email"
                    value={values.email}
                    onChange={handleChange}
                    error={errors.email}
                    maxLength={150}
                    autoComplete="email"
                />
                <Input
                    name="address"
                    label="Address"
                    value={values.address}
                    onChange={handleChange}
                    error={errors.address}
                    maxLength={255}
                    autoComplete="street-address"
                />
            </form>
        </Modal>
    );
}

export default EditAccountHolderModal;
