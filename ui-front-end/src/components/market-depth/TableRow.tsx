import React from "react";
import { QuantityCell } from "./QuantityCell";
import { PriceCell } from "./PriceCell";

export interface TableRowProps {
  level: number;
  bidQuantity: number;
  bid: number;
  offer: number;
  offerQuantity: number;
}

export const TableRow = ({ level, bidQuantity, bid, offer, offerQuantity }: TableRowProps) => {
  return (
    <tr>
      <td style={{ padding: "5px", textAlign: "center" }}>{level}</td>

      {/* bid section */}
      <td>
        <QuantityCell quantity={bidQuantity} color="blue" />
      </td>
      <td>
        <PriceCell price={bid} />
      </td>

      {/* ask section */}
      <td>
        <PriceCell price={offer} />
      </td>
      <td>
        <QuantityCell quantity={offerQuantity} color="red" />
      </td>
    </tr>
  );
};
