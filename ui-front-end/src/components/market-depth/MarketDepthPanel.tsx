import React from "react";
import "./MarketDepthPanel.css";
import { TableRow } from "./TableRow";

export interface MarketDepthPanelProps {
  data: any[];
}

export const MarketDepthPanel = ({ data }: MarketDepthPanelProps) => {
  return (
    <div className="market-depth-container">

      <h2 className="headline">My Trading Algo</h2>
      <div className="scrollable-container">
        <table className="MarketDepthPanel">
          <thead>
            <tr>
              <th>Level</th>
              <th colSpan={2}>Bid</th>
              <th colSpan={2}>Ask</th>
            </tr>
            <tr>
              <th></th>
              <th>Quantity</th>
              <th>Price</th>
              <th>Price</th>
              <th>Quantity</th>
            </tr>
          </thead>
          <tbody>
            {data.map((row, index) => (
              <TableRow
                key={index}
                level={row.level}
                bidQuantity={row.bidQuantity}
                bid={row.bid}
                offer={row.offer}
                offerQuantity={row.offerQuantity}
              />
            ))}
          </tbody>
        </table>
      </div>
    </div>
  );
};
