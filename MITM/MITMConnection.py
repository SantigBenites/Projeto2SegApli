import socket

def createSocket(host="127.0.0.1",port=4000):

    s = socket.socket()
    s.setsockopt(socket.SOL_SOCKET, socket.SO_REUSEADDR, 1)
    s.bind((host, port))
    s.listen()
    return s

def clientReceive(conn:socket.socket):
    return conn.recv(5000)

def clientSend(conn:socket.socket, message:str):
    conn.sendall(message)
    return conn

def bankReceive(s:socket.socket):
    return s.recv(5000)

def bankSend(s:socket.socket, message:str):
    s.sendall(message)
    return s


def handShake(clientSocket:socket.socket, bankSocket:socket.socket):

    clientMessage = clientReceive(clientSocket)
    bankSend(bankSocket,clientMessage)

    bankMessage = bankReceive(bankSocket)
    clientSend(clientSocket,bankMessage)

    clientMessage = clientReceive(clientSocket)
    bankSend(bankSocket,clientMessage)

    print("HandShake Finalized")