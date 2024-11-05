import React, { useRef } from "react";

export interface PriceCellProps {
  price: number;
}

export const PriceCell = ({ price }: PriceCellProps) => {
  const lastPriceRef = useRef(price);
  const direction = price > lastPriceRef.current ? "↑" : price < lastPriceRef.current ? "↓" : "";
  lastPriceRef.current = price;

  return (
    <td style={{ textAlign: "center", fontWeight: "bold", display: "flex", alignItems: "center", justifyContent: "center" }}>
      {direction && (
        <span
          className={direction === "↑" ? "arrow-up" : "arrow-down"}
        >
          {direction}
        </span>
      )}
      {price.toFixed(2)}
    </td>
  );
};
