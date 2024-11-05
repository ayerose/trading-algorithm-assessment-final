import React from "react";

export interface QuantityCellProps {
  quantity: number;
  color: "blue" | "red";
}

export const QuantityCell = ({ quantity, color }: QuantityCellProps) => {
  return (
    <td>
      <div
        className={`${color}-bar`}
        style={{
          width: `${Math.min(100, (quantity / 5000) * 100)}%`,
          backgroundColor: color === "blue" ? "#4887bb" : "#e12e2e",
          color: "white",
          textAlign: color === "blue" ? "right" : "left",
          padding: "2px 60px",
          borderRadius: "4px",
        }}
      >
        {quantity}
      </div>
    </td>
  );
};
