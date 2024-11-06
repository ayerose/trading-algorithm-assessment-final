# Trading Algo 📱

## Objective ✨
The aim of this challenge is to create a simple trading algorithm that can adapt to fluctuating market conditions by making well-defined buy, sell, and cancel decisions.

## Tasks 
- **1. Main Objective**:
 Write an algorithm (`MyAlgoLogic`) that:
Creates child orders based on favorable market conditions.
Cancels child orders if the market changes unfavorably.

- **2. Stretch Objective**: 
Implement a more advanced algorithm (`StretchAlgoLogic`) that buys at low prices and sells at high prices to maintain profitability.

## Key Classes 🔑
- **MyAlgoLogic**: Implements the simple trading algorithm to create and cancel child orders based on predefined price thresholds.

- **StretchAlgoLogic**: Enhances `MyAlgoLogic` to include `VWAP` and trend analysis for adaptive buy/sell decisions aimed at profitability.


## Key Features  🚀
### `MyAlgoLogic`
- **Order Creation**: Creates buy orders when the best ask price is within the target and spread range.

- **Order Cancellation**: Cancels active orders when price movements exceed a predefined threshold.

- **Liquidity Check**: Ensures there is sufficient liquidity before placing orders.

### `StretchAlgoLogic`
- **VWAP Calculation**: Uses Volume Weighted Average Price (VWAP) as a benchmark for determining buy/sell thresholds.
- **Trend Analysis**: Adapts trading decisions based on recent bid/ask price trends.
- **Dynamic Thresholds**: Sets buy/sell and cancellation thresholds based on VWAP and evolving market conditions, including:
  - `BUY_THRESHOLD_FACTOR`: Controls how aggressively the algorithm buys when prices are below VWAP.
  - `SELL_THRESHOLD_FACTOR`: Sets the criteria for selling when prices exceed VWAP.
  - `CANCEL_THRESHOLD_PERCENTAGE`: Defines the acceptable VWAP deviation for order cancellation.
  - `TREND_PERIOD`: Specifies the number of recent price points used for trend calculation.


## Testing Strategy 🧪
The testing framework is divided into isolated tests for verifying logic without market interaction and backtests for integrated testing with market data.

- **MyAlgoTest (Unit Test)**: Checks isolated behavior for order creation and cancellation in `MyAlgoLogic`.
![Screenshot 2024-11-06 at 16 20 42](https://github.com/user-attachments/assets/142ff681-750c-498f-a3eb-4eb7fe721007)

- **MyAlgoBackTest (Integration Test)**: Uses a full market data feed and order book, verifying orders against real-time market data.
![Screenshot 2024-11-06 at 16 20 30](https://github.com/user-attachments/assets/caa3b33c-e132-4bf5-9982-ea09cdd0d339)

- **StretchAlgoTest (Unit Test)**: Validates the profitability logic and trend-based trading in `StretchAlgoLogic`.
![Screenshot 2024-11-06 at 16 20 16](https://github.com/user-attachments/assets/d5e711d2-1711-4338-ac5e-99d253d43785)

- **StretchAlgoBackTest (Integration Test)**: Runs `StretchAlgoLogic` in simulated market conditions, verifying that orders are placed/canceled based on profitability.
![Screenshot 2024-11-06 at 16 19 59](https://github.com/user-attachments/assets/907219e2-4cc3-4c22-a8e2-982d7b7c5be9)

### Key Scenarios Tested 🧪🧪
- **Low Liquidity**: Ensures no orders are placed under low-liquidity conditions.

- **Max Order Limit**: Ensures the algorithm doesn’t exceed the allowed active child orders.

- **Spread Conditions**: Confirms that orders are created/canceled based on spread width.

- **Trend-based Buying and Selling**: Validates buy/sell actions based on price trends.

# Strategy and Logic 💡


### ➡️ MyAlgoLogic Strategy

**`MyAlgoLogic` is designed with a simple, rule-based approach, focusing on minimizing risk by carefully monitoring the spread and liquidity. The algorithm has specific checks and actions:**

**1. Order Creation**:

Creates a buy order if the spread (difference between best ask and best bid prices) is within the `SPREAD_THRESHOLD`, ensuring that trades are only placed when prices are reasonable.
Sets the target price to the best ask price, allowing the algorithm to respond to favorable market conditions.
The algorithm only creates orders if there are fewer than six active child orders, reducing exposure and managing risk.

**2. Order Cancellation**:

Cancels orders if prices move beyond the `PRICE_THRESHOLD`, ensuring that open orders don’t remain exposed to unfavorable price changes.
Includes a liquidity check: only trades when there are at least three bids and asks available, ensuring that the market has sufficient depth.

**3. Liquidity and Spread Management**:

These checks help avoid trades in low-liquidity environments or when the spread is too wide, preserving capital and reducing the risk of loss.
This design enables MyAlgoLogic to handle basic scenarios with clear thresholds and simple risk management, providing a stable foundation for trading under controlled conditions.

### ➡️ StretchAlgoLogic Strategy
StretchAlgoLogic builds on MyAlgoLogic by incorporating more advanced strategies like VWAP (Volume Weighted Average Price), trend analysis, and adaptive thresholds, allowing the algorithm to respond to fluctuating market trends for profitability.

**1. VWAP Calculation**:

Volume Weighted Average Price (`VWAP`) serves as a benchmark for buy and sell decisions, calculated by averaging bid and ask prices weighted by their quantities.
`VWAP is updated with every market tick, allowing the algorithm to make informed trading decisions based on current market trends.

**2. Trend Analysis**:

`StretchAlgoLogic` tracks recent bid and ask trends over a defined `TREND_PERIOD`.
Uses a trend-based approach for buying and selling, allowing the algorithm to act aggressively in bullish markets (where prices are trending up) and conservatively in bearish markets.

**3. Adaptive Thresholds**:

Sets dynamic buy/sell thresholds around `VWAP` (using `BUY_THRESHOLD_FACTOR` and `SELL_THRESHOLD_FACTOR`), allowing the algorithm to act opportunistically when prices dip below or rise above these levels.
For order cancellation, the algorithm uses a `VWAP` deviation threshold (`CANCEL_THRESHOLD_PERCENTAGE`) to remove orders that no longer align with the current `VWAP`.
Combines these adaptive thresholds with trend conditions to create a flexible response to both short-term and sustained price movements.

**4. Order Creation and Cancellation Logic**:

**Order Creation**: 

Initiates buy orders when the ask price is slightly below `VWAP`, and sells when the bid price exceeds `VWAP`. This leverages the market trend while maintaining risk management.

**Order Cancellation**: 

Cancels orders if the difference between `VWAP` and the active order price exceeds the defined thresholds. This minimizes exposure to unfavorable market changes, ensuring that only promising orders remain active.


# GitHub Actions CI/CD

GitHub Actions CI/CD has been set up to automatically build and test the codebase on every push or pull request to the main branch.

- **ci.yml**: Runs all tests using Maven, logs results, and displays test results for easy debugging.

**Workflow Structure:**

- **Java Setup**: Configures the environment with JDK 17.

- **Build and Run Tests**: Executes mvn test to run all tests.

- **Display Test Results**: Logs any failed tests for easier debugging in the GitHub Actions UI.

  ![Screenshot 2024-11-06 at 16 18 30](https://github.com/user-attachments/assets/c32a12ee-034d-42c3-bd93-a1836a38bdf7)


## UI Frontend
<img width="634" alt="Screenshot 2024-11-05 at 21 56 09" src="https://github.com/user-attachments/assets/cb1ade04-e7b6-496f-aa43-84e62c9b3484">


# Getting Started 🏁

## Prerequisites
- Java 17
- Maven (for build and dependency management)

## Running the Application 🚀
1. Clone the Repository:


```bash
git clone <repository-url>
cd <project-directory>
```
2. Build the project with Maven


```bash
mvn clean install
```
3. Test

```bash
mvn test
```

## Technologies Used
- **Java**: Main programming language for implementing the algorithm
- **React**: Main programming language for implementing the UI frontend
- **Maven**: Used for build automation and dependency management
- **JUnit**: Framework for unit testing and ensuring code quality
- **Mockito**: Mocking framework used to simulate dependencies and test isolated components effectively
