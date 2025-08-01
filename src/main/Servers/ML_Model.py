import os
import pandas as pd
import numpy as np
import matplotlib.pyplot as plt
from sklearn.model_selection import train_test_split
from sklearn.svm import SVC
from sklearn.ensemble import RandomForestClassifier
from sklearn.linear_model import LinearRegression, LogisticRegression
from sklearn.tree import DecisionTreeRegressor, DecisionTreeClassifier
from sklearn.metrics import mean_squared_error
import socket
from micromlgen import port

def load_data(folder_path):
    X = []
    y = []
    for file in os.listdir(folder_path):
        if file.endswith(".csv"):
            try:
                df = pd.read_csv(os.path.join(folder_path, file))
                # Drop rows with NaN values
                df.dropna(inplace=True)
                X.extend(df.iloc[:, 1:].values)
                y.extend([file.split(".")[0]] * len(df))
            except Exception as e:
                print(f"Error processing file {file}: {e}")
    return np.array(X), np.array(y)

def train_model(X_train, y_train, model_type='SVM', **model_params):
    if model_type == 'SVM':
        model = SVC(**model_params)
    elif model_type == 'RandomForest':
        model = RandomForestClassifier(**model_params)
    elif model_type == 'LinearRegression':
        model = LinearRegression()
    elif model_type == 'LogisticRegression':
        model = LogisticRegression(**model_params)
    elif model_type == 'DecisionTreeRegressor':
        model = DecisionTreeRegressor()
    elif model_type == 'DecisionTreeClassifier':
        model = DecisionTreeClassifier()
    else:
        raise ValueError("Invalid model type provided.")

    model.fit(X_train, y_train)
    return model

def convert_to_cpp(model, filename='model.h'):
    with open(filename, 'w') as f:
        f.write(port(model))

def visualize_model_performance(model_type, y_test, y_pred):
    plt.figure()
    if model_type in ['LinearRegression', 'DecisionTreeRegressor']:
        plt.scatter(y_test, y_pred)
        plt.xlabel('True Values')
        plt.ylabel('Predictions')
    else:
        plt.hist([y_test, y_pred], label=['True Values', 'Predictions'])
        plt.legend()
    plt.title(f'{model_type} Model Performance')
    plt.savefig('performance.png')
    plt.close()

def handle_client_connection(client_socket):
    try:
        # Receive data from the client
        data = client_socket.recv(1024).decode().strip()
        folder_path, model_type, *model_params = data.split()
        folder_path = "src//main//Servers//" + folder_path

        # Load data
        X, y = load_data(folder_path)

        # Split data into training and testing sets
        X_train, X_test, y_train, y_test = train_test_split(X, y, test_size=0.2, random_state=42)

        # Specify model parameters
        params_dict = {}
        if model_type in ['SVM', 'LogisticRegression']:
            params_dict['kernel'] = model_params.pop(0)
        elif model_type == 'RandomForest':
            params_dict['n_estimators'] = int(model_params.pop(0))

        # Train model
        model = train_model(X_train, y_train, model_type=model_type, **params_dict)

        # Convert model to C++ code
        convert_to_cpp(model)

        # Make predictions
        y_pred = model.predict(X_test)

        # Evaluate model
        if model_type in ['LinearRegression', 'DecisionTreeRegressor']:
            mse = mean_squared_error(y_test, y_pred)
            result = f"Mean Squared Error: {mse}"
        else:
            accuracy = (y_pred == y_test).mean()
            result = f"Accuracy: {accuracy}"

        # Visualize and save results as an image
        visualize_model_performance(model_type, y_test, y_pred)

        # Send the result back to the client
        client_socket.send(result.encode())
    except Exception as e:
        error_msg = f"Error occurred: {str(e)}"
        client_socket.send(error_msg.encode())
    finally:
        # Close the client socket
        client_socket.close()

def main():
    host = 'localhost'
    port = 123  # Change this to a different port number

    server_socket = socket.socket(socket.AF_INET, socket.SOCK_STREAM)
    server_socket.setsockopt(socket.SOL_SOCKET, socket.SO_REUSEADDR, 1)  # Allow reuse of the address
    server_socket.bind((host, port))
    server_socket.listen(5)

    print(f"Server listening on {host}:{port}")

    while True:
        client_socket, _ = server_socket.accept()
        print("Client connected")
        handle_client_connection(client_socket)

if __name__ == "__main__":
    main()