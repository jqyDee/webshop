import React from "react";

interface TotalProps {
    total: number;
}

export const Total: React.FC<TotalProps> = (props) => {
    return (
        <div className="flex justify-content-end align-items-center">
            <div className="text-right">
                <span className="text-xl font-light block mb-2">Total</span>
                <span className="text-4xl font-bold text-900">€{props.total.toFixed(2)}</span>
            </div>
        </div>
    )
}