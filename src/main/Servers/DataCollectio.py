#!/usr/bin/env python

import argparse
import os
import socket
import uuid
import serial
import serial.tools.list_ports

# Settings
DEFAULT_BAUD = 115200

def parse_args():
    parser = argparse.ArgumentParser(description="Serial Data Collection CSV Server")
    parser.add_argument('--port', '-p', type=int, default=5555, help="Port for the server to listen on")
    return parser.parse_args()

def create_output_directory(directory):
    try:
        os.makedirs(directory)
    except FileExistsError:
        pass

def configure_serial_port(port, baud):
    ser = serial.Serial()
    ser.port = port
    ser.baudrate = baud
    return ser

def main():
    args = parse_args()

    # Create socket
    sock = socket.socket(socket.AF_INET, socket.SOCK_STREAM)
    sock.setsockopt(socket.SOL_SOCKET, socket.SO_REUSEADDR, 1)
    sock.bind(('', args.port))
    sock.listen(3)

    print()
    print("Socket listening on port", args.port)

    while True:
        try:
            conn, addr = sock.accept()
            print('Connected by', addr)

            try:
                # Request the parameters
                conn.sendall(b"Enter the parameters separated by space: COM_PORT BAUDRATE FOLDER_NAME FILE_NAME")

                # Receive the parameters
                raw_params = conn.recv(4096).decode('utf-8').strip().split()
                if len(raw_params) == 4:
                    port, baud, directory, label = raw_params
                    directory = "src//main//Servers//data" 
                    baud = int(baud)
                    port = "COM4"
                    
                    print(port)

                    if port == "STOP":
                        print("Received stop message")
                        conn.sendall(b"Stopping server")
                        break

                    # Create output directory
                    create_output_directory(directory)

                    # Configure serial port
                    ser = configure_serial_port(port, baud)

                    # Attempt to connect to the serial port
                    try:
                        ser.open()
                    except Exception as e:
                        print("ERROR:", e)
                        conn.sendall(b"Error: Failed to open the serial port")
                        continue

                    # Start data collection loop
                    print()
                    print("Connected to {} at a baud rate of {}".format(port, baud))
                    print("Press 'ctrl+c' to exit")

                    # Serial receive buffer
                    rx_buf = b''

                    # Loop forever (unless ctrl+c is captured or a stop message is received)
                    try:
                        while True:

                            # Read bytes from serial port
                            if ser.in_waiting > 0:
                                while(ser.in_waiting):
                                    # Read bytes
                                    rx_buf += ser.read()

                                    # Look for an empty line
                                    if rx_buf[-4:] == b'\r\n\r\n':

                                        # Strip extra newlines (convert \r\n to \n)
                                        buf_str = rx_buf.decode('utf-8').strip()
                                        buf_str = buf_str.replace('\r', '')

                                        # Write contents to file
                                        filename = os.path.join(directory, label + "." + str(uuid.uuid4())[-12:] + ".csv")
                                        with open(filename, 'w') as file:
                                            file.write(buf_str)
                                        conn.sendall(f"Data written to: {filename}".encode('utf-8'))
                                        rx_buf = b''

                            # Check for stop message
                            stop_message = conn.recv(4096).decode('utf-8').strip()
                            if stop_message == "STOP":
                                ser.close()
                                return

                    # Look for keyboard interrupt (ctrl+c)
                    except KeyboardInterrupt:
                        pass

                # Close serial port
                    print("Closing serial port")
                    ser.close()

                else:
                    print("Invalid parameter format. Expected format: ")
                    print("    COM_PORT BAUDRATE FOLDER_NAME FILE_NAME")

            except Exception as e:
                print("ERROR:", e)

        except Exception as e:
            print("ERROR:", e)

        finally:
            conn.close()

if __name__ == '__main__':
    main()